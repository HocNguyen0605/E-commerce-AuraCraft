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

@WebServlet(urlPatterns = {"/profile-info", "/pages/profile-info"})
public class ProfileInfoServlet extends HttpServlet {
    private final ProfileDAO dao = new ProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        Integer userId = session == null ? null : (Integer) session.getAttribute("userId");
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return;
        }
        try {
            UserProfileDTO profile = dao.findUser(userId);
            if (profile == null) {
                resp.sendRedirect(req.getContextPath() + "/pages/login");
                return;
            }
            req.setAttribute("profile", profile);
            req.setAttribute("addresses", dao.findAddresses(userId));
            req.getRequestDispatcher("/pages/profile-info.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
