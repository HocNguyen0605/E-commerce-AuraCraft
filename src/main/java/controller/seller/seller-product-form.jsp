<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Đặt tại: webapp/pages/seller/seller-product-form.jsp

  Servlet cần set (request attribute):
    shop       : Shop
    categories : List<Category> getter: id, name
    product    : null khi THÊM MỚI; khi SỬA cần getter:
                 id, name, description, price, stock, categoryId, status, images (List<String>),
                 customizable (boolean), baseType (String), baseImage (String),
                 charms (List<Charm>: id, name, price, image)
    error      : thông báo lỗi (tùy chọn)
  Mở form:  GET  /seller/products?action=form[&id=...]
  Lưu:      POST /seller/products (multipart): id rỗng = INSERT, có id = UPDATE

  Các field mới (chỉ gửi khi bật "Cho phép khách tùy chỉnh charm"):
    customizable=1, baseType, baseImage (file)
    charmId[], charmName[], charmPrice[], charmImage[] (file)  -> mỗi dòng charm là 1 bộ 4 giá trị, cùng thứ tự
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="isEdit" value="${not empty product}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kênh Thợ - ${isEdit ? 'Sửa' : 'Thêm'} Sản phẩm</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleSeller.css">
</head>
<body>
<jsp:include page="/components/header.html"/>

