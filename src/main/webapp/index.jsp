<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.ProductDAO,java.util.List,java.util.Map,java.sql.SQLException" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    List<Map<String, Object>> featuredProducts = List.of();
    boolean featuredProductsAvailable = true;
    try {
        featuredProducts = new ProductDAO().findActiveProducts(
                null, null, null, null, "best", 6, 0);
    } catch (SQLException | RuntimeException exception) {
        application.log("Unable to load homepage featured products", exception);
        featuredProductsAvailable = false;
    }
    request.setAttribute("featuredProducts", featuredProducts);
    request.setAttribute("featuredProductsAvailable", featuredProductsAvailable);
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Khám phá trang sức handmade độc bản, mua sản phẩm thủ công hoặc tự thiết kế món đồ mang dấu ấn riêng tại AuraCraft.">
    <title>AuraCraft - Khơi nguồn sáng tạo</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleIndex.css">
</head>
<body>
<div id="header-placeholder"></div>

<main>
    <section class="hero-section">
        <div class="hero-overlay"></div>
        <div class="hero-content">
            <h1 class="hero-title">Khơi nguồn sáng tạo<br>với đồ thủ công độc bản</h1>
            <p class="hero-subtitle">Nền tảng thương mại điện tử kết nối người mua và thợ thủ công.<br>Khám phá hàng ngàn sản phẩm handmade hoặc tự tay thiết kế món đồ mang đậm dấu ấn cá nhân.</p>
            <div class="hero-buttons">
                <a href="${pageContext.request.contextPath}/pages/products" class="btn btn-primary">Khám phá sản phẩm</a>
                <a href="${pageContext.request.contextPath}/pages/buyer-request-form.jsp" class="btn btn-outline">Gửi yêu cầu thiết kế</a>
            </div>
        </div>
    </section>

    <section class="how-it-works">
        <div class="container">
            <h2 class="section-title">Cách thức hoạt động</h2>
            <div class="steps-grid">
                <div class="step-card">
                    <div class="step-icon"><i class="fa-solid fa-wand-magic-sparkles"></i></div>
                    <h3>1. Chọn & Sáng tạo</h3>
                    <p>Mua nguyên bản, tùy chỉnh từ mẫu có sẵn hoặc yêu cầu thợ làm một thiết kế hoàn toàn mới của riêng bạn.</p>
                </div>
                <div class="step-card">
                    <div class="step-icon"><i class="fa-solid fa-file-invoice-dollar"></i></div>
                    <h3>2. Tính giá & Báo giá</h3>
                    <p>Giá tính tự động cho sản phẩm nguyên bản hoặc custom mẫu có sẵn. Riêng thiết kế mới hoàn toàn sẽ nhận báo giá từ thợ.</p>
                </div>
                <div class="step-card">
                    <div class="step-icon"><i class="fa-regular fa-comments"></i></div>
                    <h3>3. Đặt hàng & Chốt đơn</h3>
                    <p>Đặt hàng trực tiếp với sản phẩm có sẵn/custom từ mẫu, hoặc chat với thợ để chốt đơn thiết kế mới hoàn toàn.</p>
                </div>
                <div class="step-card">
                    <div class="step-icon"><i class="fa-solid fa-box-open"></i></div>
                    <h3>4. Nhận Hàng</h3>
                    <p>Theo dõi tiến độ chế tác và nhận món đồ thủ công mang đậm dấu ấn cá nhân của riêng bạn.</p>
                </div>
            </div>
        </div>
    </section>

    <section id="custom" class="custom-highlight">
        <div class="container custom-container">
            <div class="custom-text">
                <h2>Tự tay thiết kế trang sức độc bản</h2>
                <p>Hệ thống Custom của AuraCraft cho phép bạn thỏa sức sáng tạo:</p>
                <ul>
                    <li><i class="fa-solid fa-check"></i> Tùy chọn đa dạng: Vòng tay, Dây chuyền, Lắc chân,...</li>
                    <li><i class="fa-solid fa-check"></i> Thư viện Charm phong phú: Charm hoa, Charm tim, Hạt chữ,...</li>
                    <li><i class="fa-solid fa-check"></i> Tùy chỉnh vị trí và số lượng theo ý thích</li>
                    <li><i class="fa-solid fa-check"></i> Tính giá cơ sở tự động, minh bạch</li>
                </ul>
                <a href="${pageContext.request.contextPath}/pages/customizer.jsp" class="btn btn-primary mt-4">Bắt đầu thiết kế ngay</a>
            </div>
            <div class="custom-image">
                <img src="https://images.unsplash.com/photo-1611591437281-460bfbe1220a?q=80&w=800&auto=format&fit=crop" alt="Thiết kế trang sức Custom">
            </div>
        </div>
    </section>

    <section id="featured" class="featured-products">
        <div class="container">
            <div class="home-section-heading">
                <div>
                    <h2 class="section-title">Sản phẩm nổi bật</h2>
                    <p>Sản phẩm có lượt bán cao nhất trên AuraCraft</p>
                </div>
                <a class="btn btn-outline" href="${pageContext.request.contextPath}/pages/products">Xem tất cả sản phẩm</a>
            </div>
            <div class="product-grid">
                <c:choose>
                    <c:when test="${not empty featuredProducts}">
                        <c:forEach var="product" items="${featuredProducts}">
                            <c:url var="detailUrl" value="/pages/product-detail">
                                <c:param name="id" value="${product.id}"/>
                            </c:url>
                            <c:set var="fallbackImage" value="https://images.unsplash.com/photo-1611591437281-460bfbe1220a?q=80&amp;w=900&amp;auto=format&amp;fit=crop"/>
                            <article class="product-card has-image home-product-card">
                                <a class="home-product-link" href="${detailUrl}">
                                    <c:choose>
                                        <c:when test="${not empty product.image and fn:startsWith(product.image, 'http')}">
                                            <img src="<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                        </c:when>
                                        <c:when test="${not empty product.image and (fn:startsWith(product.image, 'assets/') or fn:startsWith(product.image, 'uploads/'))}">
                                            <img src="${pageContext.request.contextPath}/<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                        </c:when>
                                        <c:when test="${not empty product.image and fn:startsWith(product.image, '/')}">
                                            <img src="${pageContext.request.contextPath}<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                        </c:when>
                                        <c:when test="${not empty product.image}">
                                            <img src="${pageContext.request.contextPath}/assets/img/<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="product-info">
                                        <span class="home-product-category"><c:out value="${product.categoryName}"/></span>
                                        <h3 title="<c:out value='${product.name}'/>"><c:out value="${product.name}"/></h3>
                                        <p class="product-shop"><i class="fa-solid fa-store" aria-hidden="true"></i> <c:out value="${product.shopName}"/></p>
                                        <div class="product-rating" aria-label="Đánh giá <fmt:formatNumber value='${product.rating}' maxFractionDigits='1'/> trên 5">
                                            <i class="fa-solid fa-star" aria-hidden="true"></i>
                                            <span class="rating-score"><fmt:formatNumber value="${product.rating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                                            <span class="home-review-count">(<c:out value="${product.reviewCount}"/> đánh giá)</span>
                                        </div>
                                        <p class="price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</p>
                                        <p class="home-product-sold"><i class="fa-solid fa-fire" aria-hidden="true"></i> Đã bán <fmt:formatNumber value="${product.soldCount}" type="number" groupingUsed="true"/></p>
                                    </div>
                                </a>
                            </article>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="home-products-empty">
                            <i class="fa-solid fa-box-open" aria-hidden="true"></i>
                            <p><c:choose><c:when test="${featuredProductsAvailable}">Chưa có sản phẩm nổi bật. Hãy quay lại sau nhé.</c:when><c:otherwise>Chưa tải được sản phẩm lúc này. Bạn có thể xem toàn bộ sản phẩm tại đây.</c:otherwise></c:choose></p>
                            <a class="btn btn-primary" href="${pageContext.request.contextPath}/pages/products">Đến trang sản phẩm</a>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
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

    fetch('./components/header.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('header-placeholder').innerHTML = data;
        })
        .catch(error => console.error('Error loading header:', error));

    fetch('./components/footer.html')
        .then(response => response.text())
        .then(data => {
            document.getElementById('footer-placeholder').innerHTML = data;
        })
        .catch(error => console.error('Error loading footer:', error));
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
</body>
</html>
