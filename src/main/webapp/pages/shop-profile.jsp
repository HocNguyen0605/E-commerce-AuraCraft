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

    <div class="shop-profile-content">
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
                <span><i class="fa-solid fa-star" aria-hidden="true"></i> <fmt:formatNumber value="${shopRating}" minFractionDigits="1" maxFractionDigits="1"/> / 5 · ${shopReviewCount} đánh giá</span>
                <span><i class="fa-solid fa-box" aria-hidden="true"></i> ${totalProducts} sản phẩm</span>
                <span><i class="fa-solid fa-wand-magic-sparkles" aria-hidden="true"></i> ${successfulCustomOrders} đơn custom thành công</span>
            </div>
            <c:if test="${not empty shop.address}"><p class="shop-location"><i class="fa-solid fa-location-dot" aria-hidden="true"></i> <c:out value="${shop.address}"/></p></c:if>
            <div class="shop-profile-actions" id="shop-actions">
                <c:choose>
                    <c:when test="${isBuyer}">
                        <form method="post" action="${pageContext.request.contextPath}/shop-follow">
                            <input type="hidden" name="shopId" value="${shop.id}">
                            <input type="hidden" name="action" value="${shopFollowing ? 'unfollow' : 'follow'}">
                            <button class="shop-action-button shop-follow-button ${shopFollowing ? 'is-following' : ''}" type="submit">
                                <i class="${shopFollowing ? 'fa-solid fa-heart' : 'fa-regular fa-heart'}" aria-hidden="true"></i>
                                ${shopFollowing ? 'Đang theo dõi' : 'Theo dõi xưởng'}
                            </button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <a class="shop-action-button shop-follow-button" href="${pageContext.request.contextPath}/pages/login">
                            <i class="fa-regular fa-heart" aria-hidden="true"></i> Theo dõi xưởng
                        </a>
                    </c:otherwise>
                </c:choose>
                <span class="shop-follower-count"><i class="fa-solid fa-users" aria-hidden="true"></i> ${shopFollowerCount} người theo dõi</span>
                <a class="shop-action-button shop-chat-button" href="${pageContext.request.contextPath}/pages/chat-buyer?shopId=${shop.id}">
                    <i class="fa-regular fa-comment-dots" aria-hidden="true"></i> Chat với thợ
                </a>
            </div>
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

    <section class="shop-reviews-section" id="shop-reviews">
        <div class="shop-products-heading">
            <div>
                <h2>Đánh giá của khách</h2>
                <p><i class="fa-solid fa-star" aria-hidden="true"></i> <fmt:formatNumber value="${shopRating}" minFractionDigits="1" maxFractionDigits="1"/> / 5 từ ${shopReviewCount} đánh giá sản phẩm</p>
            </div>
        </div>
        <c:choose>
            <c:when test="${not empty shopReviews}">
                <div class="shop-review-list">
                    <c:forEach var="review" items="${shopReviews}">
                        <article class="shop-review-card">
                            <div class="shop-review-head">
                                <strong><c:out value="${review.reviewerName}"/></strong>
                                <span class="shop-review-stars" aria-label="${review.rating} trên 5 sao">
                                    <c:forEach begin="1" end="${review.rating}"><i class="fa-solid fa-star" aria-hidden="true"></i></c:forEach>
                                    <span>${review.rating}/5</span>
                                </span>
                            </div>
                            <p class="shop-review-product">Sản phẩm: <c:out value="${review.productName}"/></p>
                            <p class="shop-review-comment"><c:choose><c:when test="${not empty review.comment}"><c:out value="${review.comment}"/></c:when><c:otherwise>Khách hàng chưa để lại nhận xét.</c:otherwise></c:choose></p>
                            <time datetime="<fmt:formatDate value='${review.date}' pattern='yyyy-MM-dd'/>" class="shop-review-date"><fmt:formatDate value="${review.date}" pattern="dd/MM/yyyy"/></time>
                        </article>
                    </c:forEach>
                </div>
            </c:when>
            <c:otherwise><div class="shop-empty-reviews"><i class="fa-regular fa-star" aria-hidden="true"></i><p>Shop chưa có đánh giá từ khách hàng.</p></div></c:otherwise>
        </c:choose>
    </section>
    </div>
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