<main class="seller-layout container">
    <%@ include file="/components/seller-sidebar.jsp" %>

    <!-- Content -->
    <section class="seller-content">
        <form action="${ctx}/seller/products" method="POST" enctype="multipart/form-data">
            <c:if test="${isEdit}"><input type="hidden" name="id" value="${product.id}"></c:if>

            <div class="content-header">
                <h1 class="page-title">${isEdit ? 'Sửa Sản phẩm' : 'Thêm Sản phẩm'}</h1>
                <div>
                    <a href="${ctx}/seller/products" class="btn btn-outline" style="margin-right: 10px;">Hủy bỏ</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Lưu Sản phẩm</button>
                </div>
            </div>

            <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

            <div class="dashboard-section form-section">
                <div class="form-row">
                    <!-- Cột trái: Thông tin cơ bản -->
                    <div class="form-col col-main">
                        <div class="form-group">
                            <label for="name">Tên sản phẩm *</label>
                            <input type="text" id="name" name="name" class="form-control" required
                                   placeholder="Nhập tên sản phẩm (Ví dụ: Vòng tay đan dây macrame...)"
                                   value="<c:out value='${product.name}'/>">
                        </div>

                        <div class="form-group">
                            <label for="description">Mô tả chi tiết</label>
                            <textarea id="description" name="description" class="form-control"
                                      placeholder="Mô tả về chất liệu, nguồn gốc đá, ý nghĩa phong thủy, hướng dẫn bảo quản..."><c:out value="${product.description}"/></textarea>
                        </div>

                        <div class="form-row">
                            <div class="form-col">
                                <div class="form-group">
                                    <label for="price">Giá bán (VND) *</label>
                                    <input type="number" id="price" name="price" class="form-control" min="0" required placeholder="0"
                                           value="<c:out value='${product.price}'/>">
                                </div>
                            </div>
                            <div class="form-col">
                                <div class="form-group">
                                    <label for="stock">Kho hàng *</label>
                                    <input type="number" id="stock" name="stock" class="form-control" min="0" required placeholder="Số lượng"
                                           value="<c:out value='${product.stock}'/>">
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Cột phải: Hình ảnh & Phân loại -->
                    <div class="form-col col-side">
                        <div class="form-group">
                            <label>Hình ảnh sản phẩm</label>
                            <div class="image-upload-area" onclick="document.getElementById('file-upload').click()">
                                <i class="bi bi-cloud-arrow-up"></i>
                                <p>Tải ảnh lên hoặc kéo thả</p>
                                <span style="font-size: 0.8rem; color: #999;">PNG, JPG, tối đa 5MB</span>
                            </div>
                            <input type="file" id="file-upload" name="images" accept="image/png,image/jpeg" style="display: none;" multiple>

                            <div class="image-preview-list" id="preview-list">
                                <%-- Ảnh đã có trong DB (khi sửa) --%>
                                <c:forEach var="img" items="${product.images}">
                                    <c:choose>
                                        <c:when test="${fn:startsWith(img, 'http')}"><c:set var="imgSrc" value="${img}"/></c:when>
                                        <c:otherwise><c:set var="imgSrc" value="${ctx}/${img}"/></c:otherwise>
                                    </c:choose>
                                    <img src="${imgSrc}" class="image-preview" alt="Ảnh sản phẩm">
                                </c:forEach>
                            </div>
                        </div>

                        <div class="form-group">
                            <label for="categoryId">Danh mục</label>
                            <select id="categoryId" name="categoryId" class="form-control" required>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}" ${isEdit and product.categoryId eq cat.id ? 'selected' : ''}>
                                        <c:out value="${cat.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="form-group">
                            <label for="status">Trạng thái</label>
                            <select id="status" name="status" class="form-control">
                                <option value="active" ${not isEdit or product.status eq 'active' ? 'selected' : ''}>Đang bán</option>
                                <option value="hidden" ${isEdit and product.status eq 'hidden' ? 'selected' : ''}>Nháp (Ẩn khỏi cửa hàng)</option>
                            </select>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ===== Tùy chỉnh charm ===== -->
            <div class="dashboard-section form-section">
                <label class="custom-toggle">
                    <input type="checkbox" id="customizable" name="customizable" value="1" ${isEdit and product.customizable ? 'checked' : ''}>
                    Cho phép khách tùy chỉnh charm
                </label>
                <p class="custom-hint">Khách chọn charm bạn đăng và gắn lên sản phẩm này.</p>

                <fieldset id="customFields" ${isEdit and product.customizable ? '' : 'disabled'}>
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group">
                                <label for="baseType">Loại sản phẩm để gắn charm</label>
                                <select id="baseType" name="baseType" class="form-control">
                                    <option value="bracelet" ${isEdit and product.baseType eq 'bracelet' ? 'selected' : ''}>Vòng tay</option>
                                    <option value="ring"     ${isEdit and product.baseType eq 'ring' ? 'selected' : ''}>Nhẫn</option>
                                    <option value="necklace" ${isEdit and product.baseType eq 'necklace' ? 'selected' : ''}>Dây chuyền</option>
                                    <option value="other"    ${isEdit and product.baseType eq 'other' ? 'selected' : ''}>Khác</option>
                                </select>
                            </div>
                        </div>
                        <div class="form-col">
                            <div class="form-group">
                                <label>Ảnh nền để gắn charm</label>
                                <div class="base-box">
                                    <label class="thumb" title="Chọn ảnh nền">
                                        <c:if test="${isEdit and not empty product.baseImage}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(product.baseImage, 'http')}"><c:set var="baseSrc" value="${product.baseImage}"/></c:when>
                                                <c:otherwise><c:set var="baseSrc" value="${ctx}/${product.baseImage}"/></c:otherwise>
                                            </c:choose>
                                            <img src="${baseSrc}" alt="Ảnh nền">
                                        </c:if>
                                        <i class="bi bi-image"></i>
                                        <input type="file" name="baseImage" accept="image/png,image/jpeg" data-thumb>
                                    </label>
                                    <span style="font-size:.85rem; color:#999;">Nên dùng ảnh nền trong suốt (PNG), chụp thẳng, tối đa 5MB.</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="charm-head">
                        <h3>Charm khách có thể chọn</h3>
                        <button type="button" class="btn btn-outline" id="addCharm"><i class="bi bi-plus-lg"></i> Thêm charm</button>
                    </div>

                    <div id="charmList">
                        <%-- Charm đã có trong DB (khi sửa) --%>
                        <c:forEach var="ch" items="${product.charms}">
                            <c:choose>
                                <c:when test="${fn:startsWith(ch.image, 'http')}"><c:set var="chSrc" value="${ch.image}"/></c:when>
                                <c:otherwise><c:set var="chSrc" value="${ctx}/${ch.image}"/></c:otherwise>
                            </c:choose>
                            <div class="charm-row">
                                <input type="hidden" name="charmId" value="${ch.id}">
                                <label class="thumb" title="Đổi ảnh charm">
                                    <img src="${chSrc}" alt="">
                                    <i class="bi bi-image"></i>
                                    <input type="file" name="charmImage" accept="image/png,image/jpeg" data-thumb>
                                </label>
                                <input type="text" name="charmName" class="form-control" placeholder="Tên charm" required value="<c:out value='${ch.name}'/>">
                                <input type="number" name="charmPrice" class="form-control charm-price" min="0" placeholder="Giá (VND)" value="<c:out value='${ch.price}'/>">
                                <button type="button" class="btn btn-outline charm-remove">Xóa</button>
                            </div>
                        </c:forEach>
                    </div>
                    <p class="charm-empty" id="charmEmpty">Chưa có charm nào. Bấm "Thêm charm" để đăng charm đầu tiên.</p>
                </fieldset>
            </div>
        </form>
    </section>
