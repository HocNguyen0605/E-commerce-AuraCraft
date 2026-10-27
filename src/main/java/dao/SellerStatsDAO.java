package dao;

import dto.*;
import util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** Số liệu cho trang Tổng quan doanh thu của shop. */
public class SellerStatsDAO {

    private static final String NET = "SUM(oi.price * oi.quantity - oi.fee_amount)";

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

    /** Doanh thu 7 ngày gần nhất (đơn không bị hủy/hoàn tiền/chưa thanh toán), đủ 7 mốc kể cả ngày 0 đồng. */
    public List<DailyRevenueDTO> last7Days(int shopId) throws SQLException {
        Map<LocalDate, Long> byDay = new HashMap<>();
        String sql = "SELECT DATE(o.date_create) AS d, SUM(oi.price * oi.quantity) AS total " +
                "FROM orderitems oi JOIN orders o ON o.id = oi.id_order " +
                "WHERE o.id_shop = ? AND o.status IN ('paid','processing','shipping','completed') " +
                "AND o.date_create >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) GROUP BY DATE(o.date_create)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) byDay.put(rs.getDate("d").toLocalDate(), rs.getLong("total"));
            }
        }
        List<DailyRevenueDTO> list = new ArrayList<>();
        LocalDate today = LocalDate.now();
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM");
        for (int i = 6; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            DailyRevenueDTO r = new DailyRevenueDTO();
            r.setLabel(d.format(f));
            r.setAmount(byDay.getOrDefault(d, 0L));
            list.add(r);
        }
        return list;
    }

    /** Lịch sử giao dịch: tiền bán hàng (+) của đơn hoàn thành và các lần rút tiền (−), mới nhất trước. */
    public List<LedgerEntryDTO> ledger(int shopId, int limit) throws SQLException {
        List<LedgerEntryDTO> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT o.id, o.date_create, " + NET + " AS net FROM orders o JOIN orderitems oi ON oi.id_order = o.id " +
                            "WHERE o.id_shop = ? AND o.status = 'completed' GROUP BY o.id, o.date_create")) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LedgerEntryDTO e = new LedgerEntryDTO();
                        e.setCode("#ORD-" + rs.getInt("id"));
                        e.setDate(rs.getTimestamp("date_create"));
                        e.setType("Bán hàng (Đơn #" + rs.getInt("id") + ")");
                        e.setAmount(rs.getLong("net"));
                        e.setStatus("success");
                        list.add(e);
                    }
                }
            }
            try (PreparedStatement ps = c.prepareStatement(
                    "SELECT id, amount, bank_name, status, date_create FROM withdrawals WHERE id_shop = ?")) {
                ps.setInt(1, shopId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        LedgerEntryDTO e = new LedgerEntryDTO();
                        e.setCode("#WD-" + rs.getInt("id"));
                        e.setDate(rs.getTimestamp("date_create"));
                        e.setType("Rút tiền về " + rs.getString("bank_name"));
                        e.setAmount(-rs.getLong("amount"));
                        e.setStatus(rs.getString("status")); // pending | approved | rejected
                        list.add(e);
                    }
                }
            }
        }
        list.sort((a, b) -> b.getDate().compareTo(a.getDate()));
        return list.size() > limit ? new ArrayList<>(list.subList(0, limit)) : list;
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