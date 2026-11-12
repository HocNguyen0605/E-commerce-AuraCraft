<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    if (request.getAttribute("productListingReady") == null) {
        String target = request.getContextPath() + "/pages/products";
        if (request.getQueryString() != null && !request.getQueryString().isBlank())
            target += "?" + request.getQueryString();
        response.sendRedirect(response.encodeRedirectURL(target));
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sản phẩm - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleProducts.css">
</head>
<body>
<div id="header-placeholder"></div>


<main class="products-page" data-context="${pageContext.request.contextPath}"
      data-database-cart="${sessionScope.role eq 'buyer' and not empty sessionScope.userId}">
    <div class="container products-layout">
        <form class="sidebar-filter" method="get" action="${pageContext.request.contextPath}/pages/products">
            <input type="hidden" name="categoryFilter" value="1">
            <input type="hidden" name="sort" value="${sort}">
            <c:if test="${not empty shopId}"><input type="hidden" name="shopId" value="${shopId}"></c:if>
            <c:if test="${not empty keyword}"><input type="hidden" name="q" value="<c:out value='${keyword}'/>"/></c:if>
            <div class="filter-group">
                <h2 class="filter-title">Danh mục</h2>
                <ul class="filter-list">
                    <li>
                        <label>
                            <input type="checkbox" id="select-all-categories"
                                   <c:if test="${allCategoriesSelected}">checked</c:if>>
                            Chọn tất cả
                        </label>
                    </li>
                    <c:forEach var="category" items="${categories}">
                        <li>
                            <label>
                                <input type="checkbox" name="category" value="${category.id}"
                                       <c:if test="${category.selected}">checked</c:if>>
                                <c:out value="${category.name}"/>
                                <small class="filter-category-count">(<c:out value="${category.productCount}"/>)</small>
                            </label>
                        </li>
                    </c:forEach>
                </ul>
            </div>

            <div class="filter-group">
                <h2 class="filter-title">Mức giá</h2>
                <ul class="filter-list">
                    <li><label><input type="checkbox" name="price" value="under-100000"
                                      <c:if test="${priceUnder100000Selected}">checked</c:if>> Dưới 100.000đ</label>
                    </li>
                    <li><label><input type="checkbox" name="price" value="100000-300000"
                                      <c:if test="${price100000To300000Selected}">checked</c:if>> 100.000đ -
                        300.000đ</label></li>
                    <li><label><input type="checkbox" name="price" value="300000-500000"
                                      <c:if test="${price300000To500000Selected}">checked</c:if>> 300.000đ -
                        500.000đ</label></li>
                    <li><label><input type="checkbox" name="price" value="over-500000"
                                      <c:if test="${priceOver500000Selected}">checked</c:if>> 500.000đ trở lên</label>
                    </li>
                </ul>
            </div>

            <div class="filter-group">
                <h2 class="filter-title">Đánh giá</h2>
                <ul class="filter-list">
                    <li><label><input type="checkbox" name="rating" value="5"
                                      <c:if test="${rating5Selected}">checked</c:if>> 5 sao</label></li>
                    <li><label><input type="checkbox" name="rating" value="4"
                                      <c:if test="${rating4Selected}">checked</c:if>> 4 sao</label></li>
                    <li><label><input type="checkbox" name="rating" value="3"
                                      <c:if test="${rating3Selected}">checked</c:if>> 3 sao</label></li>
                    <li><label><input type="checkbox" name="rating" value="2"
                                      <c:if test="${rating2Selected}">checked</c:if>> 2 sao</label></li>
                    <li><label><input type="checkbox" name="rating" value="1"
                                      <c:if test="${rating1Selected}">checked</c:if>> 1 sao</label></li>
                    <li><label><input type="checkbox" name="rating" value="0"
                                      <c:if test="${rating0Selected}">checked</c:if>> Chưa có đánh giá (0 sao)</label></li>
                </ul>
            </div>
            <button class="btn-filter" type="submit">Áp dụng lọc</button>
        </form>

        <section class="products-main" aria-labelledby="products-title">
            <div class="products-header">
                <h1 id="products-title">Tất cả sản phẩm</h1>
                <form class="products-search" method="get" action="${pageContext.request.contextPath}/pages/products">
                    <input type="hidden" name="sort" value="${sort}">
                    <c:if test="${not empty shopId}"><input type="hidden" name="shopId" value="${shopId}"></c:if>
                    <c:forEach var="categoryId" items="${selectedCategories}">
                        <input type="hidden" name="category" value="${categoryId}">
                    </c:forEach>
                    <input type="hidden" name="categoryFilter" value="1">
                    <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price"
                                                                                 value="${priceValue}"></c:forEach>
                    <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating"
                                                                                        value="${ratingValue}"></c:forEach>
                    <input type="search" name="q" value="<c:out value='${keyword}'/>"
                           placeholder="Tìm sản phẩm, gian hàng..." aria-label="Tìm sản phẩm hoặc gian hàng">
                    <button type="submit" aria-label="Tìm kiếm"><i class="fa-solid fa-magnifying-glass"
                                                                   aria-hidden="true"></i></button>
                </form>
                <form class="sort-box" method="get" action="${pageContext.request.contextPath}/pages/products">
                    <c:if test="${not empty shopId}"><input type="hidden" name="shopId" value="${shopId}"></c:if>
                    <c:forEach var="categoryId" items="${selectedCategories}">
                        <input type="hidden" name="category" value="${categoryId}">
                    </c:forEach>
                    <input type="hidden" name="categoryFilter" value="1">
                    <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price"
                                                                                 value="${priceValue}"></c:forEach>
                    <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating"
                                                                                        value="${ratingValue}"></c:forEach>
                    <c:if test="${not empty keyword}"><input type="hidden" name="q" value="<c:out value='${keyword}'/>"></c:if>
                    <label class="sr-only" for="sort-select">Sắp xếp sản phẩm</label>
                    <select id="sort-select" name="sort" onchange="this.form.submit()">
                        <option value="default" <c:if test="${sort eq 'default'}">selected</c:if>>Bán chạy</option>
                        <option value="newest" <c:if test="${sort eq 'newest'}">selected</c:if>>Mới nhất</option>
                        <option value="price-asc" <c:if test="${sort eq 'price-asc'}">selected</c:if>>Giá từ thấp đến
                            cao
                        </option>
                        <option value="price-desc" <c:if test="${sort eq 'price-desc'}">selected</c:if>>Giá từ cao đến
                            thấp
                        </option>
                        <option value="rating-desc" <c:if test="${sort eq 'rating-desc'}">selected</c:if>>Đánh giá cao
                            nhất
                        </option>
                    </select>
                </form>
            </div>

            <!-- Shop Card khi tìm thấy gian hàng -->
            <c:if test="${not empty matchedShop}">
                <div class="shop-result-card">
                    <a href="${pageContext.request.contextPath}/pages/shop-profile?id=${matchedShop.id}"
                       class="shop-card-avatar" style="text-decoration: none;">
                        <c:choose>
                            <c:when test="${not empty matchedShop.avatar and not empty fn:trim(matchedShop.avatar)}">
                                <img src="${pageContext.request.contextPath}/assets/img/${matchedShop.avatar}"
                                     alt="<c:out value='${matchedShop.name}'/>"
                                     onerror="this.onerror=null; this.parentElement.textContent='${fn:toUpperCase(fn:substring(matchedShop.name, 0, 1))}';">
                            </c:when>
                            <c:otherwise>
                                ${fn:toUpperCase(fn:substring(matchedShop.name, 0, 1))}
                            </c:otherwise>
                        </c:choose>
                    </a>
                    <div class="shop-card-main-info">
                        <a href="${pageContext.request.contextPath}/pages/shop-profile?id=${matchedShop.id}"
                           style="text-decoration: none; color: inherit;">
                            <h2 class="shop-card-name">
                                <c:out value="${matchedShop.name}" />
                            </h2>
                        </a>
                        <div class="shop-card-stats-row">
                            <span><i class="fa-solid fa-star" style="color: #f59e0b;"></i>
                                <fmt:formatNumber value="${matchedShop.rating}" maxFractionDigits="1" /> / 5.0
                            </span>
                            <span class="stat-divider">|</span>
                            <span><i class="fa-solid fa-box"></i> ${matchedShop.productCount} sản phẩm</span>
                            <span class="stat-divider">|</span>
                            <span><i class="fa-solid fa-bag-shopping"></i> ${matchedShop.totalSold} đã bán</span>
                        </div>
                        <div class="shop-card-btn-wrap">
                            <a href="${pageContext.request.contextPath}/pages/shop-profile?id=${matchedShop.id}"
                               class="btn-visit-shop">
                                <i class="fa-solid fa-store"></i> Xem Shop
                            </a>
                        </div>
                    </div>
                </div>
            </c:if>

            <div class="products-result-bar" aria-live="polite">
                <span>Hiển thị <strong>${firstProduct}–${lastProduct}</strong> / ${totalProducts} sản phẩm</span>
            </div>

            <c:if test="${not databaseAvailable}">
                <div class="products-empty-state" role="alert">Không thể kết nối cơ sở dữ liệu để tải sản phẩm. Vui lòng
                    kiểm tra cấu hình database và thử tải lại.
                </div>
            </c:if>
            <div class="product-grid">
                <c:choose>
                    <c:when test="${empty products and databaseAvailable}">
                        <p class="products-empty-state">Không tìm thấy sản phẩm phù hợp. Hãy thử thay đổi từ khóa hoặc
                            bộ lọc.</p>
                    </c:when>

                    <c:otherwise>
                        <c:forEach var="product" items="${products}">
                            <c:set var="fallbackImage"
                                   value="${pageContext.request.contextPath}/assets/img/products/bracelet_01.jpg"/>
                            <c:if test="${product.categoryId eq 2}"><c:set var="fallbackImage"
                                                                           value="${pageContext.request.contextPath}/assets/img/products/necklace_02.jpg"/></c:if>
                            <c:if test="${product.categoryId eq 3}"><c:set var="fallbackImage"
                                                                           value="${pageContext.request.contextPath}/assets/img/products/phoneStrap_01.jpg"/></c:if>
                            <article class="product-card has-image">
                                <a class="product-card-link"
                                   href="${pageContext.request.contextPath}/pages/product-detail?id=${product.id}"
                                   aria-label="Xem chi tiết ${fn:escapeXml(product.name)}">
                                    <c:choose>
                                        <c:when test="${not empty product.image and fn:startsWith(product.image, 'http')}">
                                            <img src="<c:out value='${product.image}'/>"
                                                 data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>"
                                                 loading="lazy">
                                        </c:when>
                                        <c:when test="${not empty product.image}">
                                            <img src="${pageContext.request.contextPath}/assets/img/<c:out value='${product.image}'/>"
                                                 data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>"
                                                 loading="lazy">
                                        </c:when>
                                        <c:otherwise>
                                            <img src="${fallbackImage}" alt="<c:out value='${product.name}'/>"
                                                 loading="lazy">
                                        </c:otherwise>
                                    </c:choose>
                                    <div class="product-info">
                                        <span class="product-category"><c:out value="${product.categoryName}"/></span>
                                        <h2 title="<c:out value='${product.name}'/>"><c:out
                                                value="${product.name}"/></h2>
                                        <p class="product-shop"><i class="fa-solid fa-store" aria-hidden="true"></i>
                                            <c:out value="${product.shopName}"/></p>
                                        <div class="product-rating"
                                             aria-label="Đánh giá <fmt:formatNumber value='${product.rating}' maxFractionDigits='1'/> trên 5">
                                            <i class="fa-solid fa-star" aria-hidden="true"></i>
                                            <span class="rating-score"><fmt:formatNumber value="${product.rating}"
                                                                                         minFractionDigits="1"
                                                                                         maxFractionDigits="1"/></span>
                                            <span class="review-count">(<c:out value="${product.reviewCount}"/>)</span>
                                        </div>
                                        <p class="price"><fmt:formatNumber value="${product.price}" type="number"
                                                                           groupingUsed="true"/>đ</p>
                                        <p class="product-sold">Đã bán <c:out value="${product.soldCount}"/></p>
                                    </div>
                                </a>
                                <button class="product-add-cart-link" type="button"
                                        data-product-id="${product.id}"
                                        data-product-name="<c:out value='${product.name}'/>"
                                        data-product-price="${product.price}"
                                        data-product-stock="${product.stock}"
                                        data-product-shop="<c:out value='${product.shopName}'/>"
                                        data-product-category="<c:out value='${product.categoryName}'/>"
                                        <c:if test="${product.stock le 0}">disabled</c:if>>
                                    <c:choose><c:when test="${product.stock le 0}">Hết hàng</c:when><c:otherwise>Thêm giỏ hàng</c:otherwise></c:choose>
                                </button>
                            </article>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
            </div>
            <c:if test="${totalPages gt 1}">
                <form class="products-pagination-form" method="get"
                      action="${pageContext.request.contextPath}/pages/products">
                    <input type="hidden" name="categoryFilter" value="1">
                    <input type="hidden" name="sort" value="${sort}">
                    <c:if test="${not empty shopId}"><input type="hidden" name="shopId" value="${shopId}"></c:if>
                    <c:if test="${not empty keyword}"><input type="hidden" name="q"
                                                             value="<c:out value='${keyword}'/>"/></c:if>
                    <c:forEach var="categoryId" items="${selectedCategories}"><input type="hidden" name="category"
                                                                                     value="${categoryId}"></c:forEach>
                    <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price"
                                                                                 value="${priceValue}"></c:forEach>
                    <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating"
                                                                                        value="${ratingValue}"></c:forEach>
                    <nav class="products-pagination" aria-label="Phân trang sản phẩm">
                        <button type="submit" name="page" value="${page - 1}" aria-label="Trang trước"
                                <c:if test="${page le 1}">disabled</c:if>>‹
                        </button>
                        <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                            <button type="submit" name="page" value="${pageNumber}"
                                    class="<c:if test='${pageNumber eq page}'>is-current</c:if>"
                                    <c:if test="${pageNumber eq page}">aria-current="page"</c:if>>${pageNumber}</button>
                        </c:forEach>
                        <button type="submit" name="page" value="${page + 1}" aria-label="Trang sau"
                                <c:if test="${page ge totalPages}">disabled</c:if>>›
                        </button>
                    </nav>
                </form>
            </c:if>
        </section>
    </div>
</main>

<div id="footer-placeholder"></div>
<script>
    <%--const appContextPath = '${pageContext.request.contextPath}';--%>
    <%--fetch(appContextPath + '/components/header.html')--%>
    <%--    .then(response => response.text())--%>
    <%--    .then(html => {--%>
    <%--        html = html.replace(/href="index\.html"/g, 'href="' + appContextPath + '/index.html"');--%>
    <%--        html = html.replace(/href="login\.html"/g, 'href="' + appContextPath + '/pages/login.html"').replace(/href="profile\.html"/g, 'href="' + appContextPath + '/profile"').replace(/href="cart\.jsp"/g, 'href="' + appContextPath + '/pages/cart.jsp"');--%>
    <%--        html = html.replace(/href="pages\/products"/g, 'href="' + appContextPath + '/pages/products"').replace(/href="\.\.\/products"/g, 'href="' + appContextPath + '/pages/products"');--%>
    <%--        document.getElementById('header-placeholder').innerHTML = html;--%>
    <%--    })--%>
    <%--    .catch(error => console.error('Không thể tải header:', error));--%>
    <%--fetch(appContextPath + '/components/footer.html')--%>
    <%--    .then(response => response.text())--%>
    <%--    .then(html => {--%>
    <%--        html = html.replace(/href="index\.html"/g, 'href="' + appContextPath + '/index.html"');--%>
    <%--        html = html.replace(/href="pages\/products"/g, 'href="' + appContextPath + '/pages/products"').replace(/href="\.\.\/products"/g, 'href="' + appContextPath + '/pages/products"');--%>
    <%--        document.getElementById('footer-placeholder').innerHTML = html;--%>
    <%--    })--%>
    <%--    .catch(error => console.error('Không thể tải footer:', error));--%>
    <%--document.querySelectorAll('img[data-fallback]').forEach(image => {--%>
    <%--    image.addEventListener('error', () => {--%>
    <%--        image.src = image.dataset.fallback;--%>
    <%--        image.removeAttribute('data-fallback');--%>
    <%--    }, {once: true});--%>
    <%--});--%>
    const selectAllCategories = document.getElementById('select-all-categories');
    const productsPage = document.querySelector('.products-page');
    const productsContext = productsPage.dataset.context;
    const databaseCart = productsPage.dataset.databaseCart === 'true';

    document.querySelectorAll('.product-add-cart-link').forEach(button => {
        button.addEventListener('click', async () => {
            if (button.disabled) return;
            button.disabled = true;
            try {
                const productId = button.dataset.productId;
                if (databaseCart) {
                    const response = await fetch(productsContext + '/api/cart', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                        body: new URLSearchParams({ action: 'add', productId, quantity: '1' })
                    });
                    const result = await response.json();
                    if (!response.ok) throw new Error(result.message || 'Không thể thêm sản phẩm vào giỏ.');
                } else {
                    const cart = JSON.parse(localStorage.getItem('AuraCraftCart') || '[]');
                    const existing = cart.find(item => String(item.id) === productId);
                    if (existing) {
                        existing.quantity = Math.min(Number(button.dataset.productStock || 99), Number(existing.quantity || 1) + 1);
                        existing.selected = true;
                    } else {
                        const image = button.closest('.product-card').querySelector('.product-card-link img');
                        cart.push({
                            id: Number(productId), name: button.dataset.productName,
                            price: Number(button.dataset.productPrice), quantity: 1,
                            stock: Number(button.dataset.productStock || 99), image: image?.src || '',
                            shopName: button.dataset.productShop, categoryName: button.dataset.productCategory,
                            selected: true
                        });
                    }
                    localStorage.setItem('AuraCraftCart', JSON.stringify(cart));
                }
                window.dispatchEvent(new Event('auracraft:cart-updated'));
                button.textContent = 'Đã thêm vào giỏ';
                window.setTimeout(() => { button.textContent = 'Thêm giỏ hàng'; button.disabled = false; }, 1400);
            } catch (error) {
                window.alert(error.message || 'Không thể thêm sản phẩm vào giỏ.');
                button.disabled = false;
            }
        });
    });

    const categoryCheckboxes = Array.from(document.querySelectorAll('.sidebar-filter input[name="category"]'));
    const submittedFilters = new URLSearchParams(window.location.search);
    if (submittedFilters.has('categoryFilter')) {
        const submittedCategories = submittedFilters.getAll('category');
        const submittedPrices = submittedFilters.getAll('price');
        const submittedRatings = submittedFilters.getAll('rating');
        categoryCheckboxes.forEach(checkbox => {
            checkbox.checked = submittedCategories.includes(checkbox.value);
        });
        document.querySelectorAll('.sidebar-filter input[name="price"]').forEach(checkbox => {
            checkbox.checked = submittedPrices.includes(checkbox.value);
        });
        document.querySelectorAll('.sidebar-filter input[name="rating"]').forEach(checkbox => {
            checkbox.checked = submittedRatings.includes(checkbox.value);
        });
    }
    const syncSelectAllCategories = () => {
        const selectedCount = categoryCheckboxes.filter(checkbox => checkbox.checked).length;
        selectAllCategories.checked = categoryCheckboxes.length > 0 && selectedCount === categoryCheckboxes.length;
        selectAllCategories.indeterminate = selectedCount > 0 && selectedCount < categoryCheckboxes.length;
    };
    selectAllCategories.addEventListener('change', () => {
        categoryCheckboxes.forEach(checkbox => {
            checkbox.checked = selectAllCategories.checked;
        });
        syncSelectAllCategories();
    });
    categoryCheckboxes.forEach(checkbox => checkbox.addEventListener('change', syncSelectAllCategories));
    syncSelectAllCategories();
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
