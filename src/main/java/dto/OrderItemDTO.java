package dto;

import java.math.BigDecimal;

public class OrderItemDTO {
    private String name, image, type;   // type: product | custom | quote
    private int quantity;
    private BigDecimal price;

    public OrderItemDTO(String name, String image, String type, int quantity, BigDecimal price) {
        this.name = name; this.image = image; this.type = type;
        this.quantity = quantity; this.price = price;
    }
    public String getName() { return name; }
    public String getImage() { return image; }
    public String getType() { return type; }
    public int getQuantity() { return quantity; }
    public BigDecimal getPrice() { return price; }
}