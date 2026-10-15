package controller;

import com.google.gson.Gson;
import dao.RegisterDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.EmailService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * POST /register/send-otp    : gửi mã 6 số tới email (hiệu lực 60 giây)
 * POST /register/verify-otp  : kiểm tra mã; đúng thì đánh dấu email đã xác thực trong session
 * RegisterServlet sẽ đọc dấu này trước khi cho tạo tài khoản.
 */
@WebServlet({"/register/send-otp", "/register/verify-otp"})
public class OTPServlet extends HttpServlet {

    public static final int OTP_TTL_SECONDS = 60;          // thời gian sống của mã
    public static final int RESEND_COOLDOWN_SECONDS = 60;  // chờ bao lâu mới được gửi lại cho cùng 1 email
    private static final int IP_COOLDOWN_SECONDS = 10;     // chống spam từ cùng 1 IP
    private static final int MAX_ATTEMPTS = 5;             // số lần nhập sai tối đa cho mỗi mã

    // Khóa session, RegisterServlet dùng VERIFIED_EMAIL và VERIFIED_AT
    public static final String VERIFIED_EMAIL = "verifiedEmail";
    public static final String VERIFIED_AT = "verifiedAt";
    private static final String OTP_EMAIL = "otpEmail";
    private static final String OTP_CODE = "otpCode";
    private static final String OTP_EXPIRES = "otpExpiresAt";
    private static final String OTP_ATTEMPTS = "otpAttempts";

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final SecureRandom RANDOM = new SecureRandom();
    /** Thời điểm gửi gần nhất theo email / IP. Mất khi restart server, đủ dùng cho đồ án. */
    private static final ConcurrentHashMap<String, Long> LAST_SENT = new ConcurrentHashMap<>();

    private final RegisterDAO dao = new RegisterDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        if (req.getServletPath().endsWith("/send-otp")) {
            sendOtp(req, resp);
        } else {
            verifyOtp(req, resp);
        }
    }

    private void sendOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = param(req, "email").toLowerCase();
        if (!EMAIL.matcher(email).matches() || email.length() > 150) {
            reply(resp, 400, false, "Email không hợp lệ", null);
            return;
        }

        long now = System.currentTimeMillis();
        String mailKey = "mail:" + email;
        String ipKey = "ip:" + req.getRemoteAddr();
        purgeOld(now);

        long wait = Math.max(secondsLeft(mailKey, RESEND_COOLDOWN_SECONDS, now),
                secondsLeft(ipKey, IP_COOLDOWN_SECONDS, now));
        if (wait > 0) {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("retryAfter", wait);
            reply(resp, 429, false, "Vui lòng đợi " + wait + " giây rồi gửi lại mã", extra);
            return;
        }

        try {
            if (dao.emailExists(email)) {
                reply(resp, 409, false, "Email này đã được đăng ký", null);
                return;
            }

            String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

            // Giữ chỗ cooldown trước khi gửi để chặn bấm liên tiếp
            LAST_SENT.put(mailKey, now);
            LAST_SENT.put(ipKey, now);
            try {
                EmailService.sendOtp(email, otp, OTP_TTL_SECONDS);
            } catch (Exception mailError) {
                LAST_SENT.remove(mailKey);
                LAST_SENT.remove(ipKey);
                mailError.printStackTrace();
                reply(resp, 500, false, "Không gửi được email. Vui lòng kiểm tra địa chỉ và thử lại", null);
                return;
            }

            HttpSession session = req.getSession(true);
            session.setAttribute(OTP_EMAIL, email);
            session.setAttribute(OTP_CODE, otp);
            session.setAttribute(OTP_EXPIRES, now + OTP_TTL_SECONDS * 1000L);
            session.setAttribute(OTP_ATTEMPTS, 0);
            session.removeAttribute(VERIFIED_EMAIL);
            session.removeAttribute(VERIFIED_AT);

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("expiresIn", OTP_TTL_SECONDS);
            extra.put("resendIn", RESEND_COOLDOWN_SECONDS);
            reply(resp, 200, true, "Đã gửi mã xác thực tới " + email, extra);

        } catch (SQLException e) {
            e.printStackTrace();
            reply(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại", null);
        }
    }

    private void verifyOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = param(req, "email").toLowerCase();
        String otp = param(req, "otp");

        HttpSession session = req.getSession(false);
        String code = session == null ? null : (String) session.getAttribute(OTP_CODE);
        if (code == null) {
            reply(resp, 400, false, "Bạn chưa yêu cầu mã. Hãy bấm Gửi mã", null);
            return;
        }
        if (!email.equals(session.getAttribute(OTP_EMAIL))) {
            reply(resp, 400, false, "Email đã thay đổi. Hãy gửi mã mới", null);
            return;
        }
        if (System.currentTimeMillis() > (Long) session.getAttribute(OTP_EXPIRES)) {
            clearOtp(session);
            reply(resp, 410, false, "Mã đã hết hạn. Hãy gửi lại mã mới", null);
            return;
        }

        boolean match = MessageDigest.isEqual(code.getBytes(StandardCharsets.UTF_8), otp.getBytes(StandardCharsets.UTF_8));
        if (!match) {
            int attempts = (Integer) session.getAttribute(OTP_ATTEMPTS) + 1;
            if (attempts >= MAX_ATTEMPTS) {
                clearOtp(session);
                reply(resp, 429, false, "Bạn đã nhập sai quá nhiều lần. Hãy gửi lại mã mới", null);
                return;
            }
            session.setAttribute(OTP_ATTEMPTS, attempts);
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("attemptsLeft", MAX_ATTEMPTS - attempts);
            reply(resp, 400, false, "Mã không đúng. Bạn còn " + (MAX_ATTEMPTS - attempts) + " lần thử", extra);
            return;
        }

        clearOtp(session);
        session.setAttribute(VERIFIED_EMAIL, email);
        session.setAttribute(VERIFIED_AT, System.currentTimeMillis());
        reply(resp, 200, true, "Email đã được xác thực", null);
    }

    private static void clearOtp(HttpSession s) {
        s.removeAttribute(OTP_EMAIL);
        s.removeAttribute(OTP_CODE);
        s.removeAttribute(OTP_EXPIRES);
        s.removeAttribute(OTP_ATTEMPTS);
    }

    private static long secondsLeft(String key, int cooldownSeconds, long now) {
        Long last = LAST_SENT.get(key);
        if (last == null) return 0;
        long leftMs = cooldownSeconds * 1000L - (now - last);
        return leftMs <= 0 ? 0 : (leftMs + 999) / 1000;
    }

    private static void purgeOld(long now) {
        if (LAST_SENT.size() > 10_000) {
            LAST_SENT.values().removeIf(t -> now - t > 120_000);
        }
    }

    private static String param(HttpServletRequest req, String key) {
        String v = req.getParameter(key);
        return v == null ? "" : v.trim();
    }

    private void reply(HttpServletResponse resp, int status, boolean ok, String message,
                       Map<String, Object> extra) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", ok);
        body.put("message", message);
        if (extra != null) body.putAll(extra);
        resp.getWriter().write(gson.toJson(body));
    }
}