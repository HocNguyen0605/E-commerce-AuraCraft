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
    private String paymentReference;
    private BigDecimal voucherDiscount;
    private BigDecimal coinDiscount;
    private BigDecimal discount;
    private int coinsUsed;
    private int remainingCoins;
    private int rewardCoinsPending;

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

    public String getPaymentReference() { return paymentReference; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }

    public BigDecimal getVoucherDiscount() { return voucherDiscount; }
    public void setVoucherDiscount(BigDecimal voucherDiscount) { this.voucherDiscount = voucherDiscount; }

    public BigDecimal getCoinDiscount() { return coinDiscount; }
    public void setCoinDiscount(BigDecimal coinDiscount) { this.coinDiscount = coinDiscount; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public int getCoinsUsed() { return coinsUsed; }
    public void setCoinsUsed(int coinsUsed) { this.coinsUsed = coinsUsed; }

    public int getRemainingCoins() { return remainingCoins; }
    public void setRemainingCoins(int remainingCoins) { this.remainingCoins = remainingCoins; }

    public int getRewardCoinsPending() { return rewardCoinsPending; }
    public void setRewardCoinsPending(int rewardCoinsPending) { this.rewardCoinsPending = rewardCoinsPending; }
}
