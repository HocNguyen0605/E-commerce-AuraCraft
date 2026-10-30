package dao;

import com.google.gson.JsonObject;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/** Persists buyer requests so they appear in the seller custom quote market. */
public class BuyerCustomRequestDAO {
    public int create(int userId, String productType, String materials, String description,
                      long budget, String configJson, String previewImage,
                      List<String> designImages, List<String> charmImages) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int templateId = findTemplate(c, configJson);
                if (templateId < 1) throw new SQLException("Chưa có mẫu thiết kế trong hệ thống để tạo yêu cầu.");

                int designId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customdesigns (id_user,id_template,config,total_price,preview_image) VALUES (?,?,?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, templateId);
                    ps.setString(3, configJson);
                    ps.setLong(4, budget);
                    ps.setString(5, previewImage);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được bản thiết kế.");
                        designId = keys.getInt(1);
                    }
                }

                int requestId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO customrequests (id_user,id_design,product_type,materials,charm_description,budget,status) " +
                                "VALUES (?,?,?,?,?,?,'open')", Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, userId);
                    ps.setInt(2, designId);
                    ps.setString(3, productType);
                    ps.setString(4, materials);
                    ps.setString(5, description);
                    ps.setLong(6, budget);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không tạo được yêu cầu báo giá.");
                        requestId = keys.getInt(1);
                    }
                }
                insertImages(c, requestId, designImages, "design");
                insertImages(c, requestId, charmImages, "charm");
                c.commit();
                return requestId;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private int findTemplate(Connection c, String configJson) throws SQLException {
        try {
            JsonObject config = com.google.gson.JsonParser.parseString(configJson).getAsJsonObject();
            if (config.has("productId") && config.get("productId").isJsonPrimitive()) {
                int productId = config.get("productId").getAsInt();
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM designtemplates WHERE id_product=? ORDER BY id LIMIT 1")) {
                    ps.setInt(1, productId);
                    try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
                }
            }
            if (config.has("baseMaterial") && config.get("baseMaterial").isJsonObject()) {
                JsonObject base = config.getAsJsonObject("baseMaterial");
                if (base.has("sourceProductId")) {
                    int productId = base.get("sourceProductId").getAsInt();
                    try (PreparedStatement ps = c.prepareStatement("SELECT id FROM designtemplates WHERE id_product=? ORDER BY id LIMIT 1")) {
                        ps.setInt(1, productId);
                        try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getInt(1); }
                    }
                }
            }
        } catch (RuntimeException ignored) { }
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM designtemplates ORDER BY id LIMIT 1");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private void insertImages(Connection c, int requestId, List<String> images, String kind) throws SQLException {
        if (images == null || images.isEmpty()) return;
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO customrequestimages (id_request,url,kind) VALUES (?,?,?)")) {
            for (String image : images) {
                if (image == null || image.isBlank()) continue;
                ps.setInt(1, requestId);
                ps.setString(2, image);
                ps.setString(3, kind);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }
}
