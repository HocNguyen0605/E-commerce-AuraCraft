<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<aside class="seller-sidebar">
    <div class="seller-profile">
        <c:choose>
            <c:when test="${empty shop.avatar}"><c:set var="shopImg" value="https://i.pravatar.cc/150?img=32"/></c:when>
            <c:when test="${fn:startsWith(shop.avatar, 'http')}"><c:set var="shopImg" value="${shop.avatar}"/></c:when>
            <c:otherwise><c:set var="shopImg" value="${ctx}/${shop.avatar}"/></c:otherwise>
        </c:choose>
        <img src="${shopImg}" alt="<c:out value='${shop.name}'/>" class="profile-img">
        <h3><c:out value="${shop.name}"/></h3>
    </div>
    <nav class="seller-nav">
        <a href="${ctx}/seller/dashboard" class="${sellerTab eq 'dashboard' ? 'active' : ''}"><i class="bi bi-pie-chart-fill"></i> Tổng quan Doanh thu</a>
        <a href="${ctx}/seller/products" class="${sellerTab eq 'products' ? 'active' : ''}"><i class="bi bi-box-seam-fill"></i> Quản lý Sản phẩm</a>
        <a href="${ctx}/seller/chat" class="${sellerTab eq 'chat' ? 'active' : ''}"><i class="bi bi-chat-dots-fill"></i> Tin nhắn
            <c:if test="${not empty unreadMessages and unreadMessages > 0}"><span class="noti-count">${unreadMessages}</span></c:if></a>
        <a href="${ctx}/seller/orders" class="${sellerTab eq 'orders' ? 'active' : ''}"><i class="bi bi-card-list"></i> Quản lý Đơn hàng</a>
        <a href="${ctx}/seller/custom" class="${sellerTab eq 'custom' ? 'active' : ''}"><i class="bi bi-shop"></i> Chợ Custom &amp; Báo giá</a>
        <a href="${ctx}/seller/settings" class="${sellerTab eq 'settings' ? 'active' : ''}"><i class="bi bi-gear-fill"></i> Cài đặt</a>
    </nav>
</aside>