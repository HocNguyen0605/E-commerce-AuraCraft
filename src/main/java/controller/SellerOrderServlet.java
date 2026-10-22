//package controller;
//
//import dao.SellerOrderDAO;
//import dao.ShopDAO;
//import dto.SellerOrder;
//import dto.Shop;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//import util.Flash;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.List;
//
//@WebServlet("/seller/orders")
//public class SellerOrderServlet extends HttpServlet {
//
//    private static final int DEFAULT_CRAFT_DAYS = 3;
//
//    private final SellerOrderDAO orderDAO = new SellerOrderDAO();
//    private final ShopDAO shopDAO = new ShopDAO();
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//
//        Flash.transfer(req);
//
//        try {
//            List<SellerOrder> orders = orderDAO.findByShop(shop.getId());
//            long lateCount = orders.stream().filter(SellerOrder::isOverdue).count();
//
//            req.setAttribute("shop", shop);
//            req.setAttribute("orders", orders);
//            req.setAttribute("lateCount", lateCount);
//            req.getRequestDispatcher("/pages/seller/order-management.jsp").forward(req, resp);
//        } catch (SQLException e) {
//            throw new ServletException(e);
//        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//
//        String action = req.getParameter("action");
//        int orderId = parseInt(req.getParameter("orderId"));
//
//        try {
//            boolean ok = false;
//            String okMsg = "", failMsg = "Không thực hiện được: đơn không tồn tại hoặc trạng thái không phù hợp.";
//
//            if (orderId > 0 && action != null) {
//                switch (action) {
//                    case "start":
//                        ok = orderDAO.startCrafting(orderId, shop.getId(), DEFAULT_CRAFT_DAYS);
//                        okMsg = "Đã chuyển đơn #" + orderId + " sang Đang chế tác.";
//                        break;
//                    case "deliver":
//                        ok = orderDAO.deliver(orderId, shop.getId());
//                        okMsg = "Đã chuyển đơn #" + orderId + " sang Đang giao hàng.";
//                        break;
//                    case "progress": {
//                        String note = trim(req.getParameter("note"), 500);
//                        if (note.isEmpty()) { failMsg = "Vui lòng nhập nội dung tiến độ."; break; }
//                        ok = orderDAO.addProgress(orderId, shop.getId(), note);
//                        okMsg = "Đã lưu tiến độ cho đơn #" + orderId + ".";
//                        break;
//                    }
//                    case "extend": {
//                        String reason = trim(req.getParameter("reason"), 500);
//                        int days = parseInt(req.getParameter("days"));
//                        if (reason.isEmpty() || days < 1 || days > 30) {
//                            failMsg = "Nhập lý do và số ngày gia hạn (1 - 30).";
//                            break;
//                        }
//                        ok = orderDAO.requestExtension(orderId, shop.getId(), reason, days);
//                        okMsg = "Đã gửi yêu cầu gia hạn " + days + " ngày cho đơn #" + orderId + ".";
//                        failMsg = "Không gửi được: đơn không ở trạng thái chế tác hoặc đã có yêu cầu gia hạn đang chờ duyệt.";
//                        break;
//                    }
//                    default:
//                        failMsg = "Hành động không hợp lệ.";
//                }
//            }
//            if (ok) Flash.success(req, okMsg); else Flash.error(req, failMsg);
//        } catch (SQLException e) {
//            throw new ServletException(e);
//        }
//        resp.sendRedirect(req.getContextPath() + "/seller/orders");   // PRG: tránh gửi lại khi F5
//    }
//
//    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
//    private Shop getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        HttpSession session = req.getSession(false);
//        if (session == null || session.getAttribute("userId") == null) {
//            resp.sendRedirect(req.getContextPath() + "/pages/login.html");
//            return null;
//        }
//        try {
//            int userId = ((Number) session.getAttribute("userId")).intValue();
//            Shop shop = shopDAO.findActiveByOwner(userId);
//            if (shop == null) {
//                resp.sendRedirect(req.getContextPath() + "/index.html");
//            }
//            return shop;
//        } catch (Exception e) {
//            e.printStackTrace();
//            resp.sendError(500);
//            return null;
//        }
//    }
//
//    private int parseInt(String s) {
//        try { return s == null ? 0 : Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
//    }
//
//    private String trim(String s, int max) {
//        if (s == null) return "";
//        s = s.trim();
//        return s.length() > max ? s.substring(0, max) : s;
//    }
//}