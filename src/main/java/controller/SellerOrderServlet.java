package controller;

import dao.SellerOrderDAO;
import dao.ShopDAO;
import dto.SellerOrderDTO;
import dto.SellerOrderItemDTO;
import dto.ShopDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.Flash;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@WebServlet("/pages/seller/order-management")
public class SellerOrderServlet extends HttpServlet {

    private static final String PAGE_PATH = "/pages/seller/order-management";
    private static final String VIEW = "/pages/seller/order-management.jsp";
    private static final String FALLBACK_IMG = "assets/img/products/bracelet_01.jpg";
    private static final int DEFAULT_CRAFT_DAYS = 3;

    private final SellerOrderDAO orderDAO = new SellerOrderDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        Flash.transfer(req);

        try {
            List<SellerOrderDTO> orders = orderDAO.findByShop(shop.getId());
            for (SellerOrderDTO o : orders) {
                for (SellerOrderItemDTO it : o.getItems()) {
                    it.setImageUrl(resolveImage(req, it.getImageRaw()));
                }
            }
            long lateCount = orders.stream().filter(SellerOrderDTO::isOverdue).count();
            System.out.println("[SellerOrder] shopId=" + shop.getId() + ", số đơn=" + orders.size());

            req.setAttribute("shop", shop);
            req.setAttribute("sellerTab", "orders");
            req.setAttribute("orders", orders);
            req.setAttribute("lateCount", lateCount);
            req.getRequestDispatcher(VIEW).forward(req, resp);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");   // phải đặt trước mọi getParameter
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        String action = req.getParameter("action");
        int orderId = parseInt(req.getParameter("orderId"));
        System.out.println("[SellerOrder] POST action=" + action + ", orderId=" + orderId + ", shopId=" + shop.getId());

        try {
            boolean ok = false;
            String okMsg = "", failMsg = "Không thực hiện được: đơn không tồn tại hoặc trạng thái không phù hợp.";

            if (orderId > 0 && action != null) {
                switch (action) {
                    case "start":
                        ok = orderDAO.startCrafting(orderId, shop.getId(), DEFAULT_CRAFT_DAYS);
                        okMsg = "Đã chuyển đơn #" + orderId + " sang Đang chế tác.";
                        break;
                    case "deliver":
                        ok = orderDAO.deliver(orderId, shop.getId());
                        okMsg = "Đã chuyển đơn #" + orderId + " sang Đang giao hàng.";
                        break;
                    case "progress": {
                        String note = trim(req.getParameter("note"), 500);
                        if (note.isEmpty()) { failMsg = "Vui lòng nhập nội dung tiến độ."; break; }
                        ok = orderDAO.addProgress(orderId, shop.getId(), note);
                        okMsg = "Đã lưu tiến độ cho đơn #" + orderId + ".";
                        break;
                    }
                    case "extend": {
                        String reason = trim(req.getParameter("reason"), 500);
                        int days = parseInt(req.getParameter("days"));
                        if (reason.isEmpty() || days < 1 || days > 30) {
                            failMsg = "Nhập lý do và số ngày gia hạn (1 - 30).";
                            break;
                        }
                        ok = orderDAO.requestExtension(orderId, shop.getId(), reason, days);
                        okMsg = "Đã gửi yêu cầu gia hạn " + days + " ngày cho đơn #" + orderId + ".";
                        failMsg = "Không gửi được: đơn không ở trạng thái chế tác hoặc đã có yêu cầu gia hạn đang chờ duyệt.";
                        break;
                    }
                    default:
                        failMsg = "Hành động không hợp lệ.";
                }
            }
            if (ok) Flash.success(req, okMsg); else Flash.error(req, failMsg);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + PAGE_PATH);   // PRG: tránh gửi lại khi F5
    }

    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect) nếu không hợp lệ. */
    private ShopDTO getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            System.out.println("[SellerOrder] chưa đăng nhập hoặc thiếu userId. session="
                    + (session == null ? "null" : Collections.list(session.getAttributeNames())));
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return null;
        }
        try {
            int userId = ((Number) session.getAttribute("userId")).intValue();
            ShopDTO shop = shopDAO.findActiveByOwner(userId);
            System.out.println("[SellerOrder] userId=" + userId + ", shop=" + (shop == null ? "NULL" : shop.getId()));
            if (shop == null) {
                Flash.error(req, "Tài khoản hiện tại không có gian hàng đang hoạt động.");
                resp.sendRedirect(req.getContextPath() + "/pages/home");
            }
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    /** Cùng cách xử lý ảnh với trang checkout: http(s) giữ nguyên, còn lại nằm dưới assets/img/. */
    private String resolveImage(HttpServletRequest req, String stored) {
        String ctx = req.getContextPath();
        String path = stored == null ? "" : stored.trim();
        if (path.startsWith("https://") || path.startsWith("http://")) return path;
        if (!path.isBlank()) {
            path = path.replace('\\', '/').replaceFirst("^/+", "");
            String webPath = path.startsWith("assets/") ? path : "assets/img/" + path;
            try {
                if (getServletContext().getResource("/" + webPath) != null) return ctx + "/" + webPath;
            } catch (Exception ignored) {
                // dùng ảnh mặc định bên dưới
            }
        }
        return ctx + "/" + FALLBACK_IMG;
    }

    private int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}