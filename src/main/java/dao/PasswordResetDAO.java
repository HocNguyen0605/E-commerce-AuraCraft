package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PasswordResetDAO {

    /** Chỉ tài khoản đăng ký bằng mật khẩu (provider local) và đang hoạt động mới đặt lại được mật khẩu. */
    public boolean hasLocalPassword(String email) throws SQLException {
        String sql = """
                SELECT 1 FROM users u JOIN account a ON a.id_user = u.id
                WHERE LOWER(u.email) = ? AND a.provider = 'local'
                  AND a.password IS NOT NULL AND a.status = 'active'
                LIMIT 1
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Trả về số dòng được cập nhật (0 nghĩa là không tìm thấy tài khoản phù hợp). */
    public int updatePassword(String email, String passwordHash) throws SQLException {
        String sql = """
                UPDATE account a JOIN users u ON u.id = a.id_user
                SET a.password = ?
                WHERE LOWER(u.email) = ? AND a.provider = 'local' AND a.status = 'active'
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, passwordHash);
            ps.setString(2, email.toLowerCase());
            return ps.executeUpdate();
        }
    }
}