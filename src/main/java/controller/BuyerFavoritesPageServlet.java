package controller;

import dao.ProfileDAO;
import dto.UserProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet({"/pages/profile-designs", "/pages/profile-favorites"})
public class BuyerFavoritesPageServlet extends HttpServlet {
    private final ProfileDAO profileDao = new ProfileDAO();

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
            UserProfileDTO profile = profileDao.findUser(userId.intValue());
            if (profile == null) {
                response.sendRedirect(request.getContextPath() + "/pages/login");
                return;
            }
            request.setAttribute("profile", profile);
            request.getRequestDispatcher("/pages/profile-designs.jsp").forward(request, response);
        } catch (Exception exception) {
            getServletContext().log("Unable to load buyer saved designs page", exception);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
