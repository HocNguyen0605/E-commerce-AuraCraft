<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Giỏ hàng — AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleCart.css">
</head>
<body>
<div id="header-placeholder"></div>
<main class="cart-page" data-context="${pageContext.request.contextPath}"
      data-database-cart="${sessionScope.role eq 'buyer' and not empty sessionScope.userId}">
    <div class="container">
        <h1 class="section-title">Giỏ hàng của bạn</h1>
        <div class="cart-toolbar"><label><input type="checkbox" id="selectAllCart"> Chọn tất cả</label></div>
        <div id="cartItems" class="cart-items" aria-live="polite"></div>
        <div id="cartEmpty" class="cart-empty" hidden><i class="fa-solid fa-cart-shopping"></i>
            <h2>Giỏ hàng đang trống</h2>
            <p>Hãy chọn sản phẩm bạn yêu thích để thêm vào giỏ hàng.</p>
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/pages/products">Khám phá sản phẩm</a></div>
        <div class="cart-summary" id="cartSummary" hidden><p><span id="selectedCartCount">0 sản phẩm được chọn</span>
        </p>
            <p>Tổng cộng: <strong class="total-price" id="cartTotal">0 ₫</strong></p>
            <button class="btn btn-primary checkout-btn" id="checkoutCartBtn" type="button">Tiến hành thanh toán
            </button>
        </div>
    </div>
</main>
<div id="footer-placeholder"></div>
<script src="${pageContext.request.contextPath}/assets/js/cart.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
</body>
</html>
