package controller;

import com.google.gson.Gson;
import dao.RegisterDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private static final Pattern USERNAME = Pattern.compile("^[a-z0-9_]{4,30}$");
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final RegisterDAO dao = new RegisterDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/register.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String name = param(req, "fullName");
        String username = param(req, "username").toLowerCase();
        String email = param(req, "email").toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String role = RegisterDAO.toDbRole(req.getParameter("role"));   // không bao giờ cho tự đăng ký admin

        if (name.isEmpty() || name.length() > 100) {
            send(resp, 400, false, "Vui lòng nhập họ và tên (tối đa 100 ký tự)");
            return;
        }
        if (!USERNAME.matcher(username).matches()) {
            send(resp, 400, false, "Tên đăng nhập gồm 4–30 ký tự: chữ thường, số hoặc dấu gạch dưới");
            return;
        }
        if (!EMAIL.matcher(email).matches() || email.length() > 150) {
            send(resp, 400, false, "Email không hợp lệ");
            return;
        }
        // BCrypt chỉ dùng tối đa 72 byte đầu của mật khẩu
        if (password.length() < 6 || password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 72) {
            send(resp, 400, false, "Mật khẩu phải từ 6 đến 72 ký tự");
            return;
        }

        // Email phải đã được xác thực bằng OTP (OtpServlet) trong phiên này, còn hiệu lực 15 phút
        HttpSession session = req.getSession(false);
        Object verifiedEmail = session == null ? null : session.getAttribute(OTPServlet.VERIFIED_EMAIL);
        Object verifiedAt = session == null ? null : session.getAttribute(OTPServlet.VERIFIED_AT);
        boolean verified = email.equals(verifiedEmail)
                && verifiedAt instanceof Long t
                && System.currentTimeMillis() - t <= 15 * 60 * 1000L;
        if (!verified) {
            send(resp, 403, false, "Vui lòng xác thực email bằng mã OTP trước khi đăng ký");
            return;
        }

        try {
            if (dao.usernameExists(username)) {
                send(resp, 409, false, "Tên đăng nhập đã được sử dụng");
                return;
            }
            if (dao.emailExists(email)) {
                send(resp, 409, false, "Email đã được đăng ký");
                return;
            }

            String hash = BCrypt.hashpw(password, BCrypt.gensalt(10));   // $2a$10$... giống dữ liệu hiện có
            dao.createLocal(name, username, email, hash, role);
            session.removeAttribute(OTPServlet.VERIFIED_EMAIL);
            session.removeAttribute(OTPServlet.VERIFIED_AT);
            send(resp, 201, true, "Đăng ký thành công");

        } catch (SQLIntegrityConstraintViolationException e) {
            // Hai người đăng ký trùng cùng lúc: ràng buộc UNIQUE trong DB chặn lại
            send(resp, 409, false, "Tên đăng nhập hoặc email đã được sử dụng");
        } catch (Exception e) {
            e.printStackTrace();
            send(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại");
        }
    }

    private static String param(HttpServletRequest req, String key) {
        String v = req.getParameter(key);
        return v == null ? "" : v.trim();
    }

    private void send(HttpServletResponse resp, int status, boolean ok, String message) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", ok);
        body.put("message", message);
        resp.getWriter().write(gson.toJson(body));
    }
}