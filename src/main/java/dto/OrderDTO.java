package dto;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO {
    private int id;
    private String shopName, status, receiver, phone, address, note, paymentMethod;
    private BigDecimal amount, shippingFee, discount;
    private Timestamp dateCreate;
    private List<OrderItemDTO> items = new ArrayList<>();

    public OrderDTO(int id, String shopName, String status, BigDecimal amount,
                    BigDecimal shippingFee, BigDecimal discount, Timestamp dateCreate) {
        this.id = id; this.shopName = shopName; this.status = status; this.amount = amount;
        this.shippingFee = shippingFee; this.discount = discount; this.dateCreate = dateCreate;
    }

    public OrderDTO(int id, String shopName, String status, BigDecimal amount,
                    BigDecimal shippingFee, BigDecimal discount, Timestamp dateCreate,
                    String receiver, String phone, String address, String note, String paymentMethod) {
        this(id, shopName, status, amount, shippingFee, discount, dateCreate);
        this.receiver = receiver; this.phone = phone; this.address = address;
        this.note = note; this.paymentMethod = paymentMethod;
    }

    public String getFilterKey() {
        if (status == null) return "";
        switch (status) {
            case "pending":
            case "paid":       return "pickup";      // chờ xử lý / đã thanh toán, chờ lấy hàng
            case "processing": return "processing";
            case "shipping":   return "shipping";
            case "completed":  return "done";
            case "cancelled":  return "cancel";
            case "refunded":   return "return";
            default:           return status;
        }
    }

    /** Nhãn tiếng Việt hiển thị trên thẻ đơn. */
    public String getStatusLabel() {
        if (status == null) return "";
        switch (status) {
            case "pending":    return "Chờ thanh toán";
            case "paid":       return "Đã thanh toán, chờ lấy hàng";
            case "processing": return "Đang thực hiện";
            case "shipping":   return "Đang giao";
            case "completed":  return "Đã giao";
            case "cancelled":  return "Đã hủy";
            case "refunded":   return "Đã hoàn tiền";
            default:           return status;
        }
    }

    public boolean isQuote() {
        return items.stream().anyMatch(i -> "quote".equals(i.getType()));
    }

    public int getId() { return id; }
    public String getShopName() { return shopName; }
    public String getStatus() { return status; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getShippingFee() { return shippingFee; }
    public BigDecimal getDiscount() { return discount; }
    public Timestamp getDateCreate() { return dateCreate; }
    public List<OrderItemDTO> getItems() { return items; }
    public String getReceiver() { return receiver; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
}
