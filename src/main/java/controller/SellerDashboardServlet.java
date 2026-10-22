//package controller;
//
//import dao.SellerStatsDAO;
//import dao.ShopDAO;
//import dao.WithdrawalDAO;
//import dto.DailyRevenue;
//import dto.Shop;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//import util.Flash;
//
//import java.io.IOException;
//import java.util.List;
//
//@WebServlet("/seller/dashboard")
//public class SellerDashboardServlet extends HttpServlet {
//    private final SellerStatsDAO stats = new SellerStatsDAO();
//    private final WithdrawalDAO withdrawalDAO = new WithdrawalDAO();
//    private final ShopDAO shopDAO = new ShopDAO();
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//        Flash.transfer(req);
//        try {
//            List<DailyRevenue> chart = stats.last7Days(shop.getId());
//            long max = 1;
//            for (DailyRevenue d : chart) max = Math.max(max, d.getAmount());
//            req.setAttribute("shop", shop);
//            req.setAttribute("balance", stats.availableBalance(shop.getId()));
//            req.setAttribute("revenueMonth", stats.revenueThisMonth(shop.getId()));
//            req.setAttribute("pendingOrders", stats.pendingOrders(shop.getId()));
//            req.setAttribute("chart", chart);
//            req.setAttribute("chartMax", max); // chiều cao cột = amount * 100 / chartMax (%)
//            req.setAttribute("ledger", stats.ledger(shop.getId(), 10));
//        } catch (Exception e) {
//            req.setAttribute("error", "Không tải được số liệu: " + e.getMessage());
//        }
//        req.getRequestDispatcher("/pages/seller/seller-dashboard.jsp").forward(req, resp);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//
//        try {
//            if ("withdraw".equals(req.getParameter("action"))) {
//                long amount = Long.parseLong(req.getParameter("amount").trim());
//
//                if (amount <= 0) {
//                    Flash.error(req, "Số tiền rút phải lớn hơn 0.");
//                } else if (amount < 50000) {
//                    Flash.error(req, "Số tiền rút tối thiểu là 50,000đ.");
//                } else {
//                    String bank = req.getParameter("bankName");
//                    String acc = req.getParameter("bankAccount");
//                    String holder = req.getParameter("accountHolder");
//
//                    if (isBlank(bank) || isBlank(acc) || isBlank(holder)) {
//                        Flash.error(req, "Vui lòng nhập đủ thông tin tài khoản ngân hàng.");
//                    } else {
//                        String err = withdrawalDAO.create(shop.getId(), amount, bank.trim(), acc.trim(), holder.trim());
//                        if (err == null) {
//                            Flash.success(req, "Đã gửi yêu cầu rút tiền, đang chờ duyệt.");
//                        } else {
//                            Flash.error(req, err);
//                        }
//                    }
//                }
//            }
//        } catch (NumberFormatException e) {
//            Flash.error(req, "Số tiền không hợp lệ. Vui lòng nhập số nguyên.");
//        } catch (Exception e) {
//            Flash.error(req, "Lỗi hệ thống: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//        resp.sendRedirect(req.getContextPath() + "/seller/dashboard");
//    }
//
//    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
//    private Shop getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        HttpSession session = req.getSession(false);
//        if (session == null || session.getAttribute("userId") == null) {
//            resp.sendRedirect(req.getContextPath() + "/pages/login.jsp");
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
//    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }
//}