</main>

<%-- Mẫu 1 dòng charm mới (không dùng EL/template literal vì đây là JSP) --%>
<template id="charmTpl">
    <div class="charm-row">
        <input type="hidden" name="charmId" value="">
        <label class="thumb" title="Chọn ảnh charm">
            <i class="bi bi-image"></i>
            <input type="file" name="charmImage" accept="image/png,image/jpeg" data-thumb required>
        </label>
        <input type="text" name="charmName" class="form-control" placeholder="Tên charm" required>
        <input type="number" name="charmPrice" class="form-control charm-price" min="0" placeholder="Giá (VND)">
        <button type="button" class="btn btn-outline charm-remove">Xóa</button>
    </div>
</template>

<jsp:include page="/components/footer.html"/>
<script>
    var MAX_BYTES = 5 * 1024 * 1024;

    // Xem trước ảnh sản phẩm mới chọn
    document.getElementById('file-upload').addEventListener('change', function () {
        var list = document.getElementById('preview-list');
        Array.from(this.files).forEach(function (f) {
            if (f.size > MAX_BYTES) { alert(f.name + ' vượt quá 5MB'); return; }
            var img = document.createElement('img');
            img.className = 'image-preview';
            img.src = URL.createObjectURL(f);
            list.appendChild(img);
        });
    });

    // Bật/tắt phần tùy chỉnh: fieldset disabled thì không gửi và không bắt buộc nhập
    var toggle = document.getElementById('customizable');
    var fields = document.getElementById('customFields');
    toggle.addEventListener('change', function () { fields.disabled = !toggle.checked; });

    var list = document.getElementById('charmList');
    var empty = document.getElementById('charmEmpty');
    function refreshEmpty() { empty.style.display = list.children.length ? 'none' : ''; }

    document.getElementById('addCharm').addEventListener('click', function () {
        list.appendChild(document.getElementById('charmTpl').content.cloneNode(true));
        refreshEmpty();
    });

    // Xóa dòng và xem trước ảnh (ủy quyền sự kiện cho cả dòng thêm động)
    fields.addEventListener('click', function (e) {
        var btn = e.target.closest('.charm-remove');
        if (btn) { btn.closest('.charm-row').remove(); refreshEmpty(); }
    });
    fields.addEventListener('change', function (e) {
        var input = e.target;
        if (!input.matches('input[data-thumb]') || !input.files[0]) return;
        var f = input.files[0];
        if (f.size > MAX_BYTES) { alert(f.name + ' vượt quá 5MB'); input.value = ''; return; }
        var box = input.closest('.thumb');
        var img = box.querySelector('img');
        if (!img) { img = document.createElement('img'); box.insertBefore(img, box.firstChild); }
        img.src = URL.createObjectURL(f);
    });
    refreshEmpty();
</script>
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>
