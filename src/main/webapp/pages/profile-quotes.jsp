<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Báo giá của tôi — AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-content.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-quotes.css">
</head>
<body class="profile-quotes-page" data-focus-request="<c:out value='${focusRequestId}'/>">
<div id="header-placeholder"></div>
<main>
    <div class="container">
        <img class="avatar" src="${ctx}/assets/img/profile/avatar.png" alt="Avatar">
        <p class="info_name"><b><c:out value="${profile.name}"/></b>
            <a href="${ctx}/pages/profile-info"><i class="fa-solid fa-pencil"></i></a></p>
        <p class="email"><c:out value="${profile.email}"/></p>
        <div class="row body">
            <div class="col-3 over_content">
                <ul class="menu_function">
                    <li class="funciton_item"><a href="${ctx}/pages/profile-info">Thông tin cá nhân</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-orders">Đơn hàng</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-designs">Thiết kế đã lưu</a></li>
                    <li class="funciton_item active"><a href="${ctx}/pages/profile-quotes">Báo giá</a></li>
                    <li class="funciton_item"><a href="#" data-logout style="color:#E74C3C"><i
                            class="fa-solid fa-arrow-right-from-bracket"></i> Đăng xuất</a></li>
                </ul>
            </div>
            <div class="col-9 over_content">
                <div class="col-12 panel">
                    <h2 class="panel-title">Yêu cầu thiết kế &amp; báo giá</h2>
                    <div class="quote-filter-row">
                        <button type="button" class="btn-primary filter active" data-filter="all">Tất cả</button>
                        <button type="button" class="btn-primary filter" data-filter="waiting">Chờ báo giá</button>
                        <button type="button" class="btn-primary filter" data-filter="quoted">Đã báo giá</button>
                        <button type="button" class="btn-primary filter" data-filter="accepted">Đã chấp nhận</button>
                        <button type="button" class="btn-primary filter" data-filter="expired">Đã đóng</button>
                    </div>
                    <c:if test="${not empty quotesError}">
                        <div class="quote-empty-state" role="alert"><c:out value="${quotesError}"/></div>
                    </c:if>
                    <c:choose>
                        <c:when test="${empty quoteRequests and empty quotesError}">
                            <div class="quote-empty-state"><i class="fa-regular fa-clipboard"></i>
                                <p>Bạn chưa gửi yêu cầu thiết kế nào.</p>
                                <a class="btn btn-primary btn-sm" href="${ctx}/pages/buyer-request-form">Gửi yêu cầu
                                    thiết kế</a></div>
                        </c:when>
                        <c:when test="${not empty quoteRequests}">
                            <c:forEach var="quoteRequest" items="${quoteRequests}">
                                <article class="quote-request-card" id="quote-request-${quoteRequest.id}"
                                         data-status="${quoteRequest.filterStatus}">
                                    <header class="quote-request-head">
                                        <h3 class="quote-request-title">Yêu cầu #${quoteRequest.id} · <c:out
                                                value="${quoteRequest.productType}"/></h3>
                                        <span class="quote-state">
                                            <c:choose>
                                                <c:when test="${quoteRequest.filterStatus eq 'waiting'}">Đang chờ thợ báo giá</c:when>
                                                <c:when test="${quoteRequest.filterStatus eq 'quoted'}">Đã có báo giá</c:when>
                                                <c:when test="${quoteRequest.filterStatus eq 'accepted'}">Đã chấp nhận báo giá</c:when>
                                                <c:otherwise>Đã đóng / từ chối</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </header>
                                    <details class="quote-details" id="quote-details-${quoteRequest.id}">
                                        <summary><span>Xem chi tiết yêu cầu và báo giá</span><i
                                                class="fa-solid fa-chevron-down" aria-hidden="true"></i></summary>
                                        <div class="quote-meta">
                                        <span>Ngày gửi: <fmt:formatDate value="${quoteRequest.createdAt}"
                                                                        pattern="dd/MM/yyyy HH:mm"/></span>
                                            <span>Ngân sách dự kiến: <strong><fmt:formatNumber
                                                    value="${quoteRequest.budget}"
                                                    pattern="#,##0"/>đ</strong></span>
                                            <span>${quoteRequest.quoteCount} báo giá</span>
                                        </div>
                                        <p class="quote-request-description"><c:out
                                                value="${quoteRequest.description}"/></p>
                                        <span class="quote-materials"><c:out value="${quoteRequest.materials}"/></span>
                                        <div class="quote-offers">
                                            <c:forEach var="quote" items="${quoteRequest.quotes}">
                                                <section class="quote-offer">
                                                    <div class="quote-offer-head">
                                                        <strong><c:out value="${quote.shopName}"/></strong>
                                                        <span class="quote-offer-price"><fmt:formatNumber
                                                                value="${quote.price}" pattern="#,##0"/>đ</span>
                                                    </div>
                                                    <div class="quote-meta">
                                                        <span>Thời gian dự kiến: ${quote.days} ngày</span>
                                                        <span><fmt:formatDate value="${quote.date}"
                                                                              pattern="dd/MM/yyyy HH:mm"/></span>
                                                        <span class="quote-offer-state quote-offer-state-${quote.status}">
                                                        <c:choose>
                                                            <c:when test="${quote.status eq 'pending'}">Đang chờ bạn phản hồi</c:when>
                                                            <c:when test="${quote.status eq 'accepted'}">Đã chấp nhận</c:when>
                                                            <c:when test="${quote.status eq 'rejected'}">Đã từ chối</c:when>
                                                            <c:otherwise>Đã rút báo giá</c:otherwise>
                                                        </c:choose>
                                                    </span>
                                                    </div>
                                                    <c:if test="${not empty quote.note}"><p class="quote-offer-note">
                                                        <c:out
                                                                value="${quote.note}"/></p></c:if>
                                                    <c:if test="${not empty quote.portfolio}"><a
                                                            href="<c:out value='${quote.portfolio}'/>" target="_blank"
                                                            rel="noopener noreferrer">Xem portfolio</a></c:if>
                                                </section>
                                            </c:forEach>
                                            <c:if test="${empty quoteRequest.quotes}">
                                                <div class="quote-empty-state"><i
                                                        class="fa-regular fa-hourglass-half"></i>
                                                    <p>Yêu cầu đã được gửi vào chợ. Khi shop gửi báo giá, báo giá sẽ
                                                        hiển
                                                        thị tại đây.</p></div>
                                            </c:if>
                                        </div>
                                    </details>
                                </article>
                            </c:forEach>
                        </c:when>
                    </c:choose>
                </div>
            </div>
        </div>
    </div>
</main>
<div id="footer-placeholder"></div>
<script>
    const ctx = '${ctx}';
    fetch(ctx + '/components/header.html').then(r => r.text()).then(html => document.getElementById('header-placeholder').innerHTML = html);
    fetch(ctx + '/components/footer.html').then(r => r.text()).then(html => document.getElementById('footer-placeholder').innerHTML = html);
    document.querySelectorAll('.quote-filter-row .filter').forEach(tab => tab.addEventListener('click', () => {
        document.querySelectorAll('.quote-filter-row .filter').forEach(item => item.classList.remove('active'));
        tab.classList.add('active');
        const filter = tab.dataset.filter;
        document.querySelectorAll('.quote-request-card').forEach(card => card.hidden = filter !== 'all' && card.dataset.status !== filter);
    }));
    const focusId = document.body.dataset.focusRequest;
    if (focusId) {
        const card = document.getElementById('quote-request-' + focusId);
        if (card) {
            const details = document.getElementById('quote-details-' + focusId);
            if (details) details.open = true;
            card.classList.add('is-focused');
            card.scrollIntoView({behavior: 'smooth', block: 'center'});
        }
    }
</script>
<script src="${ctx}/assets/js/popup.js"></script>
<script src="${ctx}/assets/js/site-shell.js"></script>
<script src="${ctx}/assets/js/auth.js"></script>
</body>
</html>