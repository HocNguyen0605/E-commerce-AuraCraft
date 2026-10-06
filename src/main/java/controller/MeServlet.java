package controller;

import com.google.gson.Gson;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebServlet("/api/me")
public class MeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");

        Map<String, Object> body = new LinkedHashMap<>();
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("userId") != null) {
            body.put("loggedIn", true);
            body.put("name", session.getAttribute("userName"));
            body.put("role", session.getAttribute("role"));
        } else {
            body.put("loggedIn", false);
        }
        resp.getWriter().write(new Gson().toJson(body));
    }
}