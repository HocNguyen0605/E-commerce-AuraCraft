package dto;

import java.util.Date;

/** Một đơn hàng hiển thị ở trang Quản lý đơn hàng của seller. */
public class SellerOrderDTO {
    private int id;
    private int buyerId;
    private String buyerName;
    private String productNames;
    private long amount;
    private Date dateCreate;
    private String address;
    private String status;          // orders.status: paid | processing | shipping | completed
    private Date deadline;          // orders.deadline (có thể null)
    private String latestNote;      // ghi chú tiến độ mới nhất (order_progress)
    private boolean pendingExtension; // có yêu cầu gia hạn đang chờ duyệt

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
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Date getDeadline() { return deadline; }
    public void setDeadline(Date deadline) { this.deadline = deadline; }
    public String getLatestNote() { return latestNote; }
    public void setLatestNote(String latestNote) { this.latestNote = latestNote; }
    public boolean isPendingExtension() { return pendingExtension; }
    public void setPendingExtension(boolean pendingExtension) { this.pendingExtension = pendingExtension; }

    /** Trạng thái hiển thị: pending | crafting | late | shipping | done (khớp data-tab trong JSP). */
    public String getUiStatus() {
        switch (status) {
            case "paid":       return "pending";
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