package controller;

import dao.CustomMarketDAO;
import dao.ShopDAO;
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

/** Chợ Custom & Báo giá của seller: xem yêu cầu đang mở, gửi/sửa/rút báo giá. */
@WebServlet("/seller/custom")
public class SellerCustomServlet extends HttpServlet {

    private static final int PAGE_SIZE = 8;
    private static final int MAX_DAYS = 15;                       // quy định của sàn
    private static final long MIN_PRICE = 1_000L;
    private static final long MAX_PRICE = 1_000_000_000L;

    private final CustomMarketDAO dao = new CustomMarketDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        Flash.transfer(req);

        String tab = "mine".equals(req.getParameter("tab")) ? "mine" : "open";
        try {
            req.setAttribute("shop", shop);
            req.setAttribute("tab", tab);
            req.setAttribute("pendingQuotes", dao.countPendingQuotes(shop.getId()));

            if ("mine".equals(tab)) {
                req.setAttribute("myQuotes", dao.findQuotesByShop(shop.getId()));
            } else {
                String q = req.getParameter("q");
                int page = Math.max(1, parseInt(req.getParameter("page")));
                int total = dao.countOpenRequests(shop.getId(), shop.getOwnerId(), q);
                req.setAttribute("requests", dao.findOpenRequests(shop.getId(), shop.getOwnerId(), q, page, PAGE_SIZE));
                req.setAttribute("q", q);
                req.setAttribute("page", page);
                req.setAttribute("totalPages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        req.getRequestDispatcher("/pages/seller/buyer-requests.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        String action = req.getParameter("action");
        String back = req.getContextPath() + "/seller/custom";

        try {
            if ("quote".equals(action)) {
                int requestId = parseInt(req.getParameter("requestId"));
                long price = parseLong(req.getParameter("price"));
                int days = parseInt(req.getParameter("days"));
                String note = trim(req.getParameter("note"), 1000);
                String portfolio = trim(req.getParameter("portfolio"), 255);

                if (requestId <= 0) {
                    Flash.error(req, "Yêu cầu không hợp lệ.");
                } else if (price < MIN_PRICE || price > MAX_PRICE) {
                    Flash.error(req, "Giá nhận làm phải từ 1,000đ đến 1,000,000,000đ.");
                } else if (days < 1 || days > MAX_DAYS) {
                    Flash.error(req, "Số ngày hoàn thành phải từ 1 đến " + MAX_DAYS + " ngày.");
                } else if (!portfolio.isEmpty() && !portfolio.matches("(?i)^https?://\\S+$")) {
                    Flash.error(req, "Portfolio phải là đường dẫn bắt đầu bằng http:// hoặc https://");
                } else {
                    String err = dao.saveQuote(shop.getId(), shop.getOwnerId(), requestId, price, days, note,
                            portfolio.isEmpty() ? null : portfolio);
                    if (err == null) Flash.success(req, "Đã gửi báo giá. Người mua sẽ xem và so sánh các báo giá.");
                    else Flash.error(req, err);
                }
                resp.sendRedirect(back);
                return;
            }

            if ("withdraw".equals(action)) {
                boolean ok = dao.withdrawQuote(parseInt(req.getParameter("quoteId")), shop.getId());
                if (ok) Flash.success(req, "Đã rút báo giá.");
                else Flash.error(req, "Không rút được: báo giá không tồn tại hoặc đã được xử lý.");
                resp.sendRedirect(back + "?tab=mine");
                return;
            }

            Flash.error(req, "Hành động không hợp lệ.");
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(back);   // PRG: tránh gửi lại khi F5
    }

    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
    private ShopDTO getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return null;
        }
        try {
            int userId = ((Number) session.getAttribute("userId")).intValue();
            ShopDTO shop = shopDAO.findActiveByOwner(userId);
            if (shop == null) resp.sendRedirect(req.getContextPath() + "/pages/home");
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    private int parseInt(String s) {
        try { return s == null ? 0 : Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    private long parseLong(String s) {
        try { return s == null ? 0 : Long.parseLong(s.trim()); } catch (NumberFormatException e) { return 0; }
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }
}
