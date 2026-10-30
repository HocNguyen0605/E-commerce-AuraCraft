package dao;

import dto.SellerOrderDTO;
import dto.SellerOrderItemDTO;
import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cần DB:
 *   orders.deadline datetime NULL
 *   order_progress(id, id_order, note, created_at DEFAULT CURRENT_TIMESTAMP)
 *   order_extensions(id, id_order, reason, extra_days, status, created_at DEFAULT CURRENT_TIMESTAMP)
 */
public class SellerOrderDAO {

    /** Điều kiện "đơn có thể bắt đầu chế tác": đã thanh toán, hoặc đơn COD vừa đặt. */
    private static final String STARTABLE =
            "(o.status = 'paid' OR (o.status = 'pending' AND EXISTS (" +
                    "   SELECT 1 FROM transactions t WHERE t.id_order = o.id AND t.gateway = 'cod')))";

    /** Đơn của shop mà seller cần xử lý, kèm danh sách sản phẩm của từng đơn. */
    public List<SellerOrderDTO> findByShop(int shopId) throws SQLException {
        String sql =
                "SELECT o.id, o.id_user, u.name AS buyer_name, o.amount, o.date_create, o.shipping_address, " +
                        "       o.shipping_receiver, o.shipping_phone, o.shipping_note, o.shipping_fee, o.discount, " +
                        "       o.status, o.deadline, " +
                        "       (SELECT t.gateway FROM transactions t WHERE t.id_order = o.id ORDER BY t.id DESC LIMIT 1) AS gateway, " +
                        "       (SELECT GROUP_CONCAT(COALESCE(p.name, 'Sản phẩm custom') SEPARATOR ', ') " +
                        "          FROM orderitems oi LEFT JOIN products p ON p.id = oi.id_product " +
                        "         WHERE oi.id_order = o.id) AS product_names, " +
                        "       (SELECT pr.note FROM order_progress pr WHERE pr.id_order = o.id " +
                        "         ORDER BY pr.created_at DESC, pr.id DESC LIMIT 1) AS latest_note, " +
                        "       EXISTS (SELECT 1 FROM order_extensions e WHERE e.id_order = o.id AND e.status = 'pending') AS pending_ext " +
                        "  FROM orders o JOIN users u ON u.id = o.id_user " +
                        " WHERE o.id_shop = ? AND (o.status IN ('paid','processing','shipping','completed') " +
                        "        OR (o.status = 'pending' AND EXISTS (SELECT 1 FROM transactions t " +
                        "                                              WHERE t.id_order = o.id AND t.gateway = 'cod'))) " +
                        " ORDER BY o.date_create DESC LIMIT 200";
        List<SellerOrderDTO> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(sql)) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        SellerOrderDTO o = new SellerOrderDTO();
                        o.setId(rs.getInt("id"));
                        o.setBuyerId(rs.getInt("id_user"));
                        o.setBuyerName(rs.getString("buyer_name"));
                        o.setAmount(money(rs, "amount"));
                        o.setDateCreate(rs.getTimestamp("date_create"));
                        o.setAddress(rs.getString("shipping_address"));
                        o.setReceiver(rs.getString("shipping_receiver"));
                        o.setPhone(rs.getString("shipping_phone"));
                        o.setBuyerNote(rs.getString("shipping_note"));
                        o.setShippingFee(money(rs, "shipping_fee"));
                        o.setDiscount(money(rs, "discount"));
                        o.setGateway(rs.getString("gateway"));
                        o.setStatus(rs.getString("status"));
                        o.setDeadline(rs.getTimestamp("deadline"));
                        o.setProductNames(rs.getString("product_names"));
                        o.setLatestNote(rs.getString("latest_note"));
                        o.setPendingExtension(rs.getBoolean("pending_ext"));
                        list.add(o);
                    }
                }
            }
            loadItems(con, list);
        }
        return list;
    }

    /** Nạp toàn bộ sản phẩm của các đơn bằng 1 truy vấn (kèm ảnh đầu tiên của sản phẩm). */
    private void loadItems(Connection con, List<SellerOrderDTO> orders) throws SQLException {
        if (orders.isEmpty()) return;
        Map<Integer, SellerOrderDTO> byId = new HashMap<>();
        StringBuilder in = new StringBuilder();
        for (SellerOrderDTO o : orders) {
            byId.put(o.getId(), o);
            if (in.length() > 0) in.append(',');
            in.append('?');
        }
        String sql =
                "SELECT oi.id_order, oi.id_product, COALESCE(p.name, 'Sản phẩm custom') AS name, " +
                        "       oi.quantity, oi.price, pi.url AS image " +
                        "  FROM orderitems oi " +
                        "  LEFT JOIN products p ON p.id = oi.id_product " +
                        "  LEFT JOIN productimages pi ON pi.id = " +
                        "       (SELECT MIN(pi2.id) FROM productimages pi2 WHERE pi2.id_product = p.id) " +
                        " WHERE oi.id_order IN (" + in + ") " +
                        " ORDER BY oi.id_order, oi.id";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            int i = 1;
            for (SellerOrderDTO o : orders) ps.setInt(i++, o.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SellerOrderItemDTO it = new SellerOrderItemDTO();
                    it.setProductId(rs.getInt("id_product"));
                    it.setName(rs.getString("name"));
                    it.setQuantity(rs.getInt("quantity"));
                    it.setPrice(money(rs, "price"));
                    it.setImageRaw(rs.getString("image"));
                    SellerOrderDTO o = byId.get(rs.getInt("id_order"));
                    if (o != null) o.getItems().add(it);
                }
            }
        }
    }

    private static long money(ResultSet rs, String col) throws SQLException {
        BigDecimal v = rs.getBigDecimal(col);
        return v == null ? 0L : v.longValue();
    }

    /**
     * pending(COD) | paid -> processing. Nếu đơn chưa có deadline thì đặt mặc định now + defaultDays ngày.
     */
    public boolean startCrafting(int orderId, int shopId, int defaultDays) throws SQLException {
        String sql = "UPDATE orders o SET o.status = 'processing', " +
                "o.deadline = COALESCE(o.deadline, DATE_ADD(NOW(), INTERVAL ? DAY)) " +
                "WHERE o.id = ? AND o.id_shop = ? AND " + STARTABLE;
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