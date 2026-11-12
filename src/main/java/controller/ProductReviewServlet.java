package controller;

import dao.ProductDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet(urlPatterns = {"/pages/product-review", "/product-review"})
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 9 * 1024 * 1024)
public class ProductReviewServlet extends HttpServlet {
    private static final String UPLOAD_DIR = "uploads/reviews";
    private static final int MAX_IMAGES = 4;
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
        comment = comment == null ? "" : comment.trim();
        List<String> savedImages = new ArrayList<>();

        if (userId != null && isBuyer && productId > 0 && orderId > 0 && rating >= 1 && rating <= 5
                && comment.length() <= 2000) {
            try {
                List<Part> images = request.getParts().stream()
                        .filter(part -> "reviewImages".equals(part.getName()) && part.getSize() > 0).toList();
                if (images.size() > MAX_IMAGES) {
                    result = "too-many-images";
                } else {
                    for (Part image : images) {
                        String extension = imageExtension(image);
                        if (extension == null) {
                            result = "invalid-image";
                            break;
                        }
                        Path directory = Path.of(getServletContext().getRealPath("/" + UPLOAD_DIR));
                        Files.createDirectories(directory);
                        String filename = UUID.randomUUID() + "." + extension;
                        image.write(directory.resolve(filename).toString());
                        savedImages.add(UPLOAD_DIR + "/" + filename);
                    }
                    if (!"invalid-image".equals(result)) {
                        if (productDAO.insertProductReview(userId, productId, orderId, rating,
                                comment.isEmpty() ? null : comment, savedImages)) result = "success";
                        else result = "not-eligible";
                    }
                }
            } catch (SQLException exception) {
                getServletContext().log("Unable to save product review", exception);
                result = "error";
            } catch (jakarta.servlet.ServletException exception) {
                getServletContext().log("Unable to read product review upload", exception);
                result = "invalid-image";
            } catch (IllegalStateException exception) {
                result = "invalid-image";
            } finally {
                if (!"success".equals(result)) deleteSavedImages(savedImages);
            }
        } else if (userId == null || !isBuyer) {
            result = "login";
        }
        response.sendRedirect(request.getContextPath() + "/pages/product-detail?id=" + productId + "&review=" + result + "#reviews");
    }

    private String imageExtension(Part part) throws IOException {
        byte[] header = new byte[12];
        int length;
        try (InputStream input = part.getInputStream()) { length = input.readNBytes(header, 0, header.length); }
        if (length >= 3 && (header[0] & 0xff) == 0xff && (header[1] & 0xff) == 0xd8 && (header[2] & 0xff) == 0xff) return "jpg";
        if (length >= 8 && (header[0] & 0xff) == 0x89 && header[1] == 'P' && header[2] == 'N' && header[3] == 'G') return "png";
        if (length >= 12 && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P') return "webp";
        return null;
    }

    private void deleteSavedImages(List<String> images) {
        String realPath = getServletContext().getRealPath("/");
        if (realPath == null) return;
        for (String image : images) {
            try { Files.deleteIfExists(Path.of(realPath, image)); }
            catch (IOException exception) { getServletContext().log("Unable to remove unused review image", exception); }
        }
    }

    private int parseInt(String value) {
        try { return Integer.parseInt(value); } catch (NumberFormatException exception) { return 0; }
    }

    private Integer sessionUserId(Object value) {
        if (value instanceof Number number && number.intValue() > 0) return number.intValue();
        return null;
    }
}
