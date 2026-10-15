package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class RegisterDAO {

    public record Found(int userId, String name, String role, String status) {}

    private Connection open() throws SQLException {
        return util.DBConnection.getConnection();
    }

    /** Form đăng ký có "artisan" nhưng DB chỉ có admin / seller / buyer. */
    public static String toDbRole(String role) {
        if (role == null) return "buyer";
        String r = role.trim().toLowerCase();
        return (r.equals("artisan") || r.equals("seller")) ? "seller" : "buyer";
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT 1 FROM account WHERE username = ? LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean emailExists(String email) throws SQLException {
        String sql = "SELECT 1 FROM users WHERE LOWER(email) = ? LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, email.toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Đăng ký thường: ghi users + account trong một transaction. Trả về id của users. */
    public int createLocal(String name, String username, String email, String passwordHash, String role)
            throws SQLException {
        String insertAccount = "INSERT INTO account (id_user, username, password, role, provider, email_verified, status) "
                + "VALUES (?, ?, ?, ?, 'local', 1, 'active')";
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                int userId = insertUser(c, name, email);
                try (PreparedStatement ps = c.prepareStatement(insertAccount)) {
                    ps.setInt(1, userId);
                    ps.setString(2, username);
                    ps.setString(3, passwordHash);
                    ps.setString(4, role);
                    ps.executeUpdate();
                }
                c.commit();
                return userId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public Found findByGoogleId(String googleId) throws SQLException {
        String sql = "SELECT u.id, u.name, a.role, a.status FROM account a "
                + "JOIN users u ON u.id = a.id_user "
                + "WHERE a.google_id = ? AND a.provider = 'google' LIMIT 1";
        try (Connection c = open(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, googleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new Found(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4));
            }
        }
    }

    /** Đăng ký Google: không có username và password. */
    public Found createGoogle(String name, String email, String googleId, String role) throws SQLException {
        String insertAccount = "INSERT INTO account (id_user, password, role, provider, google_id, email_verified, status) "
                + "VALUES (?, NULL, ?, 'google', ?, 1, 'active')";
        try (Connection c = open()) {
            c.setAutoCommit(false);
            try {
                int userId = insertUser(c, name, email);
                try (PreparedStatement ps = c.prepareStatement(insertAccount)) {
                    ps.setInt(1, userId);
                    ps.setString(2, role);
                    ps.setString(3, googleId);
                    ps.executeUpdate();
                }
                c.commit();
                return new Found(userId, name, role, "active");
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private int insertUser(Connection c, String name, String email) throws SQLException {
        String sql = "INSERT INTO users (name, email) VALUES (?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.setString(2, email.toLowerCase());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được id của users");
                return keys.getInt(1);
            }
        }
    }
}