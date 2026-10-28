package controller;

import dao.ShopDAO;
import dao.ShopFollowDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Locale;

@WebServlet("/shop-follow")
public class ShopFollowServlet extends HttpServlet {
    private final ShopDAO shopDAO = new ShopDAO();
    private final ShopFollowDAO followDAO = new ShopFollowDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        int shopId = parsePositiveInt(request.getParameter("shopId"));
        Object rawUserId = request.getSession(false) == null ? null : request.getSession(false).getAttribute("userId");
        Object rawRole = request.getSession(false) == null ? null : request.getSession(false).getAttribute("role");
        if (!(rawUserId instanceof Number userId) || rawRole == null
                || !"buyer".equals(rawRole.toString().toLowerCase(Locale.ROOT))) {
            response.sendRedirect(request.getContextPath() + "/pages/login");
            return;
        }
        if (shopId < 1) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        try {
            if (shopDAO.findActiveById(shopId) == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            boolean following = "follow".equals(request.getParameter("action"));
            followDAO.setFollowing(userId.intValue(), shopId, following);
            response.sendRedirect(request.getContextPath() + "/pages/shop-profile?id=" + shopId + "#shop-actions");
        } catch (SQLException exception) {
            getServletContext().log("Unable to update shop follow state", exception);
            throw new ServletException(exception);
        }
    }

    private int parsePositiveInt(String value) {
        try { int parsed = Integer.parseInt(value); return parsed > 0 ? parsed : 0; }
        catch (NumberFormatException exception) { return 0; }
    }
}
