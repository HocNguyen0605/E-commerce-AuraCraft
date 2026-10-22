package controller;

import com.google.gson.Gson;
import dao.PaymentDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/** Demo-only payment confirmation. It never contacts a bank or VNPay. */
@WebServlet("/vnpay/simulate")
public class MockPaymentServlet extends HttpServlet {
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        Object id = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        if (!(id instanceof Number userId) || userId.intValue() < 1 || !"buyer".equals(role)) {
            write(response, 401, Map.of("success", false, "message", "Vui lòng đăng nhập tài khoản người mua."));
            return;
        }
        String action = request.getParameter("action");
        String reference = request.getParameter("paymentReference");
        try {
            PaymentDAO.MockResult result = "confirm".equals(action)
                    ? paymentDAO.confirmMockPayment(reference, userId.intValue())
                    : "confirm-cod-deposit".equals(action)
                    ? paymentDAO.confirmMockCodDeposit(reference, userId.intValue())
                    : "cancel".equals(action)
                    ? paymentDAO.cancelMockPayment(reference, userId.intValue(), "vnpay")
                    : "cancel-cod-deposit".equals(action)
                    ? paymentDAO.cancelMockPayment(reference, userId.intValue(), "cod") : PaymentDAO.MockResult.UNKNOWN;
            switch (result) {
                case SUCCESS -> write(response, 200, Map.of("success", true, "message", "Thanh toán mô phỏng thành công."));
                case CANCELLED -> write(response, 200, Map.of("success", true, "message", "Đã hủy đơn và trả sản phẩm về giỏ hàng."));
                case NOT_PENDING -> write(response, 409, Map.of("success", false, "message", "Đơn hàng không còn ở trạng thái chờ thanh toán."));
                default -> write(response, 404, Map.of("success", false, "message", "Không tìm thấy giao dịch mô phỏng."));
            }
        } catch (SQLException exception) {
            getServletContext().log("Mock payment database operation failed", exception);
            write(response, 503, Map.of("success", false, "message", "Chưa thể cập nhật giao dịch. Vui lòng thử lại."));
        }
    }

    private void write(HttpServletResponse response, int status, Map<String, Object> payload) throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(payload));
    }
}
