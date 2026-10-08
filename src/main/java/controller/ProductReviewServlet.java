package controller;

import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/product-review")
public class ProductReviewServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        int productId = parseInt(request.getParameter("productId"));
        String result = "invalid";
        HttpSession session = request.getSession(false);
        Integer userId = session == null ? null : sessionUserId(session.getAttribute("userId"));
        boolean isBuyer = session != null && "buyer".equals(session.getAttribute("role"));
        int orderId = parseInt(request.getParameter("orderId"));
        int rating = parseInt(request.getParameter("rating"));
        String comment = request.getParameter("comment");

        if (userId != null && isBuyer && productId > 0 && orderId > 0 && rating >= 1 && rating <= 5
                && comment != null && !comment.isBlank() && comment.trim().length() <= 2000) {
            try {
                if (productDAO.insertProductReview(userId, productId, orderId, rating, comment.trim())) result = "success";
                else result = "not-eligible";
            } catch (SQLException exception) {
                getServletContext().log("Unable to save product review", exception);
                result = "error";
            }
        } else if (userId == null || !isBuyer) {
            result = "login";
        }
        response.sendRedirect(request.getContextPath() + "/product-detail?id=" + productId + "&review=" + result + "#reviews");
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException exception) { return 0; }
    }

    private Integer sessionUserId(Object value) {
        if (value instanceof Number number && number.intValue() > 0) return number.intValue();
        return null;
    }
}
