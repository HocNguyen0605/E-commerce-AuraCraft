package dto;

import java.math.BigDecimal;
import java.util.List;

/** Typed response returned after checkout successfully creates order(s). */
public class CheckoutResponseDTO {
    private int orderId;
    private List<Integer> orderIds;
    private BigDecimal subtotal;
    private BigDecimal shipping;
    private BigDecimal total;
    private int itemCount;
    private String paymentMethod;

    public int getOrderId() { return orderId; }
    public void setOrderId(int orderId) { this.orderId = orderId; }

    public List<Integer> getOrderIds() { return orderIds; }
    public void setOrderIds(List<Integer> orderIds) { this.orderIds = orderIds; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getShipping() { return shipping; }
    public void setShipping(BigDecimal shipping) { this.shipping = shipping; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public int getItemCount() { return itemCount; }
    public void setItemCount(int itemCount) { this.itemCount = itemCount; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}
