package controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        // Kiểm tra userId trong Session (Khớp hoàn toàn với MeServlet)
        if (session == null || session.getAttribute("userId") == null) {
            // Chưa đăng nhập -> Chuyển hướng về trang đăng nhập
            resp.sendRedirect(req.getContextPath() + "/pages/login.html");
            return;
        }

        // Đã đăng nhập -> Chuyển hướng sang trang giao diện profile.html
        resp.sendRedirect(req.getContextPath() + "/pages/profile.html");    }
}