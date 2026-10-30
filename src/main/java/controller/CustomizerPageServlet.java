package controller;

import com.google.gson.Gson;
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

@WebServlet("/pages/customizer")
public class CustomizerPageServlet extends HttpServlet {
    private final ProductDAO productDAO = new ProductDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer shopId = parsePositiveId(request.getParameter("shopId"));
        try {
            List<Map<String, Object>> sources = productDAO.findActiveCustomizerSources(shopId);
            for (Map<String, Object> source : sources) {
                source.put("baseImage", resolveImage(request, (String) source.get("baseImage")));
//                @SuppressWarnings("unchecked")
                List<Map<String, Object>> charms = (List<Map<String, Object>>) source.get("charms");
                for (Map<String, Object> charm : charms) {
                    charm.put("image", resolveImage(request, (String) charm.get("image")));
                }
            }
            request.setAttribute("customizerSourcesJson", gson.toJson(Map.of("sources", sources)));
        } catch (SQLException | RuntimeException exception) {
            getServletContext().log("Unable to load customizer materials", exception);
            request.setAttribute("customizerSourcesJson", "{\"sources\":[]}");
        }
        request.getRequestDispatcher("/pages/customizer.jsp").forward(request, response);
    }

    private Integer parsePositiveId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            int id = Integer.parseInt(value);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String resolveImage(HttpServletRequest request, String storedPath) {
        if (storedPath == null || storedPath.isBlank()) return "";
        if (storedPath.startsWith("https://") || storedPath.startsWith("http://")) return storedPath;
        return request.getContextPath() + "/" + storedPath.replace('\\', '/').replaceFirst("^/+", "");
    }
}
