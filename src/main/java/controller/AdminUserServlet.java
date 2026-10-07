package controller;

import com.google.gson.Gson;
import dao.UserAdminDAO;
import dto.UserAdminResponseDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/users")
public class AdminUserServlet extends HttpServlet {

    private UserAdminDAO userAdminDAO;

    @Override
    public void init() throws ServletException {
        userAdminDAO = new UserAdminDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        UserAdminResponseDTO data = userAdminDAO.getUsersData();

        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(data));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            java.io.BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            Gson gson = new Gson();
            java.util.Map<String, Object> map = gson.fromJson(sb.toString(), new com.google.gson.reflect.TypeToken<java.util.Map<String, Object>>(){}.getType());
            
            int userId = ((Double) map.get("userId")).intValue();
            String status = (String) map.get("status");
            
            boolean success = userAdminDAO.updateUserStatus(userId, status);
            
            if (success) {
                response.getWriter().write("{\"success\": true}");
            } else {
                response.setStatus(400);
                response.getWriter().write("{\"success\": false, \"message\": \"Failed to update database\"}");
            }
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\": false, \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}
