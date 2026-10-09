package controller;

import com.google.gson.Gson;
import dao.CheckoutDAO;
import dto.CheckoutRequestDTO;
import dto.CheckoutResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/pages/checkout", "/checkout"})
public class CheckoutServlet extends HttpServlet {
    private final CheckoutDAO checkoutDAO = new CheckoutDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        Object userId = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        boolean buyer = userId instanceof Number id && id.intValue() > 0 && "buyer".equals(role);
        request.setAttribute("databaseCheckoutAvailable", buyer);
        request.setAttribute("checkoutProductIds", request.getParameter("productIds"));
        if (request.getSession(false) != null) {
            request.setAttribute("checkoutFullName", request.getSession(false).getAttribute("fullName"));
            request.setAttribute("checkoutEmail", request.getSession(false).getAttribute("email"));
        }
        if (buyer) {
            try {
                String requestedIds = request.getParameter("productIds");
                if ("1".equals(request.getParameter("fromCart")) || requestedIds != null) {
                    List<Integer> productIds = parseIds(requestedIds);
                    if (productIds.isEmpty()) {
                        response.sendRedirect(request.getContextPath() + "/pages/cart.jsp?checkout=invalid");
                        return;
                    }
                    List<Map<String, Object>> checkoutItems = checkoutDAO.findSelectedCartItems(((Number) userId).intValue(), productIds);
                    if (checkoutItems.size() != productIds.size()) {
                        response.sendRedirect(request.getContextPath() + "/pages/cart.jsp?checkout=invalid");
                        return;
                    }
                    BigDecimal subtotal = BigDecimal.ZERO;
                    java.util.Set<Integer> shops = new java.util.LinkedHashSet<>();
                    for (Map<String, Object> item : checkoutItems) {
                        BigDecimal price = (BigDecimal) item.get("price");
                        int quantity = (Integer) item.get("quantity");
                        subtotal = subtotal.add(price.multiply(BigDecimal.valueOf(quantity)));
                        shops.add((Integer) item.get("shopId"));
                        item.put("imageUrl", checkoutImageUrl(request, (String) item.get("image"), (Integer) item.get("categoryId")));
                    }
                    BigDecimal shipping = new BigDecimal("25300").multiply(BigDecimal.valueOf(shops.size()));
                    request.setAttribute("checkoutItems", checkoutItems);
                    request.setAttribute("checkoutProductIds", String.join(",", productIds.stream().map(String::valueOf).toList()));
                    request.setAttribute("checkoutSubtotal", subtotal);
                    request.setAttribute("checkoutShipping", shipping);
                    request.setAttribute("checkoutTotal", subtotal.add(shipping));
                }
                Map<String, String> savedAddress = checkoutDAO.findDefaultAddress(((Number) userId).intValue());
                if (!savedAddress.isEmpty()) {
                    request.setAttribute("checkoutFullName", savedAddress.get("receiver"));
                    request.setAttribute("checkoutPhone", savedAddress.get("phone"));
                    request.setAttribute("checkoutAddress", savedAddress.get("address"));
                }
            } catch (SQLException exception) {
                getServletContext().log("Unable to load buyer delivery address", exception);
                if (request.getParameter("productIds") != null || "1".equals(request.getParameter("fromCart"))) {
                    response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                            "Không thể tải giỏ hàng để thanh toán. Vui lòng thử lại.");
                    return;
                }
            }
        }
        request.setAttribute("checkoutPageReady", true);
        request.getRequestDispatcher("/pages/checkout.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
        Object userId = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object role = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        if (!(userId instanceof Number number) || number.intValue() < 1 || !"buyer".equals(role)) {
            writeError(response, 401, "Vui lòng đăng nhập tài khoản người mua để đặt hàng.");
            return;
        }

        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setProductIds(request.getParameter("productIds"));
        checkoutRequest.setFullName(request.getParameter("fullName"));
        checkoutRequest.setPhone(request.getParameter("phone"));
        checkoutRequest.setAddress(request.getParameter("address"));
        checkoutRequest.setNote(request.getParameter("note"));
        checkoutRequest.setPaymentMethod(request.getParameter("paymentMethod"));

        String receiver = trim(checkoutRequest.getFullName());
        String phone = trim(checkoutRequest.getPhone()).replaceAll("\\s+", "");
        String address = trim(checkoutRequest.getAddress());
        String note = trim(checkoutRequest.getNote());
        String payment = trim(checkoutRequest.getPaymentMethod()).equalsIgnoreCase("vnpay") ? "VNPay" : "COD";
        List<Integer> productIds = parseIds(checkoutRequest.getProductIds());
        if (receiver.isBlank() || receiver.length() > 100 || !phone.matches("0[35789][0-9]{8}")
                || address.isBlank() || address.length() > 350 || note.length() > 100
                || ("Người nhận: " + receiver + " | SĐT: " + phone + " | Địa chỉ: " + address
                + (note.isBlank() ? "" : " | Ghi chú: " + note)).length() > 500 || productIds.isEmpty()) {
            writeError(response, 400, "Thông tin giao hàng hoặc sản phẩm đặt mua chưa hợp lệ.");
            return;
        }
        try {
            Map<String, Object> result = checkoutDAO.placeOrder(
                    number.intValue(), productIds, receiver, phone, address, note, payment);
            CheckoutResponseDTO checkoutResponse = new CheckoutResponseDTO();
            checkoutResponse.setOrderId(((Number) result.get("orderId")).intValue());
            @SuppressWarnings("unchecked")
            List<Integer> orderIds = (List<Integer>) result.get("orderIds");
            checkoutResponse.setOrderIds(orderIds);
            checkoutResponse.setSubtotal((java.math.BigDecimal) result.get("subtotal"));
            checkoutResponse.setShipping((java.math.BigDecimal) result.get("shipping"));
            checkoutResponse.setTotal((java.math.BigDecimal) result.get("total"));
            checkoutResponse.setItemCount(((Number) result.get("itemCount")).intValue());
            checkoutResponse.setPaymentMethod(payment);
            response.getWriter().write(gson.toJson(checkoutResponse));
        } catch (IllegalArgumentException exception) {
            writeError(response, 400, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Unable to place checkout order", exception);
            writeError(response, 503, "Chưa thể tạo đơn hàng lúc này. Vui lòng thử lại.");
        }
    }

    private List<Integer> parseIds(String value) {
        if (value == null || value.isBlank()) return List.of();
        List<Integer> ids = new ArrayList<>();
        try {
            for (String part : value.split(",")) {
                int id = Integer.parseInt(part.trim());
                if (id < 1 || ids.contains(id) || ids.size() >= 50) return List.of();
                ids.add(id);
            }
        } catch (NumberFormatException exception) { return List.of(); }
        return ids;
    }

    private String trim(String value) { return value == null ? "" : value.trim(); }

    private String checkoutImageUrl(HttpServletRequest request, String storedPath, int categoryId) {
        String path = storedPath == null ? "" : storedPath.trim();
        if (path.startsWith("https://") || path.startsWith("http://")) return path;
        // Keep the same fallback used by CartDAO so checkout previews match the cart.
        String fallback = switch (categoryId) {
            case 2 -> "necklace_02.jpg";
            case 3 -> "phoneStrap_01.jpg";
            default -> "bracelet_01.jpg";
        };
        return request.getContextPath() + "/assets/img/products/" + fallback;
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.getWriter().write(gson.toJson(Map.of("success", false, "message", message)));
    }
}
