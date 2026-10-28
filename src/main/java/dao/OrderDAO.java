package dao;

import dto.OrderDTO;
import dto.OrderItemDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO {

    // GIẢ ĐỊNH về schema (sửa nếu tên cột thực tế khác):
    //   shops(id, name)
    //   products(id, name)
    //   productimages(id_product, url)
    private static final String SQL_ORDERS =
            "SELECT o.id, o.status, o.amount, o.shipping_fee, o.discount, o.date_create, s.name AS shop_name, " +
                    "o.shipping_receiver, o.shipping_phone, o.shipping_address, o.shipping_note, pm.name AS payment_method " +
                    "FROM orders o JOIN shops s ON s.id = o.id_shop " +
                    "JOIN paymentmethods pm ON pm.id = o.id_payment_method " +
                    "WHERE o.id_user = ? ORDER BY o.date_create DESC";

    private static final String SQL_ITEMS =
            "SELECT oi.quantity, oi.price, oi.id_product, oi.id_custom_design, oi.id_quote, p.name AS product_name, " +
                    "  (SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image " +
                    "FROM orderitems oi LEFT JOIN products p ON p.id = oi.id_product " +
                    "WHERE oi.id_order = ?";

    public List<OrderDTO> findByUser(int userId) throws SQLException {
        List<OrderDTO> orders = new ArrayList<>();
        try (Connection con = DBConnection.getConnection()) {
            try (PreparedStatement ps = con.prepareStatement(SQL_ORDERS)) {
                ps.setInt(1, userId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        orders.add(new OrderDTO(rs.getInt("id"), rs.getString("shop_name"),
                                rs.getString("status"), rs.getBigDecimal("amount"),
                                rs.getBigDecimal("shipping_fee"), rs.getBigDecimal("discount"),
                                rs.getTimestamp("date_create"), rs.getString("shipping_receiver"),
                                rs.getString("shipping_phone"), rs.getString("shipping_address"),
                                rs.getString("shipping_note"), rs.getString("payment_method")));
                    }
                }
            }
            try (PreparedStatement ps = con.prepareStatement(SQL_ITEMS)) {
                for (OrderDTO o : orders) {
                    ps.setInt(1, o.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String type, name;
                            if (rs.getObject("id_quote") != null) {
                                type = "quote"; name = "Sản phẩm theo báo giá";
                            } else if (rs.getObject("id_custom_design") != null) {
                                type = "custom"; name = "Sản phẩm thiết kế riêng";
                            } else {
                                type = "product"; name = rs.getString("product_name");
                            }
                            o.getItems().add(new OrderItemDTO(name, rs.getString("image"),
                                    type, rs.getInt("quantity"), rs.getBigDecimal("price")));
                        }
                    }
                }
            }
        }
        return orders;
    }
}
