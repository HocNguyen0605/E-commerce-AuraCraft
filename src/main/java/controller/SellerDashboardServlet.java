package controller;

import dao.SellerStatsDAO;
import dao.ShopDAO;
import dao.WithdrawalDAO;
import dto.DailyRevenueDTO;
import dto.ShopDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.Flash;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.List;

@WebServlet("/pages/seller/seller-dashboard")
public class SellerDashboardServlet extends HttpServlet {
    private static final String PAGE_PATH = "/pages/seller/seller-dashboard";        // URL servlet (không .jsp)
    private static final String DASHBOARD_VIEW = "/pages/seller/seller-dashboard.jsp"; // JSP thật

    private static final int MAX_RANGE_DAYS = 366;   // giới hạn khoảng xem biểu đồ
    private static final int DAILY_LIMIT_DAYS = 31;  // > 31 ngày thì gộp theo tháng
    private static final int TOP_LIMIT = 5;
    private static final int LEDGER_PAGE_SIZE = 10;

    private final SellerStatsDAO stats = new SellerStatsDAO();
    private final WithdrawalDAO withdrawalDAO = new WithdrawalDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        Flash.transfer(req);
        req.setAttribute("shop", shop);
        req.setAttribute("sellerTab", "dashboard");

        // ---------- Bộ lọc khoảng ngày (biểu đồ + chỉ số + sản phẩm bán chạy) ----------
        LocalDate to = parseDate(req.getParameter("to"), LocalDate.now());
        LocalDate from = parseDate(req.getParameter("from"), to.minusDays(6));   // mặc định 7 ngày gần nhất
        if (from.isAfter(to)) { LocalDate tmp = from; from = to; to = tmp; }
        if (ChronoUnit.DAYS.between(from, to) + 1 > MAX_RANGE_DAYS) {
            from = to.minusDays(MAX_RANGE_DAYS - 1);
            req.setAttribute("rangeNote", "Chỉ xem được tối đa " + MAX_RANGE_DAYS + " ngày mỗi lần, đã tự điều chỉnh ngày bắt đầu.");
        }
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        String sort = "revenue".equals(req.getParameter("sort")) ? "revenue" : "qty";

        // ---------- Bộ lọc lịch sử giao dịch ----------
        LocalDate lFrom = parseDate(req.getParameter("lfrom"), null);
        LocalDate lTo = parseDate(req.getParameter("lto"), null);
        if (lFrom != null && lTo != null && lFrom.isAfter(lTo)) { LocalDate tmp = lFrom; lFrom = lTo; lTo = tmp; }
        String lType = oneOf(req.getParameter("ltype"), "sale", "withdraw");
        String lStatus = oneOf(req.getParameter("lstatus"), "success", "pending", "failed");
        String lq = req.getParameter("lq") == null ? "" : req.getParameter("lq").trim();
        if (lq.length() > 50) lq = lq.substring(0, 50);
        int page = parseInt(req.getParameter("page"), 1);

        DateTimeFormatter vn = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        req.setAttribute("fromDate", from.toString());
        req.setAttribute("toDate", to.toString());
        req.setAttribute("fromLabel", from.format(vn));
        req.setAttribute("toLabel", to.format(vn));
        req.setAttribute("sort", sort);
        req.setAttribute("lfrom", lFrom == null ? "" : lFrom.toString());
        req.setAttribute("lto", lTo == null ? "" : lTo.toString());
        req.setAttribute("ltype", lType);
        req.setAttribute("lstatus", lStatus);
        req.setAttribute("lq", lq);

        // Query string dùng cho liên kết phân trang / nút "Xóa lọc"
        StringBuilder range = new StringBuilder();
        add(range, "from", from.toString());
        add(range, "to", to.toString());
        add(range, "sort", sort);
        StringBuilder all = new StringBuilder(range);
        add(all, "lfrom", lFrom == null ? "" : lFrom.toString());
        add(all, "lto", lTo == null ? "" : lTo.toString());
        add(all, "ltype", lType);
        add(all, "lstatus", lStatus);
        add(all, "lq", lq);
        req.setAttribute("rangeQuery", range.toString());
        req.setAttribute("ledgerBase", all.toString());

