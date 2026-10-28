<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:if test="${empty ctx}"><c:set var="ctx" value="${pageContext.request.contextPath}"/></c:if>

<aside class="seller-sidebar">

    <%-- Thương hiệu, giống sidebar admin (CSS nằm trong styleSeller.css) --%>
    <div class="seller-brand">
        AuraCraft <small>Seller</small>
    </div>

    <%-- Avatar + tên gian hàng (servlet phải setAttribute("shop", shop)) --%>
    <c:if test="${not empty shop}">
        <div class="seller-shop">
            <c:choose>
                <c:when test="${not empty shop.avatar}">
                    <img class="seller-shop-avt" src="${ctx}/<c:out value='${shop.avatar}'/>" alt="Avatar shop">
                </c:when>
                <c:otherwise>
                    <div class="seller-shop-avt placeholder">
                        <c:out value="${fn:toUpperCase(fn:substring(shop.name, 0, 1))}"/>
                    </div>
                </c:otherwise>
            </c:choose>
            <div class="seller-shop-info">
                <strong><c:out value="${shop.name}"/></strong>
                <span>Kênh người bán</span>
            </div>
        </div>
    </c:if>

    <nav class="seller-nav">

        <a href="${ctx}/pages/seller/seller-dashboard"
           class="${sellerTab eq 'dashboard' ? 'active' : ''}">
            <i class="bi bi-pie-chart-fill"></i>
            <span>Tổng quan Doanh thu</span>
        </a>

        <a href="${ctx}/pages/seller/seller-products"
           class="${sellerTab eq 'products' ? 'active' : ''}">
            <i class="bi bi-box-seam-fill"></i>
            <span>Quản lý Sản phẩm</span>
        </a>

        <a href="${ctx}/pages/seller/chat-seller"
           class="${sellerTab eq 'chat' ? 'active' : ''}">
            <i class="bi bi-chat-dots-fill"></i>
            <span>Tin nhắn</span>
            <c:if test="${not empty unreadMessages and unreadMessages > 0}">
                <span class="noti-count"><c:out value="${unreadMessages}"/></span>
            </c:if>
        </a>

        <a href="${ctx}/pages/seller/order-management"
           class="${sellerTab eq 'orders' ? 'active' : ''}">
            <i class="bi bi-card-list"></i>
            <span>Quản lý Đơn hàng</span>
        </a>

        <a href="${ctx}/pages/seller/seller-custom"
           class="${sellerTab eq 'custom' ? 'active' : ''}">
            <i class="bi bi-shop"></i>
            <span>Chợ Custom &amp; Báo giá</span>
        </a>

        <a href="${ctx}/pages/seller/seller-settings"
           class="${sellerTab eq 'settings' ? 'active' : ''}">
            <i class="bi bi-gear-fill"></i>
            <span>Cài đặt</span>
        </a>

        <div class="seller-nav-divider"></div>

        <a href="${ctx}/pages/seller/seller-settings?action=logout" class="logout">
            <i class="bi bi-box-arrow-right"></i>
            <span>Đăng xuất</span>
        </a>

    </nav>

</aside>
