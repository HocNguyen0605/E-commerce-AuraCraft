package controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.ChatMessageDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.GeminiService;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = {"/api/chat", "/api/chat/reset"})
public class ChatbotServlet extends HttpServlet {
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json; charset=UTF-8");

        JsonObject jsonResponse = new JsonObject();
        try {
            // Đọc dữ liệu JSON gửi lên từ client
            BufferedReader reader = request.getReader();
            JsonObject reqJson = JsonParser.parseReader(reader).getAsJsonObject();
            String message = reqJson.has("message") ? reqJson.get("message").getAsString().trim() : "";

            if (message.isEmpty()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("reply", "Tin nhắn không được để trống!");
                response.getWriter().print(gson.toJson(jsonResponse));
                return;
            }

            // Lấy hoặc tạo mới danh sách lịch sử chat trong session
            HttpSession session = request.getSession(true);
            @SuppressWarnings("unchecked")
            List<ChatMessageDTO> history = (List<ChatMessageDTO>) session.getAttribute("chatHistory");
            if (history == null) {
                history = new ArrayList<>();
                session.setAttribute("chatHistory", history);
            }

            // Gọi AI service để sinh câu trả lời
            String reply = GeminiService.chat(message, history);

            // Lưu tin nhắn mới vào session
            history.add(new ChatMessageDTO("user", message));
            history.add(new ChatMessageDTO("model", reply));

            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("reply", reply);

        } catch (Exception e) {
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("reply", "Có lỗi xảy ra: " + e.getMessage());
        }

        response.getWriter().print(gson.toJson(jsonResponse));
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        // Xóa lịch sử trong session khi làm mới hội thoại
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("chatHistory");
        }
        JsonObject json = new JsonObject();
        json.addProperty("status", "success");
        json.addProperty("message", "Đã xóa lịch sử chat.");
        response.getWriter().print(gson.toJson(json));
    }
}
