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
    <title>Đơn hàng</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-content.css">
    <style>
        .profile-orders-panel > .order-filter-panel {
            max-height: none;
            margin: 0 0 12px;
            padding: 0 0 12px;
            overflow-x: auto;
            overflow-y: visible;
            border-bottom: 1px solid #eee8e4;
            box-shadow: none;
        }

        .order-card {
            font-size: 16px;
            padding: 16px;
            margin-bottom: 14px;
            border: 1px solid #eee7e3;
            border-radius: 14px;
            background: #fff;
            box-shadow: 0 5px 18px rgba(45, 32, 28, .06);
        }

        .order-card-head, .order-summary-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            gap: 14px;
        }

        .order-card-head {
            min-height: 58px;
            padding-bottom: 12px;
            border-bottom: 1px solid #f0ece9;
        }

        .order-shop {
            color: #655650;
        }

        .order-card .order-status {
            color: #4560a5;
            font-size: 17px;
            font-weight: 700;
            text-align: right;
        }

        .order-summary-row {
            padding: 14px 0;
            align-items: flex-start;
        }

        .order-summary-product {
            display: grid;
            grid-template-columns: 92px minmax(0, 1fr) 130px;
            align-items: center;
            gap: 14px;
            min-width: 0;
            width: 100%;
        }

        .order-summary-product img {
            width: 92px;
            height: 92px;
            flex: 0 0 92px;
            border-radius: 10px;
            object-fit: cover;
            background: #f6f3f0;
        }

        .order-summary-text {
            min-width: 0;
        }

        .order-summary-text strong {
            display: -webkit-box;
            min-height: 2.9em;
            overflow: hidden;
            font-size: 18px;
            line-height: 1.45;
            overflow-wrap: anywhere;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
        }

        .order-summary-text p {
            margin: 6px 0 0;
            color: #70645f;
            font-size: 14px;
        }

        .order-summary-price {
            min-width: 130px;
            text-align: right;
            white-space: nowrap;
            font-size: 18px;
            font-weight: 700;
        }

        .order-summary-price small {
            display: block;
            margin-top: 5px;
            color: #746964;
            font-weight: 400;
        }

        .order-card-actions {
            display: flex;
            justify-content: flex-end;
            padding-top: 4px;
        }

        .order-detail-toggle {
            min-width: 150px;
            min-height: 44px;
            padding: 10px 18px;
            border: 0;
            border-radius: 22px;
            color: #fff;
            background: #465fa3;
            font: inherit;
            font-size: 15px;
            font-weight: 700;
            cursor: pointer;
        }

        .order-detail {
            padding-top: 12px;
            border-top: 1px solid #eee7e3;
        }

        .order-detail[hidden] {
            display: none;
        }

        .order-detail-item {
            display: grid;
            grid-template-columns: 54px minmax(0, 1fr) 120px;
            align-items: center;
            gap: 12px;
            padding: 9px 0;
        }

        .order-detail-item img {
            width: 54px;
            height: 54px;
            border-radius: 8px;
            object-fit: cover;
            background: #f6f3f0;
        }

        .order-detail-item-name {
            display: -webkit-box;
            overflow: hidden;
            line-height: 1.4;
            overflow-wrap: anywhere;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
        }

        .order-detail-item > strong {
            text-align: right;
            white-space: nowrap;
        }

        .order-detail .order-meta {
            display: grid;
            gap: 3px;
            margin: 0 0 8px;
            font-size: 14px;
        }

        .order-detail .order-meta p {
            margin: 3px 0;
        }

        .order-total-lines {
            display: grid;
            gap: 6px;
            justify-items: end;
            padding-top: 10px;
            border-top: 1px solid #eee7e3;
        }

        @media (max-width: 600px) {
            .order-summary-row {
                align-items: flex-start;
            }

            .order-summary-product img {
                width: 68px;
                height: 68px;
                flex-basis: 68px;
            }

            .order-summary-product {
                grid-template-columns: 68px minmax(0, 1fr) 82px;
                gap: 10px;
            }

            .order-summary-price {
                min-width: 82px;
                font-size: .9rem;
            }

            .order-summary-text strong { font-size: 15px; }
            .order-summary-text p { font-size: 12px; }
            .order-detail-item { grid-template-columns: 44px minmax(0, 1fr) 82px; gap: 8px; }
            .order-detail-item img { width: 44px; height: 44px; }
            .order-detail-item > strong { font-size: 13px; }
        }
    </style>
