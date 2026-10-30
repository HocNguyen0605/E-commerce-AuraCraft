<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hồ sơ thợ - AuraCraft Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700;800&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../../assets/css/style.css">
    <link rel="stylesheet" href="../../assets/css/admin-layout.css">
    <link rel="stylesheet" href="../../assets/css/pages/styleAdminOrders.css">
    <link rel="stylesheet" href="../../assets/css/pages/styleAdminUsers.css">
    <link rel="stylesheet" href="../../assets/css/pages/styleAdminPortfolio.css">
</head>
<body style="background-color: #F4F6F9; margin: 0; padding: 0;">

<div class="portfolio-standalone-page">
    <div class="portfolio-standalone-container">
        <!-- Header trang: Nút quay lại & Tiêu đề Hồ sơ thợ -->
        <div class="page-header" style="display:flex;align-items:center;gap:16px;margin-bottom:24px;">
            <button class="btn btn-outline" id="btnBackToUsers" style="padding:9px 18px;font-size:14px;border-radius:8px;display:inline-flex;align-items:center;gap:8px;cursor:pointer;background:#fff;">
                <i class="fa-solid fa-arrow-left"></i> Danh sách Thợ
            </button>
            <h1 class="page-title" style="margin:0;font-size:26px;color:var(--primary-blue);display:flex;align-items:center;gap:10px;">
                <i class="fa-solid fa-id-card"></i> Hồ sơ thợ
            </h1>
        </div>

        <!-- Khung nội dung hồ sơ thợ tải động -->
        <div id="portfolioContent">
            <div class="pm-loading">
                <i class="fa-solid fa-spinner"></i>
                <p>Đang tải dữ liệu...</p>
            </div>
        </div>
    </div>
</div>

<script>window.CTX = "${pageContext.request.contextPath}";</script>
<script src="../../assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
<script charset="UTF-8" src="../../assets/js/admin-portfolio.js"></script>
</body>
</html>
