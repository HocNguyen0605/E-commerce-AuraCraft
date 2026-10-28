package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebFilter(urlPatterns = {"/pages/*"})
public class AuthFilter implements Filter {

    // Trang không cần đăng nhập (login/register...). Ai cũng vào được
    private static final List<String> OPEN_PAGES = List.of(
            "/pages/login", "/pages/register"
    );

    // Trang của buyer, và cũng là trang công khai cho khách chưa đăng nhập
    private static final List<String> BUYER_PAGES = List.of(
            "/pages/home", "/pages/products", "/pages/cart",
            "/pages/checkout", "/pages/orders", "/pages/profile","/pages/customizer"
    );

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String path = req.getServletPath() + (req.getPathInfo() != null ? req.getPathInfo() : "");

        // 1. Login/register: cho qua
        if (matches(path, OPEN_PAGES)) {
            chain.doFilter(request, response);
            return;
        }

        // 2. Lấy thông tin đăng nhập
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute("userId") != null;
        String role = "";
        if (loggedIn) {
            Object r = session.getAttribute("role");
            role = r == null ? "" : r.toString().toLowerCase();
        }

        // 3. Xác định trang này thuộc nhóm nào
        Set<String> allowedRoles;
        boolean guestAllowed = false;

        if (path.startsWith("/pages/admin/") || path.startsWith("/pages/admin-")) {
            allowedRoles = Set.of("admin");
        } else if (path.startsWith("/pages/seller/")) {
            allowedRoles = Set.of("seller", "artisan");
        } else if (true) {
            allowedRoles = Set.of("buyer");
            guestAllowed = true;
        } else {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // 4. Chưa đăng nhập
        if (!loggedIn) {
            if (guestAllowed) {
                chain.doFilter(request, response);   // khách xem home/products
            } else {
                resp.sendRedirect(req.getContextPath() + "/pages/login");
            }
            return;
        }

        // 5. Đã đăng nhập nhưng sai role: đá về trang của role đó
        if (!allowedRoles.contains(role)) {
            resp.sendRedirect(req.getContextPath() + homeOf(role));
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean matches(String path, List<String> prefixes) {
        for (String p : prefixes) {
            if (path.equals(p) || path.startsWith(p + "/")) return true;
        }
        return false;
    }

    private String homeOf(String role) {
        switch (role) {
            case "admin":   return "/pages/admin/admin-users.jsp";
            case "seller":
            case "artisan": return "/pages/seller/seller-dashboard.jsp";
            default:        return "/pages/home";
        }
    }
}