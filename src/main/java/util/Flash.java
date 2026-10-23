package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public class Flash {

    public static void success(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flash_success", msg);
    }

    public static void error(HttpServletRequest req, String msg) {
        req.getSession().setAttribute("flash_error", msg);
    }

    /** Chuyển flash từ session sang request attribute (gọi ở doGet). */
    public static void transfer(HttpServletRequest req) {
        HttpSession s = req.getSession();
        for (String key : new String[]{"success", "error"}) {
            Object v = s.getAttribute("flash_" + key);
            if (v != null) {
                req.setAttribute(key, v);
                s.removeAttribute("flash_" + key);
            }
        }
    }
}