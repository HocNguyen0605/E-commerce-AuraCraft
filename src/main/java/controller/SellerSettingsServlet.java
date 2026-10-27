package controller;

import dao.ShopSettingsDAO;
import dto.ShopProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import util.Flash;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.UUID;

/** Cài đặt shop của seller: sửa tên, liên hệ, địa chỉ, mô tả, ảnh đại diện. */
@WebServlet("/seller/settings")
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 3 * 1024 * 1024)
public class SellerSettingsServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "uploads/shops";     // lưu trong webapp, DB lưu "uploads/shops/xxx.jpg"
    private static final String LOGIN_PAGE = "/pages/login";
    private static final String PHONE_RE = "^(0|\\+84)\\d{8,10}$";
    private static final String EMAIL_RE = "^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$";

    private final ShopSettingsDAO dao = new ShopSettingsDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // Đăng xuất: /seller/settings?action=logout → huỷ session, về trang login
        if ("logout".equals(req.getParameter("action"))) {
            logout(req, resp);
            return;
        }
        ShopProfileDTO shop = loadShop(req, resp);
        if (shop == null) return;
        Flash.transfer(req);
        req.setAttribute("shop", shop);
        req.getRequestDispatcher("/pages/seller/seller-settings.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        // Đăng xuất bằng form POST (action=logout) → huỷ session, về trang login
        if ("logout".equals(req.getParameter("action"))) {
            logout(req, resp);
            return;
        }

        ShopProfileDTO shop = loadShop(req, resp);
        if (shop == null) return;

        String name = trim(req.getParameter("name"), 150);
        String phone = trim(req.getParameter("phone"), 20);
        String email = trim(req.getParameter("email"), 150);
        String address = trim(req.getParameter("address"), 500);
        String description = trim(req.getParameter("description"), 2000);

        // Giữ lại dữ liệu vừa nhập để hiển thị lại nếu có lỗi
        shop.setName(name); shop.setPhone(phone); shop.setEmail(email);
        shop.setAddress(address); shop.setDescription(description);

        String error = validate(name, phone, email);
        String newAvatar = null;
        File saved = null;

        if (error == null) {
            try {
                Part part = req.getPart("avatar");
                if (part != null && part.getSize() > 0) {
                    String ext = imageExtension(part);
                    if (ext == null) {
                        error = "Ảnh đại diện phải là JPG, PNG hoặc WEBP (tối đa 2MB).";
                    } else {
                        File dir = new File(getServletContext().getRealPath("/" + UPLOAD_DIR));
                        if (!dir.exists() && !dir.mkdirs()) throw new IOException("Không tạo được thư mục " + dir);
                        saved = new File(dir, UUID.randomUUID() + "." + ext);
                        try (InputStream in = part.getInputStream()) {
                            Files.copy(in, saved.toPath());
                        }
                        newAvatar = UPLOAD_DIR + "/" + saved.getName();
                    }
                }
            } catch (IllegalStateException e) {            // vượt maxFileSize
                error = "Ảnh quá lớn, tối đa 2MB.";
            }
        }

        if (error == null) {
            try {
                String oldAvatar = shop.getAvatar();
                boolean ok = dao.updateProfile(shop.getId(), shop.getOwnerId(), name,
                        nullIfEmpty(phone), nullIfEmpty(email), nullIfEmpty(address), nullIfEmpty(description), newAvatar);
                if (ok) {
                    if (newAvatar != null) deleteOldAvatar(oldAvatar);
                    Flash.success(req, "Đã lưu cài đặt shop.");
                    resp.sendRedirect(req.getContextPath() + "/seller/settings");   // PRG
                    return;
                }
                error = "Không lưu được: shop không còn hoạt động.";
            } catch (SQLException e) {
                if (saved != null) saved.delete();
                throw new ServletException(e);
            }
        }

        if (saved != null) saved.delete();       // lỗi thì bỏ file vừa tải lên
        req.setAttribute("error", error);
        req.setAttribute("shop", shop);
        req.getRequestDispatcher("/pages/seller/seller-settings.jsp").forward(req, resp);
    }

    /** Huỷ session và chuyển về trang login. */
    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
        // Tránh bấm Back vẫn thấy trang seller từ cache
        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.sendRedirect(req.getContextPath() + LOGIN_PAGE);
    }

    private String validate(String name, String phone, String email) {
        if (name.length() < 2) return "Tên shop phải từ 2 ký tự.";
        if (!phone.isEmpty() && !phone.matches(PHONE_RE)) return "Số điện thoại không hợp lệ (VD: 0901234567).";
        if (!email.isEmpty() && !email.matches(EMAIL_RE)) return "Email không hợp lệ.";
        return null;
    }

    /** Kiểm tra theo nội dung file (magic bytes), không tin vào tên file/Content-Type. */
    private String imageExtension(Part part) throws IOException {
        byte[] h = new byte[12];
        int n;
        try (InputStream in = part.getInputStream()) {
            n = in.readNBytes(h, 0, 12);
        }
        if (n >= 3 && (h[0] & 0xFF) == 0xFF && (h[1] & 0xFF) == 0xD8 && (h[2] & 0xFF) == 0xFF) return "jpg";
        if (n >= 4 && (h[0] & 0xFF) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G') return "png";
        if (n >= 12 && h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P') return "webp";
        return null;
    }

    private void deleteOldAvatar(String old) {
        if (old == null || !old.startsWith(UPLOAD_DIR + "/") || old.contains("..")) return;
        File f = new File(getServletContext().getRealPath("/" + old));
        if (f.isFile()) f.delete();
    }

    /** Shop đang hoạt động của user đăng nhập; null (đã redirect) nếu không hợp lệ. */
    private ShopProfileDTO loadShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + LOGIN_PAGE);
            return null;
        }
        try {
            int userId = ((Number) session.getAttribute("userId")).intValue();
            ShopProfileDTO shop = dao.findActiveByOwner(userId);
            if (shop == null) resp.sendRedirect(req.getContextPath() + "/pages/home");
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    private String trim(String s, int max) {
        if (s == null) return "";
        s = s.trim();
        return s.length() > max ? s.substring(0, max) : s;
    }

    private String nullIfEmpty(String s) { return s == null || s.isEmpty() ? null : s; }
}
