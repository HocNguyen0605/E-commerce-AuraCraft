package dao;

import dto.Shop;
import util.DBConnection;

import java.sql.*;

public class ShopDAO {

    /** Shop của chủ shop (users.id), chỉ lấy shop đang hoạt động. Null nếu không có. */
    public Shop findActiveByOwner(int ownerId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id_owner = ? AND status = 'active' ORDER BY id LIMIT 1";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Shop s = new Shop();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("id_owner"));
                s.setName(rs.getString("name"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setDescription(rs.getString("description"));
                s.setAvatar(rs.getString("avatar"));
                s.setStatus(rs.getString("status"));
                s.setRating(rs.getDouble("rating"));
                s.setCommissionRate(rs.getDouble("commission_rate"));
                return s;
            }
        }
    }
}