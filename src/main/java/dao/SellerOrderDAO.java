package dao;

import dto.*;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Cần các thay đổi DB sau (đã chạy):
 *   ALTER TABLE orders ADD COLUMN deadline datetime NULL;
 *   bảng order_progress(id, id_order, note, created_at)
 *   bảng order_extensions(id, id_order, reason, extra_days, status, created_at)
 */
public class SellerOrderDAO {

    /** Đơn của shop mà seller cần xử lý (không gồm đơn chưa thanh toán / đã huỷ / hoàn tiền). */
    public List<SellerOrderDTO> findByShop(int shopId) throws SQLException {
        String sql =
                "SELECT o.id, o.id_user, u.name AS buyer_name, o.amount, o.date_create, o.shipping_address, " +
                        "       o.status, o.deadline, " +
                        "       (SELECT GROUP_CONCAT(COALESCE(p.name, 'Sản phẩm custom') SEPARATOR ', ') " +
                        "          FROM orderitems oi LEFT JOIN products p ON p.id = oi.id_product " +
                        "         WHERE oi.id_order = o.id) AS product_names, " +
                        "       (SELECT pr.note FROM order_progress pr WHERE pr.id_order = o.id " +
                        "         ORDER BY pr.created_at DESC, pr.id DESC LIMIT 1) AS latest_note, " +
                        "       EXISTS (SELECT 1 FROM order_extensions e WHERE e.id_order = o.id AND e.status = 'pending') AS pending_ext " +
                        "  FROM orders o JOIN users u ON u.id = o.id_user " +
                        " WHERE o.id_shop = ? AND o.status IN ('paid','processing','shipping','completed') " +
                        " ORDER BY o.date_create DESC LIMIT 200";
        List<SellerOrderDTO> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SellerOrderDTO o = new SellerOrderDTO();
                    o.setId(rs.getInt("id"));
                    o.setBuyerId(rs.getInt("id_user"));
                    o.setBuyerName(rs.getString("buyer_name"));
                    o.setAmount(rs.getLong("amount"));
                    o.setDateCreate(rs.getTimestamp("date_create"));
                    o.setAddress(rs.getString("shipping_address"));
                    o.setStatus(rs.getString("status"));
                    o.setDeadline(rs.getTimestamp("deadline"));
                    o.setProductNames(rs.getString("product_names"));
                    o.setLatestNote(rs.getString("latest_note"));
                    o.setPendingExtension(rs.getBoolean("pending_ext"));
                    list.add(o);
                }
            }
        }
        return list;
    }

    /**
     * paid -> processing. Nếu đơn chưa có deadline thì đặt mặc định now + defaultDays ngày.
     * Điều kiện id_shop + status nằm trong WHERE nên vừa kiểm tra quyền sở hữu vừa chống đổi sai trạng thái.
     */
    public boolean startCrafting(int orderId, int shopId, int defaultDays) throws SQLException {
        String sql = "UPDATE orders SET status = 'processing', " +
                "deadline = COALESCE(deadline, DATE_ADD(NOW(), INTERVAL ? DAY)) " +
                "WHERE id = ? AND id_shop = ? AND status = 'paid'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, defaultDays);
            ps.setInt(2, orderId);
            ps.setInt(3, shopId);
            return ps.executeUpdate() == 1;
        }
    }

    /** processing -> shipping. */
    public boolean deliver(int orderId, int shopId) throws SQLException {
        String sql = "UPDATE orders SET status = 'shipping' " +
                "WHERE id = ? AND id_shop = ? AND status = 'processing'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, shopId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Thêm 1 dòng tiến độ (chỉ khi đơn thuộc shop và đang chế tác). */
    public boolean addProgress(int orderId, int shopId, String note) throws SQLException {
        String sql = "INSERT INTO order_progress(id_order, note) " +
                "SELECT id, ? FROM orders WHERE id = ? AND id_shop = ? AND status = 'processing'";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, note);
            ps.setInt(2, orderId);
            ps.setInt(3, shopId);
            return ps.executeUpdate() == 1;
        }
    }

    /** Gửi yêu cầu gia hạn (chỉ khi đang chế tác và chưa có yêu cầu nào đang chờ duyệt). */
    public boolean requestExtension(int orderId, int shopId, String reason, int extraDays) throws SQLException {
        String sql = "INSERT INTO order_extensions(id_order, reason, extra_days, status) " +
                "SELECT o.id, ?, ?, 'pending' FROM orders o " +
                "WHERE o.id = ? AND o.id_shop = ? AND o.status = 'processing' " +
                "AND NOT EXISTS (SELECT 1 FROM order_extensions e WHERE e.id_order = o.id AND e.status = 'pending')";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setInt(2, extraDays);
            ps.setInt(3, orderId);
            ps.setInt(4, shopId);
            return ps.executeUpdate() == 1;
        }
    }
}