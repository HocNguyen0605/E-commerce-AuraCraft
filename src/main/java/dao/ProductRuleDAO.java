package dao;

import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductRuleDAO {

    public List<Integer> findRecommendedIds(int productId, int limit) throws Exception {
        List<Integer> ids = new ArrayList<>();

        // 1) Gợi ý từ luật kết hợp
        String sqlRules = """
            SELECT r.id_consequent
            FROM product_rules r
            JOIN products p ON p.id = r.id_consequent
            WHERE r.id_antecedent = ?
              AND p.status = 'active' AND p.stock > 0
            ORDER BY r.lift DESC, r.confidence DESC
            LIMIT ?
            """;
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sqlRules)) {
            ps.setInt(1, productId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }

        // 2) Bù bằng sản phẩm bán chạy cùng danh mục nếu chưa đủ
        if (ids.size() < limit) {
            String sqlFallback = """
                SELECT p.id FROM products p
                WHERE p.id_category = (SELECT id_category FROM products WHERE id = ?)
                  AND p.id <> ? AND p.status = 'active' AND p.stock > 0
                ORDER BY p.sold_count DESC, p.id
                LIMIT ?
                """;
            try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sqlFallback)) {
                ps.setInt(1, productId);
                ps.setInt(2, productId);
                ps.setInt(3, limit + ids.size());          // lấy dư rồi lọc trùng
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next() && ids.size() < limit) {
                        int id = rs.getInt(1);
                        if (!ids.contains(id)) ids.add(id);
                    }
                }
            }
        }
        return ids;
    }
}