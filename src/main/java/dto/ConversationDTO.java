package dto;

import java.util.Date;

public class ConversationDTO {
    private long id;
    private long shopId;
    private long buyerId;
    private String buyerName;
    private String buyerAvatar;
    private String lastMessage;
    private Date lastMessageAt;
    private int sellerUnread;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getShopId() { return shopId; }
    public void setShopId(long shopId) { this.shopId = shopId; }
    public long getBuyerId() { return buyerId; }
    public void setBuyerId(long buyerId) { this.buyerId = buyerId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getBuyerAvatar() { return buyerAvatar; }
    public void setBuyerAvatar(String buyerAvatar) { this.buyerAvatar = buyerAvatar; }
    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }
    public Date getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(Date lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public int getSellerUnread() { return sellerUnread; }
    public void setSellerUnread(int sellerUnread) { this.sellerUnread = sellerUnread; }
}