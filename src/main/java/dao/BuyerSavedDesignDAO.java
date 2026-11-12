package dao;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persists a buyer's saved customizer drafts independently from submitted quote requests. */
public class BuyerSavedDesignDAO {
    private static final Gson GSON = new Gson();

    public void save(int userId, String designKey, JsonObject design) throws SQLException {
        String sql = "INSERT INTO buyer_saved_designs " +
                "(id_user,design_key,name,design_type,product_type,seller_name,shop_id,config,total_price,preview_image) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE name=VALUES(name),design_type=VALUES(design_type)," +
                "product_type=VALUES(product_type),seller_name=VALUES(seller_name),shop_id=VALUES(shop_id)," +
                "config=VALUES(config),total_price=VALUES(total_price),preview_image=VALUES(preview_image),updated_at=CURRENT_TIMESTAMP";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, designKey);
            ps.setString(3, value(design, "name", "Thiết kế của tôi"));
            ps.setString(4, value(design, "designType", ""));
            ps.setString(5, value(design, "productType", "Thiết kế sản phẩm"));
            ps.setString(6, value(design, "sellerName", "AuraCraft"));
            if (design.has("shopId") && design.get("shopId").isJsonPrimitive()) {
                String shopId = design.get("shopId").getAsString();
                try { ps.setInt(7, Integer.parseInt(shopId)); } catch (NumberFormatException e) { ps.setNull(7, java.sql.Types.INTEGER); }
            } else ps.setNull(7, java.sql.Types.INTEGER);
            ps.setString(8, GSON.toJson(design));
            ps.setBigDecimal(9, design.has("price") ? design.get("price").getAsBigDecimal() : java.math.BigDecimal.ZERO);
            String preview = preview(design);
            ps.setString(10, preview == null || preview.length() > 255 ? null : preview);
            ps.executeUpdate();
        }
    }

    public List<Map<String, Object>> findByUser(int userId) throws SQLException {
        List<Map<String, Object>> designs = new ArrayList<>();
        String sql = "SELECT design_key,config,created_at,updated_at FROM buyer_saved_designs WHERE id_user=? ORDER BY updated_at DESC";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    JsonObject config = JsonParser.parseString(rs.getString("config")).getAsJsonObject();
                    config.addProperty("id", rs.getString("design_key"));
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("design", config);
                    row.put("createdAt", rs.getTimestamp("created_at"));
                    row.put("updatedAt", rs.getTimestamp("updated_at"));
                    designs.add(row);
                }
            }
        }
        return designs;
    }

    private String value(JsonObject object, String key, String fallback) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : fallback;
    }

    private String preview(JsonObject design) {
        if (design.has("baseMaterial") && design.get("baseMaterial").isJsonObject()) {
            JsonObject base = design.getAsJsonObject("baseMaterial");
            String image = value(base, "image", value(base, "imageUrl", ""));
            if (!image.isBlank()) return image;
        }
        if (design.has("slots") && design.get("slots").isJsonArray()) {
            for (var slot : design.getAsJsonArray("slots")) {
                if (slot.isJsonObject()) {
                    JsonObject item = slot.getAsJsonObject();
                    String image = value(item, "image", value(item, "imageUrl", ""));
                    if (!image.isBlank()) return image;
                }
            }
        }
        return null;
    }
}
