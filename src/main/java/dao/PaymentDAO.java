package dao;

import util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Records demo payment decisions in the database; never calls an external gateway. */
public class PaymentDAO {
    public enum MockResult { SUCCESS, CANCELLED, UNKNOWN, NOT_PENDING }

    /** Confirms a demo payment for the signed-in buyer without contacting a payment gateway. */
    public MockResult confirmMockPayment(String reference, int userId) throws SQLException {
        if (reference == null || !reference.matches("[a-fA-F0-9]{32}")) return MockResult.UNKNOWN;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Integer> orderIds = new ArrayList<>();
                boolean allSettled = true;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT o.id,o.status,t.status AS txn_status FROM orders o " +
                                "JOIN transactions t ON t.id_order=o.id " +
                                "WHERE o.id_user=? AND t.gateway='vnpay' AND t.txn_ref LIKE ? FOR UPDATE")) {
                    query.setInt(1, userId); query.setString(2, reference + "-%");
                    try (ResultSet rs = query.executeQuery()) {
                        while (rs.next()) {
                            orderIds.add(rs.getInt("id"));
                            if (!"paid".equalsIgnoreCase(rs.getString("status")) ||
                                    !"success".equalsIgnoreCase(rs.getString("txn_status"))) allSettled = false;
                        }
                    }
                }
                if (orderIds.isEmpty()) { connection.rollback(); return MockResult.UNKNOWN; }
                if (allSettled) { connection.commit(); return MockResult.SUCCESS; }
                try (PreparedStatement tx = connection.prepareStatement(
                        "UPDATE transactions SET status='success',date=CURRENT_TIMESTAMP WHERE gateway='vnpay' AND txn_ref LIKE ? AND status='pending'")) {
                    tx.setString(1, reference + "-%");
                    if (tx.executeUpdate() != orderIds.size()) { connection.rollback(); return MockResult.NOT_PENDING; }
                }
                try (PreparedStatement orders = connection.prepareStatement(
                        "UPDATE orders SET status='paid',date_paid=CURRENT_TIMESTAMP WHERE id_user=? AND status='pending' AND id IN (" + placeholders(orderIds.size()) + ")")) {
                    orders.setInt(1, userId);
                    for (int i = 0; i < orderIds.size(); i++) orders.setInt(i + 2, orderIds.get(i));
                    if (orders.executeUpdate() != orderIds.size()) { connection.rollback(); return MockResult.NOT_PENDING; }
                }
                connection.commit();
                return MockResult.SUCCESS;
            } catch (SQLException | RuntimeException e) { connection.rollback(); throw e; }
            finally { connection.setAutoCommit(true); }
        }
    }

    /** Records a simulated 50% bank deposit while keeping the unpaid balance COD. */
    public MockResult confirmMockCodDeposit(String reference, int userId) throws SQLException {
        if (reference == null || !reference.matches("[a-fA-F0-9]{32}")) return MockResult.UNKNOWN;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Integer> orderIds = new ArrayList<>();
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT o.id FROM orders o JOIN transactions t ON t.id_order=o.id " +
                                "WHERE o.id_user=? AND t.gateway='cod' AND t.txn_ref LIKE ? " +
                                "AND o.status='pending' AND t.status='pending' FOR UPDATE")) {
                    query.setInt(1, userId); query.setString(2, reference + "-%");
                    try (ResultSet rs = query.executeQuery()) {
                        while (rs.next()) orderIds.add(rs.getInt("id"));
                    }
                }
                if (orderIds.isEmpty()) { connection.rollback(); return MockResult.UNKNOWN; }
                try (PreparedStatement tx = connection.prepareStatement(
                        "UPDATE transactions SET amount=ROUND(amount/2,0),status='success',date=CURRENT_TIMESTAMP " +
                                "WHERE gateway='cod' AND txn_ref LIKE ? AND status='pending'")) {
                    tx.setString(1, reference + "-%");
                    if (tx.executeUpdate() != orderIds.size()) { connection.rollback(); return MockResult.NOT_PENDING; }
                }
                connection.commit();
                return MockResult.SUCCESS;
            } catch (SQLException | RuntimeException e) { connection.rollback(); throw e; }
            finally { connection.setAutoCommit(true); }
        }
    }

    /** Cancels a pending demo checkout and restores its cart, stock, voucher quota, and redeemed coins. */
    public MockResult cancelMockPayment(String reference, int userId, String gateway) throws SQLException {
        if (reference == null || !reference.matches("[a-fA-F0-9]{32}")) return MockResult.UNKNOWN;
        if (!"vnpay".equals(gateway) && !"cod".equals(gateway)) return MockResult.UNKNOWN;
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Integer> orderIds = new ArrayList<>();
                List<int[]> items = new ArrayList<>();
                List<int[]> vouchers = new ArrayList<>();
                int coins = 0;
                boolean pendingOnly = true;
                try (PreparedStatement query = connection.prepareStatement(
                        "SELECT o.id,o.status,o.id_voucher,o.points_used FROM orders o " +
                                "JOIN transactions t ON t.id_order=o.id WHERE o.id_user=? AND t.gateway=? AND t.txn_ref LIKE ? FOR UPDATE")) {
                    query.setInt(1, userId); query.setString(2, gateway); query.setString(3, reference + "-%");
                    try (ResultSet rs = query.executeQuery()) {
                        while (rs.next()) {
                            int orderId = rs.getInt("id"); orderIds.add(orderId);
                            if (!"pending".equalsIgnoreCase(rs.getString("status"))) pendingOnly = false;
                            coins += rs.getInt("points_used");
                            int voucherId = rs.getInt("id_voucher");
                            if (!rs.wasNull()) vouchers.add(new int[]{voucherId});
                        }
                    }
                }
                if (orderIds.isEmpty()) { connection.rollback(); return MockResult.UNKNOWN; }
                if (!pendingOnly) { connection.rollback(); return MockResult.NOT_PENDING; }
                String ids = placeholders(orderIds.size());
                try (PreparedStatement queryItems = connection.prepareStatement(
                        "SELECT oi.id_product,oi.quantity FROM orderitems oi WHERE oi.id_order IN (" + ids + ") AND oi.id_product IS NOT NULL FOR UPDATE")) {
                    for (int i = 0; i < orderIds.size(); i++) queryItems.setInt(i + 1, orderIds.get(i));
                    try (ResultSet rs = queryItems.executeQuery()) {
                        while (rs.next()) items.add(new int[]{rs.getInt("id_product"), rs.getInt("quantity")});
                    }
                }
                for (int[] item : items) {
                    try (PreparedStatement stock = connection.prepareStatement("UPDATE products SET stock=stock+? WHERE id=?")) {
                        stock.setInt(1, item[1]); stock.setInt(2, item[0]); stock.executeUpdate();
                    }
                    try (PreparedStatement cart = connection.prepareStatement(
                            "INSERT INTO carts(id_user,id_product,quantity) VALUES(?,?,?) ON DUPLICATE KEY UPDATE quantity=quantity+VALUES(quantity)")) {
                        cart.setInt(1, userId); cart.setInt(2, item[0]); cart.setInt(3, item[1]); cart.executeUpdate();
                    }
                }
                if (coins > 0) {
                    try (PreparedStatement balance = connection.prepareStatement("UPDATE account SET loyaltyPoint=loyaltyPoint+? WHERE id_user=?")) {
                        balance.setInt(1, coins); balance.setInt(2, userId); balance.executeUpdate();
                    }
                    try (PreparedStatement ledger = connection.prepareStatement(
                            "INSERT INTO loyaltytransactions(id_user,id_order,points,type) VALUES(?,NULL,?,'adjust')")) {
                        ledger.setInt(1, userId); ledger.setInt(2, coins); ledger.executeUpdate();
                    }
                }
                for (int[] voucher : vouchers) {
                    try (PreparedStatement quota = connection.prepareStatement("UPDATE vouchers SET quota=quota+1 WHERE id=?")) {
                        quota.setInt(1, voucher[0]); quota.executeUpdate();
                    }
                }
                try (PreparedStatement deleteTransactions = connection.prepareStatement("DELETE FROM transactions WHERE id_order IN (" + ids + ")")) {
                    for (int i = 0; i < orderIds.size(); i++) deleteTransactions.setInt(i + 1, orderIds.get(i));
                    deleteTransactions.executeUpdate();
                }
                try (PreparedStatement deleteOrders = connection.prepareStatement("DELETE FROM orders WHERE id_user=? AND status='pending' AND id IN (" + ids + ")")) {
                    deleteOrders.setInt(1, userId);
                    for (int i = 0; i < orderIds.size(); i++) deleteOrders.setInt(i + 2, orderIds.get(i));
                    if (deleteOrders.executeUpdate() != orderIds.size()) { connection.rollback(); return MockResult.NOT_PENDING; }
                }
                connection.commit();
                return MockResult.CANCELLED;
            } catch (SQLException | RuntimeException e) { connection.rollback(); throw e; }
            finally { connection.setAutoCommit(true); }
        }
    }

    private String placeholders(int count) { return String.join(",", java.util.Collections.nCopies(count, "?")); }

}
