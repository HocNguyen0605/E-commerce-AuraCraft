package dao;

import util.DBConnection;
import java.sql.*;
import java.util.*;

public class ProductDAO {
    public List<Map<String, Object>> findAll() throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.description, p.sold_count, " +
                "(SELECT url FROM ProductImages i WHERE i.id_product = p.id LIMIT 1) AS image " +
                "FROM Products p WHERE p.status = 'active'";
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id"));
                m.put("name", rs.getString("name"));
                m.put("price", rs.getBigDecimal("price"));
                m.put("description", rs.getString("description"));
                m.put("soldCount", rs.getInt("sold_count"));
                m.put("image", rs.getString("image"));
                list.add(m);
            }
        }
        return list;
    }
}