package controller;

import dao.OrderDAO;
import dao.ProfileDAO;
import dto.UserProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet(urlPatterns = {"/profile-orders", "/pages/profile", "/pages/profile-orders"})
public class ProfileOrdersServlet extends HttpServlet {
    private final ProfileDAO profileDao = new ProfileDAO();
    private final OrderDAO orderDao = new OrderDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return;
        }
        try {
            UserProfileDTO profile = profileDao.findUser(userId);
            if (profile == null) {
                resp.sendRedirect(req.getContextPath() + "/pages/login");
                return;
            }
            req.setAttribute("profile", profile);
            req.setAttribute("orders", orderDao.findByUser(userId));
            req.getRequestDispatcher("/pages/profile-orders.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
