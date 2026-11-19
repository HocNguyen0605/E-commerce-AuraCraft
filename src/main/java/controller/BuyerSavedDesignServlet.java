package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.BuyerSavedDesignDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/buyer/saved-designs")
public class BuyerSavedDesignServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final BuyerSavedDesignDAO dao = new BuyerSavedDesignDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = buyerId(request);
        if (userId == null) { write(response, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập.")); return; }
        try {
            write(response, 200, Map.of("ok", true, "designs", dao.findByUser(userId)));
        } catch (Exception e) {
            getServletContext().log("Unable to load saved designs", e);
            write(response, 500, Map.of("ok", false, "message", "Không tải được thiết kế đã lưu."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Integer userId = buyerId(request);
        if (userId == null) { write(response, 401, Map.of("ok", false, "message", "Đăng nhập bằng tài khoản người mua để đồng bộ thiết kế.")); return; }
        try {
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            JsonObject design = body.has("design") && body.get("design").isJsonObject() ? body.getAsJsonObject("design") : body;
            String key = string(design, "id");
            if (key.isBlank() || key.length() > 80 || !design.has("slots") || !design.get("slots").isJsonArray()) {
                write(response, 400, Map.of("ok", false, "message", "Dữ liệu thiết kế không hợp lệ.")); return;
            }
            if (GSON.toJson(design).length() > 200_000) {
                write(response, 413, Map.of("ok", false, "message", "Thiết kế vượt quá dung lượng cho phép.")); return;
            }
            dao.save(userId, key, design);
            write(response, 200, Map.of("ok", true));
        } catch (Exception e) {
            getServletContext().log("Unable to save buyer design", e);
            write(response, 500, Map.of("ok", false, "message", "Chưa thể lưu thiết kế vào tài khoản."));
        }
    }
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer userId = buyerId(req);
        if (userId == null) {
            write(resp, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập."));
            return;
        }

        String designId = req.getParameter("id");
        if (designId == null || designId.isBlank()) {
            write(resp, 400, Map.of("ok", false, "message", "Thiếu ID thiết kế."));
            return;
        }

        try {
            boolean deleted = dao.delete(userId, designId.trim());
            if (deleted) {
                write(resp, 200, Map.of("ok", true));
            } else {
                write(resp, 404, Map.of("ok", false, "message", "Không tìm thấy thiết kế để xóa."));
            }
        } catch (SQLException e) {
            getServletContext().log("Unable to delete saved design", e);
            write(resp, 500, Map.of("ok", false, "message", "Không thể xóa thiết kế."));
        }
    }
    private Integer buyerId(HttpServletRequest request) {
        Object value = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        return value instanceof Number number && number.intValue() > 0 && "buyer".equals(role) ? number.intValue() : null;
    }

    private String string(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }

    private void write(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(GSON.toJson(payload));
    }
}
