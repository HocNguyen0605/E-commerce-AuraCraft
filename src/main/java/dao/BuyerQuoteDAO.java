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

/** Loads the buyer's custom design requests and shop quotes for the profile page. */
public class BuyerQuoteDAO {
    public List<Map<String, Object>> findRequests(int buyerId) throws SQLException {
        String sql = "SELECT r.id, r.product_type, r.materials, r.charm_description, r.budget, r.status AS request_status, r.date_create, " +
                "d.preview_image, q.id AS quote_id, q.price, q.lead_time_days, q.note, q.portfolio, q.status AS quote_status, q.date AS quote_date, s.name AS shop_name " +
                "FROM customrequests r LEFT JOIN customdesigns d ON d.id = r.id_design " +
                "LEFT JOIN quotes q ON q.id_request = r.id LEFT JOIN shops s ON s.id = q.id_shop " +
                "WHERE r.id_user = ? ORDER BY r.id DESC, q.id DESC";
        Map<Integer, Map<String, Object>> byId = new LinkedHashMap<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int requestId = rs.getInt("id");
                    Map<String, Object> request = byId.get(requestId);
                    if (request == null) {
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", requestId);
                        row.put("productType", rs.getString("product_type"));
                        row.put("materials", rs.getString("materials"));
                        row.put("description", rs.getString("charm_description"));
                        row.put("budget", rs.getLong("budget"));
                        row.put("requestStatus", rs.getString("request_status"));
                        row.put("createdAt", rs.getTimestamp("date_create"));
                        row.put("previewImage", rs.getString("preview_image"));
                        row.put("quotes", new ArrayList<Map<String, Object>>());
                        row.put("quoteCount", 0);
                        row.put("hasPending", false);
                        row.put("hasAccepted", false);
                        row.put("hasQuote", false);
                        byId.put(requestId, row);
                        request = row;
                    }
                    int quoteId = rs.getInt("quote_id");
                    if (rs.wasNull()) continue;
                    @SuppressWarnings("unchecked") List<Map<String, Object>> quotes = (List<Map<String, Object>>) request.get("quotes");
                    Map<String, Object> quote = new LinkedHashMap<>();
                    quote.put("id", quoteId);
                    quote.put("shopName", rs.getString("shop_name"));
                    quote.put("price", rs.getLong("price"));
                    quote.put("days", rs.getInt("lead_time_days"));
                    quote.put("note", rs.getString("note"));
                    quote.put("portfolio", rs.getString("portfolio"));
                    quote.put("status", rs.getString("quote_status"));
                    quote.put("date", rs.getTimestamp("quote_date"));
                    quotes.add(quote);
                    request.put("quoteCount", quotes.size());
                    request.put("hasQuote", true);
                    String status = rs.getString("quote_status");
                    if ("pending".equals(status)) request.put("hasPending", true);
                    if ("accepted".equals(status)) request.put("hasAccepted", true);
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>(byId.values());
        for (Map<String, Object> request : result) {
            String requestStatus = String.valueOf(request.get("requestStatus"));
            if (Boolean.TRUE.equals(request.get("hasAccepted")) || "assigned".equals(requestStatus)) request.put("filterStatus", "accepted");
            else if (!Boolean.TRUE.equals(request.get("hasQuote"))) request.put("filterStatus", "waiting");
            else if (Boolean.TRUE.equals(request.get("hasPending"))) request.put("filterStatus", "quoted");
            else request.put("filterStatus", "expired");
        }
        return result;
    }
}
