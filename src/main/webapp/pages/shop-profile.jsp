<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    if (request.getAttribute("shop") == null) {
        String target = request.getContextPath() + "/pages/shop-profile";
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) target += "?" + request.getQueryString();
        response.sendRedirect(response.encodeRedirectURL(target));
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><c:out value="${shop.name}"/> · AuraCraft</title>
    <meta name="description" content="Khám phá sản phẩm thủ công từ <c:out value='${shop.name}'/> trên AuraCraft.">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleShopProfile.css">
</head>
<body>
<div id="header-placeholder"></div>
<main class="container shop-profile-page">
    <nav class="shop-breadcrumb" aria-label="Điều hướng">
        <a href="${pageContext.request.contextPath}/pages/home">Trang chủ</a><span>›</span>
        <a href="${pageContext.request.contextPath}/pages/products">Sản phẩm</a><span>›</span>
        <span aria-current="page"><c:out value="${shop.name}"/></span>
    </nav>

    <section class="shop-profile-hero">
        <div class="shop-avatar">
            <c:choose>
                <c:when test="${not empty shop.avatar and fn:startsWith(shop.avatar, 'http')}">
                    <img src="<c:out value='${shop.avatar}'/>" alt="Ảnh đại diện <c:out value='${shop.name}'/>">
                </c:when>
                <c:when test="${not empty shop.avatar}">
                    <img src="${pageContext.request.contextPath}/<c:out value='${shop.avatar}'/>" alt="Ảnh đại diện <c:out value='${shop.name}'/>">
                </c:when>
                <c:otherwise><span><c:out value="${shop.name.substring(0,1)}"/></span></c:otherwise>
            </c:choose>
        </div>
        <div class="shop-profile-info">
            <p class="shop-eyebrow"><i class="fa-solid fa-store" aria-hidden="true"></i> Gian hàng AuraCraft</p>
            <h1><c:out value="${shop.name}"/></h1>
            <div class="shop-profile-stats">
                <span><i class="fa-solid fa-star" aria-hidden="true"></i> <fmt:formatNumber value="${shop.rating}" minFractionDigits="1" maxFractionDigits="1"/> / 5</span>
                <span><i class="fa-solid fa-box" aria-hidden="true"></i> ${totalProducts} sản phẩm</span>
            </div>
            <c:if test="${not empty shop.address}"><p class="shop-location"><i class="fa-solid fa-location-dot" aria-hidden="true"></i> <c:out value="${shop.address}"/></p></c:if>
        </div>
    </section>

    <section class="shop-about">
        <h2>Giới thiệu gian hàng</h2>
        <p><c:choose><c:when test="${not empty shop.description}"><c:out value="${shop.description}"/></c:when><c:otherwise>Gian hàng đang cập nhật phần giới thiệu.</c:otherwise></c:choose></p>
    </section>

    <section class="shop-products-section">
        <div class="shop-products-heading">
            <div><h2>Sản phẩm của gian hàng</h2><p>Các sản phẩm đang được bày bán</p></div>
        </div>
        <c:choose>
            <c:when test="${not empty products}">
                <div class="shop-product-grid">
                    <c:forEach var="product" items="${products}">
                        <c:url var="productUrl" value="/pages/product-detail"><c:param name="id" value="${product.id}"/></c:url>
                        <c:set var="fallbackImage" value="https://images.unsplash.com/photo-1611591437281-460bfbe1220a?q=80&amp;w=900&amp;auto=format&amp;fit=crop"/>
                        <a class="shop-product-card" href="${productUrl}">
                            <c:choose>
                                <c:when test="${not empty product.image and fn:startsWith(product.image, 'http')}"><img src="<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy"></c:when>
                                <c:when test="${not empty product.image and (fn:startsWith(product.image, 'assets/') or fn:startsWith(product.image, 'uploads/'))}"><img src="${pageContext.request.contextPath}/<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy"></c:when>
                                <c:when test="${not empty product.image and fn:startsWith(product.image, '/')}"><img src="${pageContext.request.contextPath}<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy"></c:when>
                                <c:when test="${not empty product.image}"><img src="${pageContext.request.contextPath}/assets/img/<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy"></c:when>
                                <c:otherwise><img src="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy"></c:otherwise>
                            </c:choose>
                            <div class="shop-product-info">
                                <span class="shop-product-category"><c:out value="${product.categoryName}"/></span>
                                <h3 title="<c:out value='${product.name}'/>"><c:out value="${product.name}"/></h3>
                                <div class="shop-product-rating"><i class="fa-solid fa-star" aria-hidden="true"></i> <fmt:formatNumber value="${product.rating}" minFractionDigits="1" maxFractionDigits="1"/> <span>(<c:out value="${product.reviewCount}"/>)</span></div>
                                <p class="shop-product-price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</p>
                                <p class="shop-product-sold">Đã bán <fmt:formatNumber value="${product.soldCount}" type="number" groupingUsed="true"/></p>
                            </div>
                        </a>
                    </c:forEach>
                </div>
                <c:if test="${totalPages gt 1}">
                    <nav class="shop-pagination" aria-label="Phân trang sản phẩm gian hàng">
                        <c:if test="${page gt 1}"><a href="${pageContext.request.contextPath}/pages/shop-profile?id=${shop.id}&amp;page=${page - 1}" aria-label="Trang trước">‹</a></c:if>
                        <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                            <a class="${pageNumber eq page ? 'active' : ''}" href="${pageContext.request.contextPath}/pages/shop-profile?id=${shop.id}&amp;page=${pageNumber}">${pageNumber}</a>
                        </c:forEach>
                        <c:if test="${page lt totalPages}"><a href="${pageContext.request.contextPath}/pages/shop-profile?id=${shop.id}&amp;page=${page + 1}" aria-label="Trang sau">›</a></c:if>
                    </nav>
                </c:if>
            </c:when>
            <c:otherwise><div class="shop-empty-products"><i class="fa-solid fa-box-open" aria-hidden="true"></i><p>Gian hàng chưa có sản phẩm đang bán.</p></div></c:otherwise>
        </c:choose>
    </section>
</main>
<div id="footer-placeholder"></div>
<script>
    document.addEventListener('error', event => {
        const image = event.target;
        if (image instanceof HTMLImageElement && image.dataset.fallback && !image.dataset.fallbackApplied) {
            image.dataset.fallbackApplied = 'true';
            image.src = image.dataset.fallback;
        }
    }, true);
</script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
