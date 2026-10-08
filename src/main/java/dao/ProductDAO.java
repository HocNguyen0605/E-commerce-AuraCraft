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

/** Database queries used by the public product catalogue. */
public class ProductDAO {
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

    public Map<String, Object> findActiveProductDetails(int productId) throws SQLException {
        String sql = "SELECT p.id, p.name, p.price, p.description, p.stock, p.sold_count, " +
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
                product.put("id", rs.getInt("id")); product.put("name", rs.getString("name"));
                product.put("price", rs.getBigDecimal("price")); product.put("description", rs.getString("description"));
                product.put("stock", rs.getInt("stock")); product.put("soldCount", rs.getInt("sold_count"));
                product.put("categoryId", rs.getInt("category_id")); product.put("categoryName", rs.getString("category_name"));
                product.put("shopId", rs.getInt("shop_id")); product.put("shopName", rs.getString("shop_name"));
                product.put("shopAvatar", rs.getString("shop_avatar")); product.put("shopRating", rs.getBigDecimal("shop_rating"));
                product.put("rating", rs.getBigDecimal("rating")); product.put("reviewCount", rs.getInt("review_count"));
                return product;
            }
        }
    }

    public List<String> findProductImages(int productId) throws SQLException {
        List<String> images = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT url FROM productimages WHERE id_product=? ORDER BY id")) {
            statement.setInt(1, productId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) images.add(rs.getString(1)); }
        }
        return images;
    }

    public List<Map<String, Object>> findProductReviews(int productId, int limit) throws SQLException {
        String sql = "SELECT r.rating, r.comment, r.date, u.name AS reviewer_name FROM reviews r " +
                "JOIN users u ON u.id=r.id_user WHERE r.id_product=? ORDER BY r.date DESC LIMIT ?";
        List<Map<String, Object>> reviews = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, productId); statement.setInt(2, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> review = new LinkedHashMap<>();
                    review.put("rating", rs.getInt("rating")); review.put("comment", rs.getString("comment"));
                    review.put("date", rs.getTimestamp("date")); review.put("reviewerName", rs.getString("reviewer_name"));
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
            statement.setInt(1, userId); statement.setInt(2, productId);
            try (ResultSet rs = statement.executeQuery()) { while (rs.next()) orderIds.add(rs.getInt("id")); }
        }
        return orderIds;
    }

    public boolean insertProductReview(int userId, int productId, int orderId, int rating, String comment) throws SQLException {
        String sql = "INSERT INTO reviews (id_user,id_product,id_order,rating,comment) " +
                "SELECT ?, oi.id_product, o.id, ?, ? FROM orders o JOIN account a ON a.id_user=o.id_user AND a.role='buyer' JOIN orderitems oi ON oi.id_order=o.id " +
                "WHERE o.id=? AND o.id_user=? AND o.status='completed' AND oi.id_product=? " +
                "AND NOT EXISTS (SELECT 1 FROM reviews r WHERE r.id_user=o.id_user AND r.id_product=oi.id_product AND r.id_order=o.id)";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId); statement.setInt(2, rating); statement.setString(3, comment);
            statement.setInt(4, orderId); statement.setInt(5, userId); statement.setInt(6, productId);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Map<String, Object>> findSimilarProducts(int productId, int categoryId, int limit) throws SQLException {
        String sql = "SELECT p.id,p.name,p.price,p.sold_count, " +
                "(SELECT pi.url FROM productimages pi WHERE pi.id_product=p.id ORDER BY pi.id LIMIT 1) image, " +
                "COALESCE(rs.average_rating,0) rating FROM products p JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN (SELECT id_product,AVG(rating) average_rating FROM reviews GROUP BY id_product) rs ON rs.id_product=p.id " +
                "WHERE p.status='active' AND s.status='active' AND p.id_category=? AND p.id<>? " +
                "ORDER BY p.sold_count DESC,p.id DESC LIMIT ?";
        List<Map<String, Object>> products = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, categoryId); statement.setInt(2, productId); statement.setInt(3, limit);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> product = new LinkedHashMap<>();
                    product.put("id", rs.getInt("id")); product.put("name", rs.getString("name"));
                    product.put("price", rs.getBigDecimal("price")); product.put("soldCount", rs.getInt("sold_count"));
                    product.put("image", rs.getString("image")); product.put("rating", rs.getBigDecimal("rating"));
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
                        .append(String.join(",", java.util.Collections.nCopies(categoryIds.size(), "?")))
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
                    default -> { }
                }
            }
            if (!priceConditions.isEmpty()) {
                sql.append(" AND (").append(String.join(" OR ", priceConditions)).append(')');
            }
        }

        if (minRatings != null && !minRatings.isEmpty()) {
            sql.append(" AND COALESCE(review_stats.average_rating, 0) >= ?");
            parameters.add(java.util.Collections.min(minRatings));
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
}
