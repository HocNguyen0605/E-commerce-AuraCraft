package dao;

import dto.ChartDataDTO;
import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class FinanceDAO {

    public ChartDataDTO getChartData(String startDate, String endDate) {
        ChartDataDTO dto = new ChartDataDTO();

        List<String> bLabels = new ArrayList<>();
        List<Double> bGmv    = new ArrayList<>();
        List<Double> bFee    = new ArrayList<>();
        List<String> pLabels = new ArrayList<>();
        List<Double> pData   = new ArrayList<>();

        boolean hasStart = (startDate != null && !startDate.isEmpty());
        boolean hasEnd   = (endDate   != null && !endDate.isEmpty());

        // ── Bar Chart ─────────────────────────────────────────────────────────
        // Dùng subquery để tránh nhân đôi o.amount khi JOIN 1-nhiều với orderitems
        // fee_sub tính SUM(fee_amount) trước theo id_order, rồi mới JOIN với orders
        StringBuilder barQuery = new StringBuilder(
            "SELECT DATE_FORMAT(o.date_create, '%Y-%m') AS month, " +
            "       SUM(o.amount)            AS gmv, " +
            "       SUM(fee_sub.total_fee)   AS fee " +
            "FROM orders o " +
            "JOIN ( " +
            "    SELECT id_order, SUM(fee_amount) AS total_fee " +
            "    FROM orderitems GROUP BY id_order " +
            ") fee_sub ON o.id = fee_sub.id_order " +
            "WHERE 1=1 "
        );
        if (hasStart) barQuery.append(" AND o.date_create >= ? ");
        if (hasEnd)   barQuery.append(" AND o.date_create <= ? ");
        barQuery.append(" GROUP BY month ORDER BY month");

        // ── Pie Chart ─────────────────────────────────────────────────────────
        // Chỉ tính orderitems có sản phẩm (id_product IS NOT NULL)
        StringBuilder pieQuery = new StringBuilder(
            "SELECT c.name AS category_name, SUM(oi.price * oi.quantity) AS revenue " +
            "FROM orderitems oi " +
            "JOIN products   p ON oi.id_product = p.id " +
            "JOIN categories c ON p.id_category = c.id " +
            "JOIN orders     o ON oi.id_order   = o.id " +
            "WHERE oi.id_product IS NOT NULL "
        );
        if (hasStart) pieQuery.append(" AND o.date_create >= ? ");
        if (hasEnd)   pieQuery.append(" AND o.date_create <= ? ");
        pieQuery.append(" GROUP BY c.name ORDER BY revenue DESC");

        // ── Finance Stats ─────────────────────────────────────────────────────
        // JOIN với subquery fee_sub (có cột total_fee) để tránh column không tồn tại
        StringBuilder statsQuery = new StringBuilder(
            "SELECT " +
            "    SUM(o.amount)                       AS totalGmv, " +
            "    SUM(fee_sub.total_fee)               AS totalFee, " +
            "    SUM(o.amount - fee_sub.total_fee)    AS totalPaid " +
            "FROM orders o " +
            "JOIN ( " +
            "    SELECT id_order, SUM(fee_amount) AS total_fee " +
            "    FROM orderitems GROUP BY id_order " +
            ") fee_sub ON o.id = fee_sub.id_order " +
            "WHERE 1=1 "
        );
        if (hasStart) statsQuery.append(" AND o.date_create >= ? ");
        if (hasEnd)   statsQuery.append(" AND o.date_create <= ? ");

        // ── Pending Balance ───────────────────────────────────────────────────
        // Chờ rút = tiền đã hoàn thành nhưng chưa payout (completed + processing + shipping)
        StringBuilder pendingQuery = new StringBuilder(
            "SELECT SUM(o.amount - fee_sub.total_fee) AS pendingBalance " +
            "FROM orders o " +
            "JOIN ( " +
            "    SELECT id_order, SUM(fee_amount) AS total_fee " +
            "    FROM orderitems GROUP BY id_order " +
            ") fee_sub ON o.id = fee_sub.id_order " +
            "WHERE o.status IN ('completed', 'processing', 'shipping') "
        );
        if (hasStart) pendingQuery.append(" AND o.date_create >= ? ");
        if (hasEnd)   pendingQuery.append(" AND o.date_create <= ? ");

        try (Connection conn = DBConnection.getConnection()) {

            // Bar Chart
            try (PreparedStatement ps = conn.prepareStatement(barQuery.toString())) {
                int idx = 1;
                if (hasStart) ps.setString(idx++, startDate + " 00:00:00");
                if (hasEnd)   ps.setString(idx++, endDate   + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        bLabels.add(rs.getString("month"));
                        bGmv.add(rs.getDouble("gmv") / 1_000_000.0);
                        bFee.add(rs.getDouble("fee") / 1_000_000.0);
                    }
                }
            }

            // Pie Chart
            try (PreparedStatement ps = conn.prepareStatement(pieQuery.toString())) {
                int idx = 1;
                if (hasStart) ps.setString(idx++, startDate + " 00:00:00");
                if (hasEnd)   ps.setString(idx++, endDate   + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        pLabels.add(rs.getString("category_name"));
                        pData.add(rs.getDouble("revenue"));
                    }
                }
            }

            // Finance Stats
            try (PreparedStatement ps = conn.prepareStatement(statsQuery.toString())) {
                int idx = 1;
                if (hasStart) ps.setString(idx++, startDate + " 00:00:00");
                if (hasEnd)   ps.setString(idx++, endDate   + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dto.setTotalGmv(rs.getDouble("totalGmv"));
                        dto.setTotalFee(rs.getDouble("totalFee"));
                        dto.setTotalPaidToSeller(rs.getDouble("totalPaid"));
                    }
                }
            }

            // Pending Balance
            try (PreparedStatement ps = conn.prepareStatement(pendingQuery.toString())) {
                int idx = 1;
                if (hasStart) ps.setString(idx++, startDate + " 00:00:00");
                if (hasEnd)   ps.setString(idx++, endDate   + " 23:59:59");
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dto.setTotalPendingBalance(rs.getDouble("pendingBalance"));
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("[FinanceDAO] Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
        }

        dto.setBarLabels(bLabels);
        dto.setBarGmvData(bGmv);
        dto.setBarFeeData(bFee);
        dto.setPieLabels(pLabels);
        dto.setPieData(pData);

        return dto;
    }
}
