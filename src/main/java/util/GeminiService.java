package util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dto.ChatMessageDTO;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class GeminiService {
    private static String apiKey = "";
    private static String modelName = "gemini-3.5-flash-lite";
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    private static final Gson gson = new Gson();

    static {
        // Đọc cấu hình từ file ai.properties trong thư mục resources
        try (InputStream input = GeminiService.class.getClassLoader().getResourceAsStream("ai.properties")) {
            if (input != null) {
                Properties props = new Properties();
                props.load(input);
                apiKey = props.getProperty("gemini.api.key", "").trim();
                modelName = props.getProperty("gemini.model", "gemini-3.5-flash-lite").trim();
            }
        } catch (Exception e) {
            System.err.println("Không thể đọc file ai.properties: " + e.getMessage());
        }
    }

    public static String chat(String userMessage, List<ChatMessageDTO> history) {
        if (apiKey.isEmpty() || apiKey.startsWith("YOUR_")) {
            return "Chưa cấu hình API Key trong file ai.properties. Vui lòng kiểm tra lại!";
        }

        try {
            String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/" + modelName + ":generateContent?key=" + apiKey;

            // Xây dựng JSON gửi lên Gemini
            JsonObject root = new JsonObject();

            // Huấn luyện vai trò cho AI
            JsonObject systemInstruction = new JsonObject();
            JsonArray sysParts = new JsonArray();
            JsonObject sysText = new JsonObject();
            sysText.addProperty("text",
                    "Bạn là AuraBot - Trợ lý tư vấn bán hàng của shop AuraCraft.\n" +
                    "- AuraCraft bán các sản phẩm handmade thủ công như đồ len, nến thơm, phụ kiện, vòng tay, quà tặng.\n" +
                    "- Khách có thể mua hàng có sẵn hoặc đặt làm thiết kế riêng (Customizer/Buyer Request).\n" +
                    "- Freeship cho đơn từ 300.000đ, giao hàng 2-4 ngày, đổi trả miễn phí trong 7 ngày nếu lỗi.\n" +
                    "- Trả lời thân thiện, lịch sự, xưng hô là AuraCraft hoặc mình, gọi khách là bạn."
            );
            sysParts.add(sysText);
            systemInstruction.add("parts", sysParts);
            root.add("systemInstruction", systemInstruction);

            // Nội dung cuộc hội thoại
            JsonArray contents = new JsonArray();
            if (history != null) {
                for (ChatMessageDTO msg : history) {
                    JsonObject turn = new JsonObject();
                    turn.addProperty("role", "user".equalsIgnoreCase(msg.getSenderRole()) ? "user" : "model");
                    JsonArray parts = new JsonArray();
                    JsonObject part = new JsonObject();
                    part.addProperty("text", msg.getContent());
                    parts.add(part);
                    turn.add("parts", parts);
                    contents.add(turn);
                }
            }

            // Thêm tin nhắn hiện tại của người dùng
            JsonObject currentTurn = new JsonObject();
            currentTurn.addProperty("role", "user");
            JsonArray currentParts = new JsonArray();
            JsonObject currentPart = new JsonObject();
            currentPart.addProperty("text", userMessage);
            currentParts.add(currentPart);
            currentTurn.add("parts", currentParts);
            contents.add(currentTurn);

            root.add("contents", contents);

            // Gửi request POST
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(root), StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() == 200) {
                JsonObject resJson = JsonParser.parseString(response.body()).getAsJsonObject();
                return resJson.getAsJsonArray("candidates")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("content")
                        .getAsJsonArray("parts")
                        .get(0).getAsJsonObject()
                        .get("text").getAsString().trim();
            } else {
                return "Hệ thống AI đang phản hồi chậm, bạn vui lòng thử lại sau giây lát nhé!";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Có lỗi khi kết nối tới máy chủ AI: " + e.getMessage();
        }
    }
}
