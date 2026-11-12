package dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DailyRevenueDTO {
    private String label;
    private long amount;

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }

    public static class Charm {
        private int id;
        private String name;
        private long price;
        private String image;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public long getPrice() { return price; }
        public void setPrice(long price) { this.price = price; }
        public String getImage() { return image; }
        public void setImage(String image) { this.image = image; }
    }

    public static class ChatMessageDTO {
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

    public static class ConversationDTO {
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

    public static class CustomRequestDTO {
        private int id;
        private int buyerId;
        private String buyerName;
        private String productType;
        private String materials;
        private String charmDescription;
        private long budget;
        private String status;
        private Timestamp createdAt;
        private int quoteCount;
        private QuoteDTO myQuote;                       // báo giá của shop đang đăng nhập (null nếu chưa có)
        private List<String> designImages = new ArrayList<>();
        private List<String> charmImages = new ArrayList<>();

        /** "5 phút trước", "2 giờ trước", "3 ngày trước" */
        public String getTimeAgo() {
            if (createdAt == null) return "";
            long m = Math.max(0, (System.currentTimeMillis() - createdAt.getTime()) / 60000);
            if (m < 1) return "Vừa xong";
            if (m < 60) return m + " phút trước";
            long h = m / 60;
            if (h < 24) return h + " giờ trước";
            return (h / 24) + " ngày trước";
        }

        public List<String> getAllImages() {
            List<String> all = new ArrayList<>(designImages);
            all.addAll(charmImages);
            return all;
        }

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getBuyerId() { return buyerId; }
        public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
        public String getBuyerName() { return buyerName; }
        public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
        public String getProductType() { return productType; }
        public void setProductType(String productType) { this.productType = productType; }
        public String getMaterials() { return materials; }
        public void setMaterials(String materials) { this.materials = materials; }
        public String getCharmDescription() { return charmDescription; }
        public void setCharmDescription(String charmDescription) { this.charmDescription = charmDescription; }
        public long getBudget() { return budget; }
        public void setBudget(long budget) { this.budget = budget; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Timestamp getCreatedAt() { return createdAt; }
        public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
        public int getQuoteCount() { return quoteCount; }
        public void setQuoteCount(int quoteCount) { this.quoteCount = quoteCount; }
        public QuoteDTO getMyQuote() { return myQuote; }
        public void setMyQuote(QuoteDTO myQuote) { this.myQuote = myQuote; }
        public List<String> getDesignImages() { return designImages; }
        public void setDesignImages(List<String> designImages) { this.designImages = designImages; }
        public List<String> getCharmImages() { return charmImages; }
        public void setCharmImages(List<String> charmImages) { this.charmImages = charmImages; }
    }

    public static class QuoteDTO {
        private int id;
        private int requestId;
        private int shopId;
        private long price;
        private int days;
        private String note;
        private String portfolio;
        private String status;          // pending | accepted | rejected | withdrawn
        private Timestamp createdAt;
        private Timestamp updatedAt;

        // Chỉ có khi lấy danh sách "Báo giá của tôi"
        private String requestTitle;
        private String buyerName;
        private long budget;
        private String requestStatus;

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
        public String getBuyerName() { return buyerName; }
        public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
        public long getBudget() { return budget; }
        public void setBudget(long budget) { this.budget = budget; }
        public String getRequestStatus() { return requestStatus; }
        public void setRequestStatus(String requestStatus) { this.requestStatus = requestStatus; }
    }

    /** Một đơn hàng hiển thị ở trang Quản lý đơn hàng của seller. */
    public static class SellerOrderDTO {
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

    public static class ShopDTO {
        private int id;
        private int ownerId;
        private String name;
        private String address;
        private String phone;
        private String email;
        private String description;
        private String avatar;
        private String status;
        private double rating;
        private double commissionRate;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getOwnerId() { return ownerId; }
        public void setOwnerId(int ownerId) { this.ownerId = ownerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public double getRating() { return rating; }
        public void setRating(double rating) { this.rating = rating; }
        public double getCommissionRate() { return commissionRate; }
        public void setCommissionRate(double commissionRate) { this.commissionRate = commissionRate; }
    }

    public static class Withdrawal {
        private int id;
        private int shopId;
        private long amount;
        private String bankName;
        private String bankAccount;
        private String accountHolder;
        private String status;
        private Timestamp dateCreate;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getShopId() { return shopId; }
        public void setShopId(int shopId) { this.shopId = shopId; }
        public long getAmount() { return amount; }
        public void setAmount(long amount) { this.amount = amount; }
        public String getBankName() { return bankName; }
        public void setBankName(String bankName) { this.bankName = bankName; }
        public String getBankAccount() { return bankAccount; }
        public void setBankAccount(String bankAccount) { this.bankAccount = bankAccount; }
        public String getAccountHolder() { return accountHolder; }
        public void setAccountHolder(String accountHolder) { this.accountHolder = accountHolder; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Timestamp getDateCreate() { return dateCreate; }
        public void setDateCreate(Timestamp dateCreate) { this.dateCreate = dateCreate; }
    }
}
