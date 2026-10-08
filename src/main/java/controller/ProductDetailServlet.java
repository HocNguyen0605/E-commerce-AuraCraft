package controller;

import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(urlPatterns = {"/pages/product-detail", "/product-detail"})
public class ProductDetailServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(request.getParameter("id"));
            if (id < 1) throw new NumberFormatException();
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy sản phẩm.");
            return;
        }

        try {
            Map<String, Object> product = productDAO.findActiveProductDetails(id);
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Sản phẩm không tồn tại hoặc đã ngừng bán.");
                return;
            }
            request.setAttribute("product", product);
            Object cartUserId = request.getSession(false) == null ? null
                    : request.getSession(false).getAttribute("userId");
            Object cartRole = request.getSession(false) == null ? null
                    : request.getSession(false).getAttribute("role");
            request.setAttribute("databaseCartAvailable",
                    cartUserId instanceof Number number && number.intValue() > 0 && "buyer".equals(cartRole));
            String description = (String) product.get("description");
            String introduction = description == null ? "" : description.trim();
            if (introduction.length() > 180) introduction = introduction.substring(0, 177).trim() + "…";
            request.setAttribute("productIntroduction", introduction);
            int categoryId = (Integer) product.get("categoryId");
            List<String> storedImages = productDAO.findProductImages(id);

            List<String> images = new java.util.ArrayList<>();
            for (String storedImage : storedImages) {
                String image = resolveStoredImage(storedImage);
                if (image != null && !images.contains(image)) images.add(image);
            }
            if (images.isEmpty()) images = fallbackImages(categoryId);
            request.setAttribute("productImages", images);
            request.setAttribute("reviews", productDAO.findProductReviews(id, 20));
            Object sessionUserId = request.getSession(false) == null ? null
                    : request.getSession(false).getAttribute("userId");
            String sessionRole = request.getSession(false) == null ? null
                    : (String) request.getSession(false).getAttribute("role");
            if (sessionUserId instanceof Number userId && userId.intValue() > 0 && "buyer".equals(sessionRole)) {
                request.setAttribute("reviewableOrders", productDAO.findReviewableOrders(userId.intValue(), id));
                request.setAttribute("reviewSignedIn", true);
            } else {
                request.setAttribute("reviewableOrders", List.of());
                request.setAttribute("reviewSignedIn", false);
            }
            request.setAttribute("reviewStatus", request.getParameter("review"));
            List<Map<String, Object>> similarProducts = productDAO.findSimilarProducts(
                    id, (Integer) product.get("categoryId"), 8);
            for (Map<String, Object> similar : similarProducts) {
                similar.put("image", resolveImage((String) similar.get("image"), categoryId, 0));
            }
            request.setAttribute("similarProducts", similarProducts);
            request.setAttribute("productDetailReady", true);
            request.getRequestDispatcher("/pages/product-detail.jsp").forward(request, response);
        } catch (SQLException exception) {
            getServletContext().log("Unable to load product details", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Hiện không thể tải chi tiết sản phẩm. Vui lòng thử lại sau.");
        }
    }

    private String resolveImage(String storedPath, int categoryId, int fallbackIndex) {
        if (storedPath != null && (storedPath.startsWith("https://") || storedPath.startsWith("http://"))) {
            return storedPath;
        }
        if (storedPath != null && !storedPath.isBlank()) {
            String path = storedPath.replace('\\', '/').replaceFirst("^/+", "");
            String webPath = path.startsWith("assets/") ? path
                    : path.startsWith("assets/img/") ? path : "assets/img/" + path;
            try {
                if (getServletContext().getResource("/" + webPath) != null) {
                    return getServletContext().getContextPath() + "/" + webPath;
                }
            } catch (IOException ignored) {
                // Use the category image when a stored image path is missing from the webapp.
            }
        }
        List<String> fallbacks = fallbackImages(categoryId);
        return fallbacks.isEmpty() ? null : fallbacks.get(Math.floorMod(fallbackIndex, fallbacks.size()));
    }

    private String resolveStoredImage(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return null;
        if (storedPath.startsWith("https://") || storedPath.startsWith("http://")) return storedPath;

        String path = storedPath.replace('\\', '/').replaceFirst("^/+", "");
        String webPath = path.startsWith("assets/") ? path : "assets/img/" + path;
        try {
            return getServletContext().getResource("/" + webPath) == null
                    ? null : getServletContext().getContextPath() + "/" + webPath;
        } catch (IOException ignored) {
            return null;
        }
    }

    private List<String> fallbackImages(int categoryId) {
        List<String> files = switch (categoryId) {
            case 2 -> List.of("necklace_02.jpg", "necklace_03.jpg", "necklace_04.jpg");
            case 3 -> List.of("phoneStrap_01.jpg", "phoneStrap_02.jpg", "phoneStrap_03.jpg");
            default -> List.of("bracelet_01.jpg", "bracelet_02.jpg", "bracelet_03.jpg");
        };
        return files.stream().map(file -> getServletContext().getContextPath()
                + "/assets/img/products/" + file).filter(url -> {
            try {
                return getServletContext().getResource(url.substring(getServletContext().getContextPath().length())) != null;
            } catch (IOException ignored) {
                return false;
            }
        }).toList();
    }
}
