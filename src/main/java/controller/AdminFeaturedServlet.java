package controller;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dao.ProductDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/api/admin/featured")
public class AdminFeaturedServlet extends HttpServlet {
    private ProductDAO productDAO;
    private final Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        productDAO = new ProductDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String action = request.getParameter("action");
        try {
            if ("search".equalsIgnoreCase(action)) {
                String q = request.getParameter("q");
                List<Map<String, Object>> products = productDAO.searchActiveProducts(q, 30);
                response.getWriter().write(gson.toJson(products));
            } else {
                List<Map<String, Object>> featured = productDAO.getFeaturedProducts();
                response.getWriter().write(gson.toJson(featured));
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            List<Integer> productIds = new ArrayList<>();
            String body = sb.toString().trim();

            if (body.startsWith("[")) {
                List<Double> list = gson.fromJson(body, new TypeToken<List<Double>>(){}.getType());
                if (list != null) {
                    for (Double d : list) productIds.add(d.intValue());
                }
            } else {
                Map<String, Object> map = gson.fromJson(body, new TypeToken<Map<String, Object>>(){}.getType());
                if (map != null && map.containsKey("productIds")) {
                    List<?> list = (List<?>) map.get("productIds");
                    for (Object item : list) {
                        if (item instanceof Number) {
                            productIds.add(((Number) item).intValue());
                        }
                    }
                }
            }

            boolean success = productDAO.updateFeaturedProducts(productIds);
            if (success) {
                response.getWriter().write("{\"success\":true,\"message\":\"Lưu sản phẩm nổi bật thành công!\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"success\":false,\"message\":\"Không thể lưu dữ liệu.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}
