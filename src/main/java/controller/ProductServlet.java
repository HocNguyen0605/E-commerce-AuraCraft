package controller;

import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet(urlPatterns = {"/pages/products", "/products"})
public class ProductServlet extends HttpServlet {
    private static final int MAX_KEYWORD_LENGTH = 100;
    private static final int PAGE_SIZE = 12;
    private ProductDAO productDAO;

    @Override
    public void init() {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            List<java.util.Map<String, Object>> categories = productDAO.findActiveCategories();
            Set<Integer> availableCategoryIds = new HashSet<>();
            for (java.util.Map<String, Object> category : categories) {
                availableCategoryIds.add((Integer) category.get("id"));
            }

            List<Integer> selectedCategories = new ArrayList<>();
            String[] requestedCategories = request.getParameterValues("category");
            boolean categoryFilterActive = "1".equals(request.getParameter("categoryFilter"))
                    || requestedCategories != null;
            if (requestedCategories != null) {
                for (String value : requestedCategories) {
                    try {
                        int id = Integer.parseInt(value);
                        if (availableCategoryIds.contains(id) && !selectedCategories.contains(id)) {
                            selectedCategories.add(id);
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignore invalid category query values.
                    }
                }
            }
            if (!categoryFilterActive) {
                for (java.util.Map<String, Object> category : categories) {
                    selectedCategories.add((Integer) category.get("id"));
                }
            }
            for (java.util.Map<String, Object> category : categories) {
                category.put("selected", selectedCategories.contains(category.get("id")));
            }

            List<String> selectedPrices = new ArrayList<>();
            String[] requestedPrices = request.getParameterValues("price");
            if (requestedPrices != null) {
                for (String value : requestedPrices) {
                    if (List.of("under-100000", "100000-300000", "300000-500000", "over-500000").contains(value)
                            && !selectedPrices.contains(value)) {
                        selectedPrices.add(value);
                    }
                }
            }

            List<Integer> selectedRatings = new ArrayList<>();
            List<String> selectedRatingValues = new ArrayList<>();
            String[] requestedRatings = request.getParameterValues("rating");
            if (requestedRatings != null) {
                for (String value : requestedRatings) {
                    try {
                        int rating = Integer.parseInt(value);
                        if (rating >= 1 && rating <= 5 && !selectedRatings.contains(rating)) {
                            selectedRatings.add(rating);
                            selectedRatingValues.add(value);
                        }
                    } catch (NumberFormatException ignored) {
                        // Ignore invalid rating query values.
                    }
                }
            }

            String sort = request.getParameter("sort");
            if (sort == null || !List.of("default", "price-asc", "price-desc", "rating-desc", "newest").contains(sort)) {
                sort = "default";
            }

            String keyword = request.getParameter("q");
            if (keyword != null) {
                keyword = keyword.trim();
                if (keyword.length() > MAX_KEYWORD_LENGTH) keyword = keyword.substring(0, MAX_KEYWORD_LENGTH);
                if (keyword.isBlank()) keyword = null;
            }

            int totalProducts = productDAO.countActiveProducts(
                    selectedCategories, selectedPrices, selectedRatings, keyword);
            int totalPages = Math.max(1, totalProducts / PAGE_SIZE
                    + (totalProducts % PAGE_SIZE == 0 ? 0 : 1));
            int page = 1;
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException ignored) {
                // Use the first page for missing or invalid page numbers.
            }
            page = Math.max(1, Math.min(page, totalPages));
            int offset = (page - 1) * PAGE_SIZE;
            int firstProduct = totalProducts == 0 ? 0 : offset + 1;
            int lastProduct = Math.min(offset + PAGE_SIZE, totalProducts);
            int pageStart = Math.max(1, page - 2);
            int pageEnd = Math.min(totalPages, page + 2);

            request.setAttribute("categories", categories);
            request.setAttribute("selectedCategories", selectedCategories);
            request.setAttribute("allCategoriesSelected", !categories.isEmpty()
                    && selectedCategories.size() == categories.size());
            request.setAttribute("categoryFilterActive", categoryFilterActive);
            request.setAttribute("selectedPrices", selectedPrices);
            request.setAttribute("selectedRatingValues", selectedRatingValues);
            request.setAttribute("priceUnder100000Selected", selectedPrices.contains("under-100000"));
            request.setAttribute("price100000To300000Selected", selectedPrices.contains("100000-300000"));
            request.setAttribute("price300000To500000Selected", selectedPrices.contains("300000-500000"));
            request.setAttribute("priceOver500000Selected", selectedPrices.contains("over-500000"));
            request.setAttribute("rating5Selected", selectedRatingValues.contains("5"));
            request.setAttribute("rating4Selected", selectedRatingValues.contains("4"));
            request.setAttribute("rating3Selected", selectedRatingValues.contains("3"));
            request.setAttribute("rating2Selected", selectedRatingValues.contains("2"));
            request.setAttribute("rating1Selected", selectedRatingValues.contains("1"));
            request.setAttribute("sort", sort);
            request.setAttribute("keyword", keyword == null ? "" : keyword);
            request.setAttribute("page", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalProducts", totalProducts);
            request.setAttribute("firstProduct", firstProduct);
            request.setAttribute("lastProduct", lastProduct);
            request.setAttribute("pageStart", pageStart);
            request.setAttribute("pageEnd", pageEnd);
            request.setAttribute("products", productDAO.findActiveProducts(
                    selectedCategories, selectedPrices, selectedRatings, keyword, sort, PAGE_SIZE, offset));
            request.setAttribute("databaseAvailable", true);
        } catch (SQLException | RuntimeException exception) {
            getServletContext().log("Unable to load product catalogue from database", exception);
            request.setAttribute("databaseAvailable", false);
            request.setAttribute("categories", List.of());
            request.setAttribute("products", List.of());
            request.setAttribute("selectedCategories", List.of());
            request.setAttribute("allCategoriesSelected", false);
            request.setAttribute("selectedPrices", List.of());
            request.setAttribute("selectedRatingValues", List.of());
            request.setAttribute("priceUnder100000Selected", false);
            request.setAttribute("price100000To300000Selected", false);
            request.setAttribute("price300000To500000Selected", false);
            request.setAttribute("priceOver500000Selected", false);
            request.setAttribute("rating5Selected", false);
            request.setAttribute("rating4Selected", false);
            request.setAttribute("rating3Selected", false);
            request.setAttribute("rating2Selected", false);
            request.setAttribute("rating1Selected", false);
            request.setAttribute("categoryFilterActive", false);
            request.setAttribute("keyword", "");
            request.setAttribute("sort", "default");
            request.setAttribute("page", 1);
            request.setAttribute("totalPages", 1);
            request.setAttribute("totalProducts", 0);
            request.setAttribute("firstProduct", 0);
            request.setAttribute("lastProduct", 0);
            request.setAttribute("pageStart", 1);
            request.setAttribute("pageEnd", 1);
        }

        request.setAttribute("productListingReady", true);
        request.getRequestDispatcher("/pages/products.jsp").forward(request, response);
    }
}
