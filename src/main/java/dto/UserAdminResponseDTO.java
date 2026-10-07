package dto;

import java.util.List;

public class UserAdminResponseDTO {
    private List<UserAdminDTO> users;
    private int totalUsers;
    private int buyerUsers;
    private int pendingArtisans;
    private int suspendedUsers;

    public UserAdminResponseDTO() {}

    public List<UserAdminDTO> getUsers() { return users; }
    public void setUsers(List<UserAdminDTO> users) { this.users = users; }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }

    public int getBuyerUsers() { return buyerUsers; }
    public void setBuyerUsers(int buyerUsers) { this.buyerUsers = buyerUsers; }

    public int getPendingArtisans() { return pendingArtisans; }
    public void setPendingArtisans(int pendingArtisans) { this.pendingArtisans = pendingArtisans; }

    public int getSuspendedUsers() { return suspendedUsers; }
    public void setSuspendedUsers(int suspendedUsers) { this.suspendedUsers = suspendedUsers; }
}
