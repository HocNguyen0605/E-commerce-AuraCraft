package dao;

import dto.ChatMessageDTO;
import dto.ConversationDTO;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ChatDAO {

    /** Danh sách hội thoại của shop, có thể tìm theo tên khách. */
    public List<ConversationDTO> findConversations(long shopId, String keyword) throws SQLException {
        boolean hasKw = keyword != null && !keyword.isBlank();
        String sql = "SELECT c.*, u.name AS full_name, NULL AS avatar FROM conversations c " +
                "JOIN users u ON u.id = c.buyer_id WHERE c.shop_id = ? " +
                (hasKw ? "AND u.name LIKE ? " : "") +
                "ORDER BY c.last_message_at DESC";
        List<ConversationDTO> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, shopId);
            if (hasKw) ps.setString(2, "%" + keyword.trim() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Lấy 1 hội thoại, kiểm tra thuộc đúng shop (chống xem trộm bằng cách đổi id). */
    public ConversationDTO findByIdAndShop(long id, long shopId) throws SQLException {
        String sql = "SELECT c.*, u.name AS full_name, NULL AS avatar FROM conversations c " +
                "JOIN users u ON u.id = c.buyer_id WHERE c.id = ? AND c.shop_id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.setLong(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<ChatMessageDTO> findMessages(long conversationId) throws SQLException {
        String sql = "SELECT * FROM messages WHERE conversation_id = ? ORDER BY created_at, id";
        List<ChatMessageDTO> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, conversationId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ChatMessageDTO m = new ChatMessageDTO();
                    m.setId(rs.getLong("id"));
                    m.setConversationId(rs.getLong("conversation_id"));
                    m.setSenderRole(rs.getString("sender_role"));
                    m.setContent(rs.getString("content"));
                    m.setCreatedAt(rs.getTimestamp("created_at"));
                    list.add(m);
                }
            }
        }
        return list;
    }

    /** Seller gửi tin: insert message + cập nhật hội thoại trong 1 transaction. */
    public void insertSellerMessage(long conversationId, String content) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(
                        "INSERT INTO messages(conversation_id, sender_role, content) VALUES (?, 'seller', ?)")) {
                    ps.setLong(1, conversationId);
                    ps.setString(2, content);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE conversations SET last_message = ?, last_message_at = NOW() WHERE id = ?")) {
                    ps.setString(1, content.length() > 500 ? content.substring(0, 500) : content);
                    ps.setLong(2, conversationId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    public void markReadForSeller(long conversationId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "UPDATE conversations SET seller_unread = 0 WHERE id = ?")) {
            ps.setLong(1, conversationId);
            ps.executeUpdate();
        }
    }

    /** Tổng tin chưa đọc của shop, dùng cho badge ở sidebar. */
    public int countUnread(long shopId) throws SQLException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(
                     "SELECT COALESCE(SUM(seller_unread),0) FROM conversations WHERE shop_id = ?")) {
            ps.setLong(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Mở hội thoại với người mua từ trang đơn hàng: tìm, nếu chưa có thì tạo.
     * Chỉ tạo khi người mua thật sự có đơn ở shop này. Trả về id hội thoại, hoặc 0 nếu không hợp lệ.
     */
    public long findOrCreateConversation(long shopId, long buyerId) throws SQLException {
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(
                    "INSERT IGNORE INTO conversations(shop_id, buyer_id) " +
                            "SELECT ?, ? FROM DUAL WHERE EXISTS (SELECT 1 FROM orders WHERE id_shop = ? AND id_user = ?)")) {
                ps.setLong(1, shopId);
                ps.setLong(2, buyerId);
                ps.setLong(3, shopId);
                ps.setLong(4, buyerId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT id FROM conversations WHERE shop_id = ? AND buyer_id = ?")) {
                ps.setLong(1, shopId);
                ps.setLong(2, buyerId);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? rs.getLong(1) : 0;
                }
            }
        }
    }

    private ConversationDTO map(ResultSet rs) throws SQLException {
        ConversationDTO c = new ConversationDTO();
        c.setId(rs.getLong("id"));
        c.setShopId(rs.getLong("shop_id"));
        c.setBuyerId(rs.getLong("buyer_id"));
        c.setBuyerName(rs.getString("full_name"));
        c.setBuyerAvatar(rs.getString("avatar"));
        c.setLastMessage(rs.getString("last_message"));
        c.setLastMessageAt(rs.getTimestamp("last_message_at"));
        c.setSellerUnread(rs.getInt("seller_unread"));
        return c;
    }
}