package dao;

//import dto.Charm;

import dto.Charm;
import dto.Product;
import util.DBConnection;

import java.sql.*;
import java.util.*;

/**
 * DAO sản phẩm, gồm 2 nhóm chức năng:
 * 1) Kênh seller: quản lý sản phẩm của shop (findByShop, countByShop, findByIdAndShop, insert, update, softDelete).
 * 2) Danh mục công khai cho người mua: lọc/tìm kiếm, chi tiết, đánh giá, sản phẩm tương tự.
 */
public class ProductDAO {

    // =====================================================================
    //  PHẦN 1: KÊNH SELLER (quản lý sản phẩm của shop)
    // =====================================================================

    private static final String SELECT =
            "SELECT p.*, c.name AS category_name, s.name AS shop_name, " +
                    "(SELECT url FROM productimages i WHERE i.id_product = p.id ORDER BY i.id LIMIT 1) AS image " +
                    "FROM products p JOIN categories c ON c.id = p.id_category JOIN shops s ON s.id = p.id_shop ";

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setName(rs.getString("name"));
        p.setPrice(rs.getLong("price"));
        p.setDescription(rs.getString("description"));
        p.setShopId(rs.getInt("id_shop"));
        p.setShopName(rs.getString("shop_name"));
        p.setStock(rs.getInt("stock"));
        p.setCategoryId(rs.getInt("id_category"));
        p.setCategoryName(rs.getString("category_name"));
        p.setStatus(rs.getString("status"));
        p.setSoldCount(rs.getInt("sold_count"));
        p.setImage(rs.getString("image"));
        p.setCustomizable(rs.getBoolean("customizable"));
        p.setBaseType(rs.getString("base_type"));
        p.setBaseImage(rs.getString("base_image"));
        return p;
    }

    private static class Filter {
        final StringBuilder where = new StringBuilder("WHERE p.id_shop = ? AND p.status <> 'deleted'");
        final List<Object> params = new ArrayList<>();
        Filter(int shopId, String keyword, Integer categoryId, String status) {
            params.add(shopId);
            if (keyword != null && !keyword.trim().isEmpty()) {
                where.append(" AND p.name LIKE ?");
                params.add("%" + keyword.trim() + "%");
            }
            if (categoryId != null) {
                where.append(" AND p.id_category = ?");
                params.add(categoryId);
            }
            // thêm active xét sản phẩm ẩn
            if ("active".equals(status))      where.append(" AND p.status = 'active' AND p.stock > 0");   // Đang bán
            else if ("out".equals(status))    where.append(" AND p.status = 'active' AND p.stock <= 0");  // Hết hàng
            else if ("hidden".equals(status)) where.append(" AND p.status = 'hidden'");                   // Đã ẩn
        }
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

//    public List<Product> findByShop(int shopId, String keyword, Integer categoryId, String sort, int page, int size) throws SQLException {
//        Filter f = new Filter(shopId, keyword, categoryId);
//        String order;
//        switch (sort == null ? "" : sort) {
//            case "price_asc": order = " ORDER BY p.price ASC"; break;
//            case "price_desc": order = " ORDER BY p.price DESC"; break;
//            case "best": order = " ORDER BY p.sold_count DESC"; break;
//            default: order = " ORDER BY p.id DESC";
//        }
//        List<Product> list = new ArrayList<>();
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement(SELECT + f.where + order + " LIMIT ? OFFSET ?")) {
//            bind(ps, f.params);
//            ps.setInt(f.params.size() + 1, size);
//            ps.setInt(f.params.size() + 2, (Math.max(page, 1) - 1) * size);
//            try (ResultSet rs = ps.executeQuery()) {
//                while (rs.next()) list.add(map(rs));
//            }
//        }
//        return list;
//    }

    public List<Product> findByShop(int shopId, String keyword, Integer categoryId, String status,
                                    String sort, int page, int size) throws SQLException {
        Filter f = new Filter(shopId, keyword, categoryId, status);
        String order;
        switch (sort == null ? "" : sort) {
            case "price_asc":
                order = " ORDER BY p.price ASC";
                break;
            case "price_desc":
                order = " ORDER BY p.price DESC";
                break;
            case "best":
                order = " ORDER BY p.sold_count DESC";
                break;
            default:
                order = " ORDER BY p.id DESC";
        }
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + f.where + order + " LIMIT ? OFFSET ?")) {
            bind(ps, f.params);
            ps.setInt(f.params.size() + 1, size);
            ps.setInt(f.params.size() + 2, (Math.max(page, 1) - 1) * size);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

//    public int countByShop(int shopId, String keyword, Integer categoryId) throws SQLException {
//        Filter f = new Filter(shopId, keyword, categoryId);
//        try (Connection c = DBConnection.getConnection();
//             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products p " + f.where)) {
//            bind(ps, f.params);
//            try (ResultSet rs = ps.executeQuery()) {
//                return rs.next() ? rs.getInt(1) : 0;
//            }
//        }
//    }

    public int countByShop(int shopId, String keyword, Integer categoryId, String status) throws SQLException {
        Filter f = new Filter(shopId, keyword, categoryId, status);
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM products p " + f.where)) {
            bind(ps, f.params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Lấy sản phẩm theo id, chỉ khi thuộc shop (tránh sửa sản phẩm của shop khác).
     */
    public Product findByIdAndShop(int id, int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT + "WHERE p.id = ? AND p.id_shop = ? AND p.status <> 'deleted'")) {
            ps.setInt(1, id);
            ps.setInt(2, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Product p = map(rs);
                try (PreparedStatement pi = c.prepareStatement("SELECT url FROM productimages WHERE id_product = ? ORDER BY id")) {
                    pi.setInt(1, id);
                    try (ResultSet ri = pi.executeQuery()) {
                        while (ri.next()) p.getImages().add(ri.getString(1));
                    }
                }
                try (PreparedStatement pc = c.prepareStatement("SELECT id, name, price, image FROM product_charms WHERE id_product = ? ORDER BY id")) {
                    pc.setInt(1, id);
                    try (ResultSet rc = pc.executeQuery()) {
                        while (rc.next()) {
                            Charm ch = new Charm();
                            ch.setId(rc.getInt(1));
                            ch.setName(rc.getString(2));
                            ch.setPrice(rc.getLong(3));
                            ch.setImage(rc.getString(4));
                            p.getCharms().add(ch);
                        }
                    }
                }
                return p;
            }
        }
    }


    public int insert(Product p, List<String> imageUrls) throws SQLException {
        String sql = "INSERT INTO products (name, price, description, id_shop, stock, id_category, status, customizable, base_type, base_image) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getName());
                ps.setLong(2, p.getPrice());
                ps.setString(3, p.getDescription());
                ps.setInt(4, p.getShopId());
                ps.setInt(5, p.getStock());
                ps.setInt(6, p.getCategoryId());
                ps.setString(7, p.getStatus());
                ps.setBoolean(8, p.isCustomizable());
                ps.setString(9, p.getBaseType());
                ps.setString(10, p.getBaseImage());
                ps.executeUpdate();
                int id;
                try (ResultSet k = ps.getGeneratedKeys()) {
                    k.next();
                    id = k.getInt(1);
                }
                addImages(c, id, imageUrls);
                saveCharms(c, id, p.isCustomizable(), p.getCharms());
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

//    public boolean update(Product p, List<String> newImageUrls) throws SQLException {
//        String sql = "UPDATE products SET name=?, price=?, description=?, stock=?, id_category=?, status=?, " +
//                "customizable=?, base_type=COALESCE(?, base_type), base_image=COALESCE(?, base_image) " +
//                "WHERE id=? AND id_shop=? AND status <> 'deleted'";
//        try (Connection c = DBConnection.getConnection()) {
//            c.setAutoCommit(false);
//            try (PreparedStatement ps = c.prepareStatement(sql)) {
//                ps.setString(1, p.getName());
//                ps.setLong(2, p.getPrice());
//                ps.setString(3, p.getDescription());
//                ps.setInt(4, p.getStock());
//                ps.setInt(5, p.getCategoryId());
//                ps.setString(6, p.getStatus());
//                ps.setBoolean(7, p.isCustomizable());
//                ps.setString(8, p.getBaseType());     // null = giữ giá trị cũ
//                ps.setString(9, p.getBaseImage());    // null = giữ ảnh nền cũ
//                ps.setInt(10, p.getId());
//                ps.setInt(11, p.getShopId());
//                boolean ok = ps.executeUpdate() > 0;
//                if (ok) {
//                    addImages(c, p.getId(), newImageUrls);
//                    saveCharms(c, p.getId(), p.isCustomizable(), p.getCharms());
//                }
//                c.commit();
//                return ok;
//            } catch (SQLException e) {
//                c.rollback();
//                throw e;
//            }
//        }
//    }

    public boolean update(Product p, List<String> newImageUrls, List<String> removeImageUrls) throws SQLException {
        String sql = "UPDATE products SET name=?, price=?, description=?, stock=?, id_category=?, status=?, " +
                "customizable=?, base_type=COALESCE(?, base_type), base_image=COALESCE(?, base_image) " +
                "WHERE id=? AND id_shop=? AND status <> 'deleted'";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement ps = c.prepareStatement(sql)) {
                ps.setString(1, p.getName());
                ps.setLong(2, p.getPrice());
                ps.setString(3, p.getDescription());
                ps.setInt(4, p.getStock());
                ps.setInt(5, p.getCategoryId());
                ps.setString(6, p.getStatus());
                ps.setBoolean(7, p.isCustomizable());
                ps.setString(8, p.getBaseType());     // null = giữ giá trị cũ
                ps.setString(9, p.getBaseImage());    // null = giữ ảnh nền cũ
                ps.setInt(10, p.getId());
                ps.setInt(11, p.getShopId());
                boolean ok = ps.executeUpdate() > 0;
                if (ok) {
                    removeImages(c, p.getId(), removeImageUrls);   // xóa trước, thêm sau
                    addImages(c, p.getId(), newImageUrls);
                    saveCharms(c, p.getId(), p.isCustomizable(), p.getCharms());
                    // Chốt chặn cuối: sản phẩm phải còn ít nhất 1 ảnh
                    try (PreparedStatement cnt = c.prepareStatement("SELECT COUNT(*) FROM productimages WHERE id_product = ?")) {
                        cnt.setInt(1, p.getId());
                        try (ResultSet rs = cnt.executeQuery()) {
                            if (rs.next() && rs.getInt(1) == 0)
                                throw new SQLException("Sản phẩm cần có ít nhất 1 ảnh.");
                        }
                    }
                }
                c.commit();
                return ok;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private void removeImages(Connection c, int productId, List<String> urls) throws SQLException {
        if (urls == null || urls.isEmpty()) return;
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM productimages WHERE id_product = ? AND url = ?")) {
            for (String u : urls) {
                ps.setInt(1, productId);
                ps.setString(2, u);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void addImages(Connection c, int productId, List<String> urls) throws SQLException {
        if (urls == null || urls.isEmpty()) return;
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO productimages (id_product, url) VALUES (?, ?)")) {
            for (String u : urls) {
                ps.setInt(1, productId);
                ps.setString(2, u);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /**
     * Xóa sản phẩm. Trả về: 2 = đã xóa hẳn, 1 = đã có trong đơn hàng (hoặc vướng khóa ngoại) nên chỉ gỡ khỏi danh sách,
     * 0 = không tìm thấy. filesOut nhận danh sách đường dẫn ảnh để servlet dọn file sau khi xóa hẳn.
     */
    public int deleteProduct(int id, int shopId, List<String> filesOut) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                // 1. Sản phẩm có tồn tại và thuộc shop không
                String baseImage;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT base_image FROM products WHERE id = ? AND id_shop = ? AND status <> 'deleted'")) {
                    ps.setInt(1, id);
                    ps.setInt(2, shopId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            c.rollback();
                            return 0;
                        }
                        baseImage = rs.getString(1);
                    }
                }

                // 2. Đã nằm trong đơn hàng thì không xóa hẳn (giữ lịch sử đơn)
                boolean inOrders;
                try (PreparedStatement ps = c.prepareStatement("SELECT 1 FROM orderitems WHERE id_product = ? LIMIT 1")) {
                    ps.setInt(1, id);
                    try (ResultSet rs = ps.executeQuery()) {
                        inOrders = rs.next();
                    }
                }

                // 3. Chưa có đơn: thử xóa thật
                if (!inOrders) {
                    List<String> files = new ArrayList<>();
                    if (baseImage != null && !baseImage.isBlank()) files.add(baseImage);
                    try (PreparedStatement ps = c.prepareStatement("SELECT url FROM productimages WHERE id_product = ?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) files.add(rs.getString(1));
                        }
                    }
                    try (PreparedStatement ps = c.prepareStatement("SELECT image FROM product_charms WHERE id_product = ?")) {
                        ps.setInt(1, id);
                        try (ResultSet rs = ps.executeQuery()) {
                            while (rs.next()) files.add(rs.getString(1));
                        }
                    }
                    try {
                        try (PreparedStatement ps = c.prepareStatement("DELETE FROM productimages WHERE id_product = ?")) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = c.prepareStatement("DELETE FROM product_charms WHERE id_product = ?")) {
                            ps.setInt(1, id);
                            ps.executeUpdate();
                        }
                        try (PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE id = ? AND id_shop = ?")) {
                            ps.setInt(1, id);
                            ps.setInt(2, shopId);
                            ps.executeUpdate();
                        }
                        c.commit();
                        if (filesOut != null) filesOut.addAll(files);
                        return 2;
                    } catch (SQLException e) {
                        c.rollback();
                        // 23xxx = vướng khóa ngoại (giỏ hàng, review...): rơi xuống bước gỡ khỏi danh sách bên dưới
                        if (e.getSQLState() == null || !e.getSQLState().startsWith("23")) throw e;
                    }
                }

                // 4. Không xóa hẳn được: đánh dấu 'deleted' (biến mất khỏi danh sách, giữ dữ liệu đơn hàng)
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE products SET status = 'deleted' WHERE id = ? AND id_shop = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, shopId);
                    ps.executeUpdate();
                }
                c.commit();
                return 1;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }


    /**
     * Đồng bộ danh sách charm của sản phẩm với dữ liệu từ form:
     * charm id>0 thì cập nhật (ảnh null = giữ ảnh cũ), id=0 thì thêm mới, charm không còn trong form thì xóa.
     * Không bật tùy chỉnh (customizable=false) thì xóa hết charm.
     */
    private void saveCharms(Connection c, int productId, boolean customizable, List<Charm> charms) throws SQLException {
        List<Charm> list = (customizable && charms != null) ? charms : new ArrayList<>();

        List<Integer> keep = new ArrayList<>();
        for (Charm ch : list) if (ch.getId() > 0) keep.add(ch.getId());

        StringBuilder del = new StringBuilder("DELETE FROM product_charms WHERE id_product = ?");
        if (!keep.isEmpty()) {
            del.append(" AND id NOT IN (").append(String.join(",", Collections.nCopies(keep.size(), "?"))).append(")");
        }
        try (PreparedStatement ps = c.prepareStatement(del.toString())) {
            ps.setInt(1, productId);
            for (int i = 0; i < keep.size(); i++) ps.setInt(i + 2, keep.get(i));
            ps.executeUpdate();
        }

        String upd = "UPDATE product_charms SET name = ?, price = ?, image = COALESCE(?, image) WHERE id = ? AND id_product = ?";
        String ins = "INSERT INTO product_charms (id_product, name, price, image) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pu = c.prepareStatement(upd); PreparedStatement pi = c.prepareStatement(ins)) {
            for (Charm ch : list) {
                if (ch.getId() > 0) {
                    pu.setString(1, ch.getName());
                    pu.setLong(2, ch.getPrice());
                    pu.setString(3, ch.getImage());
                    pu.setInt(4, ch.getId());
                    pu.setInt(5, productId);
                    pu.executeUpdate();
                } else {
                    if (ch.getImage() == null)
                        throw new SQLException("Charm \"" + ch.getName() + "\" thiếu ảnh hợp lệ (PNG/JPG)");
                    pi.setInt(1, productId);
                    pi.setString(2, ch.getName());
                    pi.setLong(3, ch.getPrice());
                    pi.setString(4, ch.getImage());
                    pi.executeUpdate();
                }
            }
        }
    }

    public boolean softDelete(int id, int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE products SET status = 'deleted' WHERE id = ? AND id_shop = ?")) {
            ps.setInt(1, id);
            ps.setInt(2, shopId);
            return ps.executeUpdate() > 0;
        }
    }

    // =====================================================================
    //  PHẦN 2: DANH MỤC CÔNG KHAI (người mua)
    // =====================================================================

    private static final String PRODUCT_FROM_WHERE =
            "FROM products p " +
                    "JOIN categories c ON c.id = p.id_category " +
                    "JOIN shops s ON s.id = p.id_shop " +
                    "LEFT JOIN (SELECT id_product, AVG(rating) AS average_rating, COUNT(*) AS review_count " +
                    "           FROM reviews GROUP BY id_product) review_stats ON review_stats.id_product = p.id " +
                    "WHERE p.status = 'active' AND s.status = 'active'";

    public List<Map<String, Object>> findActiveCategories() throws SQLException {
        String sql = "SELECT c.id, c.name, COUNT(p.id) AS product_count " +
                "FROM categories c " +
                "JOIN products p ON p.id_category = c.id AND p.status = 'active' " +
                "JOIN shops s ON s.id = p.id_shop AND s.status = 'active' " +
                "GROUP BY c.id, c.name ORDER BY c.name";
        List<Map<String, Object>> categories = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                Map<String, Object> category = new LinkedHashMap<>();
                category.put("id", result.getInt("id"));
                category.put("name", result.getString("name"));
                category.put("productCount", result.getInt("product_count"));
                categories.add(category);
            }
        }
        return categories;
    }

    public int countActiveProductsByShop(int shopId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM products p JOIN shops s ON s.id=p.id_shop " +
                "WHERE p.id_shop=? AND p.status='active' AND s.status='active'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public List<Map<String, Object>> findActiveProductsByShop(int shopId, int limit, int offset) throws SQLException {
        String sql = "SELECT p.id,p.name,p.price,p.description,p.stock,p.sold_count, " +
                "c.id AS category_id,c.name AS category_name,s.name AS shop_name, " +
                "COALESCE(rs.average_rating,0) AS rating,COALESCE(rs.review_count,0) AS review_count, " +
                "(SELECT pi.url FROM productimages pi WHERE pi.id_product=p.id ORDER BY pi.id LIMIT 1) AS image " +
                "FROM products p JOIN categories c ON c.id=p.id_category JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN (SELECT id_product,AVG(rating) AS average_rating,COUNT(*) AS review_count " +
                "FROM reviews GROUP BY id_product) rs ON rs.id_product=p.id " +
                "WHERE p.id_shop=? AND p.status='active' AND s.status='active' " +
                "ORDER BY p.sold_count DESC,p.id DESC LIMIT ? OFFSET ?";
        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, shopId);
            statement.setInt(2, limit);
            statement.setInt(3, offset);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", result.getInt("id"));
                    product.put("name", result.getString("name"));
                    product.put("price", result.getBigDecimal("price"));
                    product.put("description", result.getString("description"));
                    product.put("stock", result.getInt("stock"));
                    product.put("soldCount", result.getInt("sold_count"));
                    product.put("categoryId", result.getInt("category_id"));
                    product.put("categoryName", result.getString("category_name"));
                    product.put("shopName", result.getString("shop_name"));
                    product.put("rating", result.getBigDecimal("rating"));
                    product.put("reviewCount", result.getInt("review_count"));
                    product.put("image", result.getString("image"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    public Map<String, Object> findActiveProductDetails(int productId) throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.description, p.stock, p.sold_count, " +
                "p.customizable, p.base_type, p.base_image, " +
                "EXISTS (SELECT 1 FROM product_charms pc JOIN products cp ON cp.id=pc.id_product " +
                "WHERE cp.id_shop=p.id_shop AND cp.status='active' AND cp.customizable=1) AS shop_has_design_charms, " +
                "c.id AS category_id, c.name AS category_name, s.id AS shop_id, s.name AS shop_name, " +
                "s.avatar AS shop_avatar, s.rating AS shop_rating, " +
                "COALESCE(rs.average_rating, 0) AS rating, COALESCE(rs.review_count, 0) AS review_count " +
                "FROM products p JOIN categories c ON c.id=p.id_category " +
                "JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN (SELECT id_product, AVG(rating) average_rating, COUNT(*) review_count " +
                "FROM reviews GROUP BY id_product) rs ON rs.id_product=p.id " +
                "WHERE p.id=? AND p.status='active' AND s.status='active'";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price"));
                product.put("description", rs.getString("description"));
                product.put("stock", rs.getInt("stock"));
                product.put("soldCount", rs.getInt("sold_count"));
                product.put("customizable", rs.getBoolean("customizable"));
                product.put("baseType", rs.getString("base_type"));
                product.put("baseImage", rs.getString("base_image"));
                product.put("shopHasDesignCharms", rs.getBoolean("shop_has_design_charms"));
                product.put("categoryId", rs.getInt("category_id"));
                product.put("categoryName", rs.getString("category_name"));
                product.put("shopId", rs.getInt("shop_id"));
                product.put("shopName", rs.getString("shop_name"));
                product.put("shopAvatar", rs.getString("shop_avatar"));
                product.put("shopRating", rs.getBigDecimal("shop_rating"));
                product.put("rating", rs.getBigDecimal("rating"));
                product.put("reviewCount", rs.getInt("review_count"));
                return product;
            }
        }
    }

    /**
     * Public source products and their shop-configured base images and charms for the designer.
     */
    public List<Map<String, Object>> findActiveCustomizerSources(Integer shopId) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.price, p.base_type, " +
                        "COALESCE(NULLIF(p.base_image, ''), (SELECT i.url FROM productimages i WHERE i.id_product=p.id ORDER BY i.id LIMIT 1)) AS base_image, " +
                        "c.id AS category_id, c.name AS category_name, s.id AS shop_id, s.name AS shop_name " +
                        "FROM products p JOIN categories c ON c.id=p.id_category " +
                        "JOIN shops s ON s.id=p.id_shop " +
                        "WHERE p.status='active' AND s.status='active' " +
                        "AND EXISTS (SELECT 1 FROM product_charms pc JOIN products cp ON cp.id=pc.id_product " +
                        "WHERE cp.id_shop=s.id AND cp.status='active' AND cp.customizable=1) ");
        if (shopId != null && shopId > 0) sql.append("AND s.id=? ");
        sql.append("ORDER BY s.name, p.name, p.id");

        List<Map<String, Object>> sources = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                if (shopId != null && shopId > 0) statement.setInt(1, shopId);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Map<String, Object> source = new LinkedHashMap<>();
                        source.put("id", result.getInt("id"));
                        source.put("name", result.getString("name"));
                        source.put("price", result.getBigDecimal("price"));
                        source.put("baseType", result.getString("base_type"));
                        source.put("baseImage", result.getString("base_image"));
                        source.put("categoryId", result.getInt("category_id"));
                        source.put("categoryName", result.getString("category_name"));
                        source.put("shopId", result.getInt("shop_id"));
                        source.put("shopName", result.getString("shop_name"));
                        sources.add(source);
                    }
                }
            }
            try (PreparedStatement charmStatement = connection.prepareStatement(
                    "SELECT DISTINCT pc.id, pc.name, pc.price, pc.image FROM product_charms pc " +
                            "JOIN products cp ON cp.id=pc.id_product " +
                            "WHERE cp.id_shop=? AND cp.status='active' AND cp.customizable=1 ORDER BY pc.id")) {
                for (Map<String, Object> source : sources) {
                    List<Map<String, Object>> charms = new ArrayList<>();
                    charmStatement.setInt(1, (Integer) source.get("shopId"));
                    try (ResultSet charmRows = charmStatement.executeQuery()) {
                        while (charmRows.next()) {
                            Map<String, Object> charm = new LinkedHashMap<>();
                            charm.put("id", charmRows.getInt("id"));
                            charm.put("name", charmRows.getString("name"));
                            charm.put("price", charmRows.getBigDecimal("price"));
                            charm.put("image", charmRows.getString("image"));
                            charms.add(charm);
                        }
                    }
                    source.put("charms", charms);
                }
            }
        }
        return sources;
    }

    public List<String> findProductImages(int productId) throws SQLException {
        List<String> images = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT url FROM productimages WHERE id_product=? ORDER BY id")) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) images.add(rs.getString(1));
            }
        }
        return images;
    }

    public List<Map<String, Object>> findProductReviews(int productId, int limit) throws SQLException {
        String sql = "SELECT r.rating, r.comment, r.date, u.name AS reviewer_name FROM reviews r " +
                "JOIN users u ON u.id=r.id_user WHERE r.id_product=? ORDER BY r.date DESC LIMIT ?";
        List<Map<String, Object>> reviews = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId);
            statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> review = new LinkedHashMap<>();
                    review.put("rating", rs.getInt("rating"));
                    review.put("comment", rs.getString("comment"));
                    review.put("date", rs.getTimestamp("date"));
                    review.put("reviewerName", rs.getString("reviewer_name"));
                    reviews.add(review);
                }
            }
        }
        return reviews;
    }

    public List<Integer> findReviewableOrders(int userId, int productId) throws SQLException {
        String sql = "SELECT DISTINCT o.id FROM orders o JOIN account a ON a.id_user=o.id_user AND a.role='buyer' JOIN orderitems oi ON oi.id_order=o.id " +
                "LEFT JOIN reviews r ON r.id_user=o.id_user AND r.id_product=oi.id_product AND r.id_order=o.id " +
                "WHERE o.id_user=? AND oi.id_product=? AND o.status='completed' AND r.id IS NULL ORDER BY o.id DESC";
        List<Integer> orderIds = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, productId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) orderIds.add(rs.getInt("id"));
            }
        }
        return orderIds;
    }

    public boolean insertProductReview(int userId, int productId, int orderId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (id_user,id_product,id_order,rating,comment) " +
                "SELECT ?, oi.id_product, o.id, ?, ? FROM orders o JOIN account a ON a.id_user=o.id_user AND a.role='buyer' JOIN orderitems oi ON oi.id_order=o.id " +
                "WHERE o.id=? AND o.id_user=? AND o.status='completed' AND oi.id_product=? " +
                "AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.id_user=o.id_user AND r.id_product=oi.id_product AND r.id_order=o.id)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setInt(2, rating);
            statement.setString(3, comment);
            statement.setInt(4, orderId);
            statement.setInt(5, userId);
            statement.setInt(6, productId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Map<String, Object>> findSimilarProducts(int productId, int categoryId, int limit) throws SQLException {
        String sql = "SELECT p.id,p.name,p.price,p.stock,p.sold_count,c.name category_name,s.name shop_name, " +
                "(SELECT pi.url FROM productimages pi WHERE pi.id_product=p.id ORDER BY pi.id LIMIT 1) image, " +
                "COALESCE(rs.average_rating,0) rating FROM products p JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN (SELECT id_product,AVG(rating) average_rating FROM reviews GROUP BY id_product) rs ON rs.id_product=p.id " +
                "JOIN categories c ON c.id=p.id_category " +
                "WHERE p.status='active' AND s.status='active' AND p.id_category=? AND p.id<>? " +
                "ORDER BY p.sold_count DESC,p.id DESC LIMIT ?";
        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId);
            statement.setInt(2, productId);
            statement.setInt(3, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("name", rs.getString("name"));
                    product.put("price", rs.getBigDecimal("price"));
                    product.put("soldCount", rs.getInt("sold_count"));
                    product.put("stock", rs.getInt("stock"));
                    product.put("categoryName", rs.getString("category_name"));
                    product.put("shopName", rs.getString("shop_name"));
                    product.put("image", rs.getString("image"));
                    product.put("rating", rs.getBigDecimal("rating"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    public int countActiveProducts(List<Integer> categoryIds,
                                   List<String> priceRanges,
                                   List<Integer> minRatings,
                                   String keyword) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ").append(PRODUCT_FROM_WHERE);
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, categoryIds, priceRanges, minRatings, keyword);

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getInt(1) : 0;
            }
        }
    }

    public List<Map<String, Object>> findActiveProducts(List<Integer> categoryIds,
                                                        List<String> priceRanges,
                                                        List<Integer> minRatings,
                                                        String keyword,
                                                        String sort,
                                                        int limit,
                                                        int offset) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.price, p.description, p.stock, p.sold_count, " +
                        "c.id AS category_id, c.name AS category_name, s.name AS shop_name, " +
                        "COALESCE(review_stats.average_rating, 0) AS rating, " +
                        "COALESCE(review_stats.review_count, 0) AS review_count, " +
                        "(SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image ")
                .append(PRODUCT_FROM_WHERE);
        List<Object> parameters = new ArrayList<>();
        appendFilters(sql, parameters, categoryIds, priceRanges, minRatings, keyword);

        switch (sort == null ? "default" : sort) {
            case "best" -> sql.append(" ORDER BY p.sold_count DESC, p.id DESC");
            case "price-asc" -> sql.append(" ORDER BY p.price ASC, p.id DESC");
            case "price-desc" -> sql.append(" ORDER BY p.price DESC, p.id DESC");
            case "rating-desc" -> sql.append(" ORDER BY rating DESC, review_count DESC, p.id DESC");
            case "newest" -> sql.append(" ORDER BY p.id DESC");
            default -> sql.append(" ORDER BY p.sold_count DESC, p.id DESC");
        }
        sql.append(" LIMIT ? OFFSET ?");
        parameters.add(limit);
        parameters.add(offset);

        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            bindParameters(statement, parameters);
            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", result.getInt("id"));
                    product.put("name", result.getString("name"));
                    product.put("price", result.getBigDecimal("price"));
                    product.put("description", result.getString("description"));
                    product.put("stock", result.getInt("stock"));
                    product.put("soldCount", result.getInt("sold_count"));
                    product.put("categoryId", result.getInt("category_id"));
                    product.put("categoryName", result.getString("category_name"));
                    product.put("shopName", result.getString("shop_name"));
                    product.put("rating", result.getBigDecimal("rating"));
                    product.put("reviewCount", result.getInt("review_count"));
                    product.put("image", result.getString("image"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    private void appendFilters(StringBuilder sql,
                               List<Object> parameters,
                               List<Integer> categoryIds,
                               List<String> priceRanges,
                               List<Integer> minRatings,
                               String keyword) {
        if (categoryIds != null) {
            if (categoryIds.isEmpty()) {
                sql.append(" AND 1 = 0");
            } else {
                sql.append(" AND p.id_category IN (")
                        .append(String.join(",", Collections.nCopies(categoryIds.size(), "?")))
                        .append(')');
                parameters.addAll(categoryIds);
            }
        }

        if (priceRanges != null && !priceRanges.isEmpty()) {
            List<String> priceConditions = new ArrayList<>();
            for (String priceRange : priceRanges) {
                switch (priceRange) {
                    case "under-100000" -> priceConditions.add("p.price < 100000");
                    case "100000-300000" -> priceConditions.add("(p.price >= 100000 AND p.price < 300000)");
                    case "300000-500000" -> priceConditions.add("(p.price >= 300000 AND p.price < 500000)");
                    case "over-500000" -> priceConditions.add("p.price >= 500000");
                    default -> {
                    }
                }
            }
            if (!priceConditions.isEmpty()) {
                sql.append(" AND (").append(String.join(" OR ", priceConditions)).append(')');
            }
        }

        if (minRatings != null && !minRatings.isEmpty()) {
            List<String> ratingConditions = new ArrayList<>();
            if (minRatings.contains(0)) ratingConditions.add("COALESCE(review_stats.review_count, 0) = 0");
            Integer minimumRated = minRatings.stream().filter(value -> value > 0).min(Integer::compareTo).orElse(null);
            if (minimumRated != null) {
                ratingConditions.add("COALESCE(review_stats.average_rating, 0) >= ?");
                parameters.add(minimumRated);
            }
            if (!ratingConditions.isEmpty())
                sql.append(" AND (").append(String.join(" OR ", ratingConditions)).append(')');
        }

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND (p.name LIKE ? OR p.description LIKE ? OR s.name LIKE ? OR c.name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
            parameters.add(pattern);
        }
    }

    private void bindParameters(PreparedStatement statement, List<Object> parameters) throws SQLException {
        for (int index = 0; index < parameters.size(); index++) {
            statement.setObject(index + 1, parameters.get(index));
        }
    }

    /**
     * Lấy danh sách sản phẩm nổi bật từ bảng featured_products
     */
    public List<Map<String, Object>> getFeaturedProducts() throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.description, p.stock, p.sold_count, " +
                "c.id AS category_id, c.name AS category_name, s.name AS shop_name, " +
                "COALESCE(review_stats.average_rating, 0) AS rating, " +
                "COALESCE(review_stats.review_count, 0) AS review_count, " +
                "(SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image, " +
                "fp.display_order " +
                "FROM featured_products fp " +
                "JOIN products p ON p.id = fp.product_id " +
                "JOIN categories c ON c.id = p.id_category " +
                "JOIN shops s ON s.id = p.id_shop " +
                "LEFT JOIN (SELECT id_product, AVG(rating) AS average_rating, COUNT(*) AS review_count " +
                "           FROM reviews GROUP BY id_product) review_stats ON review_stats.id_product = p.id " +
                "WHERE p.status = 'active' AND s.status = 'active' " +
                "ORDER BY fp.display_order ASC";

        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> product = new LinkedHashMap<>();
                product.put("id", rs.getInt("id"));
                product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price"));
                product.put("description", rs.getString("description"));
                product.put("stock", rs.getInt("stock"));
                product.put("soldCount", rs.getInt("sold_count"));
                product.put("categoryId", rs.getInt("category_id"));
                product.put("categoryName", rs.getString("category_name"));
                product.put("shopName", rs.getString("shop_name"));
                product.put("rating", rs.getBigDecimal("rating"));
                product.put("reviewCount", rs.getInt("review_count"));
                product.put("image", rs.getString("image"));
                product.put("displayOrder", rs.getInt("display_order"));
                products.add(product);

            }
        }
        return products;
    }

    public void initFeaturedProductsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS featured_products (" +
                "id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY, " +
                "product_id INT UNSIGNED NOT NULL, " +
                "display_order INT NOT NULL DEFAULT 0, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE KEY uq_featured_product (product_id), " +
                "CONSTRAINT fk_featured_product FOREIGN KEY (product_id) REFERENCES products (id) ON DELETE CASCADE" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();

            try (PreparedStatement check = connection.prepareStatement("SELECT COUNT(*) FROM featured_products");
                 ResultSet rs = check.executeQuery()) {
                if (rs.next() && rs.getInt(1) == 0) {
                    try (PreparedStatement seed = connection.prepareStatement(
                            "INSERT INTO featured_products (product_id, display_order) " +
                                    "SELECT id, id FROM products WHERE status='active' ORDER BY sold_count DESC, id ASC LIMIT 3")) {
                        seed.executeUpdate();
                    }
                }
            }
        } catch (SQLException ignored) {
        }
    }

    /**
     * Tìm kiếm sản phẩm active cho trang quản trị sản phẩm nổi bật
     */
    public List<Map<String, Object>> searchActiveProducts(String keyword, int limit) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT p.id, p.name, p.price, p.description, p.stock, p.sold_count, " +
                        "c.id AS category_id, c.name AS category_name, s.name AS shop_name, " +
                        "COALESCE(review_stats.average_rating, 0) AS rating, " +
                        "COALESCE(review_stats.review_count, 0) AS review_count, " +
                        "(SELECT pi.url FROM productimages pi WHERE pi.id_product = p.id ORDER BY pi.id LIMIT 1) AS image " +
                        "FROM products p " +
                        "JOIN categories c ON c.id = p.id_category " +
                        "JOIN shops s ON s.id = p.id_shop " +
                        "LEFT JOIN (SELECT id_product, AVG(rating) AS average_rating, COUNT(*) AS review_count " +
                        "           FROM reviews GROUP BY id_product) review_stats ON review_stats.id_product = p.id " +
                        "WHERE p.status = 'active' AND s.status = 'active'");

        List<Object> params = new ArrayList<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append(" AND (p.name LIKE ? OR c.name LIKE ? OR s.name LIKE ?)");
            String pattern = "%" + keyword.trim() + "%";
            params.add(pattern);
            params.add(pattern);
            params.add(pattern);
        }

        sql.append(" ORDER BY p.sold_count DESC, p.id DESC LIMIT ?");
        params.add(limit <= 0 ? 50 : limit);

        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", rs.getInt("id"));
                    product.put("name", rs.getString("name"));
                    product.put("price", rs.getBigDecimal("price"));
                    product.put("description", rs.getString("description"));
                    product.put("stock", rs.getInt("stock"));
                    product.put("soldCount", rs.getInt("sold_count"));
                    product.put("categoryId", rs.getInt("category_id"));
                    product.put("categoryName", rs.getString("category_name"));
                    product.put("shopName", rs.getString("shop_name"));
                    product.put("rating", rs.getBigDecimal("rating"));
                    product.put("reviewCount", rs.getInt("review_count"));
                    product.put("image", rs.getString("image"));
                    products.add(product);
                }
            }
        }
        return products;
    }

    /**
     * Cập nhật danh sách và thứ tự sản phẩm nổi bật vào bảng featured_products
     */
    public boolean updateFeaturedProducts(List<Integer> ids) {
        String deleteSql = "DELETE FROM featured_products";
        String insertSql = "INSERT INTO featured_products (product_id, display_order) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
                    deleteStmt.executeUpdate();
                }

                if (ids != null && !ids.isEmpty()) {
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        int order = 1;
                        for (Integer id : ids) {
                            if (id != null) {
                                insertStmt.setInt(1, id);
                                insertStmt.setInt(2, order++);
                                insertStmt.addBatch();
                            }
                        }
                        insertStmt.executeBatch();
                    }
                }

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
