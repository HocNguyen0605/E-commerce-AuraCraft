package controller;

import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Map<String, Object>> featuredProducts = productDAO.getFeaturedProducts();
            request.setAttribute("featuredProducts", featuredProducts);
        } catch (Exception e) {
            request.setAttribute("featuredProducts", Collections.emptyList());
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
