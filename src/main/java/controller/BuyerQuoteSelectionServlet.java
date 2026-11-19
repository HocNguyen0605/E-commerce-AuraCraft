package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.BuyerQuoteDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/buyer/quotes/select")
public class BuyerQuoteSelectionServlet extends HttpServlet {
    private static final Gson GSON = new Gson();
    private final BuyerQuoteDAO quoteDAO = new BuyerQuoteDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        Object sessionUser = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null
                ? null : request.getSession(false).getAttribute("role");
        if (!(sessionUser instanceof Number userId) || userId.intValue() < 1 || !"buyer".equals(role)) {
            write(response, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập bằng tài khoản người mua."));
            return;
        }

        try {
            JsonObject body = JsonParser.parseReader(request.getReader()).getAsJsonObject();
            int requestId = body.get("requestId").getAsInt();
            int quoteId = body.get("quoteId").getAsInt();
            if (requestId < 1 || quoteId < 1) {
                write(response, 400, Map.of("ok", false, "message", "Yêu cầu hoặc báo giá không hợp lệ."));
                return;
            }
            quoteDAO.acceptQuote(userId.intValue(), requestId, quoteId);
            write(response, 200, Map.of("ok", true, "message", "Đã xác nhận chọn thợ cho yêu cầu này."));
        } catch (IllegalArgumentException exception) {
            write(response, 400, Map.of("ok", false, "message", exception.getMessage()));
        } catch (Exception exception) {
            getServletContext().log("Unable to accept buyer quote", exception);
            write(response, 500, Map.of("ok", false, "message", "Chưa xác nhận được thợ. Vui lòng thử lại."));
        }
    }

    private void write(HttpServletResponse response, int status, Object payload) throws IOException {
        response.setStatus(status);
        response.getWriter().write(GSON.toJson(payload));
    }
}
