package dto;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CustomRequestDTO {
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