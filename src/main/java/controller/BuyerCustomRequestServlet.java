package controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dao.BuyerCustomRequestDAO;
import dao.ProfileDAO;
import dto.UserProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@WebServlet("/api/buyer/custom-requests")
public class BuyerCustomRequestServlet extends HttpServlet {
    private static final Pattern DATA_IMAGE = Pattern.compile("^data:image/(png|jpeg|jpg|webp|gif);base64,([A-Za-z0-9+/=]+)$", Pattern.CASE_INSENSITIVE);
    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;
    private static final Gson GSON = new Gson();
    private final BuyerCustomRequestDAO requestDAO = new BuyerCustomRequestDAO();
    private final ProfileDAO profileDAO = new ProfileDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object id = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userId");
        if (!(id instanceof Number userId) || userId.intValue() < 1) {
            write(resp, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập để gửi thiết kế."));
            return;
        }
        try {
            UserProfileDTO profile = profileDAO.findUser(userId.intValue());
            write(resp, 200, Map.of("ok", true, "name", profile == null ? "" : safe(profile.getName()),
                    "email", profile == null ? "" : safe(profile.getEmail())));
        } catch (Exception e) {
            getServletContext().log("Cannot load buyer profile for custom request", e);
            write(resp, 500, Map.of("ok", false, "message", "Không tải được thông tin tài khoản."));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        Object id = req.getSession(false) == null ? null : req.getSession(false).getAttribute("userId");
        String role = req.getSession(false) == null ? null : (String) req.getSession(false).getAttribute("role");
        if (!(id instanceof Number userId) || userId.intValue() < 1 || (role != null && !"buyer".equals(role))) {
            write(resp, 401, Map.of("ok", false, "message", "Vui lòng đăng nhập bằng tài khoản người mua để gửi thiết kế."));
            return;
        }
        List<Path> createdFiles = new ArrayList<>();
        try {
            JsonObject body = JsonParser.parseReader(req.getReader()).getAsJsonObject();
            String type = value(body, "productType").trim();
            String materials = value(body, "materials").trim();
            String description = value(body, "charmDescription").trim();
            long budget = body.has("budget") ? body.get("budget").getAsLong() : 0;
            JsonObject design = body.has("design") && body.get("design").isJsonObject()
                    ? body.getAsJsonObject("design") : new JsonObject();
            if (type.isBlank() || materials.isBlank() || description.isBlank() || budget < 1) {
                write(resp, 400, Map.of("ok", false, "message", "Vui lòng điền đầy đủ loại sản phẩm, vật liệu, mô tả và giá dự kiến."));
                return;
            }
            List<String> designImages = saveImages(body.getAsJsonArray("designImages"), body.getAsJsonArray("designImageUrls"), createdFiles);
            List<String> charmImages = saveImages(body.getAsJsonArray("charmImages"), body.getAsJsonArray("charmImageUrls"), createdFiles);
            String preview = designImages.isEmpty() ? value(body, "previewImage") : designImages.get(0);
            if (preview.isBlank() || preview.length() > 255 || preview.startsWith("data:")) preview = null;
            design.addProperty("productType", type);
            design.addProperty("materials", materials);
            design.addProperty("charmDescription", description);
            design.addProperty("budget", budget);
            if (body.has("design")) design.add("snapshot", body.get("design"));
            int requestId = requestDAO.create(userId.intValue(), type, materials, description, budget,
                    GSON.toJson(design), preview, designImages, charmImages);
            write(resp, 201, Map.of("ok", true, "requestId", requestId,
                    "message", "Đã gửi thiết kế vào chợ báo giá. Bạn có thể theo dõi các báo giá từ thợ."));
        } catch (IllegalArgumentException e) {
            removeFiles(createdFiles);
            write(resp, 400, Map.of("ok", false, "message", e.getMessage()));
        } catch (Exception e) {
            removeFiles(createdFiles);
            getServletContext().log("Cannot publish buyer custom request", e);
            write(resp, 500, Map.of("ok", false, "message", "Chưa gửi được yêu cầu. Vui lòng thử lại."));
        }
    }

    private List<String> saveImages(JsonArray images, JsonArray links, List<Path> createdFiles) throws IOException {
        List<String> urls = new ArrayList<>();
        int fileCount = images == null ? 0 : images.size();
        int linkCount = links == null ? 0 : links.size();
        if (fileCount + linkCount > 5) throw new IllegalArgumentException("Mỗi loại ảnh chỉ được thêm tối đa 5 ảnh hoặc link.");
        if (images != null) for (var element : images) {
            if (!element.isJsonPrimitive()) continue;
            String value = element.getAsString().trim();
            if (value.startsWith("data:")) {
                Matcher matcher = DATA_IMAGE.matcher(value);
                if (!matcher.matches()) throw new IllegalArgumentException("Ảnh tải lên không đúng định dạng.");
                String ext = matcher.group(1).equalsIgnoreCase("jpeg") ? "jpg" : matcher.group(1).toLowerCase();
                byte[] bytes;
                try { bytes = Base64.getDecoder().decode(matcher.group(2)); }
                catch (IllegalArgumentException e) { throw new IllegalArgumentException("Ảnh tải lên bị lỗi."); }
                if (bytes.length == 0 || bytes.length > MAX_IMAGE_BYTES) throw new IllegalArgumentException("Mỗi ảnh không được vượt quá 2MB.");
                String realPath = getServletContext().getRealPath("/uploads/custom-requests");
                if (realPath == null) throw new IOException("Không thể tạo thư mục lưu ảnh.");
                Path directory = Path.of(realPath);
                Files.createDirectories(directory);
                String filename = UUID.randomUUID() + "." + ext;
                Path target = directory.resolve(filename);
                Files.write(target, bytes);
                createdFiles.add(target);
                urls.add("/uploads/custom-requests/" + filename);
            }
        }
        if (links != null) for (var element : links) {
            if (!element.isJsonPrimitive()) continue;
            String url = element.getAsString().trim();
            try {
                java.net.URI parsed = java.net.URI.create(url);
                String scheme = parsed.getScheme();
                if (!("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
                        || parsed.getHost() == null || url.length() > 1000) {
                    throw new IllegalArgumentException();
                }
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Link ảnh phải bắt đầu bằng http:// hoặc https:// và không quá 1000 ký tự.");
            }
            urls.add(url);
        }
        return urls;
    }

    private void removeFiles(List<Path> files) {
        for (Path file : files) try { Files.deleteIfExists(file); } catch (IOException ignored) { }
    }

    private String value(JsonObject object, String key) {
        return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
    }

    private String safe(String value) { return value == null ? "" : value; }

    private void write(HttpServletResponse resp, int status, Object payload) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(GSON.toJson(payload));
    }
}
