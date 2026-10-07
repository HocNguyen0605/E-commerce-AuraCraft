package dto;

import java.util.List;

public class OrderAdminResponseDTO {
    private List<OrderAdminDTO> orders;
    private int totalOrders;
    private int pendingOrders;
    private int processingOrders;
    private int shippingOrders;
    private int completedOrders;
    private int cancelledOrders;
    private double totalRevenue;
    private double totalFees;
    private List<ShopFilterDTO> shops; // for filter dropdown

    public OrderAdminResponseDTO() {}

    public List<OrderAdminDTO> getOrders() { return orders; }
    public void setOrders(List<OrderAdminDTO> orders) { this.orders = orders; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getPendingOrders() { return pendingOrders; }
    public void setPendingOrders(int pendingOrders) { this.pendingOrders = pendingOrders; }

    public int getProcessingOrders() { return processingOrders; }
    public void setProcessingOrders(int processingOrders) { this.processingOrders = processingOrders; }

    public int getShippingOrders() { return shippingOrders; }
    public void setShippingOrders(int shippingOrders) { this.shippingOrders = shippingOrders; }

    public int getCompletedOrders() { return completedOrders; }
    public void setCompletedOrders(int completedOrders) { this.completedOrders = completedOrders; }

    public int getCancelledOrders() { return cancelledOrders; }
    public void setCancelledOrders(int cancelledOrders) { this.cancelledOrders = cancelledOrders; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public double getTotalFees() { return totalFees; }
    public void setTotalFees(double totalFees) { this.totalFees = totalFees; }

    public List<ShopFilterDTO> getShops() { return shops; }
    public void setShops(List<ShopFilterDTO> shops) { this.shops = shops; }

    // Inner DTO for shop filter dropdown
    public static class ShopFilterDTO {
        private int shopId;
        private String shopName;
        private String ownerName;

        public ShopFilterDTO() {}

        public ShopFilterDTO(int shopId, String shopName, String ownerName) {
            this.shopId = shopId;
            this.shopName = shopName;
            this.ownerName = ownerName;
        }

        public int getShopId() { return shopId; }
        public void setShopId(int shopId) { this.shopId = shopId; }

        public String getShopName() { return shopName; }
        public void setShopName(String shopName) { this.shopName = shopName; }

        public String getOwnerName() { return ownerName; }
        public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    }
}
