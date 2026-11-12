package dto;

/** Một dòng sản phẩm trong đơn hàng (hiển thị ở popup chi tiết đơn của seller). */
public class SellerOrderItemDTO {
    private int productId;
    private String name;
    private String imageRaw;   // đường dẫn lưu trong productimages.url
    private String imageUrl;   // URL đầy đủ để hiển thị (servlet tính)
    private int quantity;
    private long price;        // đơn giá lúc đặt (orderitems.price)

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getImageRaw() { return imageRaw; }
    public void setImageRaw(String imageRaw) { this.imageRaw = imageRaw; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public long getPrice() { return price; }
    public void setPrice(long price) { this.price = price; }

    public long getLineTotal() { return price * quantity; }
}