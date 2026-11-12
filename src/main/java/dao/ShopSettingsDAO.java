package dao;

import dto.ShopProfileDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Cài đặt shop phía seller: đọc & cập nhật hồ sơ shop của chính chủ. */
public class ShopSettingsDAO {

    /** Shop đang hoạt động của chủ shop; null nếu không có. */
    public ShopProfileDTO findActiveByOwner(int ownerId) throws SQLException {
        String sql = "SELECT id, id_owner, name, phone, email, address, description, avatar, status, rating, commission_rate " +
                "FROM shops WHERE id_owner = ? AND status = 'active' ORDER BY id LIMIT 1";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ShopProfileDTO s = new ShopProfileDTO();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("id_owner"));
                s.setName(rs.getString("name"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setAddress(rs.getString("address"));
                s.setDescription(rs.getString("description"));
                s.setAvatar(rs.getString("avatar"));
                s.setStatus(rs.getString("status"));
                s.setRating(rs.getBigDecimal("rating"));
                s.setCommissionRate(rs.getBigDecimal("commission_rate"));
                return s;
            }
        }
    }

    /**
     * Cập nhật hồ sơ shop. newAvatar = null thì giữ ảnh cũ.
     * Điều kiện id_owner đảm bảo seller chỉ sửa được shop của chính mình.
     */
    public boolean updateProfile(int shopId, int ownerId, String name, String phone, String email,
                                 String address, String description, String newAvatar) throws SQLException {
        String sql = "UPDATE shops SET name = ?, phone = ?, email = ?, address = ?, description = ?" +
                (newAvatar != null ? ", avatar = ?" : "") +
                " WHERE id = ? AND id_owner = ? AND status = 'active'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, name);
            ps.setString(i++, phone);
            ps.setString(i++, email);
            ps.setString(i++, address);
            ps.setString(i++, description);
            if (newAvatar != null) ps.setString(i++, newAvatar);
            ps.setInt(i++, shopId);
            ps.setInt(i, ownerId);
            return ps.executeUpdate() > 0;
        }
    }
}