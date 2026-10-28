<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sellerTab" value="dashboard" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kênh Thợ - Tổng quan Doanh thu</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleSeller.css">
    <style>
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
        .alert-danger  { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
        .status-badge.warning { background:#fff3cd; color:#856404; }
    </style>
</head>
<body>
<jsp:include page="/components/header.html"/>
<main class="seller-layout container">
    <%@ include file="/components/seller-sidebar.jsp" %>

    <section class="seller-content">
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title">Tổng quan Doanh thu</h1>
            <button type="button" class="btn btn-primary" onclick="openWithdrawModal()">
                <i class="bi bi-cash-stack"></i> Rút tiền
            </button>
        </div>

        <!-- Thống kê -->
        <div class="stats-grid">
            <div class="stat-card">
                <div class="stat-icon wallet"><i class="bi bi-wallet2"></i></div>
                <div class="stat-info">
                    <p>Số dư khả dụng</p>
                    <h3><fmt:formatNumber value="${balance}" pattern="#,##0"/>đ</h3>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon chart"><i class="bi bi-graph-up"></i></div>
                <div class="stat-info">
                    <p>Doanh thu tháng này</p>
                    <h3><fmt:formatNumber value="${revenueMonth}" pattern="#,##0"/>đ</h3>
                </div>
            </div>
            <div class="stat-card">
                <div class="stat-icon cart"><i class="bi bi-cart-fill"></i></div>
                <div class="stat-info">
                    <p>Đơn hàng chờ xử lý</p>
                    <h3>${pendingOrders} Đơn</h3>
                </div>
            </div>
        </div>

        <!-- Biểu đồ -->
        <div class="dashboard-section mt-4">
            <h2 class="section-title-sm">Biểu đồ doanh thu 7 ngày qua</h2>
            <div class="chart-wrapper">
                <div class="y-axis">
                    <span><fmt:formatNumber value="${chartMax}" maxFractionDigits="0"/></span>
                    <span><fmt:formatNumber value="${chartMax * 0.8}" maxFractionDigits="0"/></span>
                    <span><fmt:formatNumber value="${chartMax * 0.6}" maxFractionDigits="0"/></span>
                    <span><fmt:formatNumber value="${chartMax * 0.4}" maxFractionDigits="0"/></span>
                    <span><fmt:formatNumber value="${chartMax * 0.2}" maxFractionDigits="0"/></span>
                    <span>0</span>
                </div>

                <div class="chart-placeholder">
                    <div class="grid-line" style="bottom: 20%;"></div>
                    <div class="grid-line" style="bottom: 40%;"></div>
                    <div class="grid-line" style="bottom: 60%;"></div>
                    <div class="grid-line" style="bottom: 80%;"></div>
                    <div class="grid-line" style="top: 0;"></div>

                    <div class="bar-chart">
                        <c:forEach var="item" items="${chart}">
                            <c:set var="height" value="${chartMax > 0 ? (item.amount * 100.0 / chartMax) : 0}"/>
                            <div class="bar" style="height: ${height}%;">
                                <span class="val"><fmt:formatNumber value="${item.amount}" maxFractionDigits="0"/></span>
                                <span class="day"><c:out value="${item.label}"/></span>
                            </div>
                        </c:forEach>
                    </div>
                </div>
            </div>
        </div>

        <!-- Lịch sử giao dịch -->
        <div class="dashboard-section mt-4">
            <h2 class="section-title-sm">Lịch sử giao dịch gần đây</h2>
            <table class="data-table">
                <thead>
                <tr>
                    <th>Mã GD</th>
                    <th>Thời gian</th>
                    <th>Loại giao dịch</th>
                    <th>Số tiền</th>
                    <th>Trạng thái</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty ledger}">
                        <tr><td colspan="5" style="text-align:center;">Chưa có giao dịch nào.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="entry" items="${ledger}">
                            <tr>
                                <td><c:out value="${entry.code}"/></td>
                                <td><fmt:formatDate value="${entry.date}" pattern="dd/MM/yyyy HH:mm"/></td>
                                <td><c:out value="${entry.type}"/></td>
                                <td class="${entry.amount > 0 ? 'text-success' : 'text-danger'}">
                                        ${entry.amount > 0 ? '+' : ''}<fmt:formatNumber value="${entry.amount}" pattern="#,##0"/>đ
                                </td>
                                <td>
                                    <c:choose>
                                        <c:when test="${entry.status eq 'success' or entry.status eq 'approved'}">
                                            <span class="status-badge success">Thành công</span>
                                        </c:when>
                                        <c:when test="${entry.status eq 'pending'}">
                                            <span class="status-badge warning">Đang xử lý</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="status-badge danger">Thất bại</span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>
    </section>

    <!-- Modal rút tiền -->
    <div id="withdrawModal" class="modal-overlay">
        <div class="modal-content">
            <form action="${ctx}/pages/seller/dashboard" method="POST">
                <input type="hidden" name="action" value="withdraw">

                <div class="modal-header">
                    <h3>Yêu cầu Rút tiền</h3>
                    <button type="button" class="close-modal" onclick="closeWithdrawModal()"><i class="bi bi-x-lg"></i></button>
                </div>
                <div class="modal-body">
                    <div class="balance-card">
                        <p>Số dư khả dụng</p>
                        <h2><fmt:formatNumber value="${balance}" pattern="#,##0"/>đ</h2>
                    </div>

                    <div class="form-group mt-3">
                        <label>Số tiền cần rút (VND)</label>
                        <input type="number" name="amount" class="form-control" placeholder="Tối thiểu 50.000đ" min="50000" max="${balance}" required>
                    </div>
                    <div class="form-group mt-3">
                        <label>Ngân hàng nhận</label>
                        <input type="text" name="bankName" class="form-control" placeholder="VD: Vietcombank" required>
                    </div>
                    <div class="form-group mt-3">
                        <label>Số tài khoản</label>
                        <input type="text" name="bankAccount" class="form-control" placeholder="VD: 1012345678" required>
                    </div>
                    <div class="form-group mt-3">
                        <label>Chủ tài khoản (Viết hoa không dấu)</label>
                        <input type="text" name="accountHolder" class="form-control" placeholder="VD: NGUYEN VAN A" required>
                    </div>

                    <p class="withdraw-fee mt-3">Phí rút tiền: <strong>Miễn phí</strong></p>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline" onclick="closeWithdrawModal()">Hủy</button>
                    <button type="submit" class="btn btn-primary" ${balance < 50000 ? 'disabled title="Số dư không đủ để rút"' : ''}>
                        <i class="bi bi-check-lg"></i> Xác nhận rút
                    </button>
                </div>
            </form>
        </div>
    </div>
</main>

<jsp:include page="/components/footer.html"/>
<script>
    function openWithdrawModal()  { document.getElementById('withdrawModal').classList.add('active'); }
    function closeWithdrawModal() { document.getElementById('withdrawModal').classList.remove('active'); }
</script>
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>
