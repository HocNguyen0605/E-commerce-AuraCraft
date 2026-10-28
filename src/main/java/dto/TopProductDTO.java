package dto;

/** Một dòng trong bảng "Sản phẩm bán chạy". */
public class TopProductDTO {
    private int id;
    private String name;
    private long quantity;     // tổng số lượng đã bán trong khoảng lọc
    private int orderCount;    // số đơn có chứa sản phẩm
    private long revenue;      // doanh thu (giá × số lượng)

    public TopProductDTO() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public long getQuantity() { return quantity; }
    public void setQuantity(long quantity) { this.quantity = quantity; }

    public int getOrderCount() { return orderCount; }
    public void setOrderCount(int orderCount) { this.orderCount = orderCount; }

    public long getRevenue() { return revenue; }
    public void setRevenue(long revenue) { this.revenue = revenue; }
}