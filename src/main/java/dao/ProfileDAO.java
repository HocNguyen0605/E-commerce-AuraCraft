package dao;

import dto.AddressDTO;
import dto.UserProfileDTO;
import util.DBConnection;   // TODO: sửa package/tên lớp + tên hàm lấy Connection cho đúng project

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProfileDAO {

    public UserProfileDTO findUser(int userId) throws SQLException {
        String sql = "SELECT id, name, email, phone, birthday FROM users WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new UserProfileDTO(rs.getInt("id"), rs.getString("name"),
                        rs.getString("email"), rs.getString("phone"), rs.getDate("birthday"));
            }
        }
    }

    public List<AddressDTO> findAddresses(int userId) throws SQLException {
        String sql = "SELECT id, receiver, phone, address, is_default FROM addresses " +
                "WHERE id_user = ? ORDER BY is_default DESC, id";
        List<AddressDTO> list = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AddressDTO(rs.getInt("id"), rs.getString("receiver"),
                            rs.getString("phone"), rs.getString("address"), rs.getBoolean("is_default")));
                }
            }
        }
        return list;
    }

    /** Cập nhật thông tin cá nhân (email không cho đổi). */
    public boolean updateUser(int userId, String name, String phone, LocalDate birthday) throws Exception {
        String sql = "UPDATE users SET name = ?, phone = ?, birthday = ? WHERE id = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name);
            if (phone == null) ps.setNull(2, Types.VARCHAR); else ps.setString(2, phone);
            if (birthday == null) ps.setNull(3, Types.DATE); else ps.setDate(3, Date.valueOf(birthday));
            ps.setInt(4, userId);
            return ps.executeUpdate() > 0;
        }
    }

    public int countAddresses(int userId) throws Exception {
        String sql = "SELECT COUNT(*) FROM addresses WHERE id_user = ?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Thêm địa chỉ. Địa chỉ đầu tiên luôn là mặc định; nếu isDefault thì bỏ mặc định của các địa chỉ khác. */
    public boolean addAddress(int userId, String receiver, String phone, String address, boolean isDefault) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean first;
                try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM addresses WHERE id_user = ? FOR UPDATE")) {
                    ps.setInt(1, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        first = rs.next() && rs.getInt(1) == 0;
                    }
                }
                boolean makeDefault = isDefault || first;
                if (makeDefault) clearDefault(c, userId);

                String sql = "INSERT INTO addresses (id_user, receiver, phone, address, is_default) VALUES (?,?,?,?,?)";
                int rows;
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setInt(1, userId);
                    ps.setString(2, receiver);
                    ps.setString(3, phone);
                    ps.setString(4, address);
                    ps.setBoolean(5, makeDefault);
                    rows = ps.executeUpdate();
                }
                c.commit();
                return rows > 0;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Sửa địa chỉ (chỉ sửa được địa chỉ của chính user). Địa chỉ đang mặc định không thể bỏ mặc định trực tiếp. */
    public boolean updateAddress(int id, int userId, String receiver, String phone, String address, boolean isDefault) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                Boolean wasDefault = null;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT is_default FROM addresses WHERE id = ? AND id_user = ? FOR UPDATE")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) wasDefault = rs.getBoolean(1);
                    }
                }
                if (wasDefault == null) { c.rollback(); return false; } // không tồn tại / không thuộc user

                boolean makeDefault = isDefault || wasDefault;
                if (makeDefault && !wasDefault) clearDefault(c, userId);

                int rows;
                String sql = "UPDATE addresses SET receiver = ?, phone = ?, address = ?, is_default = ? WHERE id = ? AND id_user = ?";
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, receiver);
                    ps.setString(2, phone);
                    ps.setString(3, address);
                    ps.setBoolean(4, makeDefault);
                    ps.setInt(5, id);
                    ps.setInt(6, userId);
                    rows = ps.executeUpdate();
                }
                c.commit();
                return rows > 0;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    /** Xóa địa chỉ. Nếu xóa địa chỉ mặc định thì tự chọn địa chỉ còn lại (id nhỏ nhất) làm mặc định. */
    public boolean deleteAddress(int id, int userId) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                boolean wasDefault = false;
                boolean exists = false;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT is_default FROM addresses WHERE id = ? AND id_user = ? FOR UPDATE")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { exists = true; wasDefault = rs.getBoolean(1); }
                    }
                }
                if (!exists) { c.rollback(); return false; }

                try (PreparedStatement ps = c.prepareStatement("DELETE FROM addresses WHERE id = ? AND id_user = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                if (wasDefault) {
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE addresses SET is_default = 1 WHERE id_user = ? ORDER BY id LIMIT 1")) {
                        ps.setInt(1, userId);
                        ps.executeUpdate();
                    }
                }
                c.commit();
                return true;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public boolean setDefaultAddress(int id, int userId) throws Exception {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT 1 FROM addresses WHERE id = ? AND id_user = ? FOR UPDATE")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) { c.rollback(); return false; }
                    }
                }
                clearDefault(c, userId);
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE addresses SET is_default = 1 WHERE id = ? AND id_user = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, userId);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private void clearDefault(Connection c, int userId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE addresses SET is_default = 0 WHERE id_user = ?")) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}