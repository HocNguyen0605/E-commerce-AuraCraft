<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đặt hàng thành công - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleSuccess.css">
</head>

<body>
<div id="header-placeholder"></div>

<main class="success-page">
    <div class="container">
        <div class="success-card">
            <div class="success-icon-wrap">
                <i class="fa-solid fa-circle-check"></i>
            </div>

            <h1>Đặt hàng thành công!</h1>
            <p class="success-subtext">
                Cảm ơn bạn đã lựa chọn AuraCraft. Đơn hàng của bạn đã được chuyển tới các nghệ nhân thủ công để
                chuẩn bị chế tác và giao đến tay bạn trong thời gian sớm nhất.
            </p>

            <div class="order-code-box">
                <span>Mã đơn hàng:</span>
                <strong id="displayOrderCode">—</strong>
                <button type="button" class="btn-copy-code" id="btnCopyCode" title="Sao chép mã đơn hàng"
                        aria-label="Sao chép mã đơn hàng">
                    <i class="fa-regular fa-copy"></i>
                </button>
            </div>

            <!-- Chi tiết đơn hàng -->
            <div class="success-order-details">
                <h3><i class="fa-solid fa-file-invoice"></i> Chi tiết đơn hàng</h3>

                <div class="detail-line">
                    <span class="detail-label">Người nhận:</span>
                    <span class="detail-val" id="detailCustomer">—</span>
                </div>

                <div class="detail-line"><span class="detail-label">Email:</span><span class="detail-val" id="detailEmail">—</span></div>

                <div class="detail-line">
                    <span class="detail-label">Địa chỉ giao hàng:</span>
                    <span class="detail-val" id="detailAddress">—</span>
                </div>

                <div class="detail-line">
                    <span class="detail-label">Ghi chú:</span>
                    <span class="detail-val" id="detailNote">—</span>
                </div>

                <div class="detail-line">
                    <span class="detail-label">Phương thức thanh toán:</span>
                    <span class="detail-val" id="detailPayment">—</span>
                </div>

                <div class="detail-line">
                    <span class="detail-label">Phí vận chuyển:</span>
                    <span class="detail-val" id="detailShipping">—</span>
                </div>

                <div class="detail-line">
                    <span class="detail-label">Tổng thanh toán:</span>
                    <span class="detail-val" id="detailTotal"
                          style="color:var(--primary-blue); font-weight:700;">—</span>
                </div>

                <div class="detail-line">
                    <span class="detail-label">Thời gian đặt:</span>
                    <span class="detail-val" id="detailTime">—</span>
                </div>
                <div class="detail-line"><span class="detail-label">Xu tích được:</span><span class="detail-val" id="detailRewardCoins">0 xu</span></div>
            </div>



            <div class="success-actions">
                <a href="${pageContext.request.contextPath}/pages/profile-orders.jsp" id="btnGoTracking" class="btn btn-primary">
                    <i class="fa-solid fa-truck-fast"></i> Theo dõi đơn hàng
                </a>
                <a href="${pageContext.request.contextPath}/pages/products" class="btn btn-outline">
                    <i class="fa-solid fa-bag-shopping"></i> Tiếp tục mua sắm
                </a>
                <a href="${pageContext.request.contextPath}/index.jsp" class="btn btn-outline">
                    <i class="fa-solid fa-house"></i> Về trang chủ
                </a>
            </div>
        </div>
    </div>
</main>

<div id="footer-placeholder"></div>

<script src="../assets/js/checkout.js"></script>
<script>
    fetch('../components/header.html')
        .then(r => r.text())
        .then(html => {
            document.getElementById('header-placeholder').innerHTML = html.replace(/href="index\.html"/g, 'href="../index.html"');
        })
        .catch(err => console.error('Lỗi tải Header:', err));

    fetch('../components/footer.html')
        .then(r => r.text())
        .then(html => {
            document.getElementById('footer-placeholder').innerHTML = html.replace(/href="index\.html"/g, 'href="../index.html"');
        })
        .catch(err => console.error('Lỗi tải Footer:', err));
</script>
<%--<script src="../assets/js/popup.js"></script>--%>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>

</html>
