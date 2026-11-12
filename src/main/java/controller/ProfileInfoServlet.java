package controller;

import dao.ProfileDAO;
import dto.UserProfileDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

@WebServlet(urlPatterns = {"/profile-info", "/pages/profile-info"})
public class ProfileInfoServlet extends HttpServlet {
    private final ProfileDAO dao = new ProfileDAO();

    private static final Pattern PHONE = Pattern.compile("^0\\d{9}$");
    private static final Pattern CODE = Pattern.compile("^\\d{1,6}$"); // mã tỉnh / xã dạng số, vd "01", "79"

    private Integer currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return;
        }
        try {
            UserProfileDTO profile = dao.findUser(userId);
            if (profile == null) {
                resp.sendRedirect(req.getContextPath() + "/pages/login");
                return;
            }
            // Flash message (hiển thị 1 lần rồi xóa)
            HttpSession session = req.getSession(false);
            moveFlash(session, req, "flashSuccess");
            moveFlash(session, req, "flashError");

            req.setAttribute("profile", profile);
            req.setAttribute("addresses", dao.findAddresses(userId));
            req.getRequestDispatcher("/pages/profile-info.jsp").forward(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return;
        }

        String action = trim(req.getParameter("action"));
        HttpSession session = req.getSession(false);

        try {
            switch (action == null ? "" : action) {
                case "updateProfile" -> handleUpdateProfile(req, session, userId);
                case "addAddress" -> handleSaveAddress(req, session, userId, false);
                case "updateAddress" -> handleSaveAddress(req, session, userId, true);
                case "deleteAddress" -> handleDeleteAddress(req, session, userId);
                case "setDefaultAddress" -> handleSetDefault(req, session, userId);
                default -> session.setAttribute("flashError", "Yêu cầu không hợp lệ.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("flashError", "Có lỗi hệ thống, vui lòng thử lại sau.");
        }
        resp.sendRedirect(req.getContextPath() + "/pages/profile-info");
    }

    /* ---------------- Handlers ---------------- */

    private void handleUpdateProfile(HttpServletRequest req, HttpSession session, int userId) throws Exception {
        String name = trim(req.getParameter("name"));
        String phone = cleanPhone(req.getParameter("phone"));
        String dobStr = trim(req.getParameter("birthday"));

        if (name == null || name.length() > 100) {
            session.setAttribute("flashError", "Họ và tên không được để trống và tối đa 100 ký tự.");
            return;
        }
        if (phone != null && !PHONE.matcher(phone).matches()) {
            session.setAttribute("flashError", "Số điện thoại không hợp lệ (gồm 10 số, bắt đầu bằng 0).");
            return;
        }
        LocalDate dob = null;
        if (dobStr != null) {
            try {
                dob = LocalDate.parse(dobStr);
            } catch (DateTimeParseException ex) {
                session.setAttribute("flashError", "Ngày sinh không hợp lệ.");
                return;
            }
            if (dob.isAfter(LocalDate.now())) {
                session.setAttribute("flashError", "Ngày sinh không được ở tương lai.");
                return;
            }
        }

        if (dao.updateUser(userId, name, phone, dob)) {
            session.setAttribute("userName", name); // nếu header đang đọc tên từ session
            session.setAttribute("flashSuccess", "Cập nhật thông tin cá nhân thành công.");
        } else {
            session.setAttribute("flashError", "Không thể cập nhật thông tin.");
        }
    }

    private void handleSaveAddress(HttpServletRequest req, HttpSession session, int userId, boolean isEdit) throws Exception {
        String receiver = trim(req.getParameter("receiver"));
        String phone = cleanPhone(req.getParameter("phone"));
        String provinceCode = trim(req.getParameter("provinceCode"));
        String province = trim(req.getParameter("province"));
        String wardCode = trim(req.getParameter("wardCode"));   // có thể rỗng khi người dùng nhập xã tay (fallback)
        String ward = trim(req.getParameter("ward"));
        String address = trim(req.getParameter("address"));      // chỉ còn số nhà, tên đường...
        boolean isDefault = "on".equals(req.getParameter("isDefault")) || "1".equals(req.getParameter("isDefault"));

        if (receiver == null || receiver.length() > 100) {
            session.setAttribute("flashError", "Tên người nhận không được để trống và tối đa 100 ký tự.");
            return;
        }
        if (phone == null || !PHONE.matcher(phone).matches()) {
            session.setAttribute("flashError", "Số điện thoại người nhận không hợp lệ.");
            return;
        }
        if (provinceCode == null || !CODE.matcher(provinceCode).matches()
                || province == null || province.length() > 100) {
            session.setAttribute("flashError", "Vui lòng chọn tỉnh / thành phố.");
            return;
        }
        if (ward == null || ward.length() > 150 || (wardCode != null && !CODE.matcher(wardCode).matches())) {
            session.setAttribute("flashError", "Vui lòng chọn xã / phường.");
            return;
        }
        if (address == null || address.length() > 250) {
            session.setAttribute("flashError", "Địa chỉ chi tiết không được để trống và tối đa 250 ký tự.");
            return;
        }

        if (isEdit) {
            int id = parseId(req.getParameter("addressId"));
            if (id <= 0 || !dao.updateAddress(id, userId, receiver, phone,
                    provinceCode, province, wardCode, ward, address, isDefault)) {
                session.setAttribute("flashError", "Không tìm thấy địa chỉ cần sửa.");
                return;
            }
            session.setAttribute("flashSuccess", "Cập nhật địa chỉ thành công.");
        } else {
            if (dao.countAddresses(userId) >= 10) {
                session.setAttribute("flashError", "Bạn chỉ có thể lưu tối đa 10 địa chỉ.");
                return;
            }
            if (dao.addAddress(userId, receiver, phone,
                    provinceCode, province, wardCode, ward, address, isDefault)) {
                session.setAttribute("flashSuccess", "Thêm địa chỉ mới thành công.");
            } else {
                session.setAttribute("flashError", "Không thể thêm địa chỉ.");
            }
        }
    }

    private void handleDeleteAddress(HttpServletRequest req, HttpSession session, int userId) throws Exception {
        int id = parseId(req.getParameter("addressId"));
        if (id > 0 && dao.deleteAddress(id, userId)) {
            session.setAttribute("flashSuccess", "Đã xóa địa chỉ.");
        } else {
            session.setAttribute("flashError", "Không tìm thấy địa chỉ cần xóa.");
        }
    }

    private void handleSetDefault(HttpServletRequest req, HttpSession session, int userId) throws Exception {
        int id = parseId(req.getParameter("addressId"));
        if (id > 0 && dao.setDefaultAddress(id, userId)) {
            session.setAttribute("flashSuccess", "Đã đặt làm địa chỉ mặc định.");
        } else {
            session.setAttribute("flashError", "Không tìm thấy địa chỉ.");
        }
    }

    /* ---------------- Helpers ---------------- */

    private static void moveFlash(HttpSession session, HttpServletRequest req, String key) {
        if (session == null) return;
        Object v = session.getAttribute(key);
        if (v != null) {
            req.setAttribute(key, v);
            session.removeAttribute(key);
        }
    }

    private static String trim(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }

    private static String cleanPhone(String s) {
        s = trim(s);
        return s == null ? null : s.replaceAll("[\\s.\\-]", "");
    }

    private static int parseId(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return -1;
        }
    }
}