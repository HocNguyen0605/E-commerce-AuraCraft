package dto;

public class ChatMessageDTO {
    private String role;     // "user" hoặc "model"
    private String content;  // Nội dung tin nhắn
    private long timestamp;

    public ChatMessageDTO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ChatMessageDTO(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
