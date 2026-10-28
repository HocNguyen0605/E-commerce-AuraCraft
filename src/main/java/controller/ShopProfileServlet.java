package controller;

import dao.CustomMarketDAO;
import dao.ProductDAO;
import dao.ShopDAO;
import dao.ShopFollowDAO;
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

@WebServlet("/pages/shop-profile")
public class ShopProfileServlet extends HttpServlet {
    private static final int PAGE_SIZE = 12;
    private final ShopDAO shopDAO = new ShopDAO();
    private final ShopFollowDAO followDAO = new ShopFollowDAO();
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int shopId;
        try {
            shopId = Integer.parseInt(request.getParameter("id"));
            if (shopId < 1) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy gian hàng.");
            return;
        }

        try {
            ShopDTO shop = shopDAO.findActiveById(shopId);
            if (shop == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Gian hàng không tồn tại hoặc đã ngừng hoạt động.");
                return;
            }

            int totalProducts = productDAO.countActiveProductsByShop(shopId);
            java.util.Map<String, Object> reviewSummary = shopDAO.getShopReviewSummary(shopId);
            HttpSession session = request.getSession(false);
            Object sessionUserId = session == null ? null : session.getAttribute("userId");
            Object sessionRole = session == null ? null : session.getAttribute("role");
            boolean isBuyer = sessionUserId instanceof Number && sessionRole != null
                    && "buyer".equalsIgnoreCase(sessionRole.toString());
            int followerCount = followDAO.countFollowers(shopId);
            boolean following = isBuyer && followDAO.isFollowing(((Number) sessionUserId).intValue(), shopId);

            int totalPages = Math.max(1, (totalProducts + PAGE_SIZE - 1) / PAGE_SIZE);
            int page = 1;
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException ignored) {
                // Use the first page for missing or invalid page numbers.
            }
            page = Math.max(1, Math.min(page, totalPages));

            request.setAttribute("shop", shop);
            request.setAttribute("products", productDAO.findActiveProductsByShop(
                    shopId, PAGE_SIZE, (page - 1) * PAGE_SIZE));
            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("shopRating", reviewSummary.get("rating"));
            request.setAttribute("shopReviewCount", reviewSummary.get("count"));
            request.setAttribute("shopReviews", shopDAO.findShopReviews(shopId, 20));
            request.setAttribute("successfulCustomOrders", shopDAO.countSuccessfulCustomOrders(shopId));
            request.setAttribute("shopFollowerCount", followerCount);
            request.setAttribute("shopFollowing", following);
            request.setAttribute("isBuyer", isBuyer);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("pageStart", Math.max(1, page - 2));
            request.setAttribute("pageEnd", Math.min(totalPages, page + 2));
            request.getRequestDispatcher("/pages/shop-profile.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load public shop profile", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Hiện không thể tải gian hàng. Vui lòng thử lại sau.");
        }
    }


}
