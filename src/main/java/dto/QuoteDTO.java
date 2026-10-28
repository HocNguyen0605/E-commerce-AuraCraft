package dto;

import java.sql.Timestamp;

public class QuoteDTO {
    private int id;
    private int requestId;
    private int shopId;
    private long price;
    private int days;
    private String note;
    private String portfolio;
    private String status;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    // dùng cho tab "Báo giá của tôi"
    private String requestTitle;
    private long budget;
    private String requestStatus;
    private String buyerName;

    public QuoteDTO() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getShopId() { return shopId; }
    public void setShopId(int shopId) { this.shopId = shopId; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }
    public int getDays() { return days; }
    public void setDays(int days) { this.days = days; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getPortfolio() { return portfolio; }
    public void setPortfolio(String portfolio) { this.portfolio = portfolio; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
    public String getRequestTitle() { return requestTitle; }
    public void setRequestTitle(String requestTitle) { this.requestTitle = requestTitle; }
    public long getBudget() { return budget; }
    public void setBudget(long budget) { this.budget = budget; }
    public String getRequestStatus() { return requestStatus; }
    public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
}