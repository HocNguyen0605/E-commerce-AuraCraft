package controller.vnp;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.DBConnection;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Tạo URL thanh toán VNPay Sandbox cho các đơn ĐÃ được tạo (status = pending) bởi /pages/checkout.
 * JS gọi: GET /payment/vnpay?paymentReference=...&orderIds=1,2
 * Số tiền lấy từ DB, không tin số tiền từ trình duyệt.
 */
@WebServlet("/payment/vnpay")
public class VNPayServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Object id = session == null ? null : session.getAttribute("userId");
        Object role = session == null ? null : session.getAttribute("role");
        if (!(id instanceof Number userId) || !"buyer".equals(role)) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.jsp");
            return;
        }

        String reference = req.getParameter("paymentReference");
        List<Integer> orderIds = parseIds(req.getParameter("orderIds"));
        if (reference == null || reference.isBlank() || orderIds.isEmpty()) {
            fail(req, resp, "invalid");
            return;
        }

        long amountVnd;
        try {
            amountVnd = totalPendingAmount(userId.intValue(), orderIds, reference);
        } catch (SQLException e) {
            getServletContext().log("VNPay: không đọc được số tiền đơn hàng", e);
            fail(req, resp, "error");
            return;
        }
        if (amountVnd <= 0) {
            fail(req, resp, "notfound");
            return;
        }

        // Ngày giờ theo múi giờ Việt Nam, hết hạn sau 15 phút
        TimeZone vn = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHHmmss");
        fmt.setTimeZone(vn);
        Calendar cal = Calendar.getInstance(vn);
        String createDate = fmt.format(cal.getTime());
        cal.add(Calendar.MINUTE, 15);
        String expireDate = fmt.format(cal.getTime());

        // localhost hay trả IPv6 (0:0:0:0:0:0:0:1) -> VNPay từ chối, đổi sang IPv4
        String ip = req.getRemoteAddr();
        if (ip == null || ip.contains(":")) ip = "127.0.0.1";

        // vnp_TxnRef = paymentReference + "_" + thời gian (để mỗi lần thanh toán là duy nhất)
        String txnRef = reference + "_" + System.currentTimeMillis();

        Map<String, String> params = new TreeMap<>();   // TreeMap = tự sắp xếp theo tên, đúng yêu cầu VNPay
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", VNPayConfig.vnp_TmnCode);
        params.put("vnp_Amount", String.valueOf(amountVnd * 100));
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", "Thanh toan don hang AuraCraft");
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", VNPayConfig.vnp_ReturnUrl);
        params.put("vnp_IpAddr", ip);
        params.put("vnp_CreateDate", createDate);
        params.put("vnp_ExpireDate", expireDate);

        StringBuilder query = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (query.length() > 0) query.append('&');
            query.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        String secureHash = new HmacSHA512().encrypt(VNPayConfig.vnp_HashSecret, query.toString());
        resp.sendRedirect(VNPayConfig.vnp_PayUrl + "?" + query + "&vnp_SecureHash=" + secureHash);
    }

    /** Tổng tiền các đơn pending của đúng người dùng này. */
    private long totalPendingAmount(int userId, List<Integer> orderIds, String reference) throws SQLException {
        String marks = String.join(",", Collections.nCopies(orderIds.size(), "?"));
        String sql = "SELECT COALESCE(SUM(t.amount), 0) FROM transactions t "
                + "JOIN orders o ON o.id = t.id_order "
                + "WHERE o.id_user = ? AND o.status = 'pending' AND t.status = 'pending' "
                + "AND t.gateway IN ('vnpay','vnpay_deposit') AND t.txn_ref LIKE ? AND o.id IN (" + marks + ")";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, reference + "-%");
            for (int i = 0; i < orderIds.size(); i++) ps.setInt(i + 3, orderIds.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1).longValue() : 0;
            }
        }
    }

    private List<Integer> parseIds(String csv) {
        List<Integer> ids = new ArrayList<>();
        if (csv == null) return ids;
        for (String s : csv.split(",")) {
            try {
                int n = Integer.parseInt(s.trim());
                if (n > 0 && !ids.contains(n)) ids.add(n);
            } catch (NumberFormatException ignored) { }
        }
        return ids;
    }

    private void fail(HttpServletRequest req, HttpServletResponse resp, String code) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/pages/cart.jsp?payment=" + code);
    }
}
