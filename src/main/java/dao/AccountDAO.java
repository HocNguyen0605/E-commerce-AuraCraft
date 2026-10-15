package dao;

import dto.AccountInfoDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

      public AccountInfoDTO findByEmail(String email) throws SQLException {
        return findByLogin(email);
    }

    /** Tìm tài khoản theo email HOẶC tên đăng nhập (cả hai đều lưu chữ thường). */
    public AccountInfoDTO findByLogin(String login) throws SQLException {
        String sql = """
                SELECT u.id, u.name, u.email, a.password, a.role, a.provider, a.email_verified
                FROM users u JOIN account a ON a.id_user = u.id
                WHERE LOWER(u.email) = ? OR a.username = ?
                LIMIT 1
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            String key = login == null ? "" : login.trim().toLowerCase();
            ps.setString(1, key);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                AccountInfoDTO a = new AccountInfoDTO();
                a.userId = rs.getInt("id");
                a.name = rs.getString("name");
                a.email = rs.getString("email");
                a.passwordHash = rs.getString("password");
                a.role = rs.getString("role");
                a.provider = rs.getString("provider");
                a.emailVerified = rs.getBoolean("email_verified");
                return a;
            }
        }
    }
}