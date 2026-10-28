package dao;

import dto.DailyRevenueDTO;
import dto.LedgerEntryDTO;
import dto.TopProductDTO;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Số liệu cho trang Tổng quan doanh thu của shop. */
public class SellerStatsDAO {

    private static final String NET = "SUM(oi.price * oi.quantity - oi.fee_amount)";
    /** Các trạng thái đơn được tính vào doanh thu (không gồm chưa thanh toán / hủy / hoàn tiền). */
    private static final String COUNTED = "('paid','processing','shipping','completed')";

    /** Kết quả lịch sử giao dịch đã lọc + phân trang. */
    public static class LedgerPage {
        public final List<LedgerEntryDTO> items;
        public final int total;
        public LedgerPage(List<LedgerEntryDTO> items, int total) { this.items = items; this.total = total; }
    }

    private static Timestamp startOf(LocalDate d) { return Timestamp.valueOf(d.atStartOfDay()); }
    /** Mốc loại trừ: 00:00 của ngày kế tiếp, để "đến ngày" bao gồm cả ngày cuối. */
    private static Timestamp endOf(LocalDate d)   { return Timestamp.valueOf(d.plusDays(1).atStartOfDay()); }

    // ===================== Thẻ thống kê =====================

    /** Số dư khả dụng = doanh thu thực nhận (sau phí sàn) của đơn đã hoàn thành − tiền đã/đang yêu cầu rút. */
    public long availableBalance(Connection c, int shopId) throws SQLException {
        long earned = scalar(c, "SELECT COALESCE(" + NET + ",0) FROM orderitems oi JOIN orders o ON o.id = oi.id_order " +
                "WHERE o.id_shop = ? AND o.status = 'completed'", shopId);
        long withdrawn = scalar(c, "SELECT COALESCE(SUM(amount),0) FROM withdrawals " +
                "WHERE id_shop = ? AND status IN ('pending','approved')", shopId);
        return earned - withdrawn;
    }

