<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="dao.ProductDAO, java.util.List, java.util.Map" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%
    List<Map<String, Object>> featuredProducts = (List<Map<String, Object>>) request.getAttribute("featuredProducts");
    if (featuredProducts == null) {
        try {
            featuredProducts = new ProductDAO().getFeaturedProducts();
            request.setAttribute("featuredProducts", featuredProducts);
        } catch (Exception e) {
            featuredProducts = java.util.Collections.emptyList();
            request.setAttribute("featuredProducts", featuredProducts);
        }
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AuraCraft - Khơi nguồn sáng tạo</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="assets/css/style.css">
    <link rel="stylesheet" href="assets/css/header-footer.css">
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
                    <a href="#featured" class="btn btn-primary">Mua sắm ngay</a>
                    <a href="pages/buyer-request-form.jsp" class="btn btn-outline">Tạo Yêu Cầu Custom</a>
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
                    <a href="pages/customizer.jsp" class="btn btn-primary mt-4">Bắt đầu thiết kế ngay</a>
                </div>
                <div class="custom-image">
                    <img src="https://images.unsplash.com/photo-1611591437281-460bfbe1220a?q=80&w=800&auto=format&fit=crop" alt="Thiết kế trang sức Custom">
                </div>
            </div>
        </section>

        <section id="featured" class="featured-products">
            <div class="container">
                <h2 class="section-title">Sản phẩm nổi bật</h2>
                <div class="product-grid">
                    <c:choose>
                        <c:when test="${not empty featuredProducts}">
                            <c:forEach var="product" items="${featuredProducts}">
                                <c:set var="fallbackImg" value="${pageContext.request.contextPath}/assets/img/products/bracelet_01.jpg" />
                                <c:if test="${product.categoryId eq 2}">
                                    <c:set var="fallbackImg" value="${pageContext.request.contextPath}/assets/img/products/necklace_02.jpg" />
                                </c:if>
                                <c:if test="${product.categoryId eq 3}">
                                    <c:set var="fallbackImg" value="${pageContext.request.contextPath}/assets/img/products/phoneStrap_01.jpg" />
                                </c:if>
                                <c:choose>
                                    <c:when test="${not empty product.image and (fn:startsWith(product.image, 'http://') or fn:startsWith(product.image, 'https://'))}">
                                        <c:set var="cardImg" value="${product.image}" />
                                    </c:when>
                                    <c:when test="${not empty product.image}">
                                        <c:set var="cardImg" value="${pageContext.request.contextPath}/assets/img/${product.image}" />
                                    </c:when>
                                    <c:otherwise>
                                        <c:set var="cardImg" value="${fallbackImg}" />
                                    </c:otherwise>
                                </c:choose>
                                <div class="product-card has-image" onclick="window.location.href='${pageContext.request.contextPath}/pages/product-detail?id=${product.id}'" style="cursor: pointer;">
                                    <img src="<c:out value='${cardImg}'/>" onerror="this.onerror=null;this.src='${fallbackImg}';" alt="<c:out value='${product.name}'/>">
                                    <div class="product-info">
                                        <h3><c:out value="${product.name}"/></h3>
                                        <p class="product-shop"><i class="fa-solid fa-store"></i> <c:out value="${product.shopName}"/></p>
                                        <div class="product-rating">
                                            <i class="fa-solid fa-star"></i>
                                            <span class="rating-score">
                                                <fmt:formatNumber value="${product.rating}" minFractionDigits="1" maxFractionDigits="1"/>
                                            </span>
                                            <span class="review-count">(<c:out value="${product.reviewCount}"/>)</span>
                                        </div>
                                        <p class="price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</p>
                                    </div>
                                </div>
                            </c:forEach>
                        </c:when>
                        <c:otherwise>
                            <p style="text-align: center; color: var(--text-muted); width: 100%; grid-column: 1 / -1; padding: 40px 0;">Hiện chưa có sản phẩm nổi bật nào.</p>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </section>
    </main>

    <div id="footer-placeholder"></div>

    <script>
        fetch('./components/header.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="((?!index\.html|http|#)[^"]+)"/g, 'href="pages/$1"');
                document.getElementById('header-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading header:', error));

        fetch('./components/footer.html')
            .then(response => response.text())
            .then(data => {
                let html = data.replace(/href="((?!index\.html|http|#)[^"]+)"/g, 'href="pages/$1"');
                document.getElementById('footer-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Error loading footer:', error));
    </script>
<script src="assets/js/popup.js"></script>
    <script src="assets/js/auth.js"></script>
    <script src="assets/js/chatbot.js"></script>
</body>
</html>
