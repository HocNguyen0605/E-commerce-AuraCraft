package dto;

public class UserAdminDTO {
    private int userId;
    private String name;
    private String email;
    private String role;
    private String shopName;
    private String status; // active, pending, locked
    private String description;
    private int shopId;

    public UserAdminDTO() {}

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
}
