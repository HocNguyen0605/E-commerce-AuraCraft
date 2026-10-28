package dao;

import dto.Category;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    /** Tất cả danh mục kèm số sản phẩm (chưa xóa) của shop, dùng cho ô lọc "Vòng tay (12)". */
    public List<Category> findAllWithCount(int shopId) throws SQLException {
        String sql = "SELECT c.id, c.name, COUNT(p.id) AS cnt FROM categories c " +
                     "LEFT JOIN products p ON p.id_category = c.id AND p.id_shop = ? AND p.status <> 'deleted' " +
                     "GROUP BY c.id, c.name ORDER BY c.id";
        List<Category> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Category cat = new Category();
                    cat.setId(rs.getInt("id"));
                    cat.setName(rs.getString("name"));
                    cat.setProductCount(rs.getInt("cnt"));
                    list.add(cat);
                }
            }
        }
        return list;
    }
}
