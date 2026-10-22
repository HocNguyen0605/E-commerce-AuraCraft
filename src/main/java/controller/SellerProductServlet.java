//package controller;
//
//import dao.CategoryDAO;
//import dao.ProductDAO;
//import dao.ShopDAO;
//import dto.Product;
//import dto.Shop;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.annotation.WebServlet;
//import jakarta.servlet.http.*;
//import util.Flash;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.UUID;
//
//@WebServlet("/seller/products")
//@MultipartConfig(maxFileSize = 5 * 1024 * 1024, maxRequestSize = 30 * 1024 * 1024)
//public class SellerProductServlet extends HttpServlet {
//    private static final int PAGE_SIZE = 10;
//    private static final List<String> EXT = Arrays.asList(".png", ".jpg", ".jpeg");
//    private final ProductDAO productDAO = new ProductDAO();
//    private final CategoryDAO categoryDAO = new CategoryDAO();
//    private final ShopDAO shopDAO = new ShopDAO();
//
//    @Override
//    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//        Flash.transfer(req);
//        try {
//            req.setAttribute("categories", categoryDAO.findAllWithCount(shop.getId()));
//            if ("form".equals(req.getParameter("action"))) {
//                String id = req.getParameter("id");
//                if (id != null && !id.isEmpty()) {
//                    Product p = productDAO.findByIdAndShop(Integer.parseInt(id), shop.getId());
//                    if (p == null) { resp.sendError(404, "Không tìm thấy sản phẩm."); return; }
//                    req.setAttribute("product", p);
//                }
//                req.getRequestDispatcher("/pages/seller/seller-product-form.jsp").forward(req, resp);
//                return;
//            }
//            String q = req.getParameter("q");
//            String sort = req.getParameter("sort");
//            Integer cat = parseIntOrNull(req.getParameter("category"));
//            int page = Math.max(1, parseIntOrDefault(req.getParameter("page"), 1));
//            int total = productDAO.countByShop(shop.getId(), q, cat);
//            req.setAttribute("products", productDAO.findByShop(shop.getId(), q, cat, sort, page, PAGE_SIZE));
//            req.setAttribute("page", page);
//            req.setAttribute("totalPages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
//            req.setAttribute("q", q);
//            req.setAttribute("category", cat);
//            req.setAttribute("sort", sort);
//        } catch (Exception e) {
//            req.setAttribute("error", "Lỗi: " + e.getMessage());
//        }
//        req.getRequestDispatcher("/pages/seller/seller-products.jsp").forward(req, resp);
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
//        Shop shop = getShop(req, resp);
//        if (shop == null) return;
//        String back = req.getContextPath() + "/seller/products";
//        try {
//            if ("delete".equals(req.getParameter("action"))) {
//                boolean ok = productDAO.softDelete(Integer.parseInt(req.getParameter("id")), shop.getId());
//                if (ok) Flash.success(req, "Đã xóa sản phẩm."); else Flash.error(req, "Không tìm thấy sản phẩm.");
//                resp.sendRedirect(back);
//                return;
//            }
//
//            Product p = new Product();
//            p.setShopId(shop.getId());
//            p.setName(req.getParameter("name") == null ? "" : req.getParameter("name").trim());
//            p.setDescription(req.getParameter("description"));
//            p.setPrice(Long.parseLong(req.getParameter("price").trim()));
//            p.setStock(Integer.parseInt(req.getParameter("stock").trim()));
//            p.setCategoryId(Integer.parseInt(req.getParameter("categoryId")));
//            p.setStatus("hidden".equals(req.getParameter("status")) ? "hidden" : "active");
//            if (p.getName().isEmpty() || p.getPrice() < 0 || p.getStock() < 0) {
//                Flash.error(req, "Tên, giá và tồn kho không hợp lệ.");
//                resp.sendRedirect(back + "?action=form" + idQuery(req));
//                return;
//            }
//
//            List<String> urls = saveImages(req);
//            String id = req.getParameter("id");
//            if (id == null || id.isEmpty()) {
//                productDAO.insert(p, urls);
//                Flash.success(req, "Đã thêm sản phẩm.");
//            } else {
//                p.setId(Integer.parseInt(id));
//                if (productDAO.update(p, urls)) Flash.success(req, "Đã lưu sản phẩm.");
//                else Flash.error(req, "Không tìm thấy sản phẩm.");
//            }
//        } catch (NumberFormatException e) {
//            Flash.error(req, "Dữ liệu số không hợp lệ (giá, tồn kho, danh mục).");
//        } catch (Exception e) {
//            Flash.error(req, "Lỗi: " + e.getMessage());
//        }
//        resp.sendRedirect(back);
//    }
//
//    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
//    private Shop getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
//        HttpSession session = req.getSession(false);
//        if (session == null || session.getAttribute("userId") == null) {
//            resp.sendRedirect(req.getContextPath() + "/pages/login.html");
//            return null;
//        }
//        try {
//            int userId = ((Number) session.getAttribute("userId")).intValue();
//            Shop shop = shopDAO.findActiveByOwner(userId);
//            if (shop == null) {
//                resp.sendRedirect(req.getContextPath() + "/index.html");
//            }
//            return shop;
//        } catch (Exception e) {
//            e.printStackTrace();
//            resp.sendError(500);
//            return null;
//        }
//    }
//
//    private List<String> saveImages(HttpServletRequest req) throws IOException, ServletException {
//        List<String> urls = new ArrayList<>();
//        // Lưu ý: getRealPath nằm trong thư mục deploy, sẽ mất khi build lại → nên đổi sang thư mục ngoài server khi chạy thật
//        File dir = new File(getServletContext().getRealPath("/uploads/products"));
//        if (!dir.exists()) dir.mkdirs();
//        for (Part part : req.getParts()) {
//            if (!"images".equals(part.getName()) || part.getSize() == 0) continue;
//            String original = Paths.get(part.getSubmittedFileName()).getFileName().toString().toLowerCase();
//            int dot = original.lastIndexOf('.');
//            String ext = dot >= 0 ? original.substring(dot) : "";
//            if (!EXT.contains(ext) || part.getContentType() == null || !part.getContentType().startsWith("image/")) continue;
//            String name = UUID.randomUUID() + ext;
//            part.write(new File(dir, name).getAbsolutePath());
//            urls.add("uploads/products/" + name);
//        }
//        return urls;
//    }
//
//    private static String idQuery(HttpServletRequest req) {
//        String id = req.getParameter("id");
//        return (id == null || id.isEmpty()) ? "" : "&id=" + id.replaceAll("[^0-9]", "");
//    }
//    private static Integer parseIntOrNull(String s) {
//        try { return (s == null || s.isEmpty()) ? null : Integer.valueOf(s); } catch (NumberFormatException e) { return null; }
//    }
//    private static int parseIntOrDefault(String s, int d) {
//        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
//    }
//}