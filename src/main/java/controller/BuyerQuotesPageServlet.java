package controller;

import dao.BuyerQuoteDAO;
import dao.ProfileDAO;
import dto.UserProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/pages/profile-quotes")
public class BuyerQuotesPageServlet extends HttpServlet {
    private final ProfileDAO profileDAO = new ProfileDAO();
    private final BuyerQuoteDAO quoteDAO = new BuyerQuoteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Object id = session == null ? null : session.getAttribute("userId");
        if (!(id instanceof Number userId) || userId.intValue() < 1) {
            response.sendRedirect(request.getContextPath() + "/pages/login");
            return;
        }
        try {
            UserProfileDTO profile = profileDAO.findUser(userId.intValue());
            if (profile == null) {
                response.sendRedirect(request.getContextPath() + "/pages/login");
                return;
            }
            request.setAttribute("profile", profile);
            try {
                request.setAttribute("quoteRequests", quoteDAO.findRequests(userId.intValue()));
            } catch (Exception exception) {
                getServletContext().log("Unable to query buyer quote requests", exception);
                request.setAttribute("quoteRequests", java.util.List.of());
                request.setAttribute("quotesError", "Chưa tải được dữ liệu báo giá. Vui lòng tải lại trang sau.");
            }
        } catch (Exception exception) {
            getServletContext().log("Unable to load buyer profile for quotes page", exception);
            request.setAttribute("profile", null);
            request.setAttribute("quoteRequests", java.util.List.of());
            request.setAttribute("quotesError", "Không tải được thông tin tài khoản. Vui lòng thử tải lại trang.");
        }
        request.setAttribute("focusRequestId", request.getParameter("requestId"));
        request.getRequestDispatcher("/pages/profile-quotes.jsp").forward(request, response);
    }
}
