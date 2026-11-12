package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/** Creates a database order and pending VNPay deposit for a saved buyer design. */
public class BuyerCustomDesignOrderDAO {
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("25300");

    public Map<String, Object> createDepositOrder(int userId, String designKey, String receiver,
                                                   String phone, String address, String note, int quantity,
                                                   String paymentReference) throws SQLException {
        if (quantity < 1 || quantity > 99) throw new IllegalArgumentException("Số lượng thiết kế phải từ 1 đến 99.");
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(true);
            new CheckoutDAO().ensureShippingColumns(c);
            c.setAutoCommit(false);
            try {
                String config;
                BigDecimal designPrice;
                Integer savedShopId;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT config,total_price,shop_id FROM buyer_saved_designs WHERE id_user=? AND design_key=? FOR UPDATE")) {
                    ps.setInt(1, userId); ps.setString(2, designKey);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy thiết kế trong tài khoản. Hãy lưu lại thiết kế trước khi thanh toán.");
                        config = rs.getString("config");
                        designPrice = rs.getBigDecimal("total_price");
                        int id = rs.getInt("shop_id");
                        savedShopId = rs.wasNull() ? null : id;
                    }
                }
                if (designPrice == null || designPrice.signum() <= 0) throw new IllegalArgumentException("Thiết kế không có giá hợp lệ.");

                int templateId;
                int shopId;
                String templateSql = "SELECT dt.id,p.id_shop FROM designtemplates dt JOIN products p ON p.id=dt.id_product " +
                        "WHERE p.status='active' AND p.id_shop=COALESCE(?,p.id_shop) ORDER BY (p.id_shop=?) DESC,dt.id LIMIT 1";
                try (PreparedStatement ps = c.prepareStatement(templateSql)) {
                    if (savedShopId == null) { ps.setNull(1, java.sql.Types.INTEGER); ps.setNull(2, java.sql.Types.INTEGER); }
                    else { ps.setInt(1, savedShopId); ps.setInt(2, savedShopId); }
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy gian hàng/mẫu phù hợp để tạo đơn thiết kế.");
                        templateId = rs.getInt("id"); shopId = rs.getInt("id_shop");
                    }
                }

                int customDesignId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customdesigns(id_user,id_template,config,total_price) VALUES(?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId); ps.setInt(2, templateId); ps.setString(3, config); ps.setBigDecimal(4, designPrice);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được bản thiết kế cho đơn hàng.");
                        customDesignId = keys.getInt(1);
                    }
                }

                BigDecimal subtotal = designPrice.multiply(BigDecimal.valueOf(quantity));
                BigDecimal total = subtotal.add(SHIPPING_FEE);
                BigDecimal deposit = total.divide(new BigDecimal("2"), 0, RoundingMode.HALF_UP);
                int paymentMethodId;
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM paymentmethods WHERE name='VNPay' LIMIT 1");
                     ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new SQLException("Chưa cấu hình phương thức VNPay.");
                    paymentMethodId = rs.getInt(1);
                }

                int orderId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orders(id_user,id_payment_method,id_shop,amount,shipping_fee,shipping_address," +
                                "shipping_receiver,shipping_phone,shipping_note,status) VALUES(?,?,?,?,?,?,?,?,?,'pending')",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId); ps.setInt(2, paymentMethodId); ps.setInt(3, shopId);
                    ps.setBigDecimal(4, total); ps.setBigDecimal(5, SHIPPING_FEE);
                    ps.setString(6, address); ps.setString(7, receiver); ps.setString(8, phone);
                    if (note.isBlank()) ps.setNull(9, java.sql.Types.VARCHAR); else ps.setString(9, note);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được đơn hàng.");
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO orderitems(id_order,id_custom_design,quantity,price,fee_amount) VALUES(?,?,?,?,0)")) {
                    ps.setInt(1, orderId); ps.setInt(2, customDesignId); ps.setInt(3, quantity);
                    ps.setBigDecimal(4, designPrice); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions(id_order,gateway,txn_ref,amount,status) VALUES(?,'vnpay_deposit',?,?,'pending')")) {
                    ps.setInt(1, orderId); ps.setString(2, paymentReference + "-" + orderId); ps.setBigDecimal(3, deposit); ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions(id_order,gateway,txn_ref,amount,status) VALUES(?,'cod',?,?, 'pending')")) {
                    ps.setInt(1, orderId); ps.setString(2, "COD-BALANCE-" + orderId);
                    ps.setBigDecimal(3, total.subtract(deposit)); ps.executeUpdate();
                }
                c.commit();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("orderId", orderId); result.put("paymentReference", paymentReference);
                result.put("total", total); result.put("deposit", deposit); result.put("remaining", total.subtract(deposit));
                return result;
            } catch (SQLException | RuntimeException e) {
                c.rollback(); throw e;
            } finally { c.setAutoCommit(true); }
        }
    }
}