</head>
<body>
<div id="header-placeholder"></div>

<main>
    <div class="container">
        <img class="avatar" src="${ctx}/assets/img/profile/avatar.png" alt="Avatar">
        <p class="info_name"><b><c:out value="${profile.name}"/></b>
            <a href="${ctx}/pages/profile-info"><i class="fa-solid fa-pencil"></i></a></p>
        <p class="email"><c:out value="${profile.email}"/></p>
        <div class="row body">
            <!--Thanh menu -->
            <div class="col-3 over_content">
                <ul class="menu_function">
                    <li class="funciton_item"><a href="${ctx}/pages/profile-info">Thông tin cá nhân</a></li>
                    <li class="funciton_item active"><a href="${ctx}/pages/profile-orders">Đơn hàng</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-designs">Thiết kế đã lưu</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-quotes">Báo giá</a></li>
                    <li class="funciton_item">
                        <a href="#" data-logout style="color: #E74C3C;">
                            <i class="fa-solid fa-arrow-right-from-bracket"></i> Đăng xuất
                        </a>
                    </li>
                </ul>
            </div>
            <!--Màn hình hiển thị thông tin -->
            <div class="col-9 over_content profile-orders-panel">
                <div class="col-12 over_content order-filter-panel">
                    <ul class="menu">
                        <li class="btn-primary filter active" data-filter="all">All</li>
                        <li class="btn-primary filter" data-filter="quote">Báo giá</li>
                        <li class="btn-primary filter" data-filter="processing">Đang thực hiện</li>
                        <li class="btn-primary filter" data-filter="pickup">Đang chờ lấy hàng</li>
                        <li class="btn-primary filter" data-filter="shipping">Đang giao</li>
                        <li class="btn-primary filter" data-filter="done">Đã giao</li>
                        <li class="btn-primary filter" data-filter="cancel">Đã hủy</li>
                        <li class="btn-primary filter" data-filter="return">Trả hàng / Khiếu nại</li>
                    </ul>
                </div>

                <c:forEach var="o" items="${orders}">
                    <div class="col-12 over_content order-card" data-status="${o.filterKey}" data-quote="${o.quote}">
                        <div class="order-card-head">
                            <strong>#${o.id} <span class="order-shop">/ <c:out value="${o.shopName}"/></span><small
                                    style="display:block;margin-top:4px;color:#817570;font-weight:400"><fmt:formatDate
                                    value="${o.dateCreate}" pattern="dd/MM/yyyy HH:mm"/></small></strong>
                            <span class="order-status"><c:out value="${o.statusLabel}"/></span>
                        </div>
                        <div class="order-summary-row">
                            <div class="order-summary-product">
                                <c:forEach var="it" items="${o.items}" begin="0" end="0">
                                    <c:choose>
                                        <c:when test="${not empty it.image and fn:startsWith(it.image, 'http')}"><img src="${it.image}" alt=""></c:when>
                                        <c:when test="${not empty it.image and fn:startsWith(it.image, '/')}"><img src="${ctx}${it.image}" alt=""></c:when>
                                        <c:when test="${not empty it.image}"><img src="${ctx}/${it.image}" alt=""></c:when>
                                        <c:otherwise><img src="${ctx}/assets/img/products/belly_02.jpg"
                                                          alt=""></c:otherwise>
                                    </c:choose>
                                    <div class="order-summary-text">
                                        <strong><c:out value="${it.name}"/></strong>
                                        <p>Loại: <c:out value="${it.type}"/> · Số lượng: ${it.quantity}<c:if
                                                test="${fn:length(o.items) gt 1}"> · +${fn:length(o.items) - 1} sản phẩm
                                            khác</c:if></p>
                                    </div>
                                    <div class="order-summary-price"><fmt:formatNumber value="${it.price}"
                                                                                       pattern="#,##0"/>đ
                                    </div>
                                </c:forEach>
                                <c:if test="${empty o.items}"><img src="${ctx}/assets/img/products/belly_02.jpg" alt="">
                                    <div class="order-summary-text"><strong>Đơn hàng #${o.id}</strong></div>
                                </c:if>
                            </div>
                        </div>
                        <div class="order-card-actions">
                            <button type="button" class="order-detail-toggle" aria-expanded="false">Xem chi tiết
                            </button>
                        </div>
                        <div class="order-detail" hidden>
                            <div class="order-meta">
                                <p>Người nhận: <c:out value="${o.receiver}"/> · <c:out value="${o.phone}"/></p>
                                <p>Địa chỉ: <c:out value="${o.address}"/></p>
                                <p>Thanh toán: <c:out value="${o.paymentMethod}"/></p>
                                <c:if test="${not empty o.note}"><p>Ghi chú: <c:out value="${o.note}"/></p></c:if>
                            </div>
                            <c:forEach var="it" items="${o.items}">
                                <div class="order-detail-item">
                                    <c:choose>
                                        <c:when test="${not empty it.image and fn:startsWith(it.image, 'http')}"><img src="${it.image}" alt=""></c:when>
                                        <c:when test="${not empty it.image and fn:startsWith(it.image, '/')}"><img src="${ctx}${it.image}" alt=""></c:when>
                                        <c:when test="${not empty it.image}"><img src="${ctx}/${it.image}" alt=""></c:when>
                                        <c:otherwise><img src="${ctx}/assets/img/products/belly_02.jpg"
                                                          alt=""></c:otherwise>
                                    </c:choose>
                                    <span class="order-detail-item-name"><c:out
                                            value="${it.name}"/> × ${it.quantity}</span>
                                    <strong><fmt:formatNumber value="${it.price}" pattern="#,##0"/>đ</strong>
                                </div>
                            </c:forEach>
                            <div class="order-total-lines">
                                <c:if test="${o.shippingFee.signum() gt 0}"><span>Phí vận chuyển: <fmt:formatNumber
                                        value="${o.shippingFee}" pattern="#,##0"/>đ</span></c:if>
                                <c:if test="${o.discount.signum() gt 0}"><span>Giảm giá: -<fmt:formatNumber
                                        value="${o.discount}" pattern="#,##0"/>đ</span></c:if>
                                <strong>Tổng đơn: <fmt:formatNumber value="${o.amount}" pattern="#,##0"/>đ</strong>
                            </div>
                        </div>
                    </div>
                </c:forEach>

                <c:if test="${empty orders}">
                    <div class="col-12 over_content"><p>Bạn chưa có đơn hàng nào.</p></div>
                </c:if>
            </div>
        </div>
    </div>
</main>

<div id="footer-placeholder"></div>

<script>
    const CTX = '${ctx}';
    fetch(CTX + '/components/header.html').then(r => r.text())
        .then(html => document.getElementById('header-placeholder').innerHTML = html);
    fetch(CTX + '/components/footer.html').then(r => r.text())
        .then(html => document.getElementById('footer-placeholder').innerHTML = html);

    // Lọc đơn theo tab
    document.querySelectorAll('.filter').forEach(tab => {
        tab.addEventListener('click', () => {
            document.querySelectorAll('.filter').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            const f = tab.dataset.filter;
            document.querySelectorAll('.order-card').forEach(card => {
                const show = f === 'all'
                    || (f === 'quote' ? card.dataset.quote === 'true' : card.dataset.status === f);
                card.style.display = show ? '' : 'none';
            });
        });
    });
    document.querySelectorAll('.order-detail-toggle').forEach(button => {
        button.addEventListener('click', () => {
            const detail = button.closest('.order-card').querySelector('.order-detail');
            const expanded = button.getAttribute('aria-expanded') === 'true';
            button.setAttribute('aria-expanded', String(!expanded));
            detail.hidden = expanded;
            button.textContent = expanded ? 'Xem chi tiết' : 'Ẩn chi tiết';
        });
    });
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>