package dao;

import dto.AddressDTO;
import dto.UserProfileDTO;
import util.DBConnection;   // TODO: sửa package/tên lớp + tên hàm lấy Connection cho đúng project

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
}