<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%--
  Đặt tại: webapp/pages/order-management.jsp  (SellerOrdersServlet forward tới đây)

  Servlet set (request attribute):
    shop      : Shop (name, avatar) - dùng cho sidebar
    orders    : List<SellerOrder> (id, buyerId, buyerName, productNames, amount, dateCreate, address,
                                   status, deadline, latestNote, pendingExtension, uiStatus, overdueDays)
    lateCount : số đơn trễ hạn
    success / error : flash message
  POST /seller/orders  action = start | progress | extend | deliver, orderId, (note | reason + days)
--%>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sellerTab" value="orders" scope="request"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Đơn hàng - AuraCraft</title>
    <!-- Fonts & Icons -->
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">

    <!-- Styles -->
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/pages/styleSeller.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/pages/styleOrders.css">
    <style>
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-success { background:#d4edda; color:#155724; border:1px solid #c3e6cb; }
        .alert-danger  { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
        .empty-orders { text-align:center; padding: 30px; color: var(--text-muted); }
    </style>
</head>
<body>
<jsp:include page="/src/main/webapp/components/header.html"/>

<main class="seller-layout container">
    <%@ include file="/src/main/webapp/components/seller-sidebar.jspf" %>

    <!-- Content -->
    <section class="seller-content">
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title"><i class="fa-solid fa-box-open"></i> Quản lý đơn hàng</h1>
        </div>

        <div class="dashboard-section table-section">
            <!-- Bộ lọc Tabs -->
            <div class="tabs">
                <div class="tab-item active" data-tab="all">Tất cả</div>
                <div class="tab-item" data-tab="pending">Chờ xử lý</div>
                <div class="tab-item" data-tab="crafting">Đang chế tác</div>
                <div class="tab-item" data-tab="late" style="color: var(--status-late); font-weight: bold;">Trễ hạn (${lateCount})</div>
                <div class="tab-item" data-tab="shipping">Đang giao</div>
                <div class="tab-item" data-tab="done">Đã hoàn thành</div>
            </div>

            <section class="custom-orders-section" aria-label="Đơn hàng Custom">
                <h2>Đơn hàng Custom</h2>
                <div class="order-list" id="customOrderList"></div>
            </section>

            <!-- Danh sách đơn hàng -->
            <div class="order-list">
                <c:if test="${empty orders}">
                    <div class="empty-orders">Chưa có đơn hàng nào.</div>
                </c:if>

                <c:forEach var="o" items="${orders}">
                    <c:set var="ui" value="${o.uiStatus}"/>
                    <div class="order-card" data-status="${ui}"
                         <c:if test="${ui eq 'late'}">style="border-left: 4px solid var(--status-late);"</c:if>
                         <c:if test="${ui eq 'done'}">style="opacity: 0.8;"</c:if>
                         data-id="#ORD-${o.id}"
                         data-buyer="<c:out value='${o.buyerName}'/>"
                         data-product="<c:out value='${o.productNames}'/>"
                         data-price="<fmt:formatNumber value='${o.amount}' pattern='#,##0'/> đ"
                         data-date="<fmt:formatDate value='${o.dateCreate}' pattern='dd/MM/yyyy - HH:mm'/>"
                         data-address="<c:out value='${o.address}'/>"
                         data-note="<c:out value='${empty o.latestNote ? "Chưa có cập nhật tiến độ." : o.latestNote}'/>">

                        <div class="order-header">
                            <span class="order-id" <c:if test="${ui eq 'late'}">style="color: var(--status-late);"</c:if>>Mã ĐH: #ORD-${o.id}</span>
                            <c:choose>
                                <c:when test="${ui eq 'pending'}"><span class="badge pending">Chờ xử lý</span></c:when>
                                <c:when test="${ui eq 'crafting'}"><span class="badge crafting">Đang chế tác</span></c:when>
                                <c:when test="${ui eq 'late'}"><span class="badge late">Trễ hạn chế tác</span></c:when>
                                <c:when test="${ui eq 'shipping'}"><span class="badge shipping">Đang giao hàng</span></c:when>
                                <c:otherwise><span class="badge done">Đã hoàn thành</span></c:otherwise>
                            </c:choose>
                        </div>

                        <div class="order-body" onclick="openOrderDetail(this.parentElement)" style="cursor: pointer;" title="Nhấn để xem chi tiết">
                            <div class="order-info">
                                <h3 class="hover-title" <c:if test="${ui eq 'done'}">style="color: var(--text-muted);"</c:if>><c:out value="${o.productNames}"/></h3>
                                <p class="req-desc" style="margin: 0; color: var(--text-muted); font-size: 14px;">Khách hàng: <strong><c:out value="${o.buyerName}"/></strong></p>
                                <c:choose>
                                    <c:when test="${ui eq 'pending'}">
                                        <span class="deadline" style="color: var(--text-muted); background: var(--gray-light);"><i class="fa-regular fa-clock"></i> Khách vừa thanh toán</span>
                                    </c:when>
                                    <c:when test="${ui eq 'crafting' and not empty o.deadline}">
                                        <span class="deadline"><i class="fa-regular fa-clock"></i> Deadline: <fmt:formatDate value="${o.deadline}" pattern="dd/MM/yyyy - HH:mm"/></span>
                                    </c:when>
                                    <c:when test="${ui eq 'late'}">
                                        <span class="deadline late-alert"><i class="fa-solid fa-triangle-exclamation"></i> Quá hạn ${o.overdueDays} ngày!</span>
                                    </c:when>
                                </c:choose>
                                <c:if test="${o.pendingExtension}">
                                    <p style="margin: 5px 0 0 0; font-size: 13px; color: var(--text-muted);"><i class="fa-solid fa-hourglass-half"></i> Đã gửi yêu cầu gia hạn, đang chờ duyệt</p>
                                </c:if>
                            </div>
                        </div>

                        <div class="order-footer">
                            <div class="total-price"><fmt:formatNumber value="${o.amount}" pattern="#,##0"/> đ</div>
                            <div class="order-actions">
                                <c:if test="${ui eq 'late'}">
                                    <button type="button" class="btn btn-outline" style="color:var(--status-late); border-color:var(--status-late);"
                                        ${o.pendingExtension ? 'disabled title="Đã có yêu cầu gia hạn đang chờ duyệt"' : ''}
                                            onclick="requestExtension(${o.id})">Xin gia hạn</button>
                                </c:if>
                                <c:if test="${ui ne 'done'}">
                                    <button type="button" class="btn btn-outline" onclick="openChat(${o.buyerId})"><i class="fa-solid fa-comments"></i> Chat</button>
                                </c:if>
                                <c:choose>
                                    <c:when test="${ui eq 'pending'}">
                                        <button type="button" class="btn btn-primary" onclick="confirmOrder(${o.id})">Bắt đầu chế tác</button>
                                    </c:when>
                                    <c:when test="${ui eq 'crafting'}">
                                        <button type="button" class="btn btn-outline" ${o.pendingExtension ? 'disabled' : ''} onclick="requestExtension(${o.id})">Xin gia hạn</button>
                                        <button type="button" class="btn btn-primary" onclick="updateProgress(${o.id})">Cập nhật tiến độ</button>
                                    </c:when>
                                    <c:when test="${ui eq 'late'}">
                                        <button type="button" class="btn btn-outline" onclick="updateProgress(${o.id})">Cập nhật tiến độ</button>
                                        <button type="button" class="btn btn-primary" onclick="deliverNow(${o.id})">Bàn giao ngay</button>
                                    </c:when>
                                </c:choose>
                                <c:if test="${ui eq 'crafting'}">
                                    <button type="button" class="btn btn-outline" onclick="deliverNow(${o.id})">Bàn giao</button>
                                </c:if>
                            </div>
                        </div>
                    </div>
                </c:forEach>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/src/main/webapp/components/footer.html"/>

<!-- Form ẩn dùng chung cho các hành động (start / progress / extend / deliver) -->
<form id="orderActionForm" action="${ctx}/seller/orders" method="POST" style="display:none;">
    <input type="hidden" name="action"  id="actAction">
    <input type="hidden" name="orderId" id="actOrderId">
    <input type="hidden" name="note"    id="actNote">
    <input type="hidden" name="reason"  id="actReason">
    <input type="hidden" name="days"    id="actDays">
</form>

<!-- Chi Tiết Đơn Hàng -->
<div class="modal-overlay" id="orderModal">
    <div class="modal-content" style="max-width: 600px;">
        <div class="modal-header">
            <h2>Chi Tiết Đơn Hàng <span id="modalOrderId" style="color: var(--primary-brown);"></span></h2>
            <button type="button" class="close-btn" onclick="closeOrderDetail()"><i class="fa-solid fa-times"></i></button>
        </div>
        <div class="detail-body">
            <h3 id="modalProduct" style="margin-top: 0; color: var(--primary-blue);"></h3>

            <div class="info-row"><strong>Khách hàng:</strong> <span id="modalBuyer"></span></div>
            <div class="info-row"><strong>Ngày đặt:</strong> <span id="modalDate"></span></div>
            <div class="info-row"><strong>Địa chỉ giao hàng:</strong> <span id="modalAddress"></span></div>
            <div class="info-row" style="margin-top: 15px; padding: 15px; background: var(--bg-cream); border-radius: 8px;">
                <strong style="display: block; margin-bottom: 5px;">Ghi chú / Tiến độ nội bộ:</strong>
                <span id="modalNote" style="color: var(--text-muted); font-size: 14px; line-height: 1.5;"></span>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 24px; border-top: 1px dashed var(--gray-light); padding-top: 16px;">
                <span style="font-size: 16px;">Tổng thanh toán:</span>
                <span id="modalPrice" class="total-price"></span>
            </div>
        </div>
        <div class="modal-actions">
            <button type="button" class="btn btn-primary" onclick="closeOrderDetail()" style="width: 100%;">Đóng</button>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="${ctx}/src/main/webapp/assets/js/custom-workflow.js"></script>
<script src="${ctx}/src/main/webapp/assets/js/custom-order-management.js"></script>
<script>
    // --- LỌC THEO TAB ---
    const tabs = document.querySelectorAll('.tab-item');
    const orders = document.querySelectorAll('.order-card');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const filter = tab.getAttribute('data-tab');
            orders.forEach(order => {
                const status = order.getAttribute('data-status');
                order.style.display = (filter === 'all' || filter === status) ? 'block' : 'none';
            });
        });
    });

    // --- MODAL CHI TIẾT ---
    const orderModal = document.getElementById('orderModal');

    function openOrderDetail(cardElement) {
        document.getElementById('modalOrderId').innerText = cardElement.getAttribute('data-id');
        document.getElementById('modalProduct').innerText = cardElement.getAttribute('data-product');
        document.getElementById('modalBuyer').innerText = cardElement.getAttribute('data-buyer');
        document.getElementById('modalDate').innerText = cardElement.getAttribute('data-date');
        document.getElementById('modalAddress').innerText = cardElement.getAttribute('data-address');
        document.getElementById('modalNote').innerText = cardElement.getAttribute('data-note');
        document.getElementById('modalPrice').innerText = cardElement.getAttribute('data-price');
        orderModal.classList.add('active');
    }

    function closeOrderDetail() { orderModal.classList.remove('active'); }

    orderModal.addEventListener('click', function (e) {
        if (e.target === orderModal) closeOrderDetail();
    });

    // --- GỬI HÀNH ĐỘNG LÊN SERVER ---
    function submitAction(action, orderId, extra) {
        extra = extra || {};
        document.getElementById('actAction').value  = action;
        document.getElementById('actOrderId').value = orderId;
        document.getElementById('actNote').value    = extra.note   || '';
        document.getElementById('actReason').value  = extra.reason || '';
        document.getElementById('actDays').value    = extra.days   || '';
        document.getElementById('orderActionForm').submit();
    }

    function openChat(buyerId) { location.href = '${ctx}/seller/chat?buyer=' + buyerId; }

    function confirmOrder(orderId) {
        if (confirm("Bạn đã chuẩn bị đủ nguyên liệu và muốn chuyển đơn #" + orderId + " sang trạng thái Đang Chế Tác?")) {
            submitAction('start', orderId);
        }
    }

    function updateProgress(orderId) {
        const note = prompt("Nhập tiến độ mới cho đơn #" + orderId + "\n(Ví dụ: Đang đính charm, Đang đổ keo...):");
        if (note && note.trim()) submitAction('progress', orderId, { note: note.trim() });
    }

    function requestExtension(orderId) {
        const reason = prompt("Nhập lý do xin gia hạn cho đơn #" + orderId + ":");
        if (!reason || !reason.trim()) return;
        const days = prompt("Cần thêm bao nhiêu ngày? (1 - 30)", "1");
        if (!days) return;
        submitAction('extend', orderId, { reason: reason.trim(), days: days.trim() });
    }

    function deliverNow(orderId) {
        if (confirm("Xác nhận đóng gói hoàn tất và chuyển đơn #" + orderId + " sang Đang giao hàng?")) {
            submitAction('deliver', orderId);
        }
    }
</script>
<script src="assets/js/auth.js"></script>
<script src="${ctx}/src/main/webapp/assets/js/popup.js"></script>
</body>
</html>
