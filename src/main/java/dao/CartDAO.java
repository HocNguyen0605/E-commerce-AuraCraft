package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CartDAO {
    public List<Map<String, Object>> findUserCart(int userId) throws SQLException {
        String sql = "SELECT p.id,p.name,p.price,p.stock,c.quantity,cat.name category_name,s.name shop_name, " +
                "CASE WHEN pi.url LIKE 'http%' THEN pi.url ELSE " +
                "CASE p.id_category WHEN 2 THEN '/assets/img/products/necklace_02.jpg' " +
                "WHEN 3 THEN '/assets/img/products/phoneStrap_01.jpg' ELSE '/assets/img/products/bracelet_01.jpg' END END image " +
                "FROM carts c JOIN products p ON p.id=c.id_product JOIN categories cat ON cat.id=p.id_category " +
                "JOIN shops s ON s.id=p.id_shop LEFT JOIN productimages pi ON pi.id=(SELECT MIN(pi2.id) FROM productimages pi2 WHERE pi2.id_product=p.id) " +
                "WHERE c.id_user=? AND p.status='active' AND s.status='active' ORDER BY c.id DESC";
        List<Map<String, Object>> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id")); item.put("name", rs.getString("name"));
                    item.put("price", rs.getBigDecimal("price")); item.put("stock", rs.getInt("stock"));
                    item.put("quantity", rs.getInt("quantity")); item.put("categoryName", rs.getString("category_name"));
                    item.put("shopName", rs.getString("shop_name")); item.put("image", rs.getString("image"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public int add(int userId, int productId, int quantity) throws SQLException {
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                int stock;
                try (PreparedStatement product = connection.prepareStatement(
                        "SELECT p.stock FROM products p JOIN shops s ON s.id=p.id_shop WHERE p.id=? AND p.status='active' AND s.status='active' FOR UPDATE")) {
                    product.setInt(1, productId);
                    try (ResultSet rs = product.executeQuery()) {
                        if (!rs.next()) throw new IllegalArgumentException("Sản phẩm không còn được bán.");
                        stock = rs.getInt("stock");
                    }
                }
                if (stock < 1 || quantity < 1) throw new IllegalArgumentException("Sản phẩm hiện đã hết hàng.");
                int current = 0;
                try (PreparedStatement existing = connection.prepareStatement(
                        "SELECT quantity FROM carts WHERE id_user=? AND id_product=? FOR UPDATE")) {
                    existing.setInt(1, userId); existing.setInt(2, productId);
                    try (ResultSet rs = existing.executeQuery()) { if (rs.next()) current = rs.getInt(1); }
                }
                int newQuantity = Math.min(stock, current + quantity);
                if (newQuantity == current) throw new IllegalArgumentException("Sản phẩm trong giỏ đã đạt số lượng tồn kho.");
                if (current == 0) {
                    try (PreparedStatement insert = connection.prepareStatement(
                            "INSERT INTO carts(id_user,id_product,id_custom_design,quantity) VALUES(?,?,NULL,?)")) {
                        insert.setInt(1, userId); insert.setInt(2, productId); insert.setInt(3, newQuantity); insert.executeUpdate();
                    }
                } else {
                    try (PreparedStatement update = connection.prepareStatement(
                            "UPDATE carts SET quantity=? WHERE id_user=? AND id_product=?")) {
                        update.setInt(1, newQuantity); update.setInt(2, userId); update.setInt(3, productId); update.executeUpdate();
                    }
                }
                connection.commit();
                return newQuantity - current;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally { connection.setAutoCommit(true); }
        }
    }

    public void updateQuantity(int userId, int productId, int quantity) throws SQLException {
        if (quantity < 1) throw new IllegalArgumentException("Số lượng phải lớn hơn 0.");
        String sql = "UPDATE carts c JOIN products p ON p.id=c.id_product SET c.quantity=? " +
                "WHERE c.id_user=? AND c.id_product=? AND p.status='active' AND ?<=p.stock";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, quantity); statement.setInt(2, userId); statement.setInt(3, productId); statement.setInt(4, quantity);
            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Số lượng vượt quá tồn kho hoặc sản phẩm không tồn tại.");
        }
    }

    public void remove(int userId, int productId) throws SQLException {
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM carts WHERE id_user=? AND id_product=?")) {
            statement.setInt(1, userId); statement.setInt(2, productId); statement.executeUpdate();
        }
    }

    public void removeMany(int userId, List<Integer> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) return;
        String placeholders = String.join(",", java.util.Collections.nCopies(productIds.size(), "?"));
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(
                "DELETE FROM carts WHERE id_user=? AND id_product IN (" + placeholders + ")")) {
            statement.setInt(1, userId);
            for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
            statement.executeUpdate();
        }
    }
}
