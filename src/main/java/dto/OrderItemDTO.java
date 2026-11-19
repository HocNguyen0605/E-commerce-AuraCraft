package dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private String name, image, type;   // type: product | custom | quote
    private int quantity;
    private int productId;
    private BigDecimal price;

    public OrderItemDTO(String name, String image, String type, int quantity, BigDecimal price) {
        this.name = name; this.image = image; this.type = type;
        this.quantity = quantity; this.price = price;
    }
    public OrderItemDTO(int productId, String name, String image, String type, int quantity, BigDecimal price) {
        this(name, image, type, quantity, price);
        this.productId = productId;
    }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public int getProductId() { return productId; }
    public BigDecimal getPrice() { return price; }
}
