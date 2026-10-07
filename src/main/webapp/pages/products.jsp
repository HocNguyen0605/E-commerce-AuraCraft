<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%
    if (request.getAttribute("databaseAvailable") == null) {
        response.sendRedirect(request.getContextPath() + "/products");
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

    <main class="products-page">
        <div class="container products-layout">
            <form class="sidebar-filter" method="get" action="${pageContext.request.contextPath}/products">
                <input type="hidden" name="categoryFilter" value="1">
                <input type="hidden" name="sort" value="${sort}">
                <c:if test="${not empty keyword}"><input type="hidden" name="q" value="<c:out value='${keyword}'/>" /></c:if>
                <div class="filter-group">
                    <h2 class="filter-title">Danh mục</h2>
                    <ul class="filter-list">
                        <li>
                            <label>
                                <input type="checkbox" id="select-all-categories" <c:if test="${allCategoriesSelected}">checked</c:if>>
                                Chọn tất cả
                            </label>
                        </li>
                        <c:forEach var="category" items="${categories}">
                            <li>
                                <label>
                                    <input type="checkbox" name="category" value="${category.id}"
                                        <c:if test="${category.selected}">checked</c:if>>
                                    <c:out value="${category.name}" />
                                    <small class="filter-category-count">(<c:out value="${category.productCount}"/>)</small>
                                </label>
                            </li>
                        </c:forEach>
                    </ul>
                </div>

                <div class="filter-group">
                    <h2 class="filter-title">Mức giá</h2>
                    <ul class="filter-list">
                        <li><label><input type="checkbox" name="price" value="under-100000" <c:if test="${priceUnder100000Selected}">checked</c:if>> Dưới 100.000đ</label></li>
                        <li><label><input type="checkbox" name="price" value="100000-300000" <c:if test="${price100000To300000Selected}">checked</c:if>> 100.000đ - 300.000đ</label></li>
                        <li><label><input type="checkbox" name="price" value="300000-500000" <c:if test="${price300000To500000Selected}">checked</c:if>> 300.000đ - 500.000đ</label></li>
                        <li><label><input type="checkbox" name="price" value="over-500000" <c:if test="${priceOver500000Selected}">checked</c:if>> 500.000đ trở lên</label></li>
                    </ul>
                </div>

                <div class="filter-group">
                    <h2 class="filter-title">Đánh giá</h2>
                    <ul class="filter-list">
                        <li><label><input type="checkbox" name="rating" value="5" <c:if test="${rating5Selected}">checked</c:if>> 5 sao</label></li>
                        <li><label><input type="checkbox" name="rating" value="4" <c:if test="${rating4Selected}">checked</c:if>> 4 sao</label></li>
                        <li><label><input type="checkbox" name="rating" value="3" <c:if test="${rating3Selected}">checked</c:if>> 3 sao</label></li>
                        <li><label><input type="checkbox" name="rating" value="2" <c:if test="${rating2Selected}">checked</c:if>> 2 sao</label></li>
                        <li><label><input type="checkbox" name="rating" value="1" <c:if test="${rating1Selected}">checked</c:if>> 1 sao</label></li>
                    </ul>
                </div>
                <button class="btn-filter" type="submit">Áp dụng lọc</button>
            </form>

            <section class="products-main" aria-labelledby="products-title">
                <div class="products-header">
                    <h1 id="products-title">Tất cả sản phẩm</h1>
                    <form class="products-search" method="get" action="${pageContext.request.contextPath}/products">
                        <input type="hidden" name="sort" value="${sort}">
                        <c:forEach var="categoryId" items="${selectedCategories}">
                            <input type="hidden" name="category" value="${categoryId}">
                        </c:forEach>
                        <input type="hidden" name="categoryFilter" value="1">
                        <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price" value="${priceValue}"></c:forEach>
                        <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating" value="${ratingValue}"></c:forEach>
                        <input type="search" name="q" value="<c:out value='${keyword}'/>" placeholder="Tìm sản phẩm, cửa hàng..." aria-label="Tìm sản phẩm">
                        <button type="submit" aria-label="Tìm kiếm"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i></button>
                    </form>
                    <form class="sort-box" method="get" action="${pageContext.request.contextPath}/products">
                        <c:forEach var="categoryId" items="${selectedCategories}">
                            <input type="hidden" name="category" value="${categoryId}">
                        </c:forEach>
                        <input type="hidden" name="categoryFilter" value="1">
                        <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price" value="${priceValue}"></c:forEach>
                        <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating" value="${ratingValue}"></c:forEach>
                        <c:if test="${not empty keyword}"><input type="hidden" name="q" value="<c:out value='${keyword}'/>"></c:if>
                        <label class="sr-only" for="sort-select">Sắp xếp sản phẩm</label>
                        <select id="sort-select" name="sort" onchange="this.form.submit()">
                            <option value="default" <c:if test="${sort eq 'default'}">selected</c:if>>Bán chạy</option>
                            <option value="newest" <c:if test="${sort eq 'newest'}">selected</c:if>>Mới nhất</option>
                            <option value="price-asc" <c:if test="${sort eq 'price-asc'}">selected</c:if>>Giá từ thấp đến cao</option>
                            <option value="price-desc" <c:if test="${sort eq 'price-desc'}">selected</c:if>>Giá từ cao đến thấp</option>
                            <option value="rating-desc" <c:if test="${sort eq 'rating-desc'}">selected</c:if>>Đánh giá cao nhất</option>
                        </select>
                    </form>
                </div>

                <div class="products-result-bar" aria-live="polite">
                    <span>Hiển thị <strong>${firstProduct}–${lastProduct}</strong> / ${totalProducts} sản phẩm</span>
                </div>

                <c:if test="${not databaseAvailable}">
                    <div class="products-empty-state" role="alert">Không thể kết nối cơ sở dữ liệu để tải sản phẩm. Vui lòng kiểm tra cấu hình database và thử tải lại.</div>
                </c:if>
                <div class="product-grid">
                    <c:choose>
                        <c:when test="${empty products and databaseAvailable}">
                            <p class="products-empty-state">Không tìm thấy sản phẩm phù hợp. Hãy thử thay đổi từ khóa hoặc bộ lọc.</p>
                        </c:when>
                        <c:otherwise>
                            <c:forEach var="product" items="${products}">
                                <c:set var="fallbackImage" value="${pageContext.request.contextPath}/assets/img/products/bracelet_01.jpg" />
                                <c:if test="${product.categoryId eq 2}"><c:set var="fallbackImage" value="${pageContext.request.contextPath}/assets/img/products/necklace_02.jpg" /></c:if>
                                <c:if test="${product.categoryId eq 3}"><c:set var="fallbackImage" value="${pageContext.request.contextPath}/assets/img/products/phoneStrap_01.jpg" /></c:if>
                                <article class="product-card has-image">
                                    <a class="product-card-link" href="${pageContext.request.contextPath}/product-detail?id=${product.id}" aria-label="Xem chi tiết ${fn:escapeXml(product.name)}">
                                        <c:choose>
                                            <c:when test="${not empty product.image and fn:startsWith(product.image, 'http')}">
                                                <img src="<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                            </c:when>
                                            <c:when test="${not empty product.image}">
                                                <img src="${pageContext.request.contextPath}/assets/img/<c:out value='${product.image}'/>" data-fallback="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                            </c:when>
                                            <c:otherwise>
                                                <img src="${fallbackImage}" alt="<c:out value='${product.name}'/>" loading="lazy">
                                            </c:otherwise>
                                        </c:choose>
                                        <div class="product-info">
                                            <span class="product-category"><c:out value="${product.categoryName}"/></span>
                                            <h2 title="<c:out value='${product.name}'/>"><c:out value="${product.name}"/></h2>
                                            <p class="product-shop"><i class="fa-solid fa-store" aria-hidden="true"></i> <c:out value="${product.shopName}"/></p>
                                            <div class="product-rating" aria-label="Đánh giá <fmt:formatNumber value='${product.rating}' maxFractionDigits='1'/> trên 5">
                                                <i class="fa-solid fa-star" aria-hidden="true"></i>
                                                <span class="rating-score"><fmt:formatNumber value="${product.rating}" minFractionDigits="1" maxFractionDigits="1"/></span>
                                                <span class="review-count">(<c:out value="${product.reviewCount}"/>)</span>
                                            </div>
                                            <p class="price"><fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/>đ</p>
                                            <p class="product-sold">Đã bán <c:out value="${product.soldCount}"/></p>
                                        </div>
                                    </a>
                                </article>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:if test="${totalPages gt 1}">
                    <form class="products-pagination-form" method="get" action="${pageContext.request.contextPath}/products">
                        <input type="hidden" name="categoryFilter" value="1">
                        <input type="hidden" name="sort" value="${sort}">
                        <c:if test="${not empty keyword}"><input type="hidden" name="q" value="<c:out value='${keyword}'/>" /></c:if>
                        <c:forEach var="categoryId" items="${selectedCategories}"><input type="hidden" name="category" value="${categoryId}"></c:forEach>
                        <c:forEach var="priceValue" items="${selectedPrices}"><input type="hidden" name="price" value="${priceValue}"></c:forEach>
                        <c:forEach var="ratingValue" items="${selectedRatingValues}"><input type="hidden" name="rating" value="${ratingValue}"></c:forEach>
                        <nav class="products-pagination" aria-label="Phân trang sản phẩm">
                            <button type="submit" name="page" value="${page - 1}" aria-label="Trang trước" <c:if test="${page le 1}">disabled</c:if>>‹</button>
                            <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                                <button type="submit" name="page" value="${pageNumber}"
                                    class="<c:if test='${pageNumber eq page}'>is-current</c:if>"
                                    <c:if test="${pageNumber eq page}">aria-current="page"</c:if>>${pageNumber}</button>
                            </c:forEach>
                            <button type="submit" name="page" value="${page + 1}" aria-label="Trang sau" <c:if test="${page ge totalPages}">disabled</c:if>>›</button>
                        </nav>
                    </form>
                </c:if>
            </section>
        </div>
    </main>

    <div id="footer-placeholder"></div>
    <script>
        const appContextPath = '${pageContext.request.contextPath}';
        fetch(appContextPath + '/components/header.html')
            .then(response => response.text())
            .then(html => {
                html = html.replace(/href="index\.html"/g, 'href="' + appContextPath + '/index.html"');
                html = html.replace(/href="\.\.\/products"/g, 'href="' + appContextPath + '/products"');
                document.getElementById('header-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Không thể tải header:', error));
        fetch(appContextPath + '/components/footer.html')
            .then(response => response.text())
            .then(html => {
                html = html.replace(/href="index\.html"/g, 'href="' + appContextPath + '/index.html"');
                html = html.replace(/href="\.\.\/products"/g, 'href="' + appContextPath + '/products"');
                document.getElementById('footer-placeholder').innerHTML = html;
            })
            .catch(error => console.error('Không thể tải footer:', error));
        document.querySelectorAll('img[data-fallback]').forEach(image => {
            image.addEventListener('error', () => {
                image.src = image.dataset.fallback;
                image.removeAttribute('data-fallback');
            }, { once: true });
        });
        const selectAllCategories = document.getElementById('select-all-categories');
        const categoryCheckboxes = Array.from(document.querySelectorAll('.sidebar-filter input[name="category"]'));
        const submittedFilters = new URLSearchParams(window.location.search);
        if (submittedFilters.has('categoryFilter')) {
            const submittedCategories = submittedFilters.getAll('category');
            const submittedPrices = submittedFilters.getAll('price');
            const submittedRatings = submittedFilters.getAll('rating');
            categoryCheckboxes.forEach(checkbox => { checkbox.checked = submittedCategories.includes(checkbox.value); });
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
            categoryCheckboxes.forEach(checkbox => { checkbox.checked = selectAllCategories.checked; });
            syncSelectAllCategories();
        });
        categoryCheckboxes.forEach(checkbox => checkbox.addEventListener('change', syncSelectAllCategories));
        syncSelectAllCategories();
    </script>
    <script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
</body>
</html>
