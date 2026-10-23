package dao;

import dto.Withdrawal;
import util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WithdrawalDAO {
    private final SellerStatsDAO stats = new SellerStatsDAO();

    /**
     * Tạo yêu cầu rút tiền. Khóa dòng shop để hai yêu cầu cùng lúc không vượt quá số dư.
     * @return null nếu thành công, ngược lại là thông báo lỗi.
     */
    public String create(int shopId, long amount, String bankName, String bankAccount, String holder) throws SQLException {
        if (amount <= 0) return "Số tiền rút phải lớn hơn 0.";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement ps = c.prepareStatement("SELECT id FROM shops WHERE id = ? FOR UPDATE")) {
                    ps.setInt(1, shopId);
                    ps.executeQuery().close();
                }
                long balance = stats.availableBalance(c, shopId);
                if (amount > balance) { c.rollback(); return "Số dư khả dụng không đủ."; }
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO withdrawals (id_shop, amount, bank_name, bank_account, account_holder) VALUES (?,?,?,?,?)")) {
                    ps.setInt(1, shopId);
                    ps.setLong(2, amount);
                    ps.setString(3, bankName);
                    ps.setString(4, bankAccount);
                    ps.setString(5, holder);
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

    public List<Withdrawal> findByShop(int shopId) throws SQLException {
        List<Withdrawal> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM withdrawals WHERE id_shop = ? ORDER BY id DESC")) {
            ps.setInt(1, shopId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Withdrawal w = new Withdrawal();
                    w.setId(rs.getInt("id"));
                    w.setShopId(shopId);
                    w.setAmount(rs.getLong("amount"));
                    w.setBankName(rs.getString("bank_name"));
                    w.setBankAccount(rs.getString("bank_account"));
                    w.setAccountHolder(rs.getString("account_holder"));
                    w.setStatus(rs.getString("status"));
                    w.setDateCreate(rs.getTimestamp("date_create"));
                    list.add(w);
                }
            }
        }
        return list;
    }
}
