package controller;

import com.google.gson.Gson;
import dao.AccountDAO;
import dto.AccountInfoDTO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private final AccountDAO dao = new AccountDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/login.html");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String email = req.getParameter("email") == null ? "" : req.getParameter("email").trim().toLowerCase();
        String password = req.getParameter("password");

        if (email.isEmpty() || password == null || password.isEmpty()) {
            send(resp, 400, false, "Vui lòng nhập email và mật khẩu", null, null);
            return;
        }

        try {
            AccountInfoDTO acc = dao.findByEmail(email);
            System.out.println("DEBUG ACC -> Email: " + email + " | Found: " + (acc != null) +
                    " | Hash: " + (acc != null ? acc.passwordHash : "null") +
                    " | Provider: " + (acc != null ? acc.provider : "null"));

            if (acc != null && (acc.passwordHash == null || "google".equals(acc.provider))) {
                send(resp, 401, false, "Tài khoản này đăng nhập bằng Google", null, null);
                return;
            }
            // Cùng một thông báo cho "không có email" và "sai mật khẩu"
            System.out.println("DEBUG INPUT -> Pass nhận từ Form: [" + password + "]");
            System.out.println("DEBUG MATCH -> Kế quả BCrypt: " + BCrypt.checkpw(password, acc.passwordHash));
            if (acc == null || !checkPassword(password, acc.passwordHash)) {
                send(resp, 401, false, "Email hoặc mật khẩu không đúng", null, null);
                return;
            }
            if (!acc.emailVerified) {
                send(resp, 403, false, "Tài khoản chưa xác nhận email", null, null);
                return;
            }

            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("userId", acc.userId);
            session.setAttribute("userName", acc.name);
            session.setAttribute("role", acc.role);

            send(resp, 200, true, "Đăng nhập thành công", acc.name, acc.role);
        } catch (Exception e) {
            e.printStackTrace();
            send(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại", null, null);
        }
    }

    private boolean checkPassword(String plain, String hash) {
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {   // hash trong DB sai định dạng
            return false;
        }
    }

    private void send(HttpServletResponse resp, int status, boolean ok, String message,
                      String name, String role) throws IOException {
        resp.setStatus(status);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", ok);
        body.put("message", message);
        if (ok) {
            body.put("name", name);
            body.put("role", role);
        }
        resp.getWriter().write(gson.toJson(body));
    }
}
