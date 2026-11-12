package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.BuyerCustomDesignOrderDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@WebServlet("/api/buyer/custom-design-deposit")
public class BuyerCustomDesignOrderServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final BuyerCustomDesignOrderDAO dao = new BuyerCustomDesignOrderDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        Object id = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        if (!(id instanceof Number userId) || userId.intValue() < 1 || !"buyer".equals(role)) {
            write(response, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập bằng tài khoản người mua để đặt cọc.")); return;
        }
        try {
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            String designId = value(body, "designId");
            String receiver = value(body, "fullName").trim();
            String phone = value(body, "phone").replaceAll("\\s+", "");
            String address = value(body, "address").trim();
            String note = value(body, "note").trim();
            int quantity;
            try {
                quantity = body.has("quantity") && !body.get("quantity").isJsonNull()
                        ? body.get("quantity").getAsInt() : 1;
            } catch (RuntimeException invalidQuantity) {
                write(response, 400, Map.of("ok", false, "message", "Số lượng thiết kế không hợp lệ.")); return;
            }
            if (designId.isBlank() || designId.length() > 80 || receiver.isBlank() || receiver.length() > 100
                    || !phone.matches("0[35789][0-9]{8}") || address.isBlank() || address.length() > 350
                    || note.length() > 100 || quantity < 1 || quantity > 99) {
                write(response, 400, Map.of("ok", false, "message", "Thông tin giao hàng, số lượng hoặc thiết kế chưa hợp lệ.")); return;
            }
            String paymentReference = UUID.randomUUID().toString().replace("-", "");
            Map<String, Object> result = dao.createDepositOrder(userId.intValue(), designId, receiver, phone,
                    address, note, quantity, paymentReference);
            write(response, 201, Map.of("ok", true, "orderId", result.get("orderId"),
                    "paymentReference", result.get("paymentReference"), "total", result.get("total"),
                    "deposit", result.get("deposit"), "remaining", result.get("remaining")));
        } catch (IllegalArgumentException exception) {
            write(response, 400, Map.of("ok", false, "message", exception.getMessage()));
        } catch (Exception exception) {
            getServletContext().log("Unable to create VNPay deposit order for custom design", exception);
            write(response, 500, Map.of("ok", false, "message", "Chưa tạo được đơn đặt cọc. Vui lòng thử lại."));
        }
    }

    private String value(JsonObject body, String key) {
        return body.has(key) && !body.get(key).isJsonNull() ? body.get(key).getAsString() : "";
    }

    private void write(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.getWriter().write(GSON.toJson(payload));
    }
}
