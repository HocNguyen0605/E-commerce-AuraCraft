package controller;

import com.google.gson.Gson;
import dao.FinanceDAO;
import dto.ChartDataDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/api/admin/finance/charts")
public class AdminFinanceServlet extends HttpServlet {

    private FinanceDAO financeDAO;

    @Override
    public void init() throws ServletException {
        financeDAO = new FinanceDAO();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String startDate = request.getParameter("startDate");
        String endDate   = request.getParameter("endDate");

        ChartDataDTO chartData = financeDAO.getChartData(startDate, endDate);

        Gson gson = new Gson();
        response.getWriter().write(gson.toJson(chartData));
    }
}
