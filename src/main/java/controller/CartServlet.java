package controller;

import com.google.gson.Gson;
import dao.CartDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {
    private final CartDAO cartDAO = new CartDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer userId = buyerId(request);
        if (userId == null) { writeError(response, 401, "Đăng nhập tài khoản người mua để đồng bộ giỏ hàng."); return; }
        try {
            List<Map<String, Object>> items = cartDAO.findUserCart(userId);
            for (Map<String, Object> item : items) {
                String image = (String) item.get("image");
                if (image != null && image.startsWith("/")) item.put("image", request.getContextPath() + image);
            }
            response.setContentType("application/json;charset=UTF-8");
            response.setHeader("Cache-Control", "no-store");
            response.getWriter().write(gson.toJson(Map.of("items", items)));
        } catch (SQLException exception) {
            getServletContext().log("Unable to load database cart", exception);
            writeError(response, 503, "Không thể tải giỏ hàng từ cơ sở dữ liệu.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        Integer userId = buyerId(request);
        if (userId == null) { writeError(response, 401, "Vui lòng đăng nhập tài khoản người mua."); return; }
        String action = request.getParameter("action");
        try {
            switch (action == null ? "" : action) {
                case "add" -> {
                    int quantityAdded = cartDAO.add(userId, positiveInt(request.getParameter("productId")),
                            positiveInt(request.getParameter("quantity")));
                    respond(response, Map.of("success", true, "quantityAdded", quantityAdded));
                }
                case "quantity" -> {
                    cartDAO.updateQuantity(userId, positiveInt(request.getParameter("productId")),
                            positiveInt(request.getParameter("quantity")));
                    respond(response, Map.of("success", true));
                }
                case "remove" -> {
                    cartDAO.remove(userId, positiveInt(request.getParameter("productId")));
                    respond(response, Map.of("success", true));
                }
                case "remove-many" -> {
                    List<Integer> ids = parseIds(request.getParameter("productIds"));
                    cartDAO.removeMany(userId, ids);
                    respond(response, Map.of("success", true));
                }
                default -> writeError(response, 400, "Thao tác giỏ hàng không hợp lệ.");
            }
        } catch (IllegalArgumentException exception) {
            writeError(response, 400, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Unable to update database cart", exception);
            writeError(response, 503, "Không thể cập nhật giỏ hàng trong cơ sở dữ liệu.");
        }
    }

    private Integer buyerId(HttpServletRequest request) {
        Object userId = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        return userId instanceof Number number && number.intValue() > 0 && "buyer".equals(role)
                ? number.intValue() : null;
    }

    private int positiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            if (parsed > 0) return parsed;
        } catch (NumberFormatException ignored) { }
        throw new IllegalArgumentException("Thông tin sản phẩm hoặc số lượng không hợp lệ.");
    }

    private List<Integer> parseIds(String value) {
        List<Integer> ids = new ArrayList<>();
        if (value == null || value.isBlank()) return ids;
        for (String part : value.split(",")) ids.add(positiveInt(part));
        return ids.stream().distinct().toList();
    }

    private void respond(HttpServletResponse response, Object body) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write(gson.toJson(body));
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        respond(response, Map.of("success", false, "message", message == null ? "Có lỗi xảy ra." : message));
    }
}
