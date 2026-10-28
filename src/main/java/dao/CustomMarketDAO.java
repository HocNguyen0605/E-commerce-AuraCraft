package dao;

import dto.*;
import util.DBConnection;

import java.sql.*;
import java.util.*;


/** Chợ Custom & Báo giá phía seller. */
public class CustomMarketDAO {

    /** Yêu cầu còn nhận báo giá, không phải của chính chủ shop; ghép sẵn báo giá của shop (nếu có). */
    private static final String OPEN_FROM =
            "FROM customrequests r JOIN users u ON u.id = r.id_user " +
                    "LEFT JOIN customdesigns d ON d.id = r.id_design " +
                    "LEFT JOIN quotes mq ON mq.id_request = r.id AND mq.id_shop = ? " +
                    "WHERE r.status IN ('open','quoted') AND r.id_user <> ? ";

    private static void appendKeyword(StringBuilder sql, List<Object> params, String q) {
        if (q == null || q.trim().isEmpty()) return;
        sql.append(" AND (r.product_type LIKE ? OR r.materials LIKE ? OR r.charm_description LIKE ?)");
        String p = "%" + q.trim() + "%";
        params.add(p); params.add(p); params.add(p);
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    public int countOpenRequests(int shopId, int ownerId, String q) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) ").append(OPEN_FROM);
        List<Object> params = new ArrayList<>();
        params.add(shopId); params.add(ownerId);
        appendKeyword(sql, params, q);
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public List<CustomRequestDTO> findOpenRequests(int shopId, int ownerId, String q, int page, int size) throws SQLException {
        StringBuilder sql = new StringBuilder(
                "SELECT r.id, r.id_user, u.name AS buyer_name, r.product_type, r.materials, r.charm_description, " +
                        "r.budget, r.status, r.date_create AS created_at, d.preview_image AS design_preview, " +
                        "(SELECT COUNT(*) FROM quotes x WHERE x.id_request = r.id AND x.status IN ('pending','accepted')) AS quote_count, " +
                        "mq.id AS my_id, mq.price AS my_price, mq.lead_time_days AS my_days, mq.note AS my_note, " +
                        "mq.portfolio AS my_portfolio, mq.status AS my_status ").append(OPEN_FROM);
        List<Object> params = new ArrayList<>();
        params.add(shopId); params.add(ownerId);
        appendKeyword(sql, params, q);
        sql.append(" ORDER BY r.id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add((Math.max(page, 1) - 1) * size);

        List<CustomRequestDTO> list = new ArrayList<>();
        Map<Integer, String> previews = new HashMap<>();   // ảnh preview của thiết kế (dự phòng khi chưa có ảnh riêng)
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomRequestDTO r = new CustomRequestDTO();
                    r.setId(rs.getInt("id"));
                    r.setBuyerId(rs.getInt("id_user"));
                    r.setBuyerName(rs.getString("buyer_name"));
                    r.setProductType(rs.getString("product_type"));
                    r.setMaterials(rs.getString("materials"));
                    r.setCharmDescription(rs.getString("charm_description"));
                    r.setBudget(rs.getLong("budget"));
                    r.setStatus(rs.getString("status"));
                    r.setCreatedAt(rs.getTimestamp("created_at"));
                    r.setQuoteCount(rs.getInt("quote_count"));
                    if (rs.getObject("my_id") != null) {
                        QuoteDTO m = new QuoteDTO();
                        m.setId(rs.getInt("my_id"));
                        m.setRequestId(r.getId());
                        m.setShopId(shopId);
                        m.setPrice(rs.getLong("my_price"));
                        m.setDays(rs.getInt("my_days"));
                        m.setNote(rs.getString("my_note"));
                        m.setPortfolio(rs.getString("my_portfolio"));
                        m.setStatus(rs.getString("my_status"));
                        r.setMyQuote(m);
                    }
                    String pv = rs.getString("design_preview");
                    if (pv != null && !pv.isEmpty()) previews.put(r.getId(), pv);
                    list.add(r);
                }
            }
            loadImages(c, list);
            for (CustomRequestDTO r : list) {
                String pv = previews.get(r.getId());
                if (pv != null && r.getAllImages().isEmpty()) r.getDesignImages().add(pv);
            }
        }
        return list;
    }

    /** Nạp ảnh cho cả trang bằng 1 truy vấn (tránh N+1). */
    private void loadImages(Connection c, List<CustomRequestDTO> list) throws SQLException {
        if (list.isEmpty()) return;
        Map<Integer, CustomRequestDTO> byId = new HashMap<>();
        for (CustomRequestDTO r : list) byId.put(r.getId(), r);
        String sql = "SELECT id_request, url, kind FROM customrequestimages WHERE id_request IN ("
                + String.join(",", Collections.nCopies(list.size(), "?")) + ") ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < list.size(); i++) ps.setInt(i + 1, list.get(i).getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    CustomRequestDTO r = byId.get(rs.getInt(1));
                    if (r == null) continue;
                    if ("charm".equals(rs.getString(3))) r.getCharmImages().add(rs.getString(2));
                    else r.getDesignImages().add(rs.getString(2));
                }
            }
        }
    }

    /** Danh sách báo giá của shop (tab "Báo giá của tôi"). */
    public List<QuoteDTO> findQuotesByShop(int shopId) throws SQLException {
        String sql = "SELECT q.id, q.id_request, q.price, q.lead_time_days AS days, q.note, q.portfolio, q.status, q.`date` AS created_at, q.updated_at, " +
                "r.product_type, r.budget, r.status AS request_status, u.name AS buyer_name " +
                "FROM quotes q JOIN customrequests r ON r.id = q.id_request JOIN users u ON u.id = r.id_user " +
                "WHERE q.id_shop = ? ORDER BY q.updated_at DESC, q.id DESC";
        List<QuoteDTO> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    QuoteDTO q = new QuoteDTO();
                    q.setId(rs.getInt("id"));
                    q.setRequestId(rs.getInt("id_request"));
                    q.setShopId(shopId);
                    q.setPrice(rs.getLong("price"));
                    q.setDays(rs.getInt("days"));
                    q.setNote(rs.getString("note"));
                    q.setPortfolio(rs.getString("portfolio"));
                    q.setStatus(rs.getString("status"));
                    q.setCreatedAt(rs.getTimestamp("created_at"));
                    q.setUpdatedAt(rs.getTimestamp("updated_at"));
                    q.setRequestTitle(rs.getString("product_type"));
                    q.setBudget(rs.getLong("budget"));
                    q.setRequestStatus(rs.getString("request_status"));
                    q.setBuyerName(rs.getString("buyer_name"));
                    list.add(q);
                }
            }
        }
        return list;
    }

    public int countPendingQuotes(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM quotes WHERE id_shop = ? AND status = 'pending'")) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /**
     * Gửi báo giá mới hoặc cập nhật báo giá cũ (đang chờ / đã rút) của shop.
     * Trả về null nếu thành công, ngược lại là thông báo lỗi để hiển thị.
     */
    public String saveQuote(int shopId, int ownerId, int requestId, long price, int days,
                            String note, String portfolio) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int buyerId;
                String status;
                // Khóa dòng yêu cầu để không báo giá vào lúc người mua đang chọn thợ
                try (PreparedStatement ps = c.prepareStatement("SELECT id_user, status FROM customrequests WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, requestId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) return fail(c, "Yêu cầu không tồn tại.");
                        buyerId = rs.getInt(1);
                        status = rs.getString(2);
                    }
                }
                if (buyerId == ownerId) return fail(c, "Bạn không thể báo giá cho yêu cầu của chính mình.");
                if (!"open".equals(status) && !"quoted".equals(status)) return fail(c, "Yêu cầu này không còn nhận báo giá.");

                int quoteId = 0;
                String quoteStatus = null;
                try (PreparedStatement ps = c.prepareStatement("SELECT id, status FROM quotes WHERE id_request = ? AND id_shop = ?")) {
                    ps.setInt(1, requestId);
                    ps.setInt(2, shopId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) { quoteId = rs.getInt(1); quoteStatus = rs.getString(2); }
                    }
                }

                if (quoteId > 0) {
                    if (!"pending".equals(quoteStatus) && !"withdrawn".equals(quoteStatus)) {
                        return fail(c, "Báo giá này đã được xử lý, không thể sửa.");
                    }
                    try (PreparedStatement ps = c.prepareStatement(
                            "UPDATE quotes SET price = ?, lead_time_days = ?, note = ?, portfolio = ?, status = 'pending' WHERE id = ?")) {
                        ps.setLong(1, price);
                        ps.setInt(2, days);
                        ps.setString(3, note);
                        ps.setString(4, portfolio);
                        ps.setInt(5, quoteId);
                        ps.executeUpdate();
                    }
                } else {
                    try (PreparedStatement ps = c.prepareStatement(
                            "INSERT INTO quotes (id_request, id_shop, price, lead_time_days, note, portfolio) VALUES (?,?,?,?,?,?)")) {
                        ps.setInt(1, requestId);
                        ps.setInt(2, shopId);
                        ps.setLong(3, price);
                        ps.setInt(4, days);
                        ps.setString(5, note);
                        ps.setString(6, portfolio);
                        ps.executeUpdate();
                    }
                }
                try (PreparedStatement ps = c.prepareStatement("UPDATE customrequests SET status = 'quoted' WHERE id = ? AND status = 'open'")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }
                c.commit();
                return null;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    /** Rút báo giá đang chờ. Nếu yêu cầu không còn báo giá nào đang chờ thì trả về trạng thái 'open'. */
    public boolean withdrawQuote(int quoteId, int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int requestId = 0;
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT id_request FROM quotes WHERE id = ? AND id_shop = ? AND status = 'pending' FOR UPDATE")) {
                    ps.setInt(1, quoteId);
                    ps.setInt(2, shopId);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) requestId = rs.getInt(1);
                    }
                }
                if (requestId == 0) { c.rollback(); return false; }

                try (PreparedStatement ps = c.prepareStatement("UPDATE quotes SET status = 'withdrawn' WHERE id = ?")) {
                    ps.setInt(1, quoteId);
                    ps.executeUpdate();
                }
                try (PreparedStatement ps = c.prepareStatement(
                        "UPDATE customrequests r SET r.status = 'open' WHERE r.id = ? AND r.status = 'quoted' " +
                                "AND NOT EXISTS (SELECT 1 FROM quotes q WHERE q.id_request = r.id AND q.status = 'pending')")) {
                    ps.setInt(1, requestId);
                    ps.executeUpdate();
                }
                c.commit();
                return true;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static String fail(Connection c, String message) throws SQLException {
        c.rollback();
        return message;
    }
}