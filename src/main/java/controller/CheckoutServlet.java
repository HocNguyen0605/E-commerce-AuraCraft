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
        Object sessionEmail = request.getSession(false) == null ? null
                : request.getSession(false).getAttribute("email");
        request.setAttribute("checkoutEmail", sessionEmail);
        if (request.getSession(false) != null) {
            Object savedName = request.getSession(false).getAttribute("fullName");
            if (savedName == null) savedName = request.getSession(false).getAttribute("userName");
            request.setAttribute("checkoutFullName", savedName);
        }
        if (buyer) {
            try {
                int buyerId = ((Number) userId).intValue();
                if (sessionEmail == null || sessionEmail.toString().isBlank()) {
                    String email = new dao.AccountDAO().findEmailByUserId(buyerId);
                    request.setAttribute("checkoutEmail", email);
                    if (email != null) request.getSession(false).setAttribute("email", email);
                }
                dto.UserProfileDTO profile = new dao.ProfileDAO().findUser(buyerId);
                if (profile != null) {
                    if (profile.getName() != null && !profile.getName().isBlank()) {
                        request.setAttribute("checkoutFullName", profile.getName());
                    }
                    if (profile.getPhone() != null && !profile.getPhone().isBlank()) {
                        request.setAttribute("checkoutPhone", profile.getPhone());
                    }
                    if ((request.getAttribute("checkoutEmail") == null
                            || request.getAttribute("checkoutEmail").toString().isBlank())
                            && profile.getEmail() != null) {
                        request.setAttribute("checkoutEmail", profile.getEmail());
                    }
                }
                String requestedIds = request.getParameter("productIds");
                if ("1".equals(request.getParameter("fromCart")) || requestedIds != null) {
                    List<Integer> productIds = parseIds(requestedIds);
                    if (productIds.isEmpty()) {
                        response.sendRedirect(request.getContextPath() + "/pages/cart?checkout=invalid");
                        return;
                    }
                    List<Map<String, Object>> checkoutItems = checkoutDAO.findSelectedCartItems(((Number) userId).intValue(), productIds);
                    if (checkoutItems.size() != productIds.size()) {
                        response.sendRedirect(request.getContextPath() + "/pages/cart?checkout=invalid");
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
                    request.setAttribute("hasCustomItem", false);
                    request.setAttribute("availableVouchers", checkoutDAO.findAvailableVouchers(buyerId, productIds));
                }
                Map<String, String> savedAddress = checkoutDAO.findDefaultAddress(buyerId);
                if (!savedAddress.isEmpty()) {
                    if (savedAddress.get("receiver") != null && !savedAddress.get("receiver").isBlank()) {
                        request.setAttribute("checkoutFullName", savedAddress.get("receiver"));
                    }
                    if (savedAddress.get("phone") != null && !savedAddress.get("phone").isBlank()) {
                        request.setAttribute("checkoutPhone", savedAddress.get("phone"));
                    }
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

        if ("preview-benefits".equals(request.getParameter("action"))) {
            List<Integer> ids = parseIds(request.getParameter("productIds"));
            if (ids.isEmpty()) {
                writeError(response, 400, "Không có sản phẩm hợp lệ để tính ưu đãi.");
                return;
            }
            try {
                Map<String, Object> quote = checkoutDAO.previewBenefits(number.intValue(), ids,
                        request.getParameter("voucherCode"), "true".equals(request.getParameter("useCoins")));
                response.getWriter().write(gson.toJson(quote));
            } catch (IllegalArgumentException exception) {
                writeError(response, 400, exception.getMessage());
            } catch (SQLException exception) {
                getServletContext().log("Unable to calculate checkout benefits", exception);
                writeError(response, 503, "Chưa thể kiểm tra voucher và xu. Vui lòng thử lại.");
            }
            return;
        }

        CheckoutRequestDTO checkoutRequest = new CheckoutRequestDTO();
        checkoutRequest.setProductIds(request.getParameter("productIds"));
        checkoutRequest.setFullName(request.getParameter("fullName"));
        checkoutRequest.setPhone(request.getParameter("phone"));
        checkoutRequest.setAddress(request.getParameter("address"));
        checkoutRequest.setNote(request.getParameter("note"));
        checkoutRequest.setPaymentMethod(request.getParameter("paymentMethod"));
        String voucherCode = trim(request.getParameter("voucherCode")).toUpperCase(java.util.Locale.ROOT);
        boolean useCoins = "true".equals(request.getParameter("useCoins"));

        String receiver = trim(checkoutRequest.getFullName());
        String phone = trim(checkoutRequest.getPhone()).replaceAll("\\s+", "");
        String address = trim(checkoutRequest.getAddress());
        String note = trim(checkoutRequest.getNote());
        String selectedPayment = trim(checkoutRequest.getPaymentMethod()).toLowerCase(java.util.Locale.ROOT);
        boolean depositPayment = "vnpay_deposit".equals(selectedPayment);
        if (!depositPayment && !"vnpay".equals(selectedPayment)
                && !"cod_standard".equals(selectedPayment) && !"cod".equals(selectedPayment)) {
            writeError(response, 400, "Phương thức thanh toán không hợp lệ.");
            return;
        }
        String payment = selectedPayment.startsWith("vnpay") ? (depositPayment ? "VNPayDeposit" : "VNPay") : "COD";
        List<Integer> productIds = parseIds(checkoutRequest.getProductIds());
        if (receiver.isBlank() || receiver.length() > 100 || !phone.matches("0[35789][0-9]{8}")
                || address.isBlank() || address.length() > 350 || note.length() > 100
                || ("Người nhận: " + receiver + " | SĐT: " + phone + " | Địa chỉ: " + address
                + (note.isBlank() ? "" : " | Ghi chú: " + note)).length() > 500 || productIds.isEmpty()) {
            writeError(response, 400, "Thông tin giao hàng hoặc sản phẩm đặt mua chưa hợp lệ.");
            return;
        }
        try {
            String paymentReference = java.util.UUID.randomUUID().toString().replace("-", "");
            Map<String, Object> result = checkoutDAO.placeOrder(
                    number.intValue(), productIds, receiver, phone, address, note, payment, voucherCode, useCoins, paymentReference);
            CheckoutResponseDTO checkoutResponse = new CheckoutResponseDTO();
            checkoutResponse.setOrderId(((Number) result.get("orderId")).intValue());
            @SuppressWarnings("unchecked")
            List<Integer> orderIds = (List<Integer>) result.get("orderIds");
            checkoutResponse.setOrderIds(orderIds);
            checkoutResponse.setSubtotal((BigDecimal) result.get("subtotal"));
            checkoutResponse.setShipping((BigDecimal) result.get("shipping"));
            checkoutResponse.setTotal((BigDecimal) result.get("total"));
            checkoutResponse.setItemCount(((Number) result.get("itemCount")).intValue());
            checkoutResponse.setPaymentMethod(depositPayment ? "vnpay_deposit" : payment.toLowerCase(java.util.Locale.ROOT));
            checkoutResponse.setHasCustomItem(Boolean.TRUE.equals(result.get("hasCustomItem")));
            checkoutResponse.setPaymentReference(paymentReference);
            checkoutResponse.setVoucherDiscount((BigDecimal) result.get("voucherDiscount"));
            checkoutResponse.setCoinDiscount((BigDecimal) result.get("coinDiscount"));
            checkoutResponse.setDiscount((BigDecimal) result.get("discount"));
            checkoutResponse.setCoinsUsed((Integer) result.get("coinsUsed"));
            checkoutResponse.setRemainingCoins((Integer) result.get("remainingCoins"));
            checkoutResponse.setRewardCoinsPending((Integer) result.get("rewardCoinsPending"));
            response.getWriter().write(gson.toJson(checkoutResponse));
        } catch (IllegalArgumentException exception) {
            writeError(response, 400, exception.getMessage());
        } catch (SQLException exception) {
            getServletContext().log("Unable to place checkout order", exception);
            if (isMissingShippingColumns(exception)) {
                writeError(response, 503,
                        "Cơ sở dữ liệu chưa có cột thông tin giao hàng mới. Hãy chạy file db_order_shipping_fields.sql một lần, sau đó thử lại.");
            } else {
                writeError(response, 503, "Chưa thể tạo đơn hàng (DB " + exception.getErrorCode() +
                        ", SQLState " + (exception.getSQLState() == null ? "unknown" : exception.getSQLState()) +
                        "). Hãy kiểm tra log Tomcat để xem chi tiết.");
            }
        }
    }

    private boolean isMissingShippingColumns(SQLException exception) {
        for (SQLException current = exception; current != null; current = current.getNextException()) {
            String message = current.getMessage();
            if (current.getErrorCode() == 1054 && message != null &&
                    (message.contains("shipping_receiver") || message.contains("shipping_phone") || message.contains("shipping_note"))) {
                return true;
            }
        }
        return false;
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
        if (!path.isBlank()) {
            path = path.replace('\\', '/').replaceFirst("^/+", "");
            String webPath = path.startsWith("assets/") ? path : "assets/img/" + path;
            try {
                if (getServletContext().getResource("/" + webPath) != null) {
                    return request.getContextPath() + "/" + webPath;
                }
            } catch (IOException ignored) {
                // Use the same category fallback as the product page when the stored file is missing.
            }
        }
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