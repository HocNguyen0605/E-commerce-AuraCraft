package controller;

import dao.CategoryDAO;
import dao.ProductDAO;
import dao.ShopDAO;
import dto.Charm;
import dto.Product;
import dto.ShopDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import util.Flash;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@WebServlet("/seller/products")
@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 30 * 1024 * 1024)
public class SellerProductServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;
    private static final List<String> EXT = Arrays.asList(".png", ".jpg", ".jpeg");
    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ShopDAO shopDAO = new ShopDAO();
    private static final List<String> BASE_TYPES = Arrays.asList("bracelet", "ring", "necklace", "other");
    private static final int MAX_CHARMS = 30;

    /** Lưu 1 ảnh hợp lệ, trả đường dẫn tương đối; null nếu không có file hoặc file không hợp lệ. */
    private String saveOne(Part part) throws IOException {
        if (part == null || part.getSize() == 0) return null;
        File dir = new File(getServletContext().getRealPath("/uploads/products"));
        if (!dir.exists()) dir.mkdirs();
        String original = Paths.get(part.getSubmittedFileName()).getFileName().toString().toLowerCase();
        int dot = original.lastIndexOf('.');
        String ext = dot >= 0 ? original.substring(dot) : "";
        if (!EXT.contains(ext) || part.getContentType() == null || !part.getContentType().startsWith("image/")) return null;
        String name = UUID.randomUUID() + ext;
        part.write(new File(dir, name).getAbsolutePath());
        return "uploads/products/" + name;
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        Flash.transfer(req);
        req.setAttribute("shop", shop);   // sidebar cần shop.name / shop.avatar (giống SellerDashboardServlet)
        try {
            req.setAttribute("categories", categoryDAO.findAllWithCount(shop.getId()));
            if ("form".equals(req.getParameter("action"))) {
                String id = req.getParameter("id");
                if (id != null && !id.isEmpty()) {
                    Product p = productDAO.findByIdAndShop(Integer.parseInt(id), shop.getId());
                    if (p == null) { resp.sendError(404, "Không tìm thấy sản phẩm."); return; }
                    req.setAttribute("product", p);
                }
                req.getRequestDispatcher("/pages/seller/seller-product-form.jsp").forward(req, resp);
                return;
            }
            String q = req.getParameter("q");
            String sort = req.getParameter("sort");
            Integer cat = parseIntOrNull(req.getParameter("category"));
            int page = Math.max(1, parseIntOrDefault(req.getParameter("page"), 1));
            int total = productDAO.countByShop(shop.getId(), q, cat);
            req.setAttribute("products", productDAO.findByShop(shop.getId(), q, cat, sort, page, PAGE_SIZE));
            req.setAttribute("page", page);
            req.setAttribute("totalPages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
            req.setAttribute("q", q);
            req.setAttribute("category", cat);
            req.setAttribute("sort", sort);
        } catch (Exception e) {
            req.setAttribute("error", "Lỗi: " + e.getMessage());
        }
        req.getRequestDispatcher("/pages/seller/seller-products.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        String back = req.getContextPath() + "/seller/products";
        try {
            if ("delete".equals(req.getParameter("action"))) {
                boolean ok = productDAO.softDelete(Integer.parseInt(req.getParameter("id")), shop.getId());
                if (ok) Flash.success(req, "Đã xóa sản phẩm."); else Flash.error(req, "Không tìm thấy sản phẩm.");
                resp.sendRedirect(back);
                return;
            }

            String id = req.getParameter("id");
            boolean isNew = (id == null || id.isEmpty());

            Product p = new Product();
            p.setShopId(shop.getId());
            p.setName(req.getParameter("name") == null ? "" : req.getParameter("name").trim());
            p.setDescription(req.getParameter("description"));
            p.setPrice(Long.parseLong(req.getParameter("price").trim()));
            p.setStock(Integer.parseInt(req.getParameter("stock").trim()));
            p.setCategoryId(Integer.parseInt(req.getParameter("categoryId")));
            p.setStatus("hidden".equals(req.getParameter("status")) ? "hidden" : "active");
            if (p.getName().isEmpty() || p.getPrice() < 0 || p.getStock() < 0) {
                Flash.error(req, "Tên, giá và tồn kho không hợp lệ.");
                resp.sendRedirect(back + "?action=form" + idQuery(req));
                return;
            }

            // ===== Tùy chỉnh charm (fieldset bị tắt thì trình duyệt không gửi gì) =====
            boolean custom = "1".equals(req.getParameter("customizable"));
            p.setCustomizable(custom);
            List<Charm> charms = new ArrayList<>();

            if (custom) {
                String baseType = req.getParameter("baseType");
                if (!BASE_TYPES.contains(baseType)) {
                    Flash.error(req, "Loại sản phẩm để gắn charm không hợp lệ.");
                    resp.sendRedirect(back + "?action=form" + idQuery(req));
                    return;
                }
                Part basePart = req.getPart("baseImage");
                if (isNew && (basePart == null || basePart.getSize() == 0)) {
                    Flash.error(req, "Vui lòng tải ảnh nền để gắn charm.");
                    resp.sendRedirect(back + "?action=form");
                    return;
                }

                String[] ids    = req.getParameterValues("charmId");
                String[] names  = req.getParameterValues("charmName");
                String[] prices = req.getParameterValues("charmPrice");
                List<Part> imgs = new ArrayList<>();
                for (Part part : req.getParts()) {
                    if ("charmImage".equals(part.getName())) imgs.add(part);
                }

                int n = names == null ? 0 : names.length;
                if (n > MAX_CHARMS || ids == null && n > 0 || prices == null && n > 0
                        || (n > 0 && (ids.length != n || prices.length != n || imgs.size() != n))) {
                    Flash.error(req, "Dữ liệu charm không hợp lệ (tối đa " + MAX_CHARMS + " charm).");
                    resp.sendRedirect(back + "?action=form" + idQuery(req));
                    return;
                }

                // Kiểm tra hết trước khi ghi file, tránh để lại ảnh mồ côi khi báo lỗi
                for (int i = 0; i < n; i++) {
                    int charmId = ids[i].isBlank() ? 0 : Integer.parseInt(ids[i].trim());
                    String cname = names[i].trim();
                    long cprice = prices[i].isBlank() ? 0 : Long.parseLong(prices[i].trim());
                    if (cname.isEmpty() || cname.length() > 100 || cprice < 0
                            || (charmId == 0 && imgs.get(i).getSize() == 0)) {
                        Flash.error(req, "Charm thứ " + (i + 1) + " thiếu tên, ảnh hoặc giá không hợp lệ.");
                        resp.sendRedirect(back + "?action=form" + idQuery(req));
                        return;
                    }
                    Charm ch = new Charm();
                    ch.setId(charmId);
                    ch.setName(cname);
                    ch.setPrice(cprice);
                    charms.add(ch);
                }

                // Ghi file: saveOne trả null nếu không chọn ảnh mới (DAO giữ ảnh cũ)
                p.setBaseType(baseType);
                p.setBaseImage(saveOne(basePart));
                for (int i = 0; i < n; i++) charms.get(i).setImage(saveOne(imgs.get(i)));
            }
            p.setCharms(charms);   // rỗng + customizable=false => DAO xóa charm cũ của sản phẩm

            List<String> urls = saveImages(req);
            if (isNew) {
                productDAO.insert(p, urls);
                Flash.success(req, "Đã thêm sản phẩm.");
            } else {
                p.setId(Integer.parseInt(id));
                if (productDAO.update(p, urls)) Flash.success(req, "Đã lưu sản phẩm.");
                else Flash.error(req, "Không tìm thấy sản phẩm.");
            }
        } catch (NumberFormatException e) {
            Flash.error(req, "Dữ liệu số không hợp lệ (giá, tồn kho, danh mục, giá charm).");
        } catch (Exception e) {
            Flash.error(req, "Lỗi: " + e.getMessage());
        }
        resp.sendRedirect(back);
    }

    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
    private ShopDTO getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.html");
            return null;
        }
        try {
            int userId = ((Number) session.getAttribute("userId")).intValue();
            ShopDTO shop = shopDAO.findActiveByOwner(userId);
            if (shop == null) {
                resp.sendRedirect(req.getContextPath() + "/index.jsp");
            }
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    private List<String> saveImages(HttpServletRequest req) throws IOException, ServletException {
        List<String> urls = new ArrayList<>();
        // Lưu ý: getRealPath nằm trong thư mục deploy, sẽ mất khi build lại → nên đổi sang thư mục ngoài server khi chạy thật
        File dir = new File(getServletContext().getRealPath("/uploads/products"));
        if (!dir.exists()) dir.mkdirs();
        for (Part part : req.getParts()) {
            if (!"images".equals(part.getName()) || part.getSize() == 0) continue;
            String original = Paths.get(part.getSubmittedFileName()).getFileName().toString().toLowerCase();
            int dot = original.lastIndexOf('.');
            String ext = dot >= 0 ? original.substring(dot) : "";
            if (!EXT.contains(ext) || part.getContentType() == null || !part.getContentType().startsWith("image/")) continue;
            String name = UUID.randomUUID() + ext;
            part.write(new File(dir, name).getAbsolutePath());
            urls.add("uploads/products/" + name);
        }
        return urls;
    }

    private static String idQuery(HttpServletRequest req) {
        String id = req.getParameter("id");
        return (id == null || id.isEmpty()) ? "" : "&id=" + id.replaceAll("[^0-9]", "");
    }
    private static Integer parseIntOrNull(String s) {
        try { return (s == null || s.isEmpty()) ? null : Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
    }
    private static int parseIntOrDefault(String s, int d) {
        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
    }

}