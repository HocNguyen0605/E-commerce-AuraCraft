package controller.vnp;

import dao.PaymentDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

/**
 * VNPay chuyển người dùng về đây sau khi thanh toán.
 * Kiểm tra chữ ký -> thành công thì xác nhận đơn, thất bại thì hủy đơn và trả hàng về giỏ
 * (dùng lại đúng logic của PaymentDAO mà luồng mô phỏng đang dùng).
 */
@WebServlet("/vnpay_return")
public class VNPayReturnServlet extends HttpServlet {
    private final PaymentDAO paymentDAO = new PaymentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String ctx = req.getContextPath();

        // 1. Kiểm tra chữ ký: chỉ băm các tham số vnp_*, bỏ qua giá trị rỗng
        Map<String, String> params = new TreeMap<>();
        for (Map.Entry<String, String[]> e : req.getParameterMap().entrySet()) {
            String key = e.getKey();
            String val = e.getValue()[0];
            if (key.startsWith("vnp_") && !key.equals("vnp_SecureHash") && !key.equals("vnp_SecureHashType")
                    && val != null && !val.isEmpty()) {
                params.put(key, val);
            }
        }
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (data.length() > 0) data.append('&');
            data.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                    .append('=')
                    .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
        }
        String expected = new HmacSHA512().encrypt(VNPayConfig.vnp_HashSecret, data.toString());
        String received = req.getParameter("vnp_SecureHash");
        if (received == null || !received.equalsIgnoreCase(expected)) {
            resp.sendRedirect(ctx + "/pages/cart.jsp?payment=invalid");
            return;
        }

        // 2. Phải còn phiên đăng nhập (ReturnUrl cần cùng host với trang web, vd cùng dùng localhost)
        HttpSession session = req.getSession(false);
        Object id = session == null ? null : session.getAttribute("userId");
        if (!(id instanceof Number userId)) {
            resp.sendRedirect(ctx + "/pages/login.jsp");
            return;
        }

        // 3. Tách paymentReference ra khỏi vnp_TxnRef (dạng "<reference>_<millis>")
        String txnRef = req.getParameter("vnp_TxnRef");
        int cut = txnRef == null ? -1 : txnRef.lastIndexOf('_');
        if (cut <= 0) {
            resp.sendRedirect(ctx + "/pages/cart.jsp?payment=invalid");
            return;
        }
        String reference = txnRef.substring(0, cut);

        String code = req.getParameter("vnp_ResponseCode");
        String status = req.getParameter("vnp_TransactionStatus");
        boolean paid = "00".equals(code) && (status == null || "00".equals(status));

        try {
            if (paid) {
                PaymentDAO.MockResult r = paymentDAO.confirmMockPayment(reference, userId.intValue());
                switch (r) {
                    case SUCCESS -> resp.sendRedirect(ctx + "/pages/success.jsp");
                    case NOT_PENDING -> resp.sendRedirect(ctx + "/profile-orders");   // F5 lại trang, đơn đã xử lý
                    default -> resp.sendRedirect(ctx + "/pages/cart.jsp?payment=notfound");
                }
            } else {
                // Thất bại hoặc người dùng bấm hủy (mã 24): hủy đơn, trả sản phẩm về giỏ
                paymentDAO.cancelMockPayment(reference, userId.intValue(), "vnpay");
                resp.sendRedirect(ctx + "/pages/cart.jsp?payment=cancelled");
            }
        } catch (SQLException e) {
            getServletContext().log("VNPay return: lỗi cập nhật đơn hàng", e);
            resp.sendRedirect(ctx + "/pages/cart.jsp?payment=error");
        }
    }
}