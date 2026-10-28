package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ShopFollowDAO {
    public boolean isFollowing(int userId, int shopId) throws SQLException {
        String sql = "SELECT 1 FROM shopfollows WHERE id_user = ? AND id_shop = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        }
    }

    public int countFollowers(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM shopfollows WHERE id_shop = ?")) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public void setFollowing(int userId, int shopId, boolean following) throws SQLException {
        String sql = following
                ? "INSERT IGNORE INTO shopfollows (id_user, id_shop) VALUES (?, ?)"
                : "DELETE FROM shopfollows WHERE id_user = ? AND id_shop = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, shopId);
            ps.executeUpdate();
        }
    }
}
