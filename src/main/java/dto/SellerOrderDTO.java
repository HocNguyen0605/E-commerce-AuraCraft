package dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Một đơn hàng hiển thị ở trang Quản lý đơn hàng của seller. */
public class SellerOrderDTO {
    private int id;
    private int buyerId;
    private String buyerName;
    private String productNames;
    private long amount;            // tổng thanh toán (đã gồm phí ship, đã trừ giảm giá)
    private Date dateCreate;
    private String address;
    private String receiver;        // orders.shipping_receiver
    private String phone;           // orders.shipping_phone
    private String buyerNote;       // orders.shipping_note (ghi chú của người mua)
    private long shippingFee;
    private long discount;
    private String gateway;         // transactions.gateway: cod | vnpay
    private String status;          // orders.status: pending | paid | processing | shipping | completed
    private Date deadline;
    private String latestNote;
    private boolean pendingExtension;
    private List<SellerOrderItemDTO> items = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getProductNames() { return productNames; }
    public void setProductNames(String productNames) { this.productNames = productNames; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    public Date getDateCreate() { return dateCreate; }
    public void setDateCreate(Date dateCreate) { this.dateCreate = dateCreate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getBuyerNote() { return buyerNote; }
    public void setBuyerNote(String buyerNote) { this.buyerNote = buyerNote; }
    public long getShippingFee() { return shippingFee; }
    public void setShippingFee(long shippingFee) { this.shippingFee = shippingFee; }
    public long getDiscount() { return discount; }
    public void setDiscount(long discount) { this.discount = discount; }
    public String getGateway() { return gateway; }
    public void setGateway(String gateway) { this.gateway = gateway; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getLatestNote() { return latestNote; }
    public void setLatestNote(String latestNote) { this.latestNote = latestNote; }
    public boolean isPendingExtension() { return pendingExtension; }
    public void setPendingExtension(boolean pendingExtension) { this.pendingExtension = pendingExtension; }
    public List<SellerOrderItemDTO> getItems() { return items; }
    public void setItems(List<SellerOrderItemDTO> items) { this.items = items; }

    /** Tổng tiền hàng (chưa gồm ship/giảm giá). */
    public long getItemsTotal() {
        long sum = 0;
        for (SellerOrderItemDTO it : items) sum += it.getLineTotal();
        return sum;
    }

    /** Tổng số lượng sản phẩm. */
    public int getItemCount() {
        int n = 0;
        for (SellerOrderItemDTO it : items) n += it.getQuantity();
        return n;
    }

    public String getPaymentLabel() {
        if (gateway == null) return "Chưa rõ";
        switch (gateway.toLowerCase()) {
            case "cod":   return "Thanh toán khi nhận hàng (COD)";
            case "vnpay": return "VNPay";
            default:      return gateway;
        }
    }

    /** Trạng thái hiển thị: pending | crafting | late | shipping | done (khớp data-tab trong JSP). */
    public String getUiStatus() {
        if (status == null) return "pending";
        switch (status) {
            case "paid":
            case "pending":    return "pending";
            case "processing": return isOverdue() ? "late" : "crafting";
            case "shipping":   return "shipping";
            case "completed":  return "done";
            default:           return status;
        }
    }

    public boolean isOverdue() {
        return "processing".equals(status) && deadline != null && deadline.before(new Date());
    }

    /** Số ngày quá hạn (tối thiểu 1 nếu đã quá hạn). */
    public long getOverdueDays() {
        if (!isOverdue()) return 0;
        long days = (System.currentTimeMillis() - deadline.getTime()) / 86_400_000L;
        return Math.max(1, days);
    }
}