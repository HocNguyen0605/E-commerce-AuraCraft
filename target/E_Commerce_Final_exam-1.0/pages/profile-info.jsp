<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thông tin cá nhân</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-content.css">
</head>
<body>
<div id="header-placeholder"></div>

<main>
    <div class="container">
        <img class="avatar" src="${ctx}/assets/img/profile/avatar.png" alt="Avatar">
        <p class="info_name"><b><c:out value="${profile.name}"/></b>
            <a href="${ctx}/pages/profile-info"><i class="fa-solid fa-pencil"></i></a></p>
        <p class="email"><c:out value="${profile.email}"/></p>
        <div class="row body">
            <!--Thanh menu -->
            <div class="col-3 over_content">
                <ul class="menu_function">
                    <li class="funciton_item active"><a href="${ctx}/pages/profile-info">Thông tin cá nhân</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-orders">Đơn hàng</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-designs">Thiết kế đã lưu</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-quotes">Báo giá</a></li>
                    <li class="funciton_item">
                        <a href="#" data-logout style="color: #E74C3C;">
                            <i class="fa-solid fa-arrow-right-from-bracket"></i> Đăng xuất
                        </a>
                    </li>
                </ul>
            </div>
            <!--Màn hình hiển thị thông tin -->
            <div class="col-9 over_content">
                <div class="col-12 panel">
                    <h2 class="panel-title">Thông tin cá nhân</h2>
                    <div class="form-grid">
                        <div class="form-group"><label for="fullname">Họ và tên</label>
                            <input class="form-input" id="fullname" type="text" value="<c:out value='${profile.name}'/>"></div>
                        <div class="form-group"><label for="phone">Số điện thoại</label>
                            <input class="form-input" id="phone" type="tel" placeholder="0901 234 567" value="<c:out value='${profile.phone}'/>"></div>
                        <div class="form-group"><label for="email">Email</label>
                            <input class="form-input" id="email" type="email" value="<c:out value='${profile.email}'/>" readonly></div>
                        <div class="form-group"><label for="dob">Ngày sinh</label>
                            <input class="form-input" id="dob" type="date" value="${profile.birthday}"></div>
                    </div>
                    <div class="form-actions"><button class="btn btn-primary btn-sm">Lưu thay đổi</button></div>
                </div>

                <div class="col-12 panel">
                    <h2 class="panel-title">Địa chỉ nhận hàng</h2>
                    <c:forEach var="a" items="${addresses}">
                        <div class="address-item">
                            <div><b><c:out value="${a.receiver}"/></b> | <c:out value="${a.phone}"/>
                                <c:if test="${a.defaultAddress}"><span class="tag">Mặc định</span></c:if>
                                <p><c:out value="${a.address}"/></p></div>
                            <div><a href="#">Sửa</a> · <a href="#">Xóa</a></div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty addresses}"><p>Bạn chưa có địa chỉ nhận hàng nào.</p></c:if>
                    <div class="form-actions"><a href="#" class="btn btn-outline btn-sm">Thêm địa chỉ mới</a></div>
                </div>

            </div>
        </div>
    </div>
</main>

<div id="footer-placeholder"></div>

<script>
    const CTX = '${ctx}';
    fetch(CTX + '/components/header.html').then(r => r.text())
        .then(html => document.getElementById('header-placeholder').innerHTML = html);
    fetch(CTX + '/components/footer.html').then(r => r.text())
        .then(html => document.getElementById('footer-placeholder').innerHTML = html);
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
