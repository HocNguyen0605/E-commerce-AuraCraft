package dao;

import dto.UserAdminDTO;
import dto.UserAdminResponseDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class UserAdminDAO {

    public UserAdminResponseDTO getUsersData() {
        UserAdminResponseDTO response = new UserAdminResponseDTO();
        List<UserAdminDTO> users = new ArrayList<>();

        int totalUsers = 0;
        int buyerUsers = 0;
        int pendingArtisans = 0;
        int suspendedUsers = 0;

        String query = "SELECT u.id, u.name, u.email, a.role, a.status as accountStatus, s.name as shopName, s.description, s.id as shopId " +
                       "FROM users u " +
                       "JOIN account a ON u.id = a.id_user " +
                       "LEFT JOIN shops s ON u.id = s.id_owner " +
                       "ORDER BY u.id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UserAdminDTO user = new UserAdminDTO();
                user.setUserId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                
                String role = rs.getString("role");
                user.setRole(role);
                user.setShopName(rs.getString("shopName"));
                user.setDescription(rs.getString("description"));
                user.setShopId(rs.getInt("shopId"));
                
                String status = rs.getString("accountStatus");
                if (status == null) status = "active"; // fallback
                user.setStatus(status);

                users.add(user);

                // Stats calculation
                totalUsers++;
                if ("buyer".equals(role)) {
                    buyerUsers++;
                } else if ("seller".equals(role)) {
                    if ("pending".equals(status)) pendingArtisans++;
                    if ("locked".equals(status)) suspendedUsers++;
                }
            }
        } catch (Exception e) {
            System.err.println("[UserAdminDAO] Error getting users data: " + e.getMessage());
            e.printStackTrace();
        }

        response.setUsers(users);
        response.setTotalUsers(totalUsers);
        response.setBuyerUsers(buyerUsers);
        response.setPendingArtisans(pendingArtisans);
        response.setSuspendedUsers(suspendedUsers);

        return response;
    }

    public boolean updateUserStatus(int userId, String status) {
        String query = "UPDATE account SET status = ? WHERE id_user = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, userId);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
