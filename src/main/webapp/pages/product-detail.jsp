<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%
    if (request.getAttribute("productDetailReady") == null) {
        String target = request.getContextPath() + "/pages/product-detail";
        if (request.getQueryString() != null && !request.getQueryString().isBlank()) target += "?" + request.getQueryString();
        response.sendRedirect(response.encodeRedirectURL(target));
        return;
    }
%>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>
        <c:out value="${product.name}"/> — AuraCraft
    </title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleProduct-detail.css">
    <style>
        .pd-gallery-main {
            background: #f7f3f0 center/contain no-repeat
        }

        .pd-gallery-main img {
            width: 100%;
            height: 100%;
            /*object-fit: contain*/
        }

        .pd-thumb {
            background: #f7f3f0 center/cover no-repeat;
            cursor: pointer
        }

        .pd-stock {
            margin: 12px 0;
            color: #66514c
        }

        .pd-stock.out {
            color: #ad3434
        }

        .pd-actions button:disabled {
            opacity: .55;
            cursor: not-allowed
        }

        .pd-empty {
            padding: 24px;
            background: #fff;
            border-radius: 12px;
            color: #735e5a
        }

    </style>
</head>

<body>
<div id="header-placeholder"></div>
<main class="container pd-wrap" data-context="${pageContext.request.contextPath}" data-id="${product.id}" data-category="<c:out value='${product.categoryName}'/>" data-shop="<c:out value='${product.shopName}'/>" data-database-cart="${databaseCartAvailable}">
    <nav class="breadcrumb" aria-label="Điều hướng">
        <a href="${pageContext.request.contextPath}/pages/products">Sản phẩm</a> /
        <a href="${pageContext.request.contextPath}/pages/products?category=${product.categoryId}&amp;categoryFilter=1"><c:out
                value="${product.categoryName}"/></a> /
        <strong aria-current="page"><c:out value="${product.name}"/></strong>
    </nav>
    <section class="pd-top">
        <div class="pd-gallery">
            <div class="pd-gallery-main" id="pdGalleryMain">
                <c:choose>
                    <c:when test="${not empty productImages}">
                        <img id="pdMainImage" src="${productImages[0]}" alt="${product.name}">
                    </c:when>
                    <c:otherwise><span>Chưa có ảnh sản phẩm</span></c:otherwise>
                </c:choose>
            </div>
            <div class="pd-gallery-thumbs" id="pdThumbs">
                <c:forEach items="${productImages}" var="image" varStatus="loop">
                    <button type="button"
                            class="pd-thumb${loop.first ? ' is-active' : ''}"
                            data-image="${image}" aria-label="Xem ảnh ${loop.count}" aria-pressed="${loop.first}">
                        <img src="${image}" alt="${product.name} — ảnh ${loop.count}" loading="lazy">
                    </button>
                </c:forEach>
            </div>
        </div>
        <div class="pd-info">


            <h1>
                <c:out value="${product.name}"/>
            </h1>
            <div class="pd-rating"><span class="stars">★★★★★</span> <span>
                        <fmt:formatNumber value="${product.rating}" maxFractionDigits="1"/>
                        (${product.reviewCount} đánh giá) · Đã bán ${product.soldCount}
                    </span></div>
            <div class="pd-price">
                <fmt:formatNumber value="${product.price}" type="number" groupingUsed="true"/> ₫
            </div>
            <p class="pd-introduction">
                <c:out value="${productIntroduction}"/>
            </p>
            <p class="pd-stock${product.stock lt 1 ? ' out' : ''}">
                <c:choose>
                    <c:when test="${product.stock gt 0}">Còn ${product.stock} sản phẩm</c:when>
                    <c:otherwise>Sản phẩm tạm hết hàng</c:otherwise>
                </c:choose>
            </p>
            <div class="pd-qty-row"><label for="qty">Số lượng</label>
                <div class="stepper">
                    <button type="button" id="qtyMinus" aria-label="Giảm">−</button>
                    <input
                            type="number" id="qty" value="1" min="1" max="${product.stock}" ${product.stock lt 1
                            ? 'disabled' : '' }>
                    <button type="button" id="qtyPlus" aria-label="Tăng" ${product.stock lt
                            1 ? 'disabled' : '' }>+
                    </button>
                </div>
            </div>
            <div class="pd-actions">
                <button class="btn btn-outline" id="pdAddToCartBtn" ${product.stock lt 1
                        ? 'disabled' : '' }>Thêm vào giỏ
                </button>
                <button class="btn btn-primary" id="pdBuyNowBtn"
                ${product.stock lt 1 ? 'disabled' : '' }>Mua ngay
                </button>
            </div>
            <p id="pdActionMessage" role="status" aria-live="polite"></p>
            <c:choose>
                <c:when test="${product.categoryId eq 2}"><c:set var="customizerType" value="necklace"/></c:when>
                <c:when test="${product.categoryId eq 3}"><c:set var="customizerType" value="charm"/></c:when>
                <c:otherwise><c:set var="customizerType" value="bracelet"/></c:otherwise>
            </c:choose>
            <c:url var="customizerUrl" value="/pages/customizer.html">
                <c:param name="type" value="${customizerType}"/>
                <c:param name="id" value="${product.id}"/>
                <c:param name="name" value="${product.name}"/>
                <c:param name="shop" value="${product.shopName}"/>
                <c:param name="context" value="${pageContext.request.contextPath}"/>
            </c:url>
            <a href="${customizerUrl}"
               class="custom-cta"><span class="custom-cta-icon">✎</span><span><strong>Tạo thiết kế tùy
                            chỉnh</strong><small>Kéo thả vật liệu, charm vào đúng vị trí mong
                            muốn</small></span></a>
        </div>
    </section>
    <section class="pd-shop-section">
        <h3>Cửa hàng</h3><a href="${pageContext.request.contextPath}/pages/shop-profile.html?id=${product.shopId}"
                            class="pd-shop"><span class="pd-shop-avatar">
                    <c:out value="${product.shopName.substring(0,1)}"/>
                </span><span class="pd-shop-body"><strong class="pd-shop-name">
                        <c:out value="${product.shopName}"/>
                    </strong><span class="pd-shop-meta">★
                        <fmt:formatNumber value="${product.shopRating}" maxFractionDigits="1"/> · Cửa hàng
                        AuraCraft
                    </span></span><span class="pd-shop-arrow">›</span></a>
    </section>
    <section class="pd-desc card">
        <h3>Mô tả sản phẩm</h3>
        <p>
            <c:out value="${product.description}"/>
        </p>
    </section>
    <section class="pd-reviews" id="reviews">
        <h2 class="section-title">Đánh giá từ người mua</h2>
        <p class="pd-review-summary">${product.reviewCount} đánh giá · Trung bình
            <fmt:formatNumber value="${product.rating}" maxFractionDigits="1"/>/5
        </p>
        <c:choose>
            <c:when test="${not empty reviews}">
                <div class="pd-review-carousel">
                    <button class="review-nav prev" type="button" aria-label="Xem đánh giá trước">‹</button>
                    <div class="review-grid" id="reviewGrid">
                    <c:forEach items="${reviews}" var="review">
                        <article class="review-card card">
                            <div class="review-head"><strong>
                                <c:out value="${review.reviewerName}"/>
                            </strong><span class="review-stars">
                                        <c:forEach begin="1" end="5" var="star">
                                            <c:choose>
                                                <c:when test="${star le review.rating}">★</c:when>
                                                <c:otherwise>☆</c:otherwise>
                                            </c:choose>
                                        </c:forEach>
                                    </span></div>
                            <p>
                                <c:out value="${review.comment}"/>
                            </p><small>
                            <fmt:formatDate value="${review.date}" pattern="dd/MM/yyyy"/>
                        </small>
                        </article>
                    </c:forEach>
                    </div>
                    <button class="review-nav next" type="button" aria-label="Xem đánh giá tiếp theo">›</button>
                </div>
            </c:when>
            <c:otherwise>
                <p class="pd-empty">Sản phẩm chưa có đánh giá.</p>
            </c:otherwise>
        </c:choose>
        <c:if test="${not empty reviewableOrders}">
            <form class="pd-review-form card" action="${pageContext.request.contextPath}/pages/product-review" method="post">
                <h3>Viết đánh giá của bạn</h3>
                <input type="hidden" name="productId" value="${product.id}">
                <label for="reviewOrder">Đơn hàng đã hoàn thành</label>
                <select id="reviewOrder" name="orderId" required><c:forEach items="${reviewableOrders}" var="orderId"><option value="${orderId}">Đơn hàng #${orderId}</option></c:forEach></select>
                <label for="reviewRating">Số sao</label>
                <select id="reviewRating" name="rating" required><option value="5">5 sao</option><option value="4">4 sao</option><option value="3">3 sao</option><option value="2">2 sao</option><option value="1">1 sao</option></select>
                <label for="reviewComment">Nhận xét</label>
                <textarea id="reviewComment" name="comment" maxlength="2000" rows="4" required placeholder="Chia sẻ trải nghiệm của bạn về sản phẩm"></textarea>
                <button class="btn btn-primary" type="submit">Gửi đánh giá</button>
            </form>
        </c:if>
    </section>
    <section class="pd-shop-suggestions" id="pdShopSuggestions">
        <h2 class="section-title">Sản phẩm cùng danh mục</h2>
        <div class="similar-carousel">
        <button class="review-nav prev" type="button" aria-label="Xem sản phẩm trước" data-carousel="similarGrid">‹</button>
        <div class="shop-suggest-grid" id="similarGrid">
            <c:choose>
                <c:when test="${not empty similarProducts}">
                    <c:forEach items="${similarProducts}" var="similar"><a class="shop-suggest-item"
                                                                           href="${pageContext.request.contextPath}/pages/product-detail?id=${similar.id}">
                        <c:choose>
                            <c:when test="${not empty similar.image}">
                                <div class="shop-suggest-img"
                                     style="background-image:url('${similar.image}')">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="shop-suggest-img">AuraCraft</div>
                            </c:otherwise>
                        </c:choose>
                        <div class="shop-suggest-body"><strong>
                            <c:out value="${similar.name}"/>
                        </strong><span class="shop-suggest-price">
                                        <fmt:formatNumber value="${similar.price}" type="number" groupingUsed="true"/>
                                        ₫
                                    </span><span class="shop-suggest-rating">★
                                        <fmt:formatNumber value="${similar.rating}" maxFractionDigits="1"/>
                                        · Đã bán ${similar.soldCount}
                                    </span></div>
                    </a></c:forEach>
                </c:when>
                <c:otherwise>
                    <p class="pd-empty">Chưa có sản phẩm tương tự.</p>
                </c:otherwise>
            </c:choose>
        </div>
        <button class="review-nav next" type="button" aria-label="Xem sản phẩm tiếp theo" data-carousel="similarGrid">›</button>
        </div>
    </section>
</main>
<div id="footer-placeholder"></div>
<script>
    const root = document.querySelector('.pd-wrap'), ctx = root.dataset.context, productId = root.dataset.id,
        stock = Number(document.getElementById('qty')?.max || 0), qty = document.getElementById('qty');
    const reviewGrid = document.getElementById('reviewGrid');
    if (reviewGrid) {
        const prev = document.querySelector('.review-nav.prev'), next = document.querySelector('.review-nav.next');
        const updateArrows = () => {
            prev.disabled = reviewGrid.scrollLeft <= 2;
            next.disabled = reviewGrid.scrollLeft + reviewGrid.clientWidth >= reviewGrid.scrollWidth - 2;
        };
        prev.addEventListener('click', () => reviewGrid.scrollBy({left: -Math.max(300, reviewGrid.clientWidth * .8), behavior: 'smooth'}));
        next.addEventListener('click', () => reviewGrid.scrollBy({left: Math.max(300, reviewGrid.clientWidth * .8), behavior: 'smooth'}));
        reviewGrid.addEventListener('scroll', updateArrows, {passive: true});
        window.addEventListener('resize', updateArrows);
        requestAnimationFrame(updateArrows);
    }
    const similarGrid = document.getElementById('similarGrid');
    if (similarGrid) {
        const prev = document.querySelector('[data-carousel="similarGrid"].prev');
        const next = document.querySelector('[data-carousel="similarGrid"].next');
        const updateArrows = () => {
            prev.disabled = similarGrid.scrollLeft <= 2;
            next.disabled = similarGrid.scrollLeft + similarGrid.clientWidth >= similarGrid.scrollWidth - 2;
        };
        prev.addEventListener('click', () => similarGrid.scrollBy({left: -Math.max(300, similarGrid.clientWidth * .8), behavior: 'smooth'}));
        next.addEventListener('click', () => similarGrid.scrollBy({left: Math.max(300, similarGrid.clientWidth * .8), behavior: 'smooth'}));
        similarGrid.addEventListener('scroll', updateArrows, {passive: true});
        window.addEventListener('resize', updateArrows);
        requestAnimationFrame(updateArrows);
    }
    document.querySelectorAll('.pd-thumb').forEach(b => b.addEventListener('click', () => {
        document.querySelectorAll('.pd-thumb').forEach(x => { x.classList.remove('is-active'); x.setAttribute('aria-pressed', 'false'); });
        b.classList.add('is-active');
        b.setAttribute('aria-pressed', 'true');
        const img = document.getElementById('pdMainImage');
        if (img) { img.src = b.dataset.image; img.alt = b.querySelector('img')?.alt || img.alt; }
    }));
    document.getElementById('qtyMinus')?.addEventListener('click', () => qty.value = Math.max(1, (+qty.value || 1) - 1));
    document.getElementById('qtyPlus')?.addEventListener('click', () => qty.value = Math.min(stock, (+qty.value || 1) + 1));

    async function addCart(goCheckout) {
        const amount = Math.max(1, Math.min(stock, parseInt(qty.value, 10) || 1));
        qty.value = amount;
        const name = document.querySelector('.pd-info h1').textContent.trim();
        const cartItem = {id:Number(productId),name,price:Number('${product.price}'),image:document.getElementById('pdMainImage')?.src||'',categoryName:root.dataset.category,shopName:root.dataset.shop,stock,selected:true,quantity:amount};
        let amountAdded = amount;
        if (root.dataset.databaseCart === 'true') {
            try {
                const response = await fetch(ctx + '/api/cart', {method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},body:new URLSearchParams({action:'add',productId,quantity:String(amount)})});
                const result = await response.json();
                if (!response.ok) throw new Error(result.message || 'Không thể thêm vào giỏ hàng.');
                amountAdded = result.quantityAdded;
            } catch (error) { window.alert(error.message); return; }
        } else {
        const cart = JSON.parse(localStorage.getItem('AuraCraftCart') || '[]');
        const item = cart.find(x => String(x.id) === productId);
        if (item) { item.quantity = Math.min(stock, item.quantity + amount); item.selected = true; } else cart.push({
            ...cartItem
        });
        localStorage.setItem('AuraCraftCart', JSON.stringify(cart));
        }
        if (goCheckout) {
            cartItem.quantity = amount;
            localStorage.setItem('AuraCraftCheckoutCart', JSON.stringify([cartItem]));
            localStorage.setItem('AuraCraftCheckoutSource', root.dataset.databaseCart === 'true' ? 'database' : 'browser');
            location.href = ctx + '/pages/checkout?fromCart=1&productIds=' + encodeURIComponent(productId);
        } else {
            const shortName = name.length > 32 ? name.slice(0, 32).trimEnd() + '…' : name;
            window.alert('Đã thêm thành công ' + amountAdded + ' x ' + shortName + ' vào giỏ hàng.');
        }
    }

    document.getElementById('pdAddToCartBtn')?.addEventListener('click', () => addCart(false));
    document.getElementById('pdBuyNowBtn')?.addEventListener('click', () => addCart(true));
    fetch(ctx + '/components/header.html').then(r => r.text()).then(html => {
        document.getElementById('header-placeholder').innerHTML = html.replace(/href="index\.html"/g, 'href="' + ctx + '/index.html"').replace(/href="pages\/products"/g, 'href="' + ctx + '/pages/products"').replace(/href="pages\/cart\.jsp"/g, 'href="' + ctx + '/pages/cart.jsp"').replace(/href="\.\.\/products"/g, 'href="' + ctx + '/pages/products"')
    });
    fetch(ctx + '/components/footer.html').then(r => r.text()).then(html => {
        document.getElementById('footer-placeholder').innerHTML = html.replace(/href="index\.html"/g, 'href="' + ctx + '/index.html"').replace(/href="pages\/products"/g, 'href="' + ctx + '/pages/products"').replace(/href="\.\.\/products"/g, 'href="' + ctx + '/pages/products"')
    });
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
</body>

</html>
