package controller.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebFilter(urlPatterns = {"/pages/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        // đường dẫn không gồm context path, vd: /pages/seller/seller-dashboard.html
        String path = req.getRequestURI().substring(req.getContextPath().length());

        boolean isAdminPage  = path.startsWith("/pages/admin/") || path.startsWith("/pages/admin-");
        boolean isSellerPage = path.startsWith("/pages/seller/");

        // Trang công khai: cho qua
        if (!isAdminPage && !isSellerPage) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.sendRedirect(req.getContextPath() + "/pages/login.html");
            return;
        }

        String role = String.valueOf(session.getAttribute("role")).toLowerCase();
        boolean isAdmin  = role.equals("admin");
        boolean isSeller = role.equals("seller") || role.equals("artisan");

        if ((isAdminPage && !isAdmin) || (isSellerPage && !isSeller && !isAdmin)) {
            resp.sendRedirect(req.getContextPath() + "/index.html");
            return;
        }

        chain.doFilter(request, response);
    }
}