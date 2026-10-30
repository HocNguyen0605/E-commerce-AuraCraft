package controller;

import com.google.gson.Gson;
import dao.PasswordResetDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;
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
 * POST /forgot/send-otp    : gửi mã 6 số tới email (hiệu lực 60 giây)
 * POST /forgot/verify-otp  : kiểm tra mã; đúng thì cho phép đổi mật khẩu trong 5 phút
 * POST /forgot/reset       : đặt mật khẩu mới cho đúng email đã xác thực (email lấy từ session, không tin client)
 */
@WebServlet({"/forgot/send-otp", "/forgot/verify-otp", "/forgot/reset", "/pages/forgot"})
public class ForgotPasswordServlet extends HttpServlet {

    private static final int IP_COOLDOWN_SECONDS = 10;
    private static final int MAX_ATTEMPTS = 5;
    private static final long RESET_WINDOW_MS = 5 * 60 * 1000L;   // xác thực xong phải đổi mật khẩu trong 5 phút

    private static final String FP_EMAIL = "fpEmail";
    private static final String FP_CODE = "fpCode";
    private static final String FP_EXPIRES = "fpExpiresAt";
    private static final String FP_ATTEMPTS = "fpAttempts";
    private static final String FP_VERIFIED_EMAIL = "fpVerifiedEmail";
    private static final String FP_VERIFIED_AT = "fpVerifiedAt";

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final ConcurrentHashMap<String, Long> LAST_SENT = new ConcurrentHashMap<>();

    private final PasswordResetDAO dao = new PasswordResetDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String path = req.getServletPath();
        if (path.endsWith("/send-otp")) {
            sendOtp(req, resp);
        } else if (path.endsWith("/verify-otp")) {
            verifyOtp(req, resp);
        } else {
            resetPassword(req, resp);
        }
    }

    // ---------------------------------------------------------------- gửi OTP
    private void sendOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = param(req, "email").toLowerCase();
        if (!EMAIL.matcher(email).matches() || email.length() > 150) {
            reply(resp, 400, false, "Email không hợp lệ", null);
            return;
        }

        long now = System.currentTimeMillis();
        String mailKey = "fp-mail:" + email;
        String ipKey = "fp-ip:" + req.getRemoteAddr();
        purgeOld(now);

        long wait = Math.max(secondsLeft(mailKey, OTPServlet.RESEND_COOLDOWN_SECONDS, now),
                secondsLeft(ipKey, IP_COOLDOWN_SECONDS, now));
        if (wait > 0) {
            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("retryAfter", wait);
            reply(resp, 429, false, "Vui lòng đợi " + wait + " giây rồi gửi lại mã", extra);
            return;
        }

        try {
            LAST_SENT.put(mailKey, now);
            LAST_SENT.put(ipKey, now);

            if (dao.hasLocalPassword(email)) {
                String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
                try {
                    EmailService.sendResetOtp(email, otp, OTPServlet.OTP_TTL_SECONDS);
                } catch (Exception mailError) {
                    LAST_SENT.remove(mailKey);
                    LAST_SENT.remove(ipKey);
                    mailError.printStackTrace();
                    reply(resp, 500, false, "Không gửi được email. Vui lòng thử lại sau", null);
                    return;
                }
                HttpSession session = req.getSession(true);
                session.setAttribute(FP_EMAIL, email);
                session.setAttribute(FP_CODE, otp);
                session.setAttribute(FP_EXPIRES, now + OTPServlet.OTP_TTL_SECONDS * 1000L);
                session.setAttribute(FP_ATTEMPTS, 0);
                session.removeAttribute(FP_VERIFIED_EMAIL);
                session.removeAttribute(FP_VERIFIED_AT);
            } else {
                // Email không tồn tại hoặc là tài khoản Google: không gửi gì, nhưng vẫn trả lời giống nhau
                // để người ngoài không dò được email nào đã đăng ký.
                HttpSession session = req.getSession(false);
                if (session != null) clearOtp(session);
            }

            Map<String, Object> extra = new LinkedHashMap<>();
            extra.put("expiresIn", OTPServlet.OTP_TTL_SECONDS);
            extra.put("resendIn", OTPServlet.RESEND_COOLDOWN_SECONDS);
            reply(resp, 200, true, "Nếu email đã đăng ký, mã xác thực đã được gửi", extra);

        } catch (SQLException e) {
            LAST_SENT.remove(mailKey);
            LAST_SENT.remove(ipKey);
            e.printStackTrace();
            reply(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại", null);
        }
    }

    // ------------------------------------------------------------ xác thực OTP
    private void verifyOtp(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String email = param(req, "email").toLowerCase();
        String otp = param(req, "otp");

        HttpSession session = req.getSession(false);
        String code = session == null ? null : (String) session.getAttribute(FP_CODE);
        if (code == null || !email.equals(session.getAttribute(FP_EMAIL))) {
            reply(resp, 400, false, "Mã không đúng hoặc đã hết hạn", null);
            return;
        }
        if (System.currentTimeMillis() > (Long) session.getAttribute(FP_EXPIRES)) {
            clearOtp(session);
            reply(resp, 410, false, "Mã đã hết hạn. Hãy gửi lại mã mới", null);
            return;
        }

        boolean match = MessageDigest.isEqual(code.getBytes(StandardCharsets.UTF_8), otp.getBytes(StandardCharsets.UTF_8));
        if (!match) {
            int attempts = (Integer) session.getAttribute(FP_ATTEMPTS) + 1;
            if (attempts >= MAX_ATTEMPTS) {
                clearOtp(session);
                reply(resp, 429, false, "Bạn đã nhập sai quá nhiều lần. Hãy gửi lại mã mới", null);
                return;
            }
            session.setAttribute(FP_ATTEMPTS, attempts);
            reply(resp, 400, false, "Mã không đúng. Bạn còn " + (MAX_ATTEMPTS - attempts) + " lần thử", null);
            return;
        }

        clearOtp(session);
        session.setAttribute(FP_VERIFIED_EMAIL, email);
        session.setAttribute(FP_VERIFIED_AT, System.currentTimeMillis());
        reply(resp, 200, true, "Xác thực thành công", null);
    }

    // ------------------------------------------------------------ đổi mật khẩu
    private void resetPassword(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        Object verifiedEmail = session == null ? null : session.getAttribute(FP_VERIFIED_EMAIL);
        Object verifiedAt = session == null ? null : session.getAttribute(FP_VERIFIED_AT);
        if (!(verifiedEmail instanceof String) || !(verifiedAt instanceof Long)
                || System.currentTimeMillis() - (Long) verifiedAt > RESET_WINDOW_MS) {
            reply(resp, 403, false, "Phiên xác thực đã hết hạn. Vui lòng xác thực email lại", null);
            return;
        }
        String email = (String) verifiedEmail;

        String password = req.getParameter("newPassword") == null ? "" : req.getParameter("newPassword");
        String confirm = req.getParameter("confirmPassword") == null ? "" : req.getParameter("confirmPassword");
        if (password.length() < 6 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            reply(resp, 400, false, "Mật khẩu phải từ 6 đến 72 ký tự", null);
            return;
        }
        if (!password.equals(confirm)) {
            reply(resp, 400, false, "Mật khẩu xác nhận không trùng khớp", null);
            return;
        }

        try {
            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));
            int rows = dao.updatePassword(email, hash);
            if (rows == 0) {
                reply(resp, 404, false, "Không tìm thấy tài khoản để cập nhật", null);
                return;
            }
            session.removeAttribute(FP_VERIFIED_EMAIL);   // mỗi lần xác thực chỉ đổi được một lần
            session.removeAttribute(FP_VERIFIED_AT);
            reply(resp, 200, true, "Đổi mật khẩu thành công", null);
        } catch (SQLException e) {
            e.printStackTrace();
            reply(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại", null);
        }
    }

    // ---------------------------------------------------------------- tiện ích
    private static void clearOtp(HttpSession s) {
        s.removeAttribute(FP_EMAIL);
        s.removeAttribute(FP_CODE);
        s.removeAttribute(FP_EXPIRES);
        s.removeAttribute(FP_ATTEMPTS);
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
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/pages/forgot.jsp").forward(request, response);
    }
}