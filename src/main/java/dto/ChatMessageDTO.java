package dto;

import java.util.Date;

public class ChatMessageDTO {
    private long id;
    private long conversationId;
    private String senderRole;   // "buyer" | "seller"
    private String content;
    private Date createdAt;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getConversationId() { return conversationId; }
    public void setConversationId(long conversationId) { this.conversationId = conversationId; }
    public String getSenderRole() { return senderRole; }
    public void setSenderRole(String senderRole) { this.senderRole = senderRole; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
    /** true nếu là tin của seller (dùng cho class "sent" trong JSP: ${m.sent}) */
    public boolean isSent() { return "seller".equals(senderRole); }
}