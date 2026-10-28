<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sellerTab" value="dashboard" scope="request"/>
<c:set var="pagePath" value="${ctx}/pages/seller/seller-dashboard"/>
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
        .alert-info    { background:#e7f1ff; color:#084298; border:1px solid #b6d4fe; }
        .status-badge.warning { background:#fff3cd; color:#856404; }

        /* ---- Thanh lọc (class riêng rv-*, không dùng .btn của site để khỏi bị ghi đè) ---- */
        .rv-filter { display:flex; flex-direction:column; gap:14px; padding:16px 18px; margin:0 0 18px;
            background:#f8f9fb; border:1px solid #e3e6ec; border-radius:12px; box-sizing:border-box; }
        .rv-presets { display:flex; flex-wrap:wrap; align-items:center; gap:8px; }
        .rv-presets-label { font-size:13px; color:#5b6472; margin-right:2px; }
        .rv-chip { all:unset; box-sizing:border-box; cursor:pointer; height:30px; padding:0 14px; display:inline-flex;
            align-items:center; border:1px solid #cfd4dc; border-radius:999px; background:#fff;
            font-size:13px; color:#333; transition:background .15s, border-color .15s; }
        .rv-chip:hover { background:#eef1f8; border-color:#a9b4d3; }
        .rv-chip.active { background:#44599a; border-color:#44599a; color:#fff; }
        .rv-chip:focus-visible, .rv-btn:focus-visible { outline:2px solid #44599a; outline-offset:2px; }
        .rv-row { display:flex; flex-wrap:wrap; align-items:flex-end; gap:12px 14px; justify-content:flex-start; }
        .rv-field { display:flex; flex-direction:column; gap:6px; flex:0 0 auto; margin:0; }
        .rv-field.grow { flex:1 1 220px; }
        .rv-field label { font-size:12px; font-weight:600; color:#5b6472; margin:0; }
        .rv-field input, .rv-field select { box-sizing:border-box; height:38px; width:auto; min-width:150px; margin:0;
            padding:0 10px; border:1px solid #cfd4dc; border-radius:8px; background:#fff; font:inherit; font-size:14px; }
        .rv-field.grow input { width:100%; }
        .rv-field input:focus, .rv-field select:focus { outline:2px solid #9fb0dd; outline-offset:0; border-color:#44599a; }
        .rv-actions { display:flex; gap:8px; align-items:center; flex:0 0 auto; }
        .rv-btn { all:unset; box-sizing:border-box; cursor:pointer; height:38px; padding:0 18px; display:inline-flex;
            align-items:center; gap:6px; border-radius:8px; border:1px solid transparent; font-size:14px;
            font-weight:600; line-height:1; white-space:nowrap; transition:background .15s; }
        .rv-btn.primary { background:#44599a; color:#fff; }
        .rv-btn.primary:hover { background:#384b85; }
        .rv-btn.ghost { background:#fff; color:#333; border-color:#cfd4dc; }
        .rv-btn.ghost:hover { background:#eef1f8; }

        /* ---- Chỉ số theo khoảng ---- */
        .range-kpis { display:grid; grid-template-columns:repeat(auto-fit, minmax(220px, 1fr)); gap:14px; margin-bottom:22px; }
        .kpi { display:flex; gap:14px; align-items:flex-start; padding:16px 18px; border:1px solid #e3e6ec;
            border-radius:12px; background:#fff; }
        .kpi-icon { flex:0 0 auto; width:42px; height:42px; border-radius:10px; display:flex; align-items:center;
            justify-content:center; font-size:20px; background:#e8edf9; color:#44599a; }
        .kpi-icon.green { background:#e3f4ea; color:#198754; }
        .kpi-icon.amber { background:#fff3d6; color:#b7791f; }
        .kpi-body { min-width:0; }
        .kpi p { margin:0 0 4px; font-size:13px; color:#5b6472; }
        .kpi strong { display:block; font-size:22px; line-height:1.25; color:#2b3a67; }
        .kpi small { display:block; margin-top:6px; font-size:12px; color:#6c757d; }
        .up { color:#198754 !important; } .down { color:#dc3545 !important; }

        /* ---- Biểu đồ ---- */
        .chart-box { position:relative; height:320px; }
        .chart-empty { text-align:center; color:#6c757d; padding:8px 0 0; font-size:14px; }

        /* ---- Sản phẩm bán chạy ---- */
        .rank { display:inline-flex; width:26px; height:26px; align-items:center; justify-content:center;
            border-radius:50%; background:#eef2f8; font-weight:600; font-size:13px; }
        .rank.r1 { background:#ffe69c; } .rank.r2 { background:#dee2e6; } .rank.r3 { background:#f5c6a5; }
        .share { display:flex; align-items:center; gap:8px; min-width:130px; }
        .share-track { flex:1; height:6px; background:#e9ecef; border-radius:3px; overflow:hidden; }
        .share-fill { height:100%; background:#0d6efd; }
        .num { text-align:right; white-space:nowrap; }

        /* ---- Phân trang ---- */
        .pagination { display:flex; gap:6px; align-items:center; justify-content:flex-end; margin-top:14px; flex-wrap:wrap; }
        .pagination a, .pagination span { min-width:34px; height:34px; padding:0 10px; display:inline-flex;
            align-items:center; justify-content:center; border:1px solid #cfd4dc; border-radius:6px;
            text-decoration:none; color:inherit; font-size:14px; background:#fff; }
        .pagination a:hover { background:#eef2f8; }
        .pagination .current { background:#0d6efd; border-color:#0d6efd; color:#fff; }
        .pagination .disabled { opacity:.45; }
        .result-count { font-size:13px; color:#5b6472; margin:0 0 8px; }
        .table-scroll { overflow-x:auto; }
    </style>
</head>
<body>
<main class="seller-layout container">
    <%@ include file="/components/seller-sidebar.jsp" %>

    <section class="seller-content">
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>
        <c:if test="${not empty rangeNote}"><div class="alert alert-info"><c:out value="${rangeNote}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title">Tổng quan Doanh thu</h1>
            <button type="button" class="btn btn-primary" onclick="openWithdrawModal()">
                <i class="bi bi-cash-stack"></i> Rút tiền
            </button>
        </div>

        <!-- Thống kê nhanh -->
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
            <div class="stat-card">
                <div class="stat-icon wallet"><i class="bi bi-hourglass-split"></i></div>
                <div class="stat-info">
                    <p>Tiền rút đang chờ duyệt</p>
                    <h3><fmt:formatNumber value="${pendingWithdraw}" pattern="#,##0"/>đ</h3>
                </div>
            </div>
        </div>

        <!-- ===== Bộ lọc khoảng ngày: áp dụng cho chỉ số, biểu đồ và sản phẩm bán chạy ===== -->
        <div class="dashboard-section mt-4">
            <h2 class="section-title-sm">Phân tích doanh thu</h2>
            <form id="rangeForm" method="get" action="${pagePath}" class="rv-filter">
                <div class="rv-presets" role="group" aria-label="Chọn nhanh khoảng thời gian">
                    <span class="rv-presets-label">Chọn nhanh:</span>
                    <button type="button" class="rv-chip" data-range="7">7 ngày</button>
                    <button type="button" class="rv-chip" data-range="30">30 ngày</button>
                    <button type="button" class="rv-chip" data-range="90">90 ngày</button>
                    <button type="button" class="rv-chip" data-range="month">Tháng này</button>
                    <button type="button" class="rv-chip" data-range="prev">Tháng trước</button>
                </div>
                <div class="rv-row">
                    <div class="rv-field">
                        <label for="fFrom">Từ ngày</label>
                        <input type="date" id="fFrom" name="from" value="<c:out value='${fromDate}'/>">
                    </div>
                    <div class="rv-field">
                        <label for="fTo">Đến ngày</label>
                        <input type="date" id="fTo" name="to" value="<c:out value='${toDate}'/>">
                    </div>
                    <div class="rv-field">
                        <label for="fSort">Xếp hạng sản phẩm theo</label>
                        <select id="fSort" name="sort">
                            <option value="qty" ${sort eq 'qty' ? 'selected' : ''}>Số lượng bán</option>
                            <option value="revenue" ${sort eq 'revenue' ? 'selected' : ''}>Doanh thu</option>
                        </select>
                    </div>
                    <div class="rv-actions">
                        <button type="submit" class="rv-btn primary"><i class="bi bi-funnel"></i> Áp dụng</button>
                    </div>
                </div>
                <%-- giữ nguyên bộ lọc lịch sử giao dịch khi đổi khoảng ngày --%>
                <input type="hidden" name="lfrom" value="<c:out value='${lfrom}'/>">
                <input type="hidden" name="lto" value="<c:out value='${lto}'/>">
                <input type="hidden" name="ltype" value="<c:out value='${ltype}'/>">
                <input type="hidden" name="lstatus" value="<c:out value='${lstatus}'/>">
                <input type="hidden" name="lq" value="<c:out value='${lq}'/>">
            </form>

            <!-- Chỉ số trong khoảng đã chọn -->
            <div class="range-kpis">
                <div class="kpi">
                    <div class="kpi-icon"><i class="bi bi-cash-coin"></i></div>
                    <div class="kpi-body">
                        <p>Doanh thu (${fromLabel} – ${toLabel})</p>
                        <strong><fmt:formatNumber value="${rangeRevenue}" pattern="#,##0"/>đ</strong>
                        <c:choose>
                            <c:when test="${not empty revenueGrowth}">
                                <small class="${revenueGrowth >= 0 ? 'up' : 'down'}">
                                    <i class="bi ${revenueGrowth >= 0 ? 'bi-arrow-up-right' : 'bi-arrow-down-right'}"></i>
                                    <fmt:formatNumber value="${revenueGrowth < 0 ? -revenueGrowth : revenueGrowth}" pattern="0.0"/>%
                                    so với kỳ trước (${prevLabel})
                                </small>
                            </c:when>
                            <c:otherwise><small>Chưa có dữ liệu kỳ trước để so sánh</small></c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="kpi">
                    <div class="kpi-icon green"><i class="bi bi-bag-check"></i></div>
                    <div class="kpi-body">
                        <p>Số đơn hàng</p>
                        <strong>${rangeOrders}</strong>
                        <small>Không tính đơn hủy, hoàn tiền hoặc chưa thanh toán</small>
                    </div>
                </div>
                <div class="kpi">
                    <div class="kpi-icon amber"><i class="bi bi-receipt"></i></div>
                    <div class="kpi-body">
                        <p>Giá trị trung bình mỗi đơn</p>
                        <strong><fmt:formatNumber value="${rangeAvg}" pattern="#,##0"/>đ</strong>
                    </div>
                </div>
            </div>

            <!-- Biểu đồ -->
            <h3 class="section-title-sm" style="margin-top:8px;">
                Biểu đồ doanh thu ${monthly ? 'theo tháng' : 'theo ngày'}
            </h3>
            <div class="chart-box"><canvas id="revenueChart" aria-label="Biểu đồ doanh thu" role="img"></canvas></div>
            <c:if test="${rangeRevenue == 0}">
                <p class="chart-empty">Không có doanh thu trong khoảng ngày này. Thử chọn khoảng rộng hơn.</p>
            </c:if>
        </div>

        <!-- Sản phẩm bán chạy -->
        <div class="dashboard-section mt-4">
            <h2 class="section-title-sm">
                Sản phẩm bán chạy (${fromLabel} – ${toLabel})
            </h2>
            <div class="table-scroll">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Sản phẩm</th>
                        <th class="num">Đã bán</th>
                        <th class="num">Số đơn</th>
                        <th class="num">Doanh thu</th>
                        <th>Tỷ trọng</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty topProducts}">
                            <tr><td colspan="6" style="text-align:center;">Chưa có sản phẩm nào được bán trong khoảng này.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="p" items="${topProducts}" varStatus="st">
                                <c:set var="pct" value="${rangeRevenue > 0 ? p.revenue * 100.0 / rangeRevenue : 0}"/>
                                <tr>
                                    <td><span class="rank ${st.count le 3 ? 'r' : ''}${st.count le 3 ? st.count : ''}">${st.count}</span></td>
                                    <td><c:out value="${p.name}"/></td>
                                    <td class="num"><fmt:formatNumber value="${p.quantity}" pattern="#,##0"/></td>
                                    <td class="num">${p.orderCount}</td>
                                    <td class="num"><fmt:formatNumber value="${p.revenue}" pattern="#,##0"/>đ</td>
                                    <td>
                                        <div class="share">
                                            <div class="share-track"><div class="share-fill" style="width:${pct > 100 ? 100 : pct}%"></div></div>
                                            <span><fmt:formatNumber value="${pct}" pattern="0.0"/>%</span>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </div>

        <!-- ===== Lịch sử giao dịch ===== -->
        <div class="dashboard-section mt-4">
            <h2 class="section-title-sm">Lịch sử giao dịch</h2>

            <form method="get" action="${pagePath}" class="rv-filter">
                <div class="rv-row">
                    <div class="rv-field">
                        <label for="lFrom">Từ ngày</label>
                        <input type="date" id="lFrom" name="lfrom" value="<c:out value='${lfrom}'/>">
                    </div>
                    <div class="rv-field">
                        <label for="lTo">Đến ngày</label>
                        <input type="date" id="lTo" name="lto" value="<c:out value='${lto}'/>">
                    </div>
                    <div class="rv-field">
                        <label for="lType">Loại giao dịch</label>
                        <select id="lType" name="ltype">
                            <option value="">Tất cả</option>
                            <option value="sale" ${ltype eq 'sale' ? 'selected' : ''}>Bán hàng</option>
                            <option value="withdraw" ${ltype eq 'withdraw' ? 'selected' : ''}>Rút tiền</option>
                        </select>
                    </div>
                    <div class="rv-field">
                        <label for="lStatus">Trạng thái</label>
                        <select id="lStatus" name="lstatus">
                            <option value="">Tất cả</option>
                            <option value="success" ${lstatus eq 'success' ? 'selected' : ''}>Thành công</option>
                            <option value="pending" ${lstatus eq 'pending' ? 'selected' : ''}>Đang xử lý</option>
                            <option value="failed" ${lstatus eq 'failed' ? 'selected' : ''}>Thất bại</option>
                        </select>
                    </div>
                    <div class="rv-field grow">
                        <label for="lQ">Tìm theo mã hoặc ngân hàng</label>
                        <input type="search" id="lQ" name="lq" maxlength="50" placeholder="VD: ORD-12, Vietcombank"
                               value="<c:out value='${lq}'/>">
                    </div>
                    <div class="rv-actions">
                        <button type="submit" class="rv-btn primary"><i class="bi bi-search"></i> Lọc</button>
                        <a class="rv-btn ghost" href="${pagePath}?<c:out value='${rangeQuery}'/>">Xóa lọc</a>
                    </div>
                </div>
                <%-- giữ nguyên khoảng ngày của phần phân tích --%>
                <input type="hidden" name="from" value="<c:out value='${fromDate}'/>">
                <input type="hidden" name="to" value="<c:out value='${toDate}'/>">
                <input type="hidden" name="sort" value="<c:out value='${sort}'/>">
            </form>

            <p class="result-count">
                <c:choose>
                    <c:when test="${ledgerTotal > 0}">Tìm thấy <strong>${ledgerTotal}</strong> giao dịch</c:when>
                    <c:otherwise>Không có giao dịch phù hợp.</c:otherwise>
                </c:choose>
            </p>

            <div class="table-scroll">
                <table class="data-table">
                    <thead>
                    <tr>
                        <th>Mã GD</th>
                        <th>Thời gian</th>
                        <th>Loại giao dịch</th>
                        <th class="num">Số tiền</th>
                        <th>Trạng thái</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty ledger}">
                            <tr><td colspan="5" style="text-align:center;">Chưa có giao dịch nào khớp với bộ lọc.</td></tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="entry" items="${ledger}">
                                <tr>
                                    <td><c:out value="${entry.code}"/></td>
                                    <td><fmt:formatDate value="${entry.date}" pattern="dd/MM/yyyy HH:mm"/></td>
                                    <td><c:out value="${entry.type}"/></td>
                                    <td class="num ${entry.amount > 0 ? 'text-success' : 'text-danger'}">
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

            <!-- Phân trang -->
            <c:if test="${ledgerPages > 1}">
                <c:set var="lo" value="${ledgerPage - 2 < 1 ? 1 : ledgerPage - 2}"/>
                <c:set var="hi" value="${ledgerPage + 2 > ledgerPages ? ledgerPages : ledgerPage + 2}"/>
                <nav class="pagination" aria-label="Phân trang lịch sử giao dịch">
                    <c:choose>
                        <c:when test="${ledgerPage > 1}">
                            <a href="${pagePath}?<c:out value='${ledgerBase}'/>&amp;page=${ledgerPage - 1}" aria-label="Trang trước"><i class="bi bi-chevron-left"></i></a>
                        </c:when>
                        <c:otherwise><span class="disabled"><i class="bi bi-chevron-left"></i></span></c:otherwise>
                    </c:choose>

                    <c:if test="${lo > 1}">
                        <a href="${pagePath}?<c:out value='${ledgerBase}'/>&amp;page=1">1</a>
                        <c:if test="${lo > 2}"><span class="disabled">…</span></c:if>
                    </c:if>
                    <c:forEach var="i" begin="${lo}" end="${hi}">
                        <c:choose>
                            <c:when test="${i == ledgerPage}"><span class="current" aria-current="page">${i}</span></c:when>
                            <c:otherwise><a href="${pagePath}?<c:out value='${ledgerBase}'/>&amp;page=${i}">${i}</a></c:otherwise>
                        </c:choose>
                    </c:forEach>
                    <c:if test="${hi < ledgerPages}">
                        <c:if test="${hi < ledgerPages - 1}"><span class="disabled">…</span></c:if>
                        <a href="${pagePath}?<c:out value='${ledgerBase}'/>&amp;page=${ledgerPages}">${ledgerPages}</a>
                    </c:if>

                    <c:choose>
                        <c:when test="${ledgerPage < ledgerPages}">
                            <a href="${pagePath}?<c:out value='${ledgerBase}'/>&amp;page=${ledgerPage + 1}" aria-label="Trang sau"><i class="bi bi-chevron-right"></i></a>
                        </c:when>
                        <c:otherwise><span class="disabled"><i class="bi bi-chevron-right"></i></span></c:otherwise>
                    </c:choose>
                </nav>
            </c:if>
        </div>
    </section>

    <!-- Modal rút tiền -->
    <div id="withdrawModal" class="modal-overlay">
        <div class="modal-content">
            <form action="${ctx}/pages/seller/seller-dashboard" method="POST">
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

<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.1/chart.umd.min.js"></script>
<script>
    function openWithdrawModal()  { document.getElementById('withdrawModal').classList.add('active'); }
    function closeWithdrawModal() { document.getElementById('withdrawModal').classList.remove('active'); }

    /* ---- Chọn nhanh khoảng ngày ---- */
    function iso(d) {
        var z = function (n) { return String(n).padStart(2, '0'); };
        return d.getFullYear() + '-' + z(d.getMonth() + 1) + '-' + z(d.getDate());
    }
    function rangeFor(kind) {
        var now = new Date(), from, to = new Date(now);
        if (kind === 'month') {
            from = new Date(now.getFullYear(), now.getMonth(), 1);
        } else if (kind === 'prev') {
            from = new Date(now.getFullYear(), now.getMonth() - 1, 1);
            to = new Date(now.getFullYear(), now.getMonth(), 0);
        } else {
            from = new Date(now);
            from.setDate(now.getDate() - (Number(kind) - 1));
        }
        return [from, to];
    }
    (function () {
        var fFrom = document.getElementById('fFrom'), fTo = document.getElementById('fTo');
        document.querySelectorAll('.rv-chip').forEach(function (btn) {
            var r = rangeFor(btn.dataset.range);
            // đánh dấu chip trùng với khoảng ngày đang xem
            if (iso(r[0]) === fFrom.value && iso(r[1]) === fTo.value) btn.classList.add('active');
            btn.addEventListener('click', function () {
                fFrom.value = iso(r[0]);
                fTo.value = iso(r[1]);
                document.getElementById('rangeForm').submit();
            });
        });
    })();

    /* ---- Biểu đồ doanh thu ---- */
    (function () {
        var labels = [<c:forEach var="item" items="${chart}" varStatus="s">"<c:out value='${item.label}'/>"${s.last ? '' : ','}</c:forEach>];
        var values = [<c:forEach var="item" items="${chart}" varStatus="s">${item.amount}${s.last ? '' : ','}</c:forEach>];
        var el = document.getElementById('revenueChart');
        if (!el || typeof Chart === 'undefined') return;
        var vnd = function (v) { return Number(v).toLocaleString('vi-VN') + 'đ'; };
        new Chart(el, {
            type: 'bar',
            data: { labels: labels, datasets: [{ label: 'Doanh thu', data: values, backgroundColor: '#0d6efd', borderRadius: 4, maxBarThickness: 36 }] },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { display: false },
                    tooltip: { callbacks: { label: function (ctx) { return vnd(ctx.parsed.y); } } }
                },
                scales: {
                    y: { beginAtZero: true, ticks: { callback: function (v) { return Number(v).toLocaleString('vi-VN'); } } },
                    x: { ticks: { autoSkip: true, maxRotation: 60 } }
                }
            }
        });
    })();
</script>
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>