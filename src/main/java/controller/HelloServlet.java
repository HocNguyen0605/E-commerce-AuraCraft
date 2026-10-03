package controller;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.DBConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/hello")
public class HelloServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");

        String idParam = req.getParameter("id");
        int id = (idParam == null) ? 1 : Integer.parseInt(idParam);

        String name = "(không tìm thấy)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT name FROM Users WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) name = rs.getString("name");
            }
        } catch (Exception e) {
            name = "LỖI: " + e.getMessage();
        }

        resp.getWriter().println("<html><body>");
        resp.getWriter().println("<h1>Xin chào, " + name + "!</h1>");
        resp.getWriter().println("</body></html>");
    }
}