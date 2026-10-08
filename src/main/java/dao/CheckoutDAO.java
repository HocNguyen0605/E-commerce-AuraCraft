package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Creates an order from the signed-in buyer's selected database cart items. */
public class CheckoutDAO {
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("25300");

    public Map<String, String> findDefaultAddress(int userId) throws SQLException {
        String sql = "SELECT receiver,phone,address FROM addresses WHERE id_user=? ORDER BY is_default DESC,id DESC LIMIT 1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return Map.of();
                Map<String, String> address = new LinkedHashMap<>();
                address.put("receiver", rs.getString("receiver"));
                address.put("phone", rs.getString("phone"));
                address.put("address", rs.getString("address"));
                return address;
            }
        }
    }

    public List<Map<String, Object>> findSelectedCartItems(int userId, List<Integer> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(productIds.size(), "?"));
        String sql = "SELECT p.id,p.name,p.price,p.stock,c.quantity,p.id_shop,p.id_category,cat.name category_name,s.name shop_name, " +
                "pi.url image FROM carts c JOIN products p ON p.id=c.id_product " +
                "JOIN categories cat ON cat.id=p.id_category JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN productimages pi ON pi.id=(SELECT MIN(pi2.id) FROM productimages pi2 WHERE pi2.id_product=p.id) " +
                "WHERE c.id_user=? AND p.id IN (" + placeholders + ") AND p.status='active' AND s.status='active' " +
                "ORDER BY p.id_shop,p.id";
        List<Map<String, Object>> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id")); item.put("name", rs.getString("name"));
                    BigDecimal price = rs.getBigDecimal("price");
                    int quantity = rs.getInt("quantity");
                    item.put("price", price); item.put("lineTotal", price.multiply(BigDecimal.valueOf(quantity)));
                    item.put("stock", rs.getInt("stock"));
                    item.put("quantity", quantity); item.put("shopId", rs.getInt("id_shop"));
                    item.put("categoryId", rs.getInt("id_category"));
                    item.put("categoryName", rs.getString("category_name")); item.put("shopName", rs.getString("shop_name"));
                    item.put("image", rs.getString("image"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public Map<String, Object> placeOrder(int userId, List<Integer> productIds,
                                          String receiver, String phone, String address,
                                          String note, String paymentMethod) throws SQLException {
        if (productIds == null || productIds.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần đặt.");
        String placeholders = String.join(",", java.util.Collections.nCopies(productIds.size(), "?"));
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement findAddress = connection.prepareStatement(
                        "SELECT id FROM addresses WHERE id_user=? AND receiver=? AND phone=? AND address=? LIMIT 1")) {
                    findAddress.setInt(1, userId); findAddress.setString(2, receiver);
                    findAddress.setString(3, phone); findAddress.setString(4, address);
                    try (ResultSet rs = findAddress.executeQuery()) {
                        int addressId = rs.next() ? rs.getInt("id") : 0;
                        try (PreparedStatement clearDefault = connection.prepareStatement(
                                "UPDATE addresses SET is_default=0 WHERE id_user=? AND is_default=1")) {
                            clearDefault.setInt(1, userId); clearDefault.executeUpdate();
                        }
                        if (addressId > 0) {
                            try (PreparedStatement setDefault = connection.prepareStatement(
                                    "UPDATE addresses SET is_default=1 WHERE id=? AND id_user=?")) {
                                setDefault.setInt(1, addressId); setDefault.setInt(2, userId); setDefault.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement saveAddress = connection.prepareStatement(
                                    "INSERT INTO addresses(id_user,receiver,phone,address,is_default) VALUES(?,?,?,?,1)")) {
                                saveAddress.setInt(1, userId); saveAddress.setString(2, receiver);
                                saveAddress.setString(3, phone); saveAddress.setString(4, address);
                                saveAddress.executeUpdate();
                            }
                        }
                    }
                }
                String query = "SELECT p.id,p.name,p.price,p.stock,c.quantity,p.id_shop " +
                        "FROM carts c JOIN products p ON p.id=c.id_product " +
                        "JOIN shops s ON s.id=p.id_shop " +
                        "WHERE c.id_user=? AND p.id IN (" + placeholders + ") " +
                        "AND p.status='active' AND s.status='active' ORDER BY p.id_shop,p.id FOR UPDATE";
                List<Map<String, Object>> items = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userId);
                    for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            int quantity = rs.getInt("quantity");
                            int stock = rs.getInt("stock");
                            if (quantity < 1 || quantity > stock) {
                                throw new IllegalArgumentException("Số lượng sản phẩm " + rs.getString("name") + " đã thay đổi. Vui lòng kiểm tra lại giỏ hàng.");
                            }
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("id", rs.getInt("id")); item.put("shopId", rs.getInt("id_shop"));
                            item.put("name", rs.getString("name")); item.put("price", rs.getBigDecimal("price"));
                            item.put("quantity", quantity);
                            items.add(item);
                        }
                    }
                }
                if (items.size() != productIds.size()) {
                    throw new IllegalArgumentException("Có sản phẩm không còn trong giỏ hoặc đã ngừng bán. Vui lòng tải lại giỏ hàng.");
                }

                int paymentMethodId = findPaymentMethod(connection, paymentMethod);
                Map<Integer, List<Map<String, Object>>> byShop = new LinkedHashMap<>();
                for (Map<String, Object> item : items) {
                    byShop.computeIfAbsent((Integer) item.get("shopId"), ignored -> new ArrayList<>()).add(item);
                }
                List<Integer> orderIds = new ArrayList<>();
                BigDecimal subtotal = BigDecimal.ZERO;
                for (Map.Entry<Integer, List<Map<String, Object>>> entry : byShop.entrySet()) {
                    BigDecimal shopSubtotal = BigDecimal.ZERO;
                    for (Map<String, Object> item : entry.getValue()) {
                        BigDecimal price = (BigDecimal) item.get("price");
                        int quantity = (Integer) item.get("quantity");
                        shopSubtotal = shopSubtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
                    }
                    BigDecimal orderTotal = shopSubtotal.add(SHIPPING_FEE);
                    String shippingAddress = "Người nhận: " + receiver + " | SĐT: " + phone + " | Địa chỉ: " + address
                            + (note.isBlank() ? "" : " | Ghi chú: " + note);
                    int orderId;
                    String insertOrder = "INSERT INTO orders(id_user,id_payment_method,id_shop,amount,shipping_fee,shipping_address,status) " +
                            "VALUES(?,?,?,?,?,?,'pending')";
                    try (PreparedStatement statement = connection.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setInt(1, userId); statement.setInt(2, paymentMethodId); statement.setInt(3, entry.getKey());
                        statement.setBigDecimal(4, orderTotal); statement.setBigDecimal(5, SHIPPING_FEE);
                        statement.setString(6, shippingAddress); statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không nhận được mã đơn hàng.");
                            orderId = keys.getInt(1);
                        }
                    }
                    orderIds.add(orderId);
                    for (Map<String, Object> item : entry.getValue()) {
                        int productId = (Integer) item.get("id");
                        int quantity = (Integer) item.get("quantity");
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO orderitems(id_order,id_product,quantity,price,fee_amount) VALUES(?,?,?, ?,0)")) {
                            statement.setInt(1, orderId); statement.setInt(2, productId);
                            statement.setInt(3, quantity); statement.setBigDecimal(4, (BigDecimal) item.get("price"));
                            statement.executeUpdate();
                        }
                        try (PreparedStatement statement = connection.prepareStatement(
                                "UPDATE products SET stock=stock-? WHERE id=? AND stock>=?")) {
                            statement.setInt(1, quantity); statement.setInt(2, productId); statement.setInt(3, quantity);
                            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Tồn kho vừa thay đổi. Vui lòng thử đặt hàng lại.");
                        }
                    }
                    subtotal = subtotal.add(shopSubtotal);
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM carts WHERE id_user=? AND id_product IN (" + placeholders + ")")) {
                    statement.setInt(1, userId);
                    for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
                    statement.executeUpdate();
                }
                connection.commit();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("orderIds", orderIds); result.put("orderId", orderIds.get(0));
                result.put("subtotal", subtotal); result.put("shipping", SHIPPING_FEE.multiply(BigDecimal.valueOf(orderIds.size())));
                result.put("total", subtotal.add(SHIPPING_FEE.multiply(BigDecimal.valueOf(orderIds.size()))));
                result.put("itemCount", items.stream().mapToInt(item -> (Integer) item.get("quantity")).sum());
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private int findPaymentMethod(Connection connection, String method) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM paymentmethods WHERE name=?")) {
            statement.setString(1, method);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
                return rs.getInt(1);
            }
        }
    }
}
