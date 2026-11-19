package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Loads the buyer's custom design requests and shop quotes for the profile page. */
public class BuyerQuoteDAO {
    /** Accepts one pending shop quote for a buyer-owned request and closes competing quotes. */
    public boolean acceptQuote(int buyerId, int requestId, int quoteId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String requestStatus;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM customrequests WHERE id=? AND id_user=? FOR UPDATE")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, buyerId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy yêu cầu thiết kế của bạn.");
                        requestStatus = rs.getString("status");
                    }
                }

                String quoteStatus;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT status FROM quotes WHERE id=? AND id_request=? FOR UPDATE")) {
                    ps.setInt(1, quoteId);
                    ps.setInt(2, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Báo giá này không thuộc yêu cầu đã chọn.");
                        quoteStatus = rs.getString("status");
                    }
                }

                if ("assigned".equalsIgnoreCase(requestStatus)) {
                    if ("accepted".equalsIgnoreCase(quoteStatus)) {
                        c.commit();
                        return true;
                    }
                    throw new IllegalArgumentException("Bạn đã xác nhận một thợ khác cho yêu cầu này.");
                }
                if (!"open".equalsIgnoreCase(requestStatus) && !"quoted".equalsIgnoreCase(requestStatus)) {
                    throw new IllegalArgumentException("Yêu cầu này đã đóng, không thể chọn thợ.");
                }
                if (!"pending".equalsIgnoreCase(quoteStatus)) {
                    throw new IllegalArgumentException("Báo giá này không còn chờ xác nhận.");
                }

                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE quotes SET status='accepted' WHERE id=? AND id_request=? AND status='pending'")) {
                    ps.setInt(1, quoteId);
                    ps.setInt(2, requestId);
                    if (ps.executeUpdate() != 1) throw new IllegalArgumentException("Báo giá vừa được cập nhật. Vui lòng tải lại trang.");
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE quotes SET status='rejected' WHERE id_request=? AND id<>? AND status='pending'")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, quoteId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE customrequests SET status='assigned' WHERE id=? AND id_user=?")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, buyerId);
                    if (ps.executeUpdate() != 1) throw new SQLException("Không thể cập nhật trạng thái yêu cầu.");
                }
                createPendingOrder(c, buyerId, requestId, quoteId);
                c.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /**
     * Sau khi khách chốt báo giá: lưu thiết kế thành đơn "Chờ xử lý" của shop (orders + orderitems + giao dịch COD),
     * để seller thấy ngay ở trang Đơn hàng. Gọi trong cùng transaction với acceptQuote.
     */
    private void createPendingOrder(Connection c, int buyerId, int requestId, int quoteId) throws SQLException {
        // Đã có đơn cho báo giá này thì thôi (tránh tạo trùng)
        try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM orderitems WHERE id_quote=? LIMIT 1")) {
            ps.setInt(1, quoteId);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return; }
        }
        Integer designId = null;
        try (PreparedStatement ps = c.prepareStatement("SELECT id_design FROM customrequests WHERE id=?")) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { int v = rs.getInt(1); if (!rs.wasNull()) designId = v; }
            }
        }
        if (designId == null) throw new IllegalArgumentException("Yêu cầu này chưa có thiết kế đính kèm nên chưa thể tạo đơn.");

        int shopId; long price; int days;
        try (PreparedStatement ps = c.prepareStatement("SELECT id_shop, price, lead_time_days FROM quotes WHERE id=?")) {
            ps.setInt(1, quoteId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Không tìm thấy báo giá.");
                shopId = rs.getInt(1); price = rs.getLong(2); days = rs.getInt(3);
            }
        }

        // Thông tin nhận hàng: ưu tiên địa chỉ mặc định của khách
        String address = "Chưa cung cấp địa chỉ", receiver = null, phone = null;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT receiver, phone, address FROM addresses WHERE id_user=? ORDER BY is_default DESC, id DESC LIMIT 1")) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) { receiver = rs.getString(1); phone = rs.getString(2); address = rs.getString(3); }
            }
        }
        if (receiver == null) {
            try (PreparedStatement ps = c.prepareStatement("SELECT name, phone FROM users WHERE id=?")) {
                ps.setInt(1, buyerId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) { receiver = rs.getString(1); phone = rs.getString(2); }
                }
            }
        }

        int orderId;
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO orders(id_user,id_payment_method,id_shop,amount,shipping_fee,shipping_address," +
                        "shipping_receiver,shipping_phone,shipping_note,status,deadline) " +
                        "VALUES(?, (SELECT id FROM paymentmethods WHERE name='COD' LIMIT 1), ?, ?, 0, ?, ?, ?, ?, 'pending', " +
                        "DATE_ADD(NOW(), INTERVAL ? DAY))", java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setInt(2, shopId);
            ps.setLong(3, price);
            ps.setString(4, address);
            ps.setString(5, receiver);
            ps.setString(6, phone);
            ps.setString(7, "Đơn thiết kế theo yêu cầu #" + requestId + " (từ báo giá #" + quoteId + ")");
            ps.setInt(8, Math.max(days, 1));
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { k.next(); orderId = k.getInt(1); }
        }
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO orderitems(id_order,id_custom_design,id_quote,quantity,price,fee_amount) VALUES(?,?,?,1,?,0)")) {
            ps.setInt(1, orderId); ps.setInt(2, designId); ps.setInt(3, quoteId); ps.setLong(4, price);
            ps.executeUpdate();
        }
        // Giao dịch COD (pending) để đơn hiện ở "Chờ xử lý" của seller
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO transactions(id_order,gateway,txn_ref,amount,status) VALUES(?,'cod',?,?,'pending')")) {
            ps.setInt(1, orderId); ps.setString(2, "COD-QUOTE-" + orderId); ps.setLong(3, price);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> findRequests(int buyerId) throws SQLException {
        String sql = "SELECT r.id, r.product_type, r.materials, r.charm_description, r.budget, r.status AS request_status, r.date_create, " +
                "d.preview_image, q.id AS quote_id, q.price, q.lead_time_days, q.note, q.portfolio, q.status AS quote_status, q.date AS quote_date, s.name AS shop_name " +
                "FROM customrequests r LEFT JOIN customdesigns d ON d.id = r.id_design " +
                "LEFT JOIN quotes q ON q.id_request = r.id LEFT JOIN shops s ON s.id = q.id_shop " +
                "WHERE r.id_user = ? ORDER BY r.id DESC, q.id DESC";
        Map<Integer, Map<String, Object>> byId = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int requestId = rs.getInt("id");
                    Map<String, Object> request = byId.get(requestId);
                    if (request == null) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", requestId);
                        row.put("productType", rs.getString("product_type"));
                        row.put("materials", rs.getString("materials"));
                        row.put("description", rs.getString("charm_description"));
                        row.put("budget", rs.getLong("budget"));
                        row.put("requestStatus", rs.getString("request_status"));
                        row.put("createdAt", rs.getTimestamp("date_create"));
                        row.put("previewImage", rs.getString("preview_image"));
                        row.put("quotes", new ArrayList<Map<String, Object>>());
                        row.put("quoteCount", 0);
                        row.put("hasPending", false);
                        row.put("hasAccepted", false);
                        row.put("hasQuote", false);
                        byId.put(requestId, row);
                        request = row;
                    }
                    int quoteId = rs.getInt("quote_id");
                    if (rs.wasNull()) continue;
                    @SuppressWarnings("unchecked") List<Map<String, Object>> quotes = (List<Map<String, Object>>) request.get("quotes");
                    Map<String, Object> quote = new LinkedHashMap<>();
                    quote.put("id", quoteId);
                    quote.put("shopName", rs.getString("shop_name"));
                    quote.put("price", rs.getLong("price"));
                    quote.put("days", rs.getInt("lead_time_days"));
                    quote.put("note", rs.getString("note"));
                    quote.put("portfolio", rs.getString("portfolio"));
                    quote.put("status", rs.getString("quote_status"));
                    quote.put("date", rs.getTimestamp("quote_date"));
                    quotes.add(quote);
                    request.put("quoteCount", quotes.size());
                    request.put("hasQuote", true);
                    String status = rs.getString("quote_status");
                    if ("pending".equals(status)) request.put("hasPending", true);
                    if ("accepted".equals(status)) request.put("hasAccepted", true);
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>(byId.values());
        for (Map<String, Object> request : result) {
            String requestStatus = String.valueOf(request.get("requestStatus"));
            if (Boolean.TRUE.equals(request.get("hasAccepted")) || "assigned".equals(requestStatus)) request.put("filterStatus", "accepted");
            else if (!Boolean.TRUE.equals(request.get("hasQuote"))) request.put("filterStatus", "waiting");
            else if (Boolean.TRUE.equals(request.get("hasPending"))) request.put("filterStatus", "quoted");
            else request.put("filterStatus", "expired");
        }
        return result;
    }
}