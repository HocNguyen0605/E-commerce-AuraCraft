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
import java.util.*;

/**
 * Quản lý sản phẩm của seller.
 *
 * Khi lưu bị lỗi: KHÔNG redirect (sẽ mất dữ liệu) mà forward lại form kèm
 *   f           : Map các giá trị người dùng vừa nhập
 *   fieldErrors : Map tên-trường -> thông báo lỗi (JSP đánh dấu * đỏ ở đúng ô)
 *   formCharms  : danh sách charm người dùng vừa nhập (kèm lỗi từng dòng, kèm imageUrl)
 *
 * Lưu ý: file ảnh chọn từ máy không thể giữ lại sau khi server trả form (giới hạn của trình duyệt),
 * nên form có kiểm tra phía trình duyệt trước để hạn chế tối đa việc phải chọn lại ảnh.
 * maxFileSize đặt lớn hơn 5MB để server tự báo "ảnh vượt 5MB" mà vẫn đọc được dữ liệu form.
 */
@WebServlet("/pages/seller/seller-products")
@MultipartConfig(maxFileSize = 25 * 1024 * 1024, maxRequestSize = 150 * 1024 * 1024)
public class SellerProductServlet extends HttpServlet {
    private static final String PAGE_PATH = "/pages/seller/seller-products";            // URL servlet (không .jsp)
    private static final String VIEW_LIST = "/pages/seller/seller-products.jsp";        // JSP thật
    private static final String VIEW_FORM = "/pages/seller/seller-product-form.jsp";    // JSP thật

    private static final int PAGE_SIZE = 10;
    private static final List<String> EXT = Arrays.asList(".png", ".jpg", ".jpeg");
    private static final List<String> BASE_TYPES = Arrays.asList("bracelet", "ring", "necklace", "other");
    private static final int MAX_CHARMS = 30;
    private static final int MAX_EXTRA_IMAGES = 8;          // ảnh phụ tối đa mỗi lần lưu (upload + link)
    private static final int MAX_URL_LEN = 255;
    private static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;
    private static final String URL_RE = "(?i)^https?://\\S+$";

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    // ===================================================== GET

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        Flash.transfer(req);
        req.setAttribute("shop", shop);   // sidebar cần shop.name / shop.avatar
        req.setAttribute("sellerTab", "products");
        try {
            req.setAttribute("categories", categoryDAO.findAllWithCount(shop.getId()));
            if ("form".equals(req.getParameter("action"))) {
                String id = req.getParameter("id");
                boolean editing = id != null && !id.isEmpty();
                req.setAttribute("editing", editing);
                if (editing) {
                    Product p = productDAO.findByIdAndShop(Integer.parseInt(id), shop.getId());
                    if (p == null) { resp.sendError(404, "Không tìm thấy sản phẩm."); return; }
                    req.setAttribute("product", p);
                    req.setAttribute("editId", p.getId());
                }
                req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
                return;
            }
            String q = req.getParameter("q");
            String sort = req.getParameter("sort");
            Integer cat = parseIntOrNull(req.getParameter("category"));
            String status = req.getParameter("status");     // active | hidden | out | (rỗng = tất cả)
            int page = Math.max(1, parseIntOrDefault(req.getParameter("page"), 1));
            int total = productDAO.countByShop(shop.getId(), q, cat, status);
            req.setAttribute("products", productDAO.findByShop(shop.getId(), q, cat, status, sort, page, PAGE_SIZE));
            req.setAttribute("page", page);
            req.setAttribute("totalPages", Math.max(1, (total + PAGE_SIZE - 1) / PAGE_SIZE));
            req.setAttribute("q", q);
            req.setAttribute("category", cat);
            req.setAttribute("sort", sort);
            req.setAttribute("status", status);
        } catch (Exception e) {
            req.setAttribute("error", "Lỗi: " + e.getMessage());
        }
        req.getRequestDispatcher(VIEW_LIST).forward(req, resp);
    }

    // ===================================================== POST

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;
        String back = req.getContextPath() + PAGE_PATH;

        // ---- Xóa (form thường, không multipart) ----
        if ("delete".equals(req.getParameter("action"))) {
            try {
                List<String> files = new ArrayList<>();
                int r = productDAO.deleteProduct(Integer.parseInt(req.getParameter("id")), shop.getId(), files);
                if (r == 2) {
                    for (String u : files) deleteLocalFile(u);        // dọn file ảnh sau khi DB đã xóa xong
                    Flash.success(req, "Đã xóa sản phẩm.");
                } else if (r == 1) {
                    Flash.success(req, "Sản phẩm đã có trong đơn hàng nên được gỡ khỏi danh sách (dữ liệu đơn hàng vẫn được giữ lại).");
                } else {
                    Flash.error(req, "Không tìm thấy sản phẩm.");
                }
            } catch (Exception e) {
                Flash.error(req, "Lỗi: " + e.getMessage());
            }
            resp.sendRedirect(back);
            return;
        }

        // ---- Thêm / sửa ----
        String id = val(req, "id");
        boolean isNew = id.isEmpty();

        // Sản phẩm gốc khi sửa (để lấy ảnh/charm hiện có)
        Product orig = null;
        if (!isNew) {
            try {
                Integer pid = parseIntOrNull(id);
                if (pid != null) orig = productDAO.findByIdAndShop(pid, shop.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (orig == null) { resp.sendError(404, "Không tìm thấy sản phẩm."); return; }
        }

        // Giữ lại mọi thứ người dùng vừa nhập để đổ lại form khi có lỗi
        Map<String, String> f = new LinkedHashMap<>();
        f.put("name", val(req, "name"));
        f.put("description", val(req, "description"));
        f.put("price", val(req, "price"));
        f.put("stock", val(req, "stock"));
        f.put("categoryId", val(req, "categoryId"));
        f.put("status", "hidden".equals(val(req, "status")) ? "hidden" : "active");
        f.put("customizable", "1".equals(val(req, "customizable")) ? "1" : "");
        f.put("baseType", val(req, "baseType"));
        f.put("mainImageUrl", val(req, "mainImageUrl"));
        f.put("extraImageUrls", val(req, "extraImageUrls"));

        Map<String, String> fe = new LinkedHashMap<>();            // lỗi theo từng ô
        List<Map<String, Object>> rows = new ArrayList<>();        // charm người dùng vừa nhập
        List<String> removeImgs = new ArrayList<>();               // ảnh đã lưu mà seller bấm xóa
        String fatal = null;

        try {
            // ---------- 1. Kiểm tra thông tin cơ bản ----------
            String name = f.get("name");
            if (name.isEmpty()) fe.put("name", "Vui lòng nhập tên sản phẩm.");
            else if (name.length() > 255) fe.put("name", "Tên sản phẩm tối đa 255 ký tự.");

            Long price = parseLongOrNull(f.get("price"));
            if (price == null || price < 0) fe.put("price", "Giá bán không hợp lệ (số nguyên từ 0).");

            Integer stock = parseIntOrNull(f.get("stock"));
            if (stock == null || stock < 0) fe.put("stock", "Số lượng kho không hợp lệ (số nguyên từ 0).");

            Integer categoryId = parseIntOrNull(f.get("categoryId"));
            if (categoryId == null || categoryId <= 0) fe.put("categoryId", "Vui lòng chọn danh mục.");

            // ---------- 2. Ảnh chính ----------
            Part mainPart = req.getPart("mainImage");
            boolean hasMainFile = mainPart != null && mainPart.getSize() > 0;
            String mainUrl = f.get("mainImageUrl");
            if (hasMainFile) {
                String err = imageError(mainPart);
                if (err != null) fe.put("mainImage", err);
            } else if (!mainUrl.isEmpty()) {
                String err = urlError(mainUrl);
                if (err != null) fe.put("mainImage", err);
            } else if (isNew) {
                fe.put("mainImage", "Vui lòng chọn ảnh chính (tải lên hoặc dán link).");
            }

            // ---------- 3. Ảnh phụ (upload + link) ----------
            List<Part> extraParts = new ArrayList<>();
            for (Part part : req.getParts()) {
                if ("images".equals(part.getName()) && part.getSize() > 0) extraParts.add(part);
            }
            List<String> extraUrls = new ArrayList<>();
            for (String line : f.get("extraImageUrls").split("\\R")) {
                String u = line.trim();
                if (!u.isEmpty()) extraUrls.add(u);
            }
            String extraErr = null;
            for (Part part : extraParts) {
                String e = imageError(part);
                if (e != null) { extraErr = "Ảnh phụ \"" + part.getSubmittedFileName() + "\": " + e; break; }
            }
            if (extraErr == null) {
                for (String u : extraUrls) {
                    String e = urlError(u);
                    if (e != null) { extraErr = "Link ảnh phụ không hợp lệ: " + shorten(u) + " (" + e + ")"; break; }
                }
            }
            if (extraErr == null && extraParts.size() + extraUrls.size() > MAX_EXTRA_IMAGES) {
                extraErr = "Tối đa " + MAX_EXTRA_IMAGES + " ảnh phụ mỗi lần lưu (hiện có "
                        + (extraParts.size() + extraUrls.size()) + ").";
            }
            if (extraErr != null) fe.put("extraImages", extraErr);

            // ---------- 3b. Ảnh đã lưu bị đánh dấu xóa (chỉ nhận ảnh thật sự thuộc sản phẩm này) ----------
            if (orig != null) {
                String[] rm = req.getParameterValues("removeImage");
                if (rm != null) {
                    for (String u : rm) {
                        if (u != null && orig.getImages().contains(u) && !removeImgs.contains(u)) removeImgs.add(u);
                    }
                }
                int kept = 0;
                for (String u : orig.getImages()) if (!removeImgs.contains(u)) kept++;
                boolean hasNewMain = hasMainFile || !mainUrl.isEmpty();
                int totalAfter = kept + (hasNewMain ? 1 : 0) + extraParts.size() + extraUrls.size();
                if (totalAfter == 0) {
                    fe.put(orig.getImages().isEmpty() ? "mainImage" : "existingImages",
                            "Sản phẩm cần có ít nhất 1 ảnh. Hãy giữ lại một ảnh hoặc thêm ảnh mới.");
                }
            }

            // ---------- 4. Tùy chỉnh charm (fieldset bị tắt thì trình duyệt không gửi gì) ----------
            boolean custom = "1".equals(f.get("customizable"));
            String baseType = f.get("baseType");
            Part basePart = null;
            List<Part> imgs = new ArrayList<>();
            List<Charm> charms = new ArrayList<>();
            List<String> charmLinks = new ArrayList<>();   // link ảnh của từng charm (song song với charms)

            if (custom) {
                if (!BASE_TYPES.contains(baseType)) {
                    fe.put("baseType", "Loại sản phẩm để gắn charm không hợp lệ.");
                }
                basePart = req.getPart("baseImage");
                boolean hasBaseFile = basePart != null && basePart.getSize() > 0;
                boolean hadBase = orig != null && orig.getBaseImage() != null && !orig.getBaseImage().isBlank();
                if (hasBaseFile) {
                    String err = imageError(basePart);
                    if (err != null) fe.put("baseImage", err);
                } else if (!hadBase) {
                    fe.put("baseImage", "Vui lòng tải ảnh nền để gắn charm.");
                }

                String[] ids    = req.getParameterValues("charmId");
                String[] names  = req.getParameterValues("charmName");
                String[] prices = req.getParameterValues("charmPrice");
                String[] curls  = req.getParameterValues("charmImageUrl");
                for (Part part : req.getParts()) {
                    if ("charmImage".equals(part.getName())) imgs.add(part);
                }

                int n = names == null ? 0 : names.length;
                boolean shapeOk = n <= MAX_CHARMS
                        && (n == 0 || (ids != null && prices != null && curls != null
                        && ids.length == n && prices.length == n && curls.length == n && imgs.size() == n));
                if (!shapeOk) {
                    fe.put("charms", n > MAX_CHARMS
                            ? "Tối đa " + MAX_CHARMS + " charm."
                            : "Dữ liệu charm không hợp lệ, vui lòng thêm lại các charm.");
                } else {
                    boolean rowErr = false;
                    for (int i = 0; i < n; i++) {
                        String error = null;
                        int charmId = 0;
                        try {
                            charmId = ids[i].isBlank() ? 0 : Integer.parseInt(ids[i].trim());
                        } catch (NumberFormatException e) {
                            error = "Mã charm không hợp lệ.";
                        }
                        String cname = names[i].trim();
                        String cpriceRaw = prices[i].trim();
                        long cprice = 0;
                        try {
                            cprice = cpriceRaw.isEmpty() ? 0 : Long.parseLong(cpriceRaw);
                        } catch (NumberFormatException e) {
                            error = "Giá charm không hợp lệ.";
                        }
                        String curl = curls[i].trim();
                        Part img = imgs.get(i);
                        boolean hasImg = img.getSize() > 0;
                        if (error == null) {
                            if (cname.isEmpty()) error = "Vui lòng nhập tên charm.";
                            else if (cname.length() > 100) error = "Tên charm tối đa 100 ký tự.";
                            else if (cprice < 0) error = "Giá charm không hợp lệ.";
                            else if (hasImg) error = imageError(img);                       // file được ưu tiên
                            else if (!curl.isEmpty()) error = urlError(curl);
                            else if (charmId == 0) error = "Vui lòng chọn ảnh hoặc dán link ảnh cho charm.";
                        }

                        // Ảnh hiện có của charm cũ (để vẫn hiện thumbnail khi form trả lại)
                        String existingImage = null;
                        if (charmId > 0 && orig != null && orig.getCharms() != null) {
                            for (Charm oc : orig.getCharms()) {
                                if (oc.getId() == charmId) { existingImage = oc.getImage(); break; }
                            }
                        }
                        Map<String, Object> row = new LinkedHashMap<>();
                        row.put("id", charmId);
                        row.put("name", cname);
                        row.put("price", cpriceRaw);
                        row.put("image", existingImage);
                        row.put("imageUrl", curl);
                        row.put("error", error);
                        rows.add(row);

                        if (error != null) {
                            rowErr = true;
                        } else {
                            Charm ch = new Charm();
                            ch.setId(charmId);
                            ch.setName(cname);
                            ch.setPrice(cprice);
                            charms.add(ch);
                            charmLinks.add(curl);
                        }
                    }
                    if (rowErr) fe.put("charms", "Một số charm chưa hợp lệ, vui lòng kiểm tra các dòng được đánh dấu đỏ.");
                }
            }

            // ---------- 5. Có lỗi thì dừng, KHÔNG ghi file nào ----------
            if (fe.isEmpty()) {
                Product p = new Product();
                p.setShopId(shop.getId());
                p.setName(name);
                p.setDescription(f.get("description"));
                p.setPrice(price);
                p.setStock(stock);
                p.setCategoryId(categoryId);
                p.setStatus(f.get("status"));
                p.setCustomizable(custom);

                if (custom) {
                    // saveOne trả null nếu không chọn ảnh mới (DAO giữ ảnh cũ)
                    p.setBaseType(baseType);
                    p.setBaseImage(saveOne(basePart));
                    for (int i = 0; i < charms.size(); i++) {
                        String saved = saveOne(imgs.get(i));            // file ưu tiên
                        String link = charmLinks.get(i);
                        charms.get(i).setImage(saved != null ? saved : (link.isEmpty() ? null : link));
                    }
                }
                p.setCharms(charms);   // rỗng + customizable=false => DAO xóa charm cũ của sản phẩm

                // Thứ tự ảnh: ảnh chính đứng đầu, sau đó ảnh phụ (tải lên rồi tới link)
                List<String> urls = new ArrayList<>();
                String mainPath = hasMainFile ? saveOne(mainPart) : (mainUrl.isEmpty() ? null : mainUrl);
                if (mainPath != null) urls.add(mainPath);
                for (Part part : extraParts) {
                    String saved = saveOne(part);
                    if (saved != null) urls.add(saved);
                }
                urls.addAll(extraUrls);

                if (isNew) {
                    productDAO.insert(p, urls);
                    Flash.success(req, "Đã thêm sản phẩm.");
                } else {
                    p.setId(Integer.parseInt(id));
                    if (productDAO.update(p, urls, removeImgs)) {
                        for (String u : removeImgs) deleteLocalFile(u);   // chỉ xóa file sau khi DB đã lưu xong
                        Flash.success(req, "Đã lưu sản phẩm.");
                    } else Flash.error(req, "Không tìm thấy sản phẩm.");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fatal = "Lỗi: " + e.getMessage();
        }

        if (fatal != null || !fe.isEmpty()) {
            showForm(req, resp, shop, isNew, id, orig, f, fe, rows, removeImgs,
                    fatal != null ? fatal : "Vui lòng kiểm tra lại các mục được đánh dấu đỏ bên dưới.");
            return;
        }
        resp.sendRedirect(back);
    }

    // ===================================================== helper

    /** Trả lại form kèm dữ liệu người dùng đã nhập + lỗi từng ô. */
    private void showForm(HttpServletRequest req, HttpServletResponse resp, ShopDTO shop, boolean isNew, String id,
                          Product orig, Map<String, String> f, Map<String, String> fe,
                          List<Map<String, Object>> rows, List<String> removed, String message) throws ServletException, IOException {
        try {
            req.setAttribute("categories", categoryDAO.findAllWithCount(shop.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        req.setAttribute("shop", shop);
        req.setAttribute("sellerTab", "products");
        req.setAttribute("editing", !isNew);
        if (!isNew) req.setAttribute("editId", id.replaceAll("[^0-9]", ""));
        if (orig != null) req.setAttribute("product", orig);
        req.setAttribute("f", f);
        req.setAttribute("fieldErrors", fe);
        req.setAttribute("formCharms", rows);
        req.setAttribute("removedImages", removed);    // để JSP vẫn đánh dấu "đã xóa" sau khi lỗi
        req.setAttribute("error", message);
        req.getRequestDispatcher(VIEW_FORM).forward(req, resp);
    }

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

    /** Xóa file ảnh đã tải lên trong uploads/products; bỏ qua link http và đường dẫn lạ. */
    private void deleteLocalFile(String path) {
        try {
            if (path == null || path.startsWith("http") || path.contains("..")
                    || !path.startsWith("uploads/products/")) return;
            File f = new File(getServletContext().getRealPath("/" + path));
            if (f.isFile()) f.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Thông báo lỗi nếu file tải lên không hợp lệ; null nếu hợp lệ. */
    private String imageError(Part part) {
        String submitted = part.getSubmittedFileName();
        String lower = submitted == null ? "" : submitted.toLowerCase();
        int dot = lower.lastIndexOf('.');
        String ext = dot >= 0 ? lower.substring(dot) : "";
        if (!EXT.contains(ext) || part.getContentType() == null || !part.getContentType().startsWith("image/")) {
            return "Ảnh phải là PNG hoặc JPG.";
        }
        if (part.getSize() > MAX_IMAGE_BYTES) return "Ảnh vượt quá 5MB.";
        return null;
    }

    /** Thông báo lỗi nếu link ảnh không hợp lệ; null nếu hợp lệ. */
    private String urlError(String url) {
        if (url.length() > MAX_URL_LEN) return "Link tối đa " + MAX_URL_LEN + " ký tự.";
        if (!url.matches(URL_RE)) return "Link ảnh phải bắt đầu bằng http:// hoặc https://";
        return null;
    }

    private static String shorten(String s) {
        return s.length() > 50 ? s.substring(0, 50) + "…" : s;
    }

    private static String val(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    /** Lấy shop đang hoạt động của user đăng nhập; trả null (và đã redirect/báo lỗi) nếu không hợp lệ. */
    private ShopDTO getShop(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login");
            return null;
        }
        try {
            int userId = ((Number) session.getAttribute("userId")).intValue();
            ShopDTO shop = shopDAO.findActiveByOwner(userId);
            if (shop == null) {
                resp.sendRedirect(req.getContextPath() + "/pages/home");
            }
            return shop;
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(500);
            return null;
        }
    }

    private static Integer parseIntOrNull(String s) {
        try { return (s == null || s.isEmpty()) ? null : Integer.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static Long parseLongOrNull(String s) {
        try { return (s == null || s.isEmpty()) ? null : Long.valueOf(s.trim()); } catch (NumberFormatException e) { return null; }
    }
    private static int parseIntOrDefault(String s, int d) {
        try { return Integer.parseInt(s); } catch (Exception e) { return d; }
    }

}