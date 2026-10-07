package dto;

public class OrderAdminDTO {
    private int orderId;
    private String buyerName;
    private String buyerEmail;
    private String sellerName;
    private String shopName;
    private String status; // pending, paid, processing, shipping, completed, cancelled, refunded
    private double amount;
    private double shippingFee;
    private double discount;
    private double totalFee; // sum of fee_amount from orderitems
    private String shippingAddress;
    private String dateCreate;
    private String datePaid;
    private String paymentMethod;
    private String productSummary; // concatenated product names
    private int itemCount;
    private boolean isCustom;

    public OrderAdminDTO() {}

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }

    public String getBuyerEmail() { return buyerEmail; }
    public void setBuyerEmail(String buyerEmail) { this.buyerEmail = buyerEmail; }

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public double getShippingFee() { return shippingFee; }
    public void setShippingFee(double shippingFee) { this.shippingFee = shippingFee; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }

    public double getTotalFee() { return totalFee; }
    public void setTotalFee(double totalFee) { this.totalFee = totalFee; }

    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }

    public String getDateCreate() { return dateCreate; }
    public void setDateCreate(String dateCreate) { this.dateCreate = dateCreate; }

    public String getDatePaid() { return datePaid; }
    public void setDatePaid(String datePaid) { this.datePaid = datePaid; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getProductSummary() { return productSummary; }
    public void setProductSummary(String productSummary) { this.productSummary = productSummary; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean isCustom) { this.isCustom = isCustom; }
}
