package dao;

import dto.AccountInfoDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountDAO {

    public AccountInfoDTO findByEmail(String email) throws SQLException {
        String sql = """
                SELECT u.id, u.name, u.email, a.password, a.role, a.provider, a.email_verified
                FROM users u JOIN account a ON a.id_user = u.id WHERE u.email = ?
                """;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
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