package dao;

import util.DBConnection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Creates an order from the signed-in buyer's selected database cart items. */
public class CheckoutDAO {
    private static final BigDecimal SHIPPING_FEE = new BigDecimal("25300");
    private static final BigDecimal COIN_VALUE = new BigDecimal("10");

    public Map<String, Object> previewBenefits(int userId, List<Integer> productIds,
                                                String voucherCode, boolean useCoins) throws SQLException {
        List<Map<String, Object>> items = findSelectedCartItems(userId, productIds);
        if (items.size() != productIds.size()) {
            throw new IllegalArgumentException("Sản phẩm trong giỏ đã thay đổi. Vui lòng tải lại trang.");
        }
        try (Connection connection = DBConnection.getConnection()) {
            connection.setAutoCommit(false);
            try {
                syncCompletedOrderRewards(connection, userId);
                Map<String, Object> result = calculateBenefits(connection, userId, items, voucherCode, useCoins, true);
                BigDecimal subtotal = BigDecimal.ZERO;
                java.util.Set<Integer> shops = new java.util.HashSet<>();
                for (Map<String, Object> item : items) {
                    subtotal = subtotal.add(((BigDecimal) item.get("price")).multiply(BigDecimal.valueOf((Integer) item.get("quantity"))));
                    shops.add((Integer) item.get("shopId"));
                }
                BigDecimal shipping = SHIPPING_FEE.multiply(BigDecimal.valueOf(shops.size()));
                boolean hasCustomItem = false;
                for (Map<String, Object> item : items) {
                    String name = (String) item.get("name");
                    if (name != null && name.toLowerCase().contains("custom")) {
                        hasCustomItem = true;
                        break;
                    }
                }
                result.put("hasCustomItem", hasCustomItem);
                result.put("subtotal", subtotal);
                result.put("shipping", shipping);
                result.put("total", subtotal.subtract((BigDecimal) result.get("discount")).add(shipping));
                result.put("pendingCoins", findPendingRewardCoins(connection, userId));
                connection.commit();
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    public List<Map<String, Object>> findAvailableVouchers(int userId, List<Integer> productIds) throws SQLException {
        List<Map<String, Object>> items = findSelectedCartItems(userId, productIds);
        Map<Integer, BigDecimal> shopTotals = new LinkedHashMap<>();
        for (Map<String, Object> item : items) {
            BigDecimal line = ((BigDecimal) item.get("price")).multiply(BigDecimal.valueOf((Integer) item.get("quantity")));
            shopTotals.merge((Integer) item.get("shopId"), line, BigDecimal::add);
        }
        String sql = "SELECT v.id,v.code,v.discount_type,v.value,v.min_order,v.end_date,v.id_shop " +
                "FROM vouchers v WHERE v.start_date<=CURRENT_TIMESTAMP AND v.end_date>=CURRENT_TIMESTAMP AND v.quota>0 " +
                "AND NOT EXISTS(SELECT 1 FROM voucherusage u WHERE u.id_voucher=v.id AND u.id_user=?) " +
                "ORDER BY v.end_date ASC,v.code ASC";
        List<Map<String, Object>> vouchers = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int shopId = rs.getInt("id_shop");
                    Integer voucherShop = rs.wasNull() ? null : shopId;
                    BigDecimal eligible = voucherShop == null ? shopTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                            : shopTotals.getOrDefault(voucherShop, BigDecimal.ZERO);
                    if (eligible.compareTo(rs.getBigDecimal("min_order")) < 0) continue;
                    String discount = "percent".equals(rs.getString("discount_type"))
                            ? rs.getBigDecimal("value").stripTrailingZeros().toPlainString() + "%"
                            : rs.getBigDecimal("value").stripTrailingZeros().toPlainString() + "đ";
                    Map<String, Object> voucher = new LinkedHashMap<>();
                    voucher.put("code", rs.getString("code"));
                    voucher.put("discountText", discount);
                    voucher.put("minOrder", rs.getBigDecimal("min_order"));
                    voucher.put("endDate", new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(rs.getTimestamp("end_date")));
                    vouchers.add(voucher);
                }
            }
        }
        return vouchers;
    }

    private Map<String, Object> calculateBenefits(Connection connection, int userId,
                                                    List<Map<String, Object>> items,
                                                    String voucherCode, boolean useCoins,
                                                    boolean lockRows) throws SQLException {
        Map<Integer, BigDecimal> shopTotals = new LinkedHashMap<>();
        for (Map<String, Object> item : items) {
            int shopId = (Integer) item.get("shopId");
            BigDecimal line = ((BigDecimal) item.get("price")).multiply(BigDecimal.valueOf((Integer) item.get("quantity")));
            shopTotals.merge(shopId, line, BigDecimal::add);
        }
        BigDecimal subtotal = shopTotals.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        int accountCoins;
        String accountSql = "SELECT loyaltyPoint FROM account WHERE id_user=?" + (lockRows ? " FOR UPDATE" : "");
        try (PreparedStatement statement = connection.prepareStatement(accountSql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy tài khoản tích xu.");
                accountCoins = rs.getInt("loyaltyPoint");
            }
        }
        int availableCoins = Math.max(0, accountCoins - findRewardDebt(connection, userId));

        int voucherId = 0;
        Integer voucherShopId = null;
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        Map<Integer, BigDecimal> voucherByShop = new LinkedHashMap<>();
        String code = voucherCode == null ? "" : voucherCode.trim().toUpperCase(java.util.Locale.ROOT);
        if (!code.isBlank()) {
            String voucherSql = "SELECT id,discount_type,value,min_order,quota,id_shop " +
                    "FROM vouchers WHERE code=? AND start_date<=CURRENT_TIMESTAMP AND end_date>=CURRENT_TIMESTAMP AND quota>0" +
                    (lockRows ? " FOR UPDATE" : "");
            try (PreparedStatement statement = connection.prepareStatement(voucherSql)) {
                statement.setString(1, code);
                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next()) throw new IllegalArgumentException("Voucher không tồn tại, chưa đến hạn, đã hết hạn hoặc hết lượt.");
                    voucherId = rs.getInt("id");
                    String type = rs.getString("discount_type");
                    BigDecimal value = rs.getBigDecimal("value");
                    BigDecimal minOrder = rs.getBigDecimal("min_order");
                    int quota = rs.getInt("quota");
                    int rawShopId = rs.getInt("id_shop");
                    voucherShopId = rs.wasNull() ? null : rawShopId;
                    if (quota < 1) {
                        throw new IllegalArgumentException("Voucher chưa đến hạn, đã hết hạn hoặc hết lượt sử dụng.");
                    }
                    try (PreparedStatement used = connection.prepareStatement(
                            "SELECT 1 FROM voucherusage WHERE id_voucher=? AND id_user=? LIMIT 1")) {
                        used.setInt(1, voucherId); used.setInt(2, userId);
                        try (ResultSet usedRs = used.executeQuery()) {
                            if (usedRs.next()) throw new IllegalArgumentException("Bạn đã sử dụng voucher này rồi.");
                        }
                    }
                    BigDecimal eligible = voucherShopId == null ? subtotal : shopTotals.getOrDefault(voucherShopId, BigDecimal.ZERO);
                    if (eligible.compareTo(minOrder) < 0) {
                        throw new IllegalArgumentException("Đơn chưa đạt giá trị tối thiểu của voucher.");
                    }
                    BigDecimal discount = "percent".equalsIgnoreCase(type)
                            ? eligible.multiply(value).divide(new BigDecimal("100"), 0, RoundingMode.DOWN)
                            : value;
                    discount = discount.min(eligible);
                    if (discount.signum() <= 0) {
                        throw new IllegalArgumentException("Giá trị đơn chưa đủ để voucher tạo ra mức giảm.");
                    }
                    if (voucherShopId == null && "fixed".equalsIgnoreCase(type)) {
                        BigDecimal remaining = discount;
                        for (Map.Entry<Integer, BigDecimal> entry : shopTotals.entrySet()) {
                            BigDecimal part = remaining.min(entry.getValue());
                            if (part.signum() > 0) voucherByShop.put(entry.getKey(), part);
                            remaining = remaining.subtract(part);
                        }
                    } else if (voucherShopId == null) {
                        for (Map.Entry<Integer, BigDecimal> entry : shopTotals.entrySet()) {
                            voucherByShop.put(entry.getKey(), entry.getValue().multiply(value)
                                    .divide(new BigDecimal("100"), 0, RoundingMode.DOWN));
                        }
                        BigDecimal allocated = voucherByShop.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                        BigDecimal remainder = discount.subtract(allocated);
                        for (Map.Entry<Integer, BigDecimal> entry : shopTotals.entrySet()) {
                            if (remainder.signum() <= 0) break;
                            BigDecimal room = entry.getValue().subtract(voucherByShop.getOrDefault(entry.getKey(), BigDecimal.ZERO));
                            BigDecimal part = room.min(remainder);
                            voucherByShop.merge(entry.getKey(), part, BigDecimal::add);
                            remainder = remainder.subtract(part);
                        }
                    } else {
                        voucherByShop.put(voucherShopId, discount);
                    }
                    voucherDiscount = discount;
                }
            }
        }

        BigDecimal afterVoucher = subtotal.subtract(voucherDiscount).max(BigDecimal.ZERO);
        int maxUsableCoins = afterVoucher.divide(COIN_VALUE, 0, RoundingMode.DOWN).intValue();
        int coinsUsed = useCoins ? Math.min(availableCoins, maxUsableCoins) : 0;
        BigDecimal coinDiscount = COIN_VALUE.multiply(BigDecimal.valueOf(coinsUsed));
        Map<Integer, BigDecimal> coinsByShopDiscount = new LinkedHashMap<>();
        BigDecimal remainingCoinsDiscount = coinDiscount;
        for (Map.Entry<Integer, BigDecimal> entry : shopTotals.entrySet()) {
            BigDecimal shopAfterVoucher = entry.getValue().subtract(voucherByShop.getOrDefault(entry.getKey(), BigDecimal.ZERO));
            BigDecimal part = remainingCoinsDiscount.min(shopAfterVoucher);
            if (part.signum() > 0) coinsByShopDiscount.put(entry.getKey(), part);
            remainingCoinsDiscount = remainingCoinsDiscount.subtract(part);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("voucherId", voucherId);
        result.put("voucherShopId", voucherShopId);
        result.put("voucherDiscount", voucherDiscount);
        result.put("voucherByShop", voucherByShop);
        result.put("coinsByShopDiscount", coinsByShopDiscount);
        result.put("coinDiscount", coinDiscount);
        result.put("coinsUsed", coinsUsed);
        result.put("coinsAvailableToUse", maxUsableCoins);
        result.put("availableCoins", availableCoins);
        result.put("remainingCoins", availableCoins - coinsUsed);
        result.put("discount", voucherDiscount.add(coinDiscount));
        return result;
    }

    /** Reconciles order reward ledgers with the current accrual rule and each order's completion state. */
    private void syncCompletedOrderRewards(Connection connection, int userId) throws SQLException {
        try (PreparedStatement balanceLock = connection.prepareStatement(
                "SELECT loyaltyPoint FROM account WHERE id_user=? FOR UPDATE")) {
            balanceLock.setInt(1, userId);
            try (ResultSet rs = balanceLock.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Không tìm thấy tài khoản tích xu.");
            }
        }
        String sql = "SELECT o.id, " +
                "FLOOR(GREATEST(o.amount-o.shipping_fee,0)/10000) - COALESCE(SUM(lt.points),0) AS delta " +
                "FROM orders o " +
                "LEFT JOIN loyaltytransactions lt ON lt.id_order=o.id AND lt.id_user=o.id_user AND lt.type IN ('earn','adjust') " +
                "WHERE o.id_user=? AND o.status='completed' " +
                "GROUP BY o.id, o.amount, o.shipping_fee " +
                "HAVING delta <> 0";
        List<int[]> deltas = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    deltas.add(new int[]{rs.getInt("id"), rs.getInt("delta")});
                }
            }
        }
        for (int[] delta : deltas) {
            applyOrderRewardDelta(connection, userId, delta[0], delta[1]);
        }
    }

    private int getOrderRewardLedgerBalance(Connection connection, int userId, int orderId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(SUM(points),0) FROM loyaltytransactions WHERE id_user=? AND id_order=? AND type IN ('earn','adjust')")) {
            statement.setInt(1, userId); statement.setInt(2, orderId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private void applyOrderRewardDelta(Connection connection, int userId, int orderId, int delta) throws SQLException {
        if (delta == 0) return;
        if (delta > 0) {
            try (PreparedStatement balance = connection.prepareStatement(
                    "UPDATE account SET loyaltyPoint=loyaltyPoint+? WHERE id_user=?")) {
                balance.setInt(1, delta); balance.setInt(2, userId);
                if (balance.executeUpdate() != 1) throw new SQLException("Không tìm thấy số dư xu của người mua.");
            }
            insertOrderRewardLedger(connection, userId, orderId, delta, "earn");
        } else {
            // Negative adjustments track points that must be recovered after refunds or reward corrections.
            insertOrderRewardLedger(connection, userId, orderId, delta, "adjust");
        }
    }

    private void insertOrderRewardLedger(Connection connection, int userId, int orderId, int points, String type) throws SQLException {
        try (PreparedStatement ledger = connection.prepareStatement(
                "INSERT INTO loyaltytransactions(id_user,id_order,points,type) VALUES(?,?,?,?)")) {
            ledger.setInt(1, userId); ledger.setInt(2, orderId); ledger.setInt(3, points); ledger.setString(4, type);
            ledger.executeUpdate();
        }
    }

    private int findRewardDebt(Connection connection, int userId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT COALESCE(SUM(-points),0) FROM loyaltytransactions WHERE id_user=? AND type='adjust' AND points<0")) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    private int findPendingRewardCoins(Connection connection, int userId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(FLOOR(GREATEST(amount-shipping_fee,0)/10000)),0) " +
                "FROM orders WHERE id_user=? AND status NOT IN ('completed','cancelled','refunded')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Map<String, String> findDefaultAddress(int userId) throws SQLException {
        String sql = "SELECT receiver,phone,address FROM addresses WHERE id_user=? ORDER BY is_default DESC,id DESC LIMIT 1";
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return Map.of();
                Map<String, String> address = new LinkedHashMap<>();
                address.put("receiver", rs.getString("receiver"));
                address.put("phone", rs.getString("phone"));
                address.put("address", rs.getString("address"));
                return address;
            }
        }
    }

    public List<Map<String, Object>> findSelectedCartItems(int userId, List<Integer> productIds) throws SQLException {
        if (productIds == null || productIds.isEmpty()) return List.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(productIds.size(), "?"));
        String sql = "SELECT p.id,p.name,p.price,p.stock,c.quantity,p.id_shop,p.id_category,cat.name category_name,s.name shop_name, " +
                "pi.url image FROM carts c JOIN products p ON p.id=c.id_product " +
                "JOIN categories cat ON cat.id=p.id_category JOIN shops s ON s.id=p.id_shop " +
                "LEFT JOIN productimages pi ON pi.id=(SELECT MIN(pi2.id) FROM productimages pi2 WHERE pi2.id_product=p.id) " +
                "WHERE c.id_user=? AND p.id IN (" + placeholders + ") AND p.status='active' AND s.status='active' " +
                "ORDER BY p.id_shop,p.id";
        List<Map<String, Object>> items = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getInt("id")); item.put("name", rs.getString("name"));
                    BigDecimal price = rs.getBigDecimal("price");
                    int quantity = rs.getInt("quantity");
                    item.put("price", price); item.put("lineTotal", price.multiply(BigDecimal.valueOf(quantity)));
                    item.put("stock", rs.getInt("stock"));
                    item.put("quantity", quantity); item.put("shopId", rs.getInt("id_shop"));
                    item.put("categoryId", rs.getInt("id_category"));
                    item.put("categoryName", rs.getString("category_name")); item.put("shopName", rs.getString("shop_name"));
                    item.put("image", rs.getString("image"));
                    items.add(item);
                }
            }
        }
        return items;
    }

    public Map<String, Object> placeOrder(int userId, List<Integer> productIds,
                                          String receiver, String phone, String address,
                                          String note, String paymentMethod,
                                          String voucherCode, boolean useCoins, String paymentReference) throws SQLException {
        if (productIds == null || productIds.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn sản phẩm cần đặt.");
        String placeholders = String.join(",", java.util.Collections.nCopies(productIds.size(), "?"));
        try (Connection connection = DBConnection.getConnection()) {
            // Older local databases may not yet have the split recipient columns.
            // Add nullable columns before opening the order transaction so DDL cannot commit it implicitly.
            connection.setAutoCommit(true);
            ensureShippingColumns(connection);
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement findAddress = connection.prepareStatement(
                        "SELECT id FROM addresses WHERE id_user=? AND receiver=? AND phone=? AND address=? LIMIT 1")) {
                    findAddress.setInt(1, userId); findAddress.setString(2, receiver);
                    findAddress.setString(3, phone); findAddress.setString(4, address);
                    try (ResultSet rs = findAddress.executeQuery()) {
                        int addressId = rs.next() ? rs.getInt("id") : 0;
                        try (PreparedStatement clearDefault = connection.prepareStatement(
                                "UPDATE addresses SET is_default=0 WHERE id_user=? AND is_default=1")) {
                            clearDefault.setInt(1, userId); clearDefault.executeUpdate();
                        }
                        if (addressId > 0) {
                            try (PreparedStatement setDefault = connection.prepareStatement(
                                    "UPDATE addresses SET is_default=1 WHERE id=? AND id_user=?")) {
                                setDefault.setInt(1, addressId); setDefault.setInt(2, userId); setDefault.executeUpdate();
                            }
                        } else {
                            try (PreparedStatement saveAddress = connection.prepareStatement(
                                    "INSERT INTO addresses(id_user,receiver,phone,address,is_default) VALUES(?,?,?,?,1)")) {
                                saveAddress.setInt(1, userId); saveAddress.setString(2, receiver);
                                saveAddress.setString(3, phone); saveAddress.setString(4, address);
                                saveAddress.executeUpdate();
                            }
                        }
                    }
                }
                String query = "SELECT p.id,p.name,p.price,p.stock,c.quantity,p.id_shop " +
                        "FROM carts c JOIN products p ON p.id=c.id_product " +
                        "JOIN shops s ON s.id=p.id_shop " +
                        "WHERE c.id_user=? AND p.id IN (" + placeholders + ") " +
                        "AND p.status='active' AND s.status='active' ORDER BY p.id_shop,p.id FOR UPDATE";
                List<Map<String, Object>> items = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(query)) {
                    statement.setInt(1, userId);
                    for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
                    try (ResultSet rs = statement.executeQuery()) {
                        while (rs.next()) {
                            int quantity = rs.getInt("quantity");
                            int stock = rs.getInt("stock");
                            if (quantity < 1 || quantity > stock) {
                                throw new IllegalArgumentException("Số lượng sản phẩm " + rs.getString("name") + " đã thay đổi. Vui lòng kiểm tra lại giỏ hàng.");
                            }
                            Map<String, Object> item = new LinkedHashMap<>();
                            item.put("id", rs.getInt("id")); item.put("shopId", rs.getInt("id_shop"));
                            item.put("name", rs.getString("name")); item.put("price", rs.getBigDecimal("price"));
                            item.put("quantity", quantity);
                            items.add(item);
                        }
                    }
                }
                if (items.size() != productIds.size()) {
                    throw new IllegalArgumentException("Có sản phẩm không còn trong giỏ hoặc đã ngừng bán. Vui lòng tải lại giỏ hàng.");
                }

                syncCompletedOrderRewards(connection, userId);
                Map<String, Object> benefits = calculateBenefits(connection, userId, items, voucherCode, useCoins, true);
                int paymentMethodId = findPaymentMethod(connection, paymentMethod);
                Map<Integer, List<Map<String, Object>>> byShop = new LinkedHashMap<>();
                for (Map<String, Object> item : items) {
                    byShop.computeIfAbsent((Integer) item.get("shopId"), ignored -> new ArrayList<>()).add(item);
                }
                List<Integer> orderIds = new ArrayList<>();
                BigDecimal subtotal = BigDecimal.ZERO;
                int rewardCoinsPending = 0;
                for (Map.Entry<Integer, List<Map<String, Object>>> entry : byShop.entrySet()) {
                    BigDecimal shopSubtotal = BigDecimal.ZERO;
                    for (Map<String, Object> item : entry.getValue()) {
                        BigDecimal price = (BigDecimal) item.get("price");
                        int quantity = (Integer) item.get("quantity");
                        shopSubtotal = shopSubtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
                    }
                    int shopId = entry.getKey();
                    BigDecimal voucherPart = ((Map<Integer, BigDecimal>) benefits.get("voucherByShop"))
                            .getOrDefault(shopId, BigDecimal.ZERO);
                    BigDecimal coinPart = ((Map<Integer, BigDecimal>) benefits.get("coinsByShopDiscount"))
                            .getOrDefault(shopId, BigDecimal.ZERO);
                    BigDecimal shopDiscount = voucherPart.add(coinPart);
                    BigDecimal netShopGoods = shopSubtotal.subtract(shopDiscount).max(BigDecimal.ZERO);
                    BigDecimal orderTotal = netShopGoods.add(SHIPPING_FEE);
                    rewardCoinsPending += netShopGoods.divide(new BigDecimal("10000"), 0, RoundingMode.DOWN).intValue();
                    int pointsUsedForShop = coinPart.divide(COIN_VALUE, 0, RoundingMode.DOWN).intValue();
                    Integer voucherShopId = (Integer) benefits.get("voucherShopId");
                    int voucherId = (Integer) benefits.get("voucherId");
                    boolean voucherAppliedHere = voucherId > 0 && voucherPart.signum() > 0;
                    int orderId;
                    String insertOrder = "INSERT INTO orders(id_user,id_payment_method,id_shop,amount,shipping_fee,discount,id_voucher,points_used,shipping_address,shipping_receiver,shipping_phone,shipping_note,status) " +
                            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,'pending')";
                    try (PreparedStatement statement = connection.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setInt(1, userId); statement.setInt(2, paymentMethodId); statement.setInt(3, shopId);
                        statement.setBigDecimal(4, orderTotal); statement.setBigDecimal(5, SHIPPING_FEE);
                        statement.setBigDecimal(6, shopDiscount);
                        if (voucherAppliedHere && (voucherShopId == null || voucherShopId == shopId)) statement.setInt(7, voucherId);
                        else statement.setNull(7, Types.INTEGER);
                        statement.setInt(8, pointsUsedForShop);
                        statement.setString(9, address);
                        statement.setString(10, receiver);
                        statement.setString(11, phone);
                        if (note.isBlank()) statement.setNull(12, Types.VARCHAR);
                        else statement.setString(12, note);
                        statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) throw new SQLException("Không nhận được mã đơn hàng.");
                            orderId = keys.getInt(1);
                        }
                    }
                    orderIds.add(orderId);
                    String gateway = "VNPay".equalsIgnoreCase(paymentMethod) ? "vnpay" : "cod";
                    String transactionReference = paymentReference + "-" + orderId;
                    try (PreparedStatement transaction = connection.prepareStatement(
                            "INSERT INTO transactions(id_order,gateway,txn_ref,amount,status) VALUES(?,?,?,?, 'pending')")) {
                        transaction.setInt(1, orderId);
                        transaction.setString(2, gateway);
                        transaction.setString(3, transactionReference);
                        transaction.setBigDecimal(4, orderTotal);
                        transaction.executeUpdate();
                    }
                    if (pointsUsedForShop > 0) {
                        try (PreparedStatement ledger = connection.prepareStatement(
                                "INSERT INTO loyaltytransactions(id_user,id_order,points,type) VALUES(?,?,?,'redeem')")) {
                            ledger.setInt(1, userId);
                            ledger.setInt(2, orderId);
                            ledger.setInt(3, -pointsUsedForShop);
                            ledger.executeUpdate();
                        }
                    }
                    if (voucherAppliedHere && (voucherShopId == null || voucherShopId == shopId)) {
                        try (PreparedStatement usage = connection.prepareStatement(
                                "INSERT INTO voucherusage(id_voucher,id_user,id_order) VALUES(?,?,?)")) {
                            usage.setInt(1, voucherId); usage.setInt(2, userId); usage.setInt(3, orderId);
                            usage.executeUpdate();
                        }
                    }
                    for (Map<String, Object> item : entry.getValue()) {
                        int productId = (Integer) item.get("id");
                        int quantity = (Integer) item.get("quantity");
                        try (PreparedStatement statement = connection.prepareStatement(
                                "INSERT INTO orderitems(id_order,id_product,quantity,price,fee_amount) VALUES(?,?,?, ?,0)")) {
                            statement.setInt(1, orderId); statement.setInt(2, productId);
                            statement.setInt(3, quantity); statement.setBigDecimal(4, (BigDecimal) item.get("price"));
                            statement.executeUpdate();
                        }
                        try (PreparedStatement statement = connection.prepareStatement(
                                "UPDATE products SET stock=stock-? WHERE id=? AND stock>=?")) {
                            statement.setInt(1, quantity); statement.setInt(2, productId); statement.setInt(3, quantity);
                            if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Tồn kho vừa thay đổi. Vui lòng thử đặt hàng lại.");
                        }
                    }
                    subtotal = subtotal.add(shopSubtotal);
                }
                try (PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM carts WHERE id_user=? AND id_product IN (" + placeholders + ")")) {
                    statement.setInt(1, userId);
                    for (int i = 0; i < productIds.size(); i++) statement.setInt(i + 2, productIds.get(i));
                    statement.executeUpdate();
                }
                int coinsUsed = (Integer) benefits.get("coinsUsed");
                if (coinsUsed > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE account SET loyaltyPoint=loyaltyPoint-? WHERE id_user=? AND loyaltyPoint>=?")) {
                        statement.setInt(1, coinsUsed); statement.setInt(2, userId); statement.setInt(3, coinsUsed);
                        if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Số xu đã thay đổi. Vui lòng tải lại trang.");
                    }
                }
                int voucherId = (Integer) benefits.get("voucherId");
                if (voucherId > 0) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE vouchers SET quota=quota-1 WHERE id=? AND quota>0")) {
                        statement.setInt(1, voucherId);
                        if (statement.executeUpdate() != 1) throw new IllegalArgumentException("Voucher vừa hết lượt sử dụng. Vui lòng chọn mã khác.");
                    }
                }
                connection.commit();
                Map<String, Object> result = new LinkedHashMap<>();
                result.put("orderIds", orderIds); result.put("orderId", orderIds.get(0));
                result.put("paymentReference", paymentReference);
                result.put("subtotal", subtotal); result.put("shipping", SHIPPING_FEE.multiply(BigDecimal.valueOf(orderIds.size())));
                result.put("voucherDiscount", benefits.get("voucherDiscount"));
                result.put("coinDiscount", benefits.get("coinDiscount"));
                result.put("discount", benefits.get("discount"));
                result.put("coinsUsed", benefits.get("coinsUsed"));
                result.put("remainingCoins", benefits.get("remainingCoins"));
                result.put("rewardCoinsPending", rewardCoinsPending);
                result.put("total", subtotal.subtract((BigDecimal) benefits.get("discount"))
                        .add(SHIPPING_FEE.multiply(BigDecimal.valueOf(orderIds.size()))));
                boolean hasCustomItem = false;
                for (Map<String, Object> item : items) {
                    String name = (String) item.get("name");
                    if (name != null && name.toLowerCase().contains("custom")) {
                        hasCustomItem = true;
                        break;
                    }
                }
                result.put("hasCustomItem", hasCustomItem);
                result.put("itemCount", items.stream().mapToInt(item -> (Integer) item.get("quantity")).sum());
                return result;
            } catch (SQLException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    private static volatile boolean shippingColumnsEnsured = false;

    private void ensureShippingColumns(Connection connection) throws SQLException {
        if (shippingColumnsEnsured) return;
        boolean migrateLegacyShipping = !hasOrderColumn(connection, "shipping_receiver")
                || !hasOrderColumn(connection, "shipping_phone")
                || !hasOrderColumn(connection, "shipping_note");
        ensureOrderColumn(connection, "shipping_receiver",
                "ALTER TABLE `orders` ADD COLUMN `shipping_receiver` varchar(100) DEFAULT NULL");
        ensureOrderColumn(connection, "shipping_phone",
                "ALTER TABLE `orders` ADD COLUMN `shipping_phone` varchar(20) DEFAULT NULL");
        ensureOrderColumn(connection, "shipping_note",
                "ALTER TABLE `orders` ADD COLUMN `shipping_note` varchar(500) DEFAULT NULL");

        // Keep historical orders readable on the one checkout that upgrades an old schema.
        // Running this table-wide migration on every checkout caused increasingly slow payments.
        if (migrateLegacyShipping) {
            try (PreparedStatement migrate = connection.prepareStatement(
                    "UPDATE orders SET shipping_receiver=SUBSTRING_INDEX(SUBSTRING_INDEX(shipping_address,' | SĐT: ',1),'Người nhận: ',-1)," +
                            "shipping_phone=SUBSTRING_INDEX(SUBSTRING_INDEX(shipping_address,' | Địa chỉ: ',1),' | SĐT: ',-1)," +
                            "shipping_note=CASE WHEN shipping_address LIKE '% | Ghi chú: %' THEN SUBSTRING_INDEX(shipping_address,' | Ghi chú: ',-1) ELSE NULL END," +
                            "shipping_address=TRIM(SUBSTRING_INDEX(SUBSTRING_INDEX(shipping_address,' | Ghi chú: ',1),' | Địa chỉ: ',-1)) " +
                            "WHERE shipping_address LIKE 'Người nhận: % | SĐT: % | Địa chỉ: %'")) {
                migrate.executeUpdate();
            }
        }
        ensureTxnRefIndex(connection);
        shippingColumnsEnsured = true;
    }

    private void ensureTxnRefIndex(Connection connection) {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate("CREATE INDEX IF NOT EXISTS idx_txn_ref ON transactions(txn_ref)");
        } catch (SQLException ignored) {
        }
    }

    private void ensureOrderColumn(Connection connection, String column, String alterSql) throws SQLException {
        if (hasOrderColumn(connection, column)) return;
        try (PreparedStatement alter = connection.prepareStatement(alterSql)) {
            alter.executeUpdate();
        } catch (SQLException exception) {
            // If another checkout request added the same column concurrently, proceed normally.
            if (exception.getErrorCode() != 1060 && !hasOrderColumn(connection, column)) throw exception;
        }
    }

    private boolean hasOrderColumn(Connection connection, String column) throws SQLException {
        try (ResultSet columns = connection.getMetaData().getColumns(connection.getCatalog(), null, "orders", null)) {
            while (columns.next()) {
                if (column.equalsIgnoreCase(columns.getString("COLUMN_NAME"))) return true;
            }
        }
        return false;
    }

    private int findPaymentMethod(Connection connection, String method) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT id FROM paymentmethods WHERE name=?")) {
            statement.setString(1, method);
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("Phương thức thanh toán không hợp lệ.");
                return rs.getInt(1);
            }
        }
    }
}
