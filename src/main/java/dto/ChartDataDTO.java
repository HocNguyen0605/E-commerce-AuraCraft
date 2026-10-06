package dto;

import java.util.List;

public class ChartDataDTO {
    // Chart data
    private List<String> barLabels;
    private List<Double> barGmvData;
    private List<Double> barFeeData;
    private List<String> pieLabels;
    private List<Double> pieData;

    // Finance stats thực tế
    private double totalGmv;          // Tổng GMV (VND)
    private double totalFee;          // Tổng phí sàn thu được
    private double totalPaidToSeller; // Đã thanh toán cho thợ (GMV - fee)
    private double totalPendingBalance; // Chờ rút (balance chưa payout)

    public ChartDataDTO() {}

    public List<String> getBarLabels() { return barLabels; }
    public void setBarLabels(List<String> barLabels) { this.barLabels = barLabels; }

    public List<Double> getBarGmvData() { return barGmvData; }
    public void setBarGmvData(List<Double> barGmvData) { this.barGmvData = barGmvData; }

    public List<Double> getBarFeeData() { return barFeeData; }
    public void setBarFeeData(List<Double> barFeeData) { this.barFeeData = barFeeData; }

    public List<String> getPieLabels() { return pieLabels; }
    public void setPieLabels(List<String> pieLabels) { this.pieLabels = pieLabels; }

    public List<Double> getPieData() { return pieData; }
    public void setPieData(List<Double> pieData) { this.pieData = pieData; }

    public double getTotalGmv() { return totalGmv; }
    public void setTotalGmv(double totalGmv) { this.totalGmv = totalGmv; }

    public double getTotalFee() { return totalFee; }
    public void setTotalFee(double totalFee) { this.totalFee = totalFee; }

    public double getTotalPaidToSeller() { return totalPaidToSeller; }
    public void setTotalPaidToSeller(double totalPaidToSeller) { this.totalPaidToSeller = totalPaidToSeller; }

    public double getTotalPendingBalance() { return totalPendingBalance; }
    public void setTotalPendingBalance(double totalPendingBalance) { this.totalPendingBalance = totalPendingBalance; }
}
