<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:choose>
    <c:when test="${empty shop.avatar}"><c:set var="avatarSrc" value=""/></c:when>
    <c:when test="${fn:startsWith(shop.avatar, 'http')}"><c:set var="avatarSrc" value="${shop.avatar}"/></c:when>
    <c:otherwise><c:set var="avatarSrc" value="${ctx}/${shop.avatar}"/></c:otherwise>
</c:choose>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cài đặt shop - Kênh Thợ</title>
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleSeller.css">
</head>
<body>
<jsp:include page="/components/header.html"/>

<main class="seller-layout container">
    <c:set var="sellerTab" value="settings" scope="request"/>
    <%@ include file="/components/seller-sidebar.jsp" %>

    <section class="seller-content">
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title">Cài đặt shop</h1>
        </div>

        <div class="dashboard-section">
            <form method="post" action="${ctx}/seller/settings" enctype="multipart/form-data" id="settingsForm">
                <div class="settings-grid">

                    <!-- Ảnh đại diện + thông tin chỉ đọc -->
                    <div class="avatar-box">
                        <c:choose>
                            <c:when test="${not empty avatarSrc}">
                                <img id="avatarPreview" class="avatar-preview" src="<c:out value='${avatarSrc}'/>" alt="Ảnh đại diện shop"
                                     onerror="this.style.display='none'; document.getElementById('avatarFallback').style.display='flex';">
                                <div id="avatarFallback" class="avatar-placeholder" style="display:none;"><i class="fa-solid fa-store"></i></div>
                            </c:when>
                            <c:otherwise>
                                <img id="avatarPreview" class="avatar-preview" src="" alt="" style="display:none;">
                                <div id="avatarFallback" class="avatar-placeholder"><i class="fa-solid fa-store"></i></div>
                            </c:otherwise>
                        </c:choose>
                        <label for="avatarInput" class="btn btn-outline" style="cursor:pointer;">
                            <i class="fa-solid fa-camera"></i> Đổi ảnh
                        </label>
                        <input type="file" id="avatarInput" name="avatar" accept="image/png,image/jpeg,image/webp" style="display:none;">
                        <small>JPG, PNG hoặc WEBP, tối đa 2MB</small>

                        <div class="info-box" style="text-align:left;">
                            <h4>Thông tin sàn</h4>
                            <div class="info-line"><span>Trạng thái</span><strong>Đang hoạt động</strong></div>
                            <div class="info-line"><span>Đánh giá</span><strong><fmt:formatNumber value="${shop.rating}" pattern="0.0"/> / 5</strong></div>
                            <div class="info-line"><span>Hoa hồng sàn</span><strong><fmt:formatNumber value="${shop.commissionRate}" pattern="0.##"/>%</strong></div>
                        </div>
                    </div>

                    <!-- Form -->
                    <div class="settings-form">
                        <div class="form-row">
                            <label for="shopName">Tên shop <span style="color:#c00;">*</span></label>
                            <input type="text" id="shopName" name="name" maxlength="150" required
                                   value="<c:out value='${shop.name}'/>">
                        </div>

                        <div class="form-two">
                            <div class="form-row">
                                <label for="shopPhone">Số điện thoại</label>
                                <input type="tel" id="shopPhone" name="phone" maxlength="20" placeholder="0901234567"
                                       value="<c:out value='${shop.phone}'/>">
                            </div>
                            <div class="form-row">
                                <label for="shopEmail">Email liên hệ</label>
                                <input type="email" id="shopEmail" name="email" maxlength="150"
                                       value="<c:out value='${shop.email}'/>">
                            </div>
                        </div>

                        <div class="form-row">
                            <label for="shopAddress">Địa chỉ</label>
                            <input type="text" id="shopAddress" name="address" maxlength="500"
                                   placeholder="Số nhà, đường, quận/huyện, tỉnh/thành"
                                   value="<c:out value='${shop.address}'/>">
                        </div>

                        <div class="form-row">
                            <label for="shopDesc">Giới thiệu shop</label>
                            <textarea id="shopDesc" name="description" maxlength="2000"
                                      placeholder="Shop chuyên làm gì, chất liệu, phong cách..."><c:out value="${shop.description}"/></textarea>
                            <div class="hint"><span id="descCount">0</span>/2000 ký tự</div>
                        </div>

                        <div class="form-actions">
                            <button type="submit" class="btn btn-primary"><i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi</button>
                            <a href="${ctx}/seller/settings" class="btn btn-outline">Hủy</a>
                        </div>
                    </div>
                </div>
            </form>
        </div>

        <!-- Tài khoản: đăng xuất -->
        <div class="dashboard-section account-section">
            <div class="account-row">
                <div>
                    <h3>Tài khoản</h3>
                    <p>Đăng xuất khỏi tài khoản người bán trên thiết bị này.</p>
                </div>
                <a href="${ctx}/logout" class="btn-logout" id="logoutBtn">
                    <i class="fa-solid fa-right-from-bracket"></i> Đăng xuất
                </a>
            </div>
        </div>
    </section>
</main>

<jsp:include page="/components/footer.html"/>

<script>
    // Xem trước ảnh đại diện + kiểm tra dung lượng phía trình duyệt
    var input = document.getElementById('avatarInput');
    input.addEventListener('change', function () {
        var f = input.files[0];
        if (!f) return;
        if (f.size > 2 * 1024 * 1024) { alert('Ảnh quá lớn, tối đa 2MB.'); input.value = ''; return; }
        var img = document.getElementById('avatarPreview');
        img.src = URL.createObjectURL(f);
        img.style.display = 'block';
        document.getElementById('avatarFallback').style.display = 'none';
    });

    var desc = document.getElementById('shopDesc'), cnt = document.getElementById('descCount');
    function upd() { cnt.textContent = desc.value.length; }
    desc.addEventListener('input', upd); upd();

    // Xác nhận trước khi đăng xuất
    document.getElementById('logoutBtn').addEventListener('click', function (e) {
        if (!confirm('Bạn có chắc muốn đăng xuất?')) e.preventDefault();
    });
</script>
<script src="${ctx}/assets/js/popup.js"></script>
<script src="${ctx}/assets/js/auth.js"></script>
</body>
</html>
