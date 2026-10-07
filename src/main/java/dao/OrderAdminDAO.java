package dao;

import dto.OrderAdminDTO;
import dto.OrderAdminResponseDTO;
import dto.OrderAdminResponseDTO.ShopFilterDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class OrderAdminDAO {

    /**
     * Lấy toàn bộ dữ liệu đơn hàng cho trang quản lý đơn hàng admin.
     * JOIN orders với users (buyer), shops, users (seller/owner), paymentmethods,
     * và tổng hợp orderitems để lấy danh sách sản phẩm + phí sàn.
     */
    public OrderAdminResponseDTO getOrdersData() {
        OrderAdminResponseDTO response = new OrderAdminResponseDTO();
        List<OrderAdminDTO> orders = new ArrayList<>();

        int totalOrders = 0;
        int pendingOrders = 0;
        int processingOrders = 0;
        int shippingOrders = 0;
        int completedOrders = 0;
        int cancelledOrders = 0;
        double totalRevenue = 0;
        double totalFees = 0;

        // Query lấy thông tin đơn hàng cùng tên sản phẩm, phí sàn từ orderitems
        String query =
            "SELECT o.id AS orderId, o.amount, o.shipping_fee, o.discount, " +
            "       o.status, o.date_create, o.date_paid, o.shipping_address, " +
            "       buyer.name AS buyerName, buyer.email AS buyerEmail, " +
            "       seller.name AS sellerName, s.name AS shopName, " +
            "       pm.name AS paymentMethod, " +
            "       item_sub.productSummary, item_sub.itemCount, item_sub.totalFee, item_sub.isCustom " +
            "FROM orders o " +
            "JOIN users buyer ON o.id_user = buyer.id " +
            "JOIN shops s ON o.id_shop = s.id " +
            "JOIN users seller ON s.id_owner = seller.id " +
            "JOIN paymentmethods pm ON o.id_payment_method = pm.id " +
            "LEFT JOIN ( " +
            "    SELECT oi.id_order, " +
            "           GROUP_CONCAT( " +
            "               CASE " +
            "                   WHEN oi.id_product IS NOT NULL THEN p.name " +
            "                   WHEN oi.id_custom_design IS NOT NULL THEN CONCAT('Custom: ', dt.name) " +
            "               END " +
            "               SEPARATOR ', ' " +
            "           ) AS productSummary, " +
            "           SUM(oi.quantity) AS itemCount, " +
            "           SUM(oi.fee_amount) AS totalFee, " +
            "           MAX(CASE WHEN oi.id_custom_design IS NOT NULL THEN 1 ELSE 0 END) AS isCustom " +
            "    FROM orderitems oi " +
            "    LEFT JOIN products p ON oi.id_product = p.id " +
            "    LEFT JOIN customdesigns cd ON oi.id_custom_design = cd.id " +
            "    LEFT JOIN designtemplates dt ON cd.id_template = dt.id " +
            "    GROUP BY oi.id_order " +
            ") item_sub ON o.id = item_sub.id_order " +
            "ORDER BY o.date_create DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                OrderAdminDTO order = new OrderAdminDTO();
                order.setOrderId(rs.getInt("orderId"));
                order.setAmount(rs.getDouble("amount"));
                order.setShippingFee(rs.getDouble("shipping_fee"));
                order.setDiscount(rs.getDouble("discount"));

                String status = rs.getString("status");
                order.setStatus(status != null ? status : "pending");
                order.setDateCreate(rs.getString("date_create"));
                order.setDatePaid(rs.getString("date_paid"));
                order.setShippingAddress(rs.getString("shipping_address"));
                order.setBuyerName(rs.getString("buyerName"));
                order.setBuyerEmail(rs.getString("buyerEmail"));
                order.setSellerName(rs.getString("sellerName"));
                order.setShopName(rs.getString("shopName"));
                order.setPaymentMethod(rs.getString("paymentMethod"));
                order.setProductSummary(rs.getString("productSummary"));
                order.setItemCount(rs.getInt("itemCount"));
                order.setTotalFee(rs.getDouble("totalFee"));
                order.setCustom(rs.getInt("isCustom") > 0);

                orders.add(order);

                // Stats
                totalOrders++;
                double amount = order.getAmount();
                totalRevenue += amount;
                totalFees += order.getTotalFee();

                switch (order.getStatus()) {
                    case "pending":
                    case "paid":
                        pendingOrders++;
                        break;
                    case "processing":
                        processingOrders++;
                        break;
                    case "shipping":
                        shippingOrders++;
                        break;
                    case "completed":
                        completedOrders++;
                        break;
                    case "cancelled":
                    case "refunded":
                        cancelledOrders++;
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("[OrderAdminDAO] Error getting orders data: " + e.getMessage());
            e.printStackTrace();
        }

        response.setOrders(orders);
        response.setTotalOrders(totalOrders);
        response.setPendingOrders(pendingOrders);
        response.setProcessingOrders(processingOrders);
        response.setShippingOrders(shippingOrders);
        response.setCompletedOrders(completedOrders);
        response.setCancelledOrders(cancelledOrders);
        response.setTotalRevenue(totalRevenue);
        response.setTotalFees(totalFees);

        // Load shops for filter dropdown
        response.setShops(getShopsForFilter());

        return response;
    }

    /**
     * Lấy danh sách shop để hiển thị trong bộ lọc dropdown.
     */
    private List<ShopFilterDTO> getShopsForFilter() {
        List<ShopFilterDTO> shops = new ArrayList<>();
        String query = "SELECT s.id, s.name AS shopName, u.name AS ownerName " +
                       "FROM shops s JOIN users u ON s.id_owner = u.id " +
                       "ORDER BY s.name";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                shops.add(new ShopFilterDTO(
                    rs.getInt("id"),
                    rs.getString("shopName"),
                    rs.getString("ownerName")
                ));
            }
        } catch (Exception e) {
            System.err.println("[OrderAdminDAO] Error loading shops: " + e.getMessage());
            e.printStackTrace();
        }
        return shops;
    }

    /**
     * Cập nhật trạng thái đơn hàng (dùng cho admin hủy/hoàn tiền).
     */
    public boolean updateOrderStatus(int orderId, String status) {
        String query = "UPDATE orders SET status = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (Exception e) {
            System.err.println("[OrderAdminDAO] Error updating order status: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
