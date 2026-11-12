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

@WebServlet(urlPatterns = {"/home", "/pages/home"})
public class HomeServlet extends HttpServlet {
    private ProductDAO productDAO;

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ProductDAO dao = productDAO;
        try {
            List<Map<String, Object>> bestSellingProducts = dao.findActiveProducts(
                    null, null, null, null, "best", 6, 0);
            request.setAttribute("bestSellingProducts", bestSellingProducts);
            request.setAttribute("bestSellingProductsAvailable", true);
        } catch (Exception e) {
            request.setAttribute("bestSellingProducts", Collections.emptyList());
            request.setAttribute("bestSellingProductsAvailable", false);
        }
        try {
            List<Map<String, Object>> featuredProducts = dao.getFeaturedProducts();
            request.setAttribute("featuredProducts", featuredProducts);
            request.setAttribute("featuredProductsAvailable", true);
        } catch (Exception e) {
            request.setAttribute("featuredProducts", Collections.emptyList());
            request.setAttribute("featuredProductsAvailable", false);
        }
        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }
}