        try {
            // Thẻ thống kê
            req.setAttribute("balance", stats.availableBalance(shop.getId()));
            req.setAttribute("revenueMonth", stats.revenueThisMonth(shop.getId()));
            req.setAttribute("pendingOrders", stats.pendingOrders(shop.getId()));
            req.setAttribute("pendingWithdraw", stats.pendingWithdrawAmount(shop.getId()));

            // Chỉ số + so sánh với kỳ trước có cùng độ dài
            long revenue = stats.revenueBetween(shop.getId(), from, to);
            int orders = stats.orderCountBetween(shop.getId(), from, to);
            LocalDate prevTo = from.minusDays(1);
            LocalDate prevFrom = prevTo.minusDays(days - 1);
            long prevRevenue = stats.revenueBetween(shop.getId(), prevFrom, prevTo);
            req.setAttribute("rangeRevenue", revenue);
            req.setAttribute("rangeOrders", orders);
            req.setAttribute("rangeAvg", orders == 0 ? 0 : revenue / orders);
            if (prevRevenue > 0) req.setAttribute("revenueGrowth", (revenue - prevRevenue) * 100.0 / prevRevenue);
            req.setAttribute("prevLabel", prevFrom.format(vn) + " – " + prevTo.format(vn));

            // Biểu đồ
            boolean monthly = days > DAILY_LIMIT_DAYS;
            List<DailyRevenueDTO> chart = stats.revenueByRange(shop.getId(), from, to, monthly);
            req.setAttribute("chart", chart);
            req.setAttribute("monthly", monthly);

            // Sản phẩm bán chạy
            req.setAttribute("topProducts", stats.topProducts(shop.getId(), from, to, sort, TOP_LIMIT));

            // Lịch sử giao dịch
            SellerStatsDAO.LedgerPage lp = stats.ledger(shop.getId(), lFrom, lTo, lType, lStatus, lq, page, LEDGER_PAGE_SIZE);
            int pages = Math.max(1, (int) Math.ceil(lp.total / (double) LEDGER_PAGE_SIZE));
            if (page > pages) {  // trang vượt quá → lấy lại trang cuối
                page = pages;
                lp = stats.ledger(shop.getId(), lFrom, lTo, lType, lStatus, lq, page, LEDGER_PAGE_SIZE);
            }
            req.setAttribute("ledger", lp.items);
            req.setAttribute("ledgerTotal", lp.total);
            req.setAttribute("ledgerPage", Math.max(page, 1));
            req.setAttribute("ledgerPages", pages);
        } catch (Exception e) {
            e.printStackTrace();
            req.setAttribute("error", "Không tải được số liệu: " + e.getMessage());
        }
        req.getRequestDispatcher(DASHBOARD_VIEW).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        try {
            if ("withdraw".equals(req.getParameter("action"))) {
                long amount = Long.parseLong(req.getParameter("amount").trim());

                if (amount <= 0) {
                    Flash.error(req, "Số tiền rút phải lớn hơn 0.");
                } else if (amount < 50000) {
                    Flash.error(req, "Số tiền rút tối thiểu là 50,000đ.");
                } else {
                    String bank = req.getParameter("bankName");
                    String acc = req.getParameter("bankAccount");
                    String holder = req.getParameter("accountHolder");

                    if (isBlank(bank) || isBlank(acc) || isBlank(holder)) {
                        Flash.error(req, "Vui lòng nhập đủ thông tin tài khoản ngân hàng.");
                    } else {
                        String err = withdrawalDAO.create(shop.getId(), amount, bank.trim(), acc.trim(), holder.trim());
                        if (err == null) {
                            Flash.success(req, "Đã gửi yêu cầu rút tiền, đang chờ duyệt.");
                        } else {
                            Flash.error(req, err);
                        }
                    }
                }
            }
        } catch (NumberFormatException e) {
            Flash.error(req, "Số tiền không hợp lệ. Vui lòng nhập số nguyên.");
        } catch (Exception e) {
            Flash.error(req, "Lỗi hệ thống: " + e.getMessage());
            e.printStackTrace();
        }

        resp.sendRedirect(req.getContextPath() + PAGE_PATH);   // PRG
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
            if (shop == null) {
                resp.sendRedirect(req.getContextPath() + "/pages/home");
            }
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    // ---------- helpers ----------

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static LocalDate parseDate(String s, LocalDate def) {
        if (s == null || s.isBlank()) return def;
        try { return LocalDate.parse(s.trim()); } catch (DateTimeParseException e) { return def; }
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s.trim()); } catch (Exception e) { return def; }
    }

    /** Trả về s nếu nằm trong danh sách cho phép, ngược lại trả chuỗi rỗng. */
    private static String oneOf(String s, String... allowed) {
        if (s != null) for (String a : allowed) if (a.equals(s)) return s;
        return "";
    }

    private static void add(StringBuilder sb, String key, String value) {
        if (value == null || value.isEmpty()) return;
        if (sb.length() > 0) sb.append('&');
        sb.append(key).append('=').append(URLEncoder.encode(value, StandardCharsets.UTF_8));
    }
}