    public long availableBalance(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return availableBalance(c, shopId);
        }
    }

    public long revenueThisMonth(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return scalar(c, "SELECT COALESCE(SUM(oi.price * oi.quantity),0) FROM orderitems oi JOIN orders o ON o.id = oi.id_order " +
                    "WHERE o.id_shop = ? AND o.status = 'completed' " +
                    "AND YEAR(o.date_create) = YEAR(CURDATE()) AND MONTH(o.date_create) = MONTH(CURDATE())", shopId);
        }
    }

    /** Đơn chờ xử lý: chưa xác nhận hoặc đang chuẩn bị. */
    public int pendingOrders(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return (int) scalar(c, "SELECT COUNT(*) FROM orders WHERE id_shop = ? AND status IN ('pending','paid','processing')", shopId);
        }
    }

    /** Tổng tiền đang chờ duyệt rút. */
    public long pendingWithdrawAmount(int shopId) throws SQLException {
        try (Connection c = DBConnection.getConnection()) {
            return scalar(c, "SELECT COALESCE(SUM(amount),0) FROM withdrawals WHERE id_shop = ? AND status = 'pending'", shopId);
        }
    }

    // ===================== Theo khoảng ngày =====================

    /** Tổng doanh thu trong khoảng [from, to] (bao gồm cả hai đầu). */
    public long revenueBetween(int shopId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COALESCE(SUM(oi.price * oi.quantity),0) FROM orderitems oi JOIN orders o ON o.id = oi.id_order " +
                "WHERE o.id_shop = ? AND o.status IN " + COUNTED + " AND o.date_create >= ? AND o.date_create < ?";
        return rangeScalar(sql, shopId, from, to);
    }

    /** Số đơn trong khoảng [from, to]. */
    public int orderCountBetween(int shopId, LocalDate from, LocalDate to) throws SQLException {
        String sql = "SELECT COUNT(*) FROM orders o WHERE o.id_shop = ? AND o.status IN " + COUNTED +
                " AND o.date_create >= ? AND o.date_create < ?";
        return (int) rangeScalar(sql, shopId, from, to);
    }

    /**
     * Doanh thu theo từng mốc trong khoảng [from, to], đủ mốc kể cả 0 đồng.
     * monthly = false: mỗi ngày một cột (dd/MM). monthly = true: gộp theo tháng (MM/yyyy).
     */
    public List<DailyRevenueDTO> revenueByRange(int shopId, LocalDate from, LocalDate to, boolean monthly) throws SQLException {
        Map<LocalDate, Long> byDay = new HashMap<>();
        String sql = "SELECT DATE(o.date_create) AS d, SUM(oi.price * oi.quantity) AS total " +
                "FROM orderitems oi JOIN orders o ON o.id = oi.id_order " +
                "WHERE o.id_shop = ? AND o.status IN " + COUNTED + " " +
                "AND o.date_create >= ? AND o.date_create < ? GROUP BY DATE(o.date_create)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setTimestamp(2, startOf(from));
            ps.setTimestamp(3, endOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) byDay.put(rs.getDate("d").toLocalDate(), rs.getLong("total"));
            }
        }

        List<DailyRevenueDTO> list = new ArrayList<>();
        if (!monthly) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                DailyRevenueDTO r = new DailyRevenueDTO();
                r.setLabel(d.format(f));
                r.setAmount(byDay.getOrDefault(d, 0L));
                list.add(r);
            }
        } else {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("MM/yyyy");
            Map<LocalDate, Long> byMonth = new LinkedHashMap<>();
            for (LocalDate m = from.withDayOfMonth(1); !m.isAfter(to); m = m.plusMonths(1)) byMonth.put(m, 0L);
            for (Map.Entry<LocalDate, Long> e : byDay.entrySet()) byMonth.merge(e.getKey().withDayOfMonth(1), e.getValue(), Long::sum);
            for (Map.Entry<LocalDate, Long> e : byMonth.entrySet()) {
                DailyRevenueDTO r = new DailyRevenueDTO();
                r.setLabel(e.getKey().format(f));
                r.setAmount(e.getValue());
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Sản phẩm bán chạy trong khoảng [from, to].
     * sortBy: "revenue" = theo doanh thu, mặc định theo số lượng bán.
     * LƯU Ý: giả định orderitems.id_product và bảng products(id, name). Sửa lại nếu schema của bạn khác.
     */
    public List<TopProductDTO> topProducts(int shopId, LocalDate from, LocalDate to, String sortBy, int limit) throws SQLException {
        String order = "revenue".equals(sortBy) ? "revenue DESC, qty DESC" : "qty DESC, revenue DESC";
        String sql = "SELECT p.id, p.name, SUM(oi.quantity) AS qty, SUM(oi.price * oi.quantity) AS revenue, " +
                "COUNT(DISTINCT o.id) AS orders_cnt " +
                "FROM orderitems oi JOIN orders o ON o.id = oi.id_order JOIN products p ON p.id = oi.id_product " +
                "WHERE o.id_shop = ? AND o.status IN " + COUNTED + " AND o.date_create >= ? AND o.date_create < ? " +
                "GROUP BY p.id, p.name ORDER BY " + order + " LIMIT ?";
        List<TopProductDTO> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setTimestamp(2, startOf(from));
            ps.setTimestamp(3, endOf(to));
            ps.setInt(4, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TopProductDTO t = new TopProductDTO();
                    t.setId(rs.getInt("id"));
                    t.setName(rs.getString("name"));
                    t.setQuantity(rs.getLong("qty"));
                    t.setRevenue(rs.getLong("revenue"));
                    t.setOrderCount(rs.getInt("orders_cnt"));
                    list.add(t);
                }
            }
        }
        return list;
    }

    // ===================== Lịch sử giao dịch =====================

    /**
     * Lịch sử giao dịch: tiền bán hàng (+) của đơn hoàn thành và các lần rút tiền (−), mới nhất trước.
     *
     * @param from   lọc từ ngày (null = không giới hạn)
     * @param to     lọc đến ngày, gồm cả ngày này (null = không giới hạn)
     * @param type   "sale" | "withdraw" | rỗng = tất cả
     * @param status "success" | "pending" | "failed" | rỗng = tất cả
     * @param q      từ khóa theo mã GD hoặc tên ngân hàng (rỗng = bỏ qua)
     * @param page   trang bắt đầu từ 1
     */
    public LedgerPage ledger(int shopId, LocalDate from, LocalDate to, String type, String status,
                             String q, int page, int pageSize) throws SQLException {
        String union =
                "(SELECT CONCAT('#ORD-', o.id) AS code, o.date_create AS dt, 'sale' AS kind, " + NET + " AS amount, " +
                        "'success' AS status, '' AS bank " +
                        "FROM orders o JOIN orderitems oi ON oi.id_order = o.id " +
                        "WHERE o.id_shop = ? AND o.status = 'completed' GROUP BY o.id, o.date_create " +
                        "UNION ALL " +
                        "SELECT CONCAT('#WD-', w.id), w.date_create, 'withdraw', -w.amount, w.status, w.bank_name " +
                        "FROM withdrawals w WHERE w.id_shop = ?) t";

        StringBuilder where = new StringBuilder(" WHERE 1=1");
        List<Object> params = new ArrayList<>();
        params.add(shopId);
        params.add(shopId);

        if (from != null) { where.append(" AND t.dt >= ?"); params.add(startOf(from)); }
        if (to != null)   { where.append(" AND t.dt < ?");  params.add(endOf(to)); }
        if ("sale".equals(type) || "withdraw".equals(type)) { where.append(" AND t.kind = ?"); params.add(type); }
        if ("success".equals(status))      where.append(" AND t.status IN ('success','approved')");
        else if ("pending".equals(status)) where.append(" AND t.status = 'pending'");
        else if ("failed".equals(status))  where.append(" AND t.status NOT IN ('success','approved','pending')");
        if (q != null && !q.isBlank()) {
            where.append(" AND (t.code LIKE ? OR t.bank LIKE ?)");
            String like = "%" + q.trim() + "%";
            params.add(like);
            params.add(like);
        }

        List<LedgerEntryDTO> list = new ArrayList<>();
        int total;
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement("SELECT COUNT(*) FROM " + union + where)) {
                bind(ps, params);
                try (ResultSet rs = ps.executeQuery()) { total = rs.next() ? rs.getInt(1) : 0; }
            }
            List<Object> pageParams = new ArrayList<>(params);
            pageParams.add(pageSize);
            pageParams.add((Math.max(page, 1) - 1) * pageSize);
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT t.* FROM " + union + where + " ORDER BY t.dt DESC LIMIT ? OFFSET ?")) {
                bind(ps, pageParams);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String code = rs.getString("code");
                        LedgerEntryDTO e = new LedgerEntryDTO();
                        e.setCode(code);
                        e.setDate(rs.getTimestamp("dt"));
                        e.setAmount(rs.getLong("amount"));
                        e.setStatus(rs.getString("status")); // success | pending | approved | rejected
                        if ("sale".equals(rs.getString("kind"))) {
                            e.setType("Bán hàng (Đơn #" + code.substring(5) + ")");
                        } else {
                            e.setType("Rút tiền về " + rs.getString("bank"));
                        }
                        list.add(e);
                    }
                }
            }
        }
        return new LedgerPage(list, total);
    }

    // ===================== Helpers =====================

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
    }

    private long rangeScalar(String sql, int shopId, LocalDate from, LocalDate to) throws SQLException {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            ps.setTimestamp(2, startOf(from));
            ps.setTimestamp(3, endOf(to));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }

    private long scalar(Connection c, String sql, int shopId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0;
            }
        }
    }
}