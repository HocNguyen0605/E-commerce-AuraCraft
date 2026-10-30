<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
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
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleSeller.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleOrders.css">
</head>
<body>

<main class="seller-layout container">
    <%@ include file="/components/seller-sidebar.jsp" %>

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

            <%--            <section class="custom-orders-section" aria-label="Đơn hàng Custom">--%>
            <%--                <h2>Đơn hàng Custom</h2>--%>
            <%--                <div class="order-list" id="customOrderList"></div>--%>
            <%--            </section>--%>

            <!-- Danh sách đơn hàng -->
            <div class="order-list" id="serverOrderList">
                <c:if test="${empty orders}">
                    <div class="empty-orders">Chưa có đơn hàng nào.</div>
                </c:if>

                <c:forEach var="o" items="${orders}">
                    <c:set var="ui" value="${o.uiStatus}"/>
                    <c:choose>
                        <c:when test="${ui eq 'pending'}"><c:set var="uiText" value="Chờ xử lý"/></c:when>
                        <c:when test="${ui eq 'crafting'}"><c:set var="uiText" value="Đang chế tác"/></c:when>
                        <c:when test="${ui eq 'late'}"><c:set var="uiText" value="Trễ hạn chế tác"/></c:when>
                        <c:when test="${ui eq 'shipping'}"><c:set var="uiText" value="Đang giao hàng"/></c:when>
                        <c:otherwise><c:set var="uiText" value="Đã hoàn thành"/></c:otherwise>
                    </c:choose>

                    <div class="order-card" data-status="${ui}" data-id="#ORD-${o.id}"
                         <c:if test="${ui eq 'late'}">style="border-left: 4px solid var(--status-late);"</c:if>
                         <c:if test="${ui eq 'done'}">style="opacity: 0.8;"</c:if>>

                        <div class="order-header">
                            <span class="order-id" <c:if test="${ui eq 'late'}">style="color: var(--status-late);"</c:if>>Mã ĐH: #ORD-${o.id}</span>
                            <span class="badge ${ui}">${uiText}</span>
                        </div>

                        <div class="order-body" onclick="openOrderDetail(this.closest('.order-card'))" style="cursor: pointer;" title="Nhấn để xem chi tiết">
                            <div class="order-info">
                                <h3 class="hover-title" <c:if test="${ui eq 'done'}">style="color: var(--text-muted);"</c:if>><c:out value="${o.productNames}"/></h3>
                                <p class="req-desc" style="margin: 0; color: var(--text-muted); font-size: 14px;">Khách hàng: <strong><c:out value="${empty o.receiver ? o.buyerName : o.receiver}"/></strong></p>
                                <c:choose>
                                    <c:when test="${ui eq 'pending'}">
                                        <span class="deadline" style="color: var(--text-muted); background: var(--gray-light);"><i class="fa-regular fa-clock"></i> Đơn mới, chờ bạn xác nhận</span>
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
                            <div class="total-price"><fmt:formatNumber value="${o.amount}" pattern="#,##0"/>đ</div>
                            <div class="order-actions">
                                <button type="button" class="btn btn-outline" onclick="openOrderDetail(this.closest('.order-card'))">
                                </button>
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

                        <!-- Nội dung popup chi tiết (không hiển thị, JS sao chép vào modal) -->
                        <template class="order-detail-tpl">
                            <div class="od-grid">
                                <div class="od-field"><span>Trạng thái</span><strong><span class="badge ${ui}">${uiText}</span></strong></div>
                                <div class="od-field"><span>Ngày đặt</span><strong><fmt:formatDate value="${o.dateCreate}" pattern="dd/MM/yyyy - HH:mm"/></strong></div>
                                <div class="od-field"><span>Người nhận</span><strong><c:out value="${empty o.receiver ? o.buyerName : o.receiver}"/></strong></div>
                                <div class="od-field"><span>Số điện thoại</span><strong><c:out value="${empty o.phone ? 'Chưa có' : o.phone}"/></strong></div>
                                <div class="od-field"><span>Thanh toán</span><strong><c:out value="${o.paymentLabel}"/></strong></div>
                                <c:if test="${not empty o.deadline}">
                                    <div class="od-field"><span>Hạn chế tác</span><strong><fmt:formatDate value="${o.deadline}" pattern="dd/MM/yyyy - HH:mm"/></strong></div>
                                </c:if>
                                <div class="od-field od-full"><span>Địa chỉ giao hàng</span><strong><c:out value="${o.address}"/></strong></div>
                                <c:if test="${not empty o.buyerNote}">
                                    <div class="od-field od-full"><span>Ghi chú của người mua</span><strong><c:out value="${o.buyerNote}"/></strong></div>
                                </c:if>
                            </div>

                            <h4 class="od-heading">Sản phẩm trong đơn (${fn:length(o.items)} loại, ${o.itemCount} sản phẩm)</h4>
                            <div class="od-items">
                                <c:forEach var="it" items="${o.items}">
                                    <div class="od-item">
                                        <img src="<c:out value='${it.imageUrl}'/>" alt="<c:out value='${it.name}'/>"
                                             onerror="this.onerror=null;this.src='${ctx}/assets/img/products/bracelet_01.jpg'">
                                        <div class="od-item-info">
                                            <div class="od-item-name"><c:out value="${it.name}"/></div>
                                            <div class="od-item-meta">
                                                <fmt:formatNumber value="${it.price}" pattern="#,##0"/>đ × ${it.quantity}
                                            </div>
                                        </div>
                                        <div class="od-item-total"><fmt:formatNumber value="${it.lineTotal}" pattern="#,##0"/>đ</div>
                                    </div>
                                </c:forEach>
                                <c:if test="${empty o.items}">
                                    <div class="od-empty">Không có dữ liệu sản phẩm cho đơn này.</div>
                                </c:if>
                            </div>

                            <div class="od-summary">
                                <div class="od-sum-row"><span>Tiền hàng</span><span><fmt:formatNumber value="${o.itemsTotal}" pattern="#,##0"/>đ</span></div>
                                <div class="od-sum-row"><span>Phí vận chuyển</span><span><fmt:formatNumber value="${o.shippingFee}" pattern="#,##0"/>đ</span></div>
                                <c:if test="${o.discount > 0}">
                                    <div class="od-sum-row"><span>Giảm giá</span><span>-<fmt:formatNumber value="${o.discount}" pattern="#,##0"/>đ</span></div>
                                </c:if>
                                <div class="od-sum-row od-sum-total"><span>Tổng thanh toán</span><span><fmt:formatNumber value="${o.amount}" pattern="#,##0"/>đ</span></div>
                            </div>

                            <div class="od-note">
                                <strong>Ghi chú / Tiến độ nội bộ</strong>
                                <span><c:out value="${empty o.latestNote ? 'Chưa có cập nhật tiến độ.' : o.latestNote}"/></span>
                            </div>
                        </template>
                    </div>
                </c:forEach>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/components/footer.html"/>

<!-- Form ẩn dùng chung cho các hành động (start / progress / extend / deliver) -->
<form id="orderActionForm" action="${ctx}/pages/seller/order-management" method="POST" style="display:none;">
    <input type="hidden" name="action"  id="actAction">
    <input type="hidden" name="orderId" id="actOrderId">
    <input type="hidden" name="note"    id="actNote">
    <input type="hidden" name="reason"  id="actReason">
    <input type="hidden" name="days"    id="actDays">
</form>

<!-- Chi Tiết Đơn Hàng -->
<div class="modal-overlay" id="orderModal">
    <div class="modal-content order-modal">
        <div class="modal-header">
            <h2>Chi tiết đơn hàng <span id="modalOrderId" style="color: var(--primary-brown);"></span></h2>
            <button type="button" class="close-btn" onclick="closeOrderDetail()" aria-label="Đóng"><i class="fa-solid fa-times"></i></button>
        </div>
        <div class="od-body" id="modalBody"></div>
        <div class="od-footer">
            <button type="button" class="btn btn-primary" onclick="closeOrderDetail()" style="width: 100%;">Đóng</button>
        </div>
    </div>
</div>

<!-- Scripts -->
<%--<script src="${ctx}/assets/js/custom-workflow.js"></script>--%>
<%--<script src="${ctx}/assets/js/custom-order-management.js"></script>--%>
<script>
    // --- LỌC THEO TAB (chỉ áp dụng cho danh sách đơn từ server) ---
    const sellerTabEls   = document.querySelectorAll('.tabs .tab-item');
    const sellerOrderEls = document.querySelectorAll('#serverOrderList .order-card');

    sellerTabEls.forEach(tab => {
        tab.addEventListener('click', () => {
            sellerTabEls.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const filter = tab.getAttribute('data-tab');
            sellerOrderEls.forEach(card => {
                const status = card.getAttribute('data-status');
                card.style.display = (filter === 'all' || filter === status) ? 'block' : 'none';
            });
        });
    });

    // --- MODAL CHI TIẾT ---
    const sellerOrderModal = document.getElementById('orderModal');

    function openOrderDetail(card) {
        if (!card) return;
        const tpl = card.querySelector('.order-detail-tpl');
        document.getElementById('modalOrderId').textContent = card.getAttribute('data-id');
        document.getElementById('modalBody').innerHTML = tpl ? tpl.innerHTML : '';
        document.getElementById('modalBody').scrollTop = 0;
        sellerOrderModal.classList.add('active');
    }

    // Dùng cho đơn Custom (custom-order-management.js): đưa 1 phần tử DOM vào popup
    function showOrderDetailNode(title, node) {
        document.getElementById('modalOrderId').textContent = title || '';
        const body = document.getElementById('modalBody');
        body.replaceChildren(node);
        body.scrollTop = 0;
        sellerOrderModal.classList.add('active');
    }

    function closeOrderDetail() { sellerOrderModal.classList.remove('active'); }

    sellerOrderModal.addEventListener('click', function (e) {
        if (e.target === sellerOrderModal) closeOrderDetail();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape') closeOrderDetail();
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

    function openChat(buyerId) { location.href = '${ctx}/pages/seller/chat-seller?buyer=' + buyerId; }

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
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>