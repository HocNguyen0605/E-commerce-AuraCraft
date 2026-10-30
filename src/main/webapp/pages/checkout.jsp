<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<fmt:setLocale value="vi_VN"/>
<%
    if (request.getAttribute("checkoutPageReady") == null) {
        String target = request.getContextPath() + "/pages/checkout";
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
    <title>Thanh toán đơn hàng - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleCheckout.css?v=checkout-benefits-1">
</head>

<body>
<div id="header-placeholder"></div>

<main class="checkout-page">
    <div class="container">
        <div class="checkout-header-bar">
            <h1 class="section-title">Thanh toán đơn hàng</h1>
            <nav class="checkout-breadcrumb" aria-label="Breadcrumb">
                <a href="${pageContext.request.contextPath}/pages/cart">Giỏ hàng</a>
                <span><i class="fa-solid fa-chevron-right"></i></span>
                <span class="active">Thông tin giao hàng & Thanh toán</span>
                <span><i class="fa-solid fa-chevron-right"></i></span>
                <span>Hoàn tất đơn hàng</span>
            </nav>
        </div>

        <form id="checkoutForm" novalidate data-context="${pageContext.request.contextPath}"
              data-database-checkout="${databaseCheckoutAvailable}"
              data-has-custom="${hasCustomItem}"
              data-product-ids="<c:out value='${checkoutProductIds}'/>">
            <div class="checkout-layout">

                <div class="checkout-form-section">

                    <!-- Thông tin người nhận -->
                    <div class="checkout-card">
                        <h2 class="checkout-card-title">
                            <i class="fa-solid fa-location-dot"></i>
                            <span>1. Thông tin người nhận</span>
                        </h2>

                        <div class="form-row">
                            <div class="form-group">
                                <label for="fullName">Họ và tên người nhận <span class="required">*</span></label>
                                <input type="text" id="fullName" class="form-control"
                                       placeholder="Ví dụ: Nguyễn Văn A" maxlength="100" value="<c:out value='${checkoutFullName}'/>" required>
                                <span class="field-error-msg">Vui lòng nhập họ và tên người nhận.</span>
                            </div>

                            <div class="form-group">
                                <label for="phone">Số điện thoại <span class="required">*</span></label>
                                <input type="tel" id="phone" class="form-control" placeholder="Ví dụ: 0912345678"
                                       value="<c:out value='${checkoutPhone}'/>" required>
                                <span class="field-error-msg">Vui lòng nhập số điện thoại hợp lệ (10 số).</span>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="email">Địa chỉ Email</label>
                            <input type="email" id="email" class="form-control" placeholder="name@example.com" value="<c:out value='${checkoutEmail}'/>" readonly>
                        </div>

                        <div class="form-group">
                            <label for="province">Tỉnh / thành phố <span class="required">*</span></label>
                            <select id="province" class="form-control" required>
                                <option value="">Chọn tỉnh / thành phố</option>
                                <option value="01">Hà Nội</option><option value="04">Cao Bằng</option><option value="08">Tuyên Quang</option><option value="11">Điện Biên</option><option value="12">Lai Châu</option><option value="14">Sơn La</option><option value="15">Lào Cai</option><option value="19">Thái Nguyên</option><option value="20">Lạng Sơn</option><option value="22">Quảng Ninh</option><option value="24">Bắc Ninh</option><option value="25">Phú Thọ</option><option value="31">Hải Phòng</option><option value="33">Hưng Yên</option><option value="37">Ninh Bình</option><option value="38">Thanh Hóa</option><option value="40">Nghệ An</option><option value="42">Hà Tĩnh</option><option value="44">Quảng Trị</option><option value="46">Huế</option><option value="48">Đà Nẵng</option><option value="51">Quảng Ngãi</option><option value="52">Gia Lai</option><option value="56">Khánh Hòa</option><option value="66">Đắk Lắk</option><option value="68">Lâm Đồng</option><option value="75">Đồng Nai</option><option value="79">Thành phố Hồ Chí Minh</option><option value="80">Tây Ninh</option><option value="82">Đồng Tháp</option><option value="86">Vĩnh Long</option><option value="91">An Giang</option><option value="92">Cần Thơ</option><option value="96">Cà Mau</option>
                            </select>
                            <span class="field-error-msg">Vui lòng chọn tỉnh / thành phố.</span>
                        </div>

                        <div class="form-group">
                            <label for="ward">Xã / phường <span class="required">*</span></label>
                            <select id="ward" class="form-control" required disabled>
                                <option value="">Vui lòng chọn tỉnh / thành phố trước</option>
                            </select>
                            <input type="text" id="wardFallback" class="form-control" maxlength="120"
                                   placeholder="Nhập xã, phường" hidden>
                            <span class="field-error-msg">Vui lòng chọn xã / phường.</span>
                            <%--                            <small id="wardLoadingMessage" class="field-help-msg" aria-live="polite"></small>--%>
                        </div>

                        <div class="form-group">
                            <label for="address">Số nhà, tên đường / địa chỉ chi tiết <span class="required">*</span></label>
                            <input type="text" id="address" class="form-control" maxlength="250"
                                   placeholder="Số nhà, tên đường, thôn / tổ dân phố..." value="<c:out value='${checkoutAddress}'/>" required>
                            <span class="field-error-msg">Vui lòng nhập địa chỉ nhận hàng chi tiết.</span>
                        </div>

                        <div class="form-group">
                            <label for="orderNote">Ghi chú cho thợ thủ công / Shipper (tùy chọn)</label>
                            <textarea id="orderNote" class="form-control" maxlength="100"
                                      placeholder="Ví dụ: Giao giờ hành chính, gọi trước khi giao, kích cỡ cổ tay 15cm..."></textarea>
                        </div>
                    </div>

                    <!-- Phương thức thanh toán -->
                    <div class="checkout-card">
                        <h2 class="checkout-card-title">
                            <i class="fa-solid fa-credit-card"></i>
                            <span>2. Phương thức thanh toán</span>
                        </h2>

                        <div class="payment-methods-grid">
                            <!-- COD applies to ready-made products and customizations of an existing product. -->
                            <label class="payment-method-card active" for="paymentCODStandard" id="paymentCodStandardCard">
                                <input type="radio" id="paymentCODStandard" name="paymentMethod" value="cod_standard"
                                       <c:if test="${not hasCustomItem}">checked</c:if>>
                                <div class="payment-method-info">
                                    <div class="payment-method-name">
                                        <span>Thanh toán 100% khi nhận hàng (COD)</span>
                                        <span class="payment-badge badge-standard">Thanh toán khi nhận hàng</span>
                                    </div>
                                    <p class="payment-method-desc">Áp dụng cho sản phẩm có sẵn và sản phẩm đã tùy chỉnh từ mẫu có sẵn. Thanh toán toàn bộ cho shipper khi nhận hàng.</p>
                                </div>
                                <div class="payment-method-icon">
                                    <i class="fa-solid fa-hand-holding-dollar"></i>
                                </div>
                            </label>

                            <!-- Cọc 50% bằng chuyển khoản cho đơn thiết kế -->
                            <label class="payment-method-card" for="paymentCODCustom" id="paymentCodCustomCard">
                                <input type="radio" id="paymentCODCustom" name="paymentMethod" value="vnpay_deposit"
                                       <c:if test="${hasCustomItem}">checked</c:if>>
                                <div class="payment-method-info">
                                    <div class="payment-method-name">
                                        <span>Đặt cọc 50% qua chuyển khoản</span>
                                        <span class="payment-badge badge-custom">Đơn thiết kế</span>
                                    </div>
                                    <p class="payment-method-desc">Thanh toán cọc 50% qua VNPay để shop bắt đầu chế tác. 50% còn lại thanh toán khi nhận hàng.</p>
                                </div>
                                <div class="payment-method-icon">
                                    <i class="fa-solid fa-wand-magic-sparkles"></i>
                                </div>
                            </label>

                            <!-- Full transfer applies to ready-made products and their customizations. -->
                            <label class="payment-method-card" for="paymentVNPay" id="paymentVnpayCard">
                                <input type="radio" id="paymentVNPay" name="paymentMethod" value="vnpay">
                                <div class="payment-method-info">
                                    <div class="payment-method-name">
                                        <span>Chuyển khoản qua VNPay</span>
                                        <span class="payment-badge badge-online">Thanh toán 100%</span>
                                    </div>
                                    <p class="payment-method-desc">Thanh toán trực tuyến toàn bộ đơn hàng qua cổng VNPay.</p>
                                </div>
                                <div class="payment-method-icon">
                                    <i class="fa-solid fa-qrcode"></i>
                                </div>
                            </label>
                        </div>

                        <div id="customDepositAlert" class="checkout-deposit-alert" hidden>
                            <i class="fa-solid fa-circle-info"></i> Đơn thiết kế cần cọc 50% bằng chuyển khoản; 50% còn lại thanh toán khi nhận hàng.
                        </div>
                    </div>

                </div>

                <!-- Bảng tóm tắt đơn hàng -->
                <aside class="checkout-summary-section">
                    <div class="checkout-card">
                        <h2 class="checkout-card-title">
                            <i class="fa-solid fa-receipt"></i>
                            <span>Tóm tắt đơn hàng</span>
                        </h2>

                        <!-- Danh sách sản phẩm đặt mua -->
                        <div class="summary-products-list" id="summaryProductsList">
                            <c:choose>
                                <c:when test="${not empty checkoutItems}">
                                    <c:forEach items="${checkoutItems}" var="item">
                                        <div class="summary-product-item">
                                            <img src="<c:out value='${item.imageUrl}'/>" alt="<c:out value='${item.name}'/>" class="summary-product-thumb">
                                            <div class="summary-product-details">
                                                <h3 class="summary-product-title"><c:out value="${item.name}"/></h3>
                                                <p class="summary-product-meta">Số lượng: <c:out value="${item.quantity}"/> · <c:out value="${item.shopName}"/></p>
                                            </div>
                                            <span class="summary-product-price"><fmt:formatNumber value="${item.lineTotal}" type="number" groupingUsed="true"/>đ</span>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <c:otherwise><div class="summary-client-fallback"></div></c:otherwise>
                            </c:choose>
                        </div>

                        <section class="checkout-benefits" aria-label="Voucher và xu AuraCraft">
                            <h3>Ưu đãi đơn hàng</h3>
                            <label for="voucherCode">Mã voucher</label>
                            <div class="checkout-voucher-controls">
                                <input type="text" id="voucherCode" maxlength="50" placeholder="Nhập mã voucher">
                                <button type="button" id="btnClearVoucher" class="btn btn-outline" aria-label="Bỏ voucher">Bỏ mã</button>
                            </div>
                            <p id="voucherMessage" class="checkout-benefit-message" aria-live="polite">Voucher chỉ dùng được trong thời hạn và khi đơn đạt giá trị tối thiểu.</p>
                            <c:if test="${not empty availableVouchers}">
                                <div class="checkout-voucher-list">
                                    <c:forEach items="${availableVouchers}" var="voucher">
                                        <button type="button" class="checkout-voucher-option" data-voucher-code="<c:out value='${voucher.code}'/>">
                                            <strong><c:out value="${voucher.code}"/></strong>
                                            <span>Giảm <c:out value="${voucher.discountText}"/> · Đơn từ <fmt:formatNumber value="${voucher.minOrder}" type="number" groupingUsed="true"/>đ</span>
                                            <small>Hết hạn <c:out value="${voucher.endDate}"/></small>
                                        </button>
                                    </c:forEach>
                                </div>
                            </c:if>
                            <div class="checkout-coins-controls">
                                <span>Bạn có <strong id="availableCoins">0 xu</strong></span>
                                <button type="button" id="btnUseCoins" class="btn btn-outline" disabled>Dùng xu</button>
                            </div>
                            <p id="coinsMessage" class="checkout-benefit-message">100 xu giảm 1.000đ. Tích 1 xu mỗi 10.000đ tiền hàng thực trả khi đơn hoàn tất.</p>
                        </section>

                        <!-- Chi phí & Phí vận chuyển -->
                        <div class="summary-cost-table">
                            <div class="cost-row">
                                <span>Tạm tính</span>
                                <span class="cost-value" id="summarySubtotal"><fmt:formatNumber value="${checkoutSubtotal}" type="number" groupingUsed="true"/>đ</span>
                            </div>

                            <div class="cost-row shipping-fee-row">
                                <span><i class="fa-solid fa-truck-fast"></i> Phí vận chuyển</span>
                                <span class="cost-value" id="summaryShipping"><fmt:formatNumber value="${checkoutShipping}" type="number" groupingUsed="true"/>đ</span>
                            </div>

                            <div class="cost-row checkout-discount-row" id="voucherDiscountRow" hidden>
                                <span>Voucher</span>
                                <span class="cost-value" id="summaryVoucherDiscount">-0đ</span>
                            </div>
                            <div class="cost-row checkout-discount-row" id="coinDiscountRow" hidden>
                                <span>Xu đã dùng (<span id="summaryCoinsUsed">0</span> xu)</span>
                                <span class="cost-value" id="summaryCoinDiscount">-0đ</span>
                            </div>

                            <div class="cost-row total-row">
                                <span>Tổng cộng</span>
                                <span class="total-amount" id="summaryTotal"><fmt:formatNumber value="${checkoutTotal}" type="number" groupingUsed="true"/>đ</span>
                            </div>

                            <div class="cost-row deposit-row" id="depositRow" hidden>
                                <span>Tiền cọc cần thanh toán (50%)</span>
                                <span class="cost-value deposit-value" id="summaryDeposit">0đ</span>
                            </div>

                            <div class="cost-row remaining-row" id="remainingRow" hidden>
                                <span>Còn lại (thanh toán sau)</span>
                                <span class="cost-value" id="summaryRemaining">0đ</span>
                            </div>
                        </div>

                        <button type="submit" id="btnConfirmOrder" class="btn btn-primary btn-confirm-order">
                            <span id="btnConfirmText">Xác nhận đặt hàng</span>
                            <i class="fa-solid fa-arrow-right"></i>
                        </button>

                        <div class="checkout-guarantee">
                            <i class="fa-solid fa-shield-halved"></i>
                            <span>Kiểm tra hàng trước khi nhận - Đảm bảo độc bản 100%</span>
                        </div>
                    </div>
                </aside>

            </div>
        </form>
    </div>
</main>

<!-- QR Payment Modal -->
<div id="qrPaymentModal" class="modal-overlay qr-payment-overlay" hidden>
    <div class="qr-payment-card">
        <h3 id="mockPaymentTitle">Mô phỏng thanh toán VNPay</h3>
        <p id="mockPaymentDescription">Đây là giao dịch giả lập, không chuyển tiền thật. Số tiền mô phỏng: <strong id="qrDepositAmount"></strong></p>
        <div class="qr-code-placeholder">
            <i class="fa-solid fa-laptop-code"></i>
            <p id="mockPaymentDemoNote">CHẾ ĐỘ DEMO · KHÔNG GIAO DỊCH NGÂN HÀNG</p>
        </div>
        <p>Mã tham chiếu: <strong id="mockPaymentReference"></strong></p>
        <div class="qr-payment-actions">
            <button type="button" id="btnCancelQr" class="btn btn-outline">Quay lại</button>
            <button type="button" id="btnSuccessQr" class="btn btn-primary">Xác nhận giả lập thanh toán</button>
        </div>
    </div>
</div>

<div id="footer-placeholder"></div>

<script src="${pageContext.request.contextPath}/assets/js/custom-workflow.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/checkout.js?v=loyalty-10k-100-10-v2"></script>
<script>
    const contextPath = '${pageContext.request.contextPath}';
    fetch(contextPath + '/components/header.html')
        .then(r => r.text())
        .then(html => {
            document.getElementById('header-placeholder').innerHTML = html;
        })
        .catch(err => console.error('Lỗi tải Header:', err));

    fetch(contextPath + '/components/footer.html')
        .then(r => r.text())
        .then(html => {
            document.getElementById('footer-placeholder').innerHTML = html;
        })
        .catch(err => console.error('Lỗi tải Footer:', err));
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>

</html>