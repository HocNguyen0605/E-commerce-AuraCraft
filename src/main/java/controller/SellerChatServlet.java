package controller;

import dao.ChatDAO;
import dao.ShopDAO;
import dto.ChatMessageDTO;
import dto.ConversationDTO;
import dto.ShopDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/pages/seller/chat-seller")
public class SellerChatServlet extends HttpServlet {

    private static final String PAGE_PATH = "/pages/seller/chat-seller";             // URL servlet (không .jsp)
    private static final String VIEW = "/pages/seller/chat-seller.jsp";              // JSP thật

    private final ChatDAO chatDAO = new ChatDAO();
    private final ShopDAO shopDAO = new ShopDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        try {
            long buyerId = parseLong(req.getParameter("buyer"));
            if (buyerId > 0) {
                long convId = chatDAO.findOrCreateConversation(shop.getId(), buyerId);
                resp.sendRedirect(req.getContextPath() + PAGE_PATH + (convId > 0 ? "?c=" + convId : ""));
                return;
            }

            String q = req.getParameter("q");
            List<ConversationDTO> conversations = chatDAO.findConversations(shop.getId(), q);

            ConversationDTO active = null;
            long cid = parseLong(req.getParameter("c"));
            if (cid > 0) {
                active = chatDAO.findByIdAndShop(cid, shop.getId());
            } else if (!conversations.isEmpty()) {
                active = conversations.get(0);
            }

            if (active != null) {
                List<ChatMessageDTO> messages = chatDAO.findMessages(active.getId());
                chatDAO.markReadForSeller(active.getId());
                active.setSellerUnread(0);
                req.setAttribute("messages", messages);
            }

            int unread = chatDAO.countUnread(shop.getId());
            req.setAttribute("shop", shop);
            req.setAttribute("sellerTab", "chat");
            req.setAttribute("conversations", conversations);
            req.setAttribute("active", active);
            req.setAttribute("unreadCount", unread);
            req.setAttribute("unreadMessages", unread);   // sidebar dùng tên này
            req.getRequestDispatcher(VIEW).forward(req, resp);
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        ShopDTO shop = getShop(req, resp);
        if (shop == null) return;

        long cid = parseLong(req.getParameter("conversationId"));
        String content = req.getParameter("content");

        try {
            if ("send".equals(req.getParameter("action"))
                    && content != null && !content.isBlank() && content.length() <= 2000
                    && chatDAO.findByIdAndShop(cid, shop.getId()) != null) {   // kiểm tra quyền sở hữu
                chatDAO.insertSellerMessage(cid, content.trim());
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
        resp.sendRedirect(req.getContextPath() + PAGE_PATH + "?c=" + cid);   // PRG: tránh gửi lại khi F5
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

    private long parseLong(String s) {
        try { return s == null ? 0 : Long.parseLong(s); } catch (NumberFormatException e) { return 0; }
    }
}