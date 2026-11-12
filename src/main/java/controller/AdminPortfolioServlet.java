package controller;

import com.google.gson.Gson;
import dao.ShopDAO;
import dao.ProductDAO;
import util.DBConnection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.*;
import java.util.*;

/**
 * API cung cấp dữ liệu Portfolio của thợ cho Admin xem.
 * GET /api/admin/portfolio?shopId=X
 */
@WebServlet("/api/admin/portfolio")
public class AdminPortfolioServlet extends HttpServlet {

    private ShopDAO shopDAO;
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        shopDAO = new ShopDAO();
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String shopIdParam = request.getParameter("shopId");
        if (shopIdParam == null || shopIdParam.isBlank()) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"Thiếu shopId\"}");
            return;
        }

        int shopId;
        try {
            shopId = Integer.parseInt(shopIdParam.trim());
        } catch (NumberFormatException e) {
            response.setStatus(400);
            response.getWriter().write("{\"error\":\"shopId không hợp lệ\"}");
            return;
        }

        try {
            Map<String, Object> result = buildPortfolioData(shopId);
            if (result == null) {
                response.setStatus(404);
                response.getWriter().write("{\"error\":\"Không tìm thấy gian hàng\"}");
                return;
            }
            response.getWriter().write(new Gson().toJson(result));
        } catch (Exception e) {
            getServletContext().log("AdminPortfolioServlet error", e);
            response.setStatus(500);
            response.getWriter().write("{\"error\":\"Lỗi máy chủ\"}");
        }
    }

    private Map<String, Object> buildPortfolioData(int shopId) throws Exception {
        Map<String, Object> data = new LinkedHashMap<>();

        // --- Thông tin shop + chủ shop ---
        String shopSql =
            "SELECT s.id, s.name AS shopName, s.description, s.avatar, s.phone AS shopPhone, s.email AS shopEmail, " +
            "       s.address, s.rating, s.commission_rate, s.status, " +
            "       u.id AS userId, u.name AS ownerName, u.email AS ownerEmail, " +
            "       a.status AS accountStatus, " +
            "       (SELECT COUNT(*) FROM products p WHERE p.id_shop = s.id AND p.status = 'active') AS activeProducts, " +
            "       (SELECT COALESCE(SUM(p.sold_count), 0) FROM products p WHERE p.id_shop = s.id) AS totalSold " +
            "FROM shops s " +
            "JOIN users u ON s.id_owner = u.id " +
            "JOIN account a ON u.id = a.id_user " +
            "WHERE s.id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(shopSql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                Map<String, Object> shopInfo = new LinkedHashMap<>();
                shopInfo.put("shopId",        rs.getInt("id"));
                shopInfo.put("shopName",      rs.getString("shopName"));
                shopInfo.put("description",   rs.getString("description"));
                shopInfo.put("avatar",        rs.getString("avatar"));
                shopInfo.put("shopPhone",     rs.getString("shopPhone"));
                shopInfo.put("shopEmail",     rs.getString("shopEmail"));
                shopInfo.put("address",       rs.getString("address"));
                shopInfo.put("rating",        rs.getDouble("rating"));
                shopInfo.put("commissionRate",rs.getDouble("commission_rate"));
                shopInfo.put("shopStatus",    rs.getString("status"));
                shopInfo.put("userId",        rs.getInt("userId"));
                shopInfo.put("ownerName",     rs.getString("ownerName"));
                shopInfo.put("ownerEmail",    rs.getString("ownerEmail"));
                shopInfo.put("accountStatus", rs.getString("accountStatus"));
                shopInfo.put("activeProducts",rs.getInt("activeProducts"));
                shopInfo.put("totalSold",     rs.getLong("totalSold"));
                data.put("shop", shopInfo);
            }
        }

        // --- Thống kê đơn hàng ---
        String orderStatsSql =
            "SELECT " +
            "  COUNT(*) AS totalOrders, " +
            "  SUM(CASE WHEN o.status = 'completed' THEN 1 ELSE 0 END) AS completedOrders, " +
            "  COALESCE(SUM(CASE WHEN o.status = 'completed' THEN o.amount ELSE 0 END), 0) AS totalRevenue, " +
            "  COALESCE(SUM(oi.fee_amount), 0) AS totalCommission, " +
            "  COALESCE((SELECT AVG(r.rating) FROM reviews r JOIN products p ON r.id_product = p.id WHERE p.id_shop = ?), 0) AS avgRating, " +
            "  (SELECT COUNT(*) FROM reviews r JOIN products p ON r.id_product = p.id WHERE p.id_shop = ?) AS reviewCount " +
            "FROM orders o " +
            "LEFT JOIN orderitems oi ON oi.id_order = o.id " +
            "WHERE o.id_shop = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(orderStatsSql)) {
            ps.setInt(1, shopId);
            ps.setInt(2, shopId);
            ps.setInt(3, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> stats = new LinkedHashMap<>();
                    stats.put("totalOrders",     rs.getInt("totalOrders"));
                    stats.put("completedOrders", rs.getInt("completedOrders"));
                    stats.put("totalRevenue",    rs.getDouble("totalRevenue"));
                    stats.put("totalCommission", rs.getDouble("totalCommission"));
                    stats.put("avgRating",       rs.getDouble("avgRating"));
                    stats.put("reviewCount",     rs.getInt("reviewCount"));
                    data.put("stats", stats);
                }
            }
        }

        // --- Danh sách sản phẩm ---
        String productSql =
            "SELECT p.id, p.name, p.price, p.stock, p.sold_count, p.status, " +
            "       COALESCE(rv.avg_rating, 0) AS rating, " +
            "       (SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image " +
            "FROM products p " +
            "LEFT JOIN (SELECT id_product, AVG(rating) AS avg_rating FROM reviews GROUP BY id_product) rv ON rv.id_product = p.id " +
            "WHERE p.id_shop = ? " +
            "ORDER BY p.status ASC, p.id DESC";

        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(productSql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> p = new LinkedHashMap<>();
                    p.put("id",       rs.getInt("id"));
                    p.put("name",     rs.getString("name"));
                    p.put("price",    rs.getDouble("price"));
                    p.put("stock",    rs.getInt("stock"));
                    p.put("soldCount",rs.getInt("sold_count"));
                    p.put("status",   rs.getString("status"));
                    p.put("rating",   rs.getDouble("rating"));
                    p.put("image",    rs.getString("image"));
                    products.add(p);
                }
            }
        }
        data.put("products", products);

        // --- Mẫu thiết kế Custom ---
        List<Map<String, Object>> templates = new ArrayList<>();
        try {
            String templateSql =
                "SELECT dt.id, dt.name, dt.base_price, dt.type, dt.status, dt.thumbnail_url " +
                "FROM designtemplates dt " +
                "WHERE dt.id_shop = ? " +
                "ORDER BY dt.id DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(templateSql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> t = new LinkedHashMap<>();
                        t.put("id",          rs.getInt("id"));
                        t.put("name",        rs.getString("name"));
                        t.put("basePrice",   rs.getDouble("base_price"));
                        t.put("type",        rs.getString("type"));
                        t.put("status",      rs.getString("status"));
                        t.put("thumbnailUrl",rs.getString("thumbnail_url"));
                        templates.add(t);
                    }
                }
            }
        } catch (SQLException ignored) {
            // designtemplates có thể chưa có trường id_shop
        }
        data.put("templates", templates);

        // --- Lịch sử đơn hàng gần đây (20 đơn mới nhất) ---
        String orderHistorySql =
            "SELECT o.id AS orderId, o.amount, o.status, o.date_create, o.date_paid, " +
            "       buyer.name AS buyerName, " +
            "       item_sub.productSummary, item_sub.itemCount " +
            "FROM orders o " +
            "JOIN users buyer ON o.id_user = buyer.id " +
            "LEFT JOIN ( " +
            "    SELECT oi.id_order, " +
            "           GROUP_CONCAT( " +
            "               CASE WHEN oi.id_product IS NOT NULL THEN p.name " +
            "                    WHEN oi.id_custom_design IS NOT NULL THEN CONCAT('Custom: ', COALESCE(dt2.name, '?')) " +
            "               END SEPARATOR ', ') AS productSummary, " +
            "           SUM(oi.quantity) AS itemCount " +
            "    FROM orderitems oi " +
            "    LEFT JOIN products p ON oi.id_product = p.id " +
            "    LEFT JOIN customdesigns cd ON oi.id_custom_design = cd.id " +
            "    LEFT JOIN designtemplates dt2 ON cd.id_template = dt2.id " +
            "    GROUP BY oi.id_order " +
            ") item_sub ON item_sub.id_order = o.id " +
            "WHERE o.id_shop = ? " +
            "ORDER BY o.id DESC LIMIT 20";

        List<Map<String, Object>> orders = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(orderHistorySql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> o = new LinkedHashMap<>();
                    o.put("orderId",        rs.getInt("orderId"));
                    o.put("amount",         rs.getDouble("amount"));
                    o.put("status",         rs.getString("status"));
                    o.put("dateCreate",     rs.getString("date_create"));
                    o.put("datePaid",       rs.getString("date_paid"));
                    o.put("buyerName",      rs.getString("buyerName"));
                    o.put("productSummary", rs.getString("productSummary"));
                    o.put("itemCount",      rs.getInt("itemCount"));
                    orders.add(o);
                }
            }
        }
        data.put("orders", orders);

        return data;
    }
}
