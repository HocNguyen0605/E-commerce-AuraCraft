package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.RegisterDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.LinkedHashMap;
import java.util.Map;


@WebServlet("/auth/google")
public class GoogleAuthServlet extends HttpServlet {

    public static final String CLIENT_ID = "825665609931-aehm70fij6qh8o99khm9bkisfk3isvr9.apps.googleusercontent.com";

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private final RegisterDAO dao = new RegisterDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/login.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");

        String credential = req.getParameter("credential");
        if (credential == null || credential.isBlank()) {
            send(resp, 400, false, "Thiếu thông tin đăng nhập Google", null, null);
            return;
        }

        try {
            JsonObject claims = verify(credential);
            if (claims == null) {
                send(resp, 401, false, "Không xác thực được tài khoản Google", null, null);
                return;
            }

            String googleId = claims.get("sub").getAsString();
            String email = claims.get("email").getAsString().toLowerCase();
            String name = claims.has("name") ? claims.get("name").getAsString() : email.substring(0, email.indexOf('@'));

            // 1. Đã có tài khoản Google này: đăng nhập
            RegisterDAO.Found acc = dao.findByGoogleId(googleId);

            if (acc == null && "login".equals(req.getParameter("mode"))) {
                // Từ trang đăng nhập: chỉ đăng nhập, không tự tạo tài khoản mới
                send(resp, 404, false,
                        "Chưa có tài khoản nào liên kết với Google này. Hãy đăng ký trước.", null, null);
                return;
            }

            if (acc == null) {
                // 2. Email đã được đăng ký theo cách khác: không tự động gộp tài khoản
                if (dao.emailExists(email)) {
                    send(resp, 409, false,
                            "Email này đã đăng ký bằng mật khẩu. Hãy đăng nhập bằng tên đăng nhập và mật khẩu.", null, null);
                    return;
                }
                // 3. Chưa có gì: tạo mới theo vai trò đã chọn
                acc = dao.createGoogle(name, email, googleId, RegisterDAO.toDbRole(req.getParameter("role")));
            }

            if (!"active".equals(acc.status())) {
                send(resp, 403, false, "Tài khoản đã bị khóa", null, null);
                return;
            }

            // Tạo session giống LoginServlet
            HttpSession old = req.getSession(false);
            if (old != null) old.invalidate();
            HttpSession session = req.getSession(true);
            session.setMaxInactiveInterval(30 * 60);
            session.setAttribute("userId", acc.userId());
            session.setAttribute("userName", acc.name());
            session.setAttribute("role", acc.role());

            send(resp, 200, true, "Đăng nhập thành công", acc.name(), acc.role());

        } catch (SQLIntegrityConstraintViolationException e) {
            send(resp, 409, false, "Tài khoản đã tồn tại", null, null);
        } catch (Exception e) {
            e.printStackTrace();
            send(resp, 500, false, "Lỗi máy chủ, vui lòng thử lại", null, null);
        }
    }
    private JsonObject verify(String idToken) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create(
                "https://oauth2.googleapis.com/tokeninfo?id_token="
                        + URLEncoder.encode(idToken, StandardCharsets.UTF_8))).GET().build();
        HttpResponse<String> res = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) return null;

        JsonObject o = JsonParser.parseString(res.body()).getAsJsonObject();
        if (!CLIENT_ID.equals(o.get("aud").getAsString())) return null;   // token phải cấp cho đúng app của bạn
        String iss = o.get("iss").getAsString();
        if (!iss.equals("accounts.google.com") && !iss.equals("https://accounts.google.com")) return null;
        if (!"true".equals(o.get("email_verified").getAsString())) return null;
        return o;
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