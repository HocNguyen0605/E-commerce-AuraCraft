package dao;

import dto.ShopDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.*;

/**
 * Xu ly truy van du lieu gian hang va tim kiem mem theo tu khoa.
 */
public class ShopDAO {

    /**
     * Loai bo dau tieng Viet de tim kiem khong phan biet co dau hay khong dau.
     */
    public static String removeAccents(String text) {
        if (text == null) return "";
        String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").replace('đ', 'd').replace('Đ', 'D').trim().toLowerCase();
    }

    /**
     * Tim kiem mem gian hang phu hop voi tu khoa (ten shop, mo ta).
     * Ho tro tim kiem khong dau, bo tu thua nhu 'shop', 'tiem', 'cua hang'.
     */
    public Map<String, Object> findMatchingShop(String rawKeyword) {
        if (rawKeyword == null || rawKeyword.trim().isEmpty()) {
            return null;
        }

        String rawClean = rawKeyword.trim().toLowerCase();
        String noAccents = removeAccents(rawClean);

        // Loc bo cac tu thua thuong gap
        String cleaned = noAccents.replaceAll("^(shop|tiem|cua hang|gian hang)\\s+", "").trim();
        if (cleaned.isEmpty()) {
            cleaned = noAccents;
        }

        String sql = "SELECT s.id, s.id_owner, s.name, s.address, s.phone, s.email, " +
                "       s.description, s.avatar, s.rating, " +
                "       (SELECT COUNT(*) FROM products p WHERE p.id_shop = s.id AND p.status = 'active') AS product_count, " +
                "       (SELECT COALESCE(SUM(p.sold_count), 0) FROM products p WHERE p.id_shop = s.id AND p.status = 'active') AS total_sold " +
                "FROM shops s " +
                "WHERE s.status = 'active' " +
                "  AND (LOWER(s.name) LIKE ? OR LOWER(s.description) LIKE ? " +
                "       OR LOWER(s.name) LIKE ? OR LOWER(s.description) LIKE ?) " +
                "ORDER BY s.rating DESC, total_sold DESC LIMIT 1";

        String p1 = "%" + rawClean + "%";
        String p2 = "%" + cleaned + "%";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p1);
            ps.setString(2, p1);
            ps.setString(3, p2);
            ps.setString(4, p2);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShopRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi tim kiem shop: " + e.getMessage());
        }

        return null;
    }

    /**
     * Lay thong tin chi tiet cua gian hang theo id.
     */
    public Map<String, Object> getShopById(int shopId) {
        String sql = "SELECT s.id, s.id_owner, s.name, s.address, s.phone, s.email, " +
                "       s.description, s.avatar, s.rating, " +
                "       (SELECT COUNT(*) FROM products p WHERE p.id_shop = s.id AND p.status = 'active') AS product_count, " +
                "       (SELECT COALESCE(SUM(p.sold_count), 0) FROM products p WHERE p.id_shop = s.id AND p.status = 'active') AS total_sold " +
                "FROM shops s " +
                "WHERE s.id = ? AND s.status = 'active'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapShopRow(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi lay thong tin shop: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lay danh sach san pham noi bat cua shop de hien thi card xem nhanh.
     */
    public List<Map<String, Object>> findTopProductsByShop(int shopId, int limit) {
        String sql = "SELECT p.id, p.name, p.price, p.sold_count, " +
                "       (SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image " +
                "FROM products p " +
                "WHERE p.id_shop = ? AND p.status = 'active' " +
                "ORDER BY p.sold_count DESC, p.id DESC LIMIT ?";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("name", rs.getString("name"));
                    item.put("price", rs.getBigDecimal("price"));
                    item.put("soldCount", rs.getInt("sold_count"));
                    item.put("image", rs.getString("image"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi lay san pham top cua shop: " + e.getMessage());
        }
        return list;
    }

    /**
     * Lay toan bo san pham cua shop cho trang shop-profile.
     */
    public List<Map<String, Object>> getProductsByShop(int shopId) {
        String sql = "SELECT p.id, p.name, p.price, p.sold_count, p.stock, " +
                "       COALESCE(rv.avg_rating, 0) AS rating, " +
                "       (SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image " +
                "FROM products p " +
                "LEFT JOIN (SELECT id_product, AVG(rating) AS avg_rating FROM reviews GROUP BY id_product) rv ON rv.id_product = p.id " +
                "WHERE p.id_shop = ? AND p.status = 'active' " +
                "ORDER BY p.id DESC";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id"));
                    item.put("name", rs.getString("name"));
                    item.put("price", rs.getBigDecimal("price"));
                    item.put("soldCount", rs.getInt("sold_count"));
                    item.put("stock", rs.getInt("stock"));
                    item.put("rating", rs.getDouble("rating"));
                    item.put("image", rs.getString("image"));
                    list.add(item);
                }
            }
        } catch (SQLException e) {
            System.err.println("Loi lay danh sach san pham cua shop: " + e.getMessage());
        }
        return list;
    }

    private Map<String, Object> mapShopRow(ResultSet rs) throws SQLException {
        Map<String, Object> shop = new LinkedHashMap<>();
        shop.put("id", rs.getInt("id"));
        shop.put("ownerId", rs.getInt("id_owner"));
        shop.put("name", rs.getString("name"));
        shop.put("address", rs.getString("address"));
        shop.put("phone", rs.getString("phone"));
        shop.put("email", rs.getString("email"));
        shop.put("description", rs.getString("description"));
        shop.put("avatar", rs.getString("avatar"));
        shop.put("rating", rs.getBigDecimal("rating"));
        shop.put("productCount", rs.getInt("product_count"));
        shop.put("totalSold", rs.getInt("total_sold"));
        return shop;
    }

    public ShopDTO findActiveById(int shopId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id = ? AND status = 'active'";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ShopDTO s = new ShopDTO();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("id_owner"));
                s.setName(rs.getString("name"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setDescription(rs.getString("description"));
                s.setAvatar(rs.getString("avatar"));
                s.setStatus(rs.getString("status"));
                s.setRating(rs.getDouble("rating"));
                s.setCommissionRate(rs.getDouble("commission_rate"));
                return s;
            }
        }
    }

    /** Shop của chủ shop (users.id), chỉ lấy shop đang hoạt động. Null nếu không có. */
    public ShopDTO findActiveByOwner(int ownerId) throws SQLException {
        String sql = "SELECT * FROM shops WHERE id_owner = ? AND status = 'active' ORDER BY id LIMIT 1";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, ownerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ShopDTO s = new ShopDTO();
                s.setId(rs.getInt("id"));
                s.setOwnerId(rs.getInt("id_owner"));
                s.setName(rs.getString("name"));
                s.setAddress(rs.getString("address"));
                s.setPhone(rs.getString("phone"));
                s.setEmail(rs.getString("email"));
                s.setDescription(rs.getString("description"));
                s.setAvatar(rs.getString("avatar"));
                s.setStatus(rs.getString("status"));
                s.setRating(rs.getDouble("rating"));
                s.setCommissionRate(rs.getDouble("commission_rate"));
                return s;
            }
        }
    }

}
