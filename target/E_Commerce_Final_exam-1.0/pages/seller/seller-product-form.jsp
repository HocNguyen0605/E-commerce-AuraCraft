<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Đặt tại: webapp/pages/seller/seller-product-form.jsp

  Servlet set (request attribute):
    shop, categories, sellerTab
    editing     : true khi SỬA, false khi THÊM
    editId      : id sản phẩm đang sửa
    product     : sản phẩm gốc khi SỬA (getter: id, name, description, price, stock, categoryId, status,
                  images, customizable, baseType, baseImage, charms)
    --- chỉ có khi lưu bị lỗi (để giữ lại dữ liệu đã nhập) ---
    f           : Map giá trị người dùng đã nhập
    fieldErrors : Map tên-trường -> thông báo lỗi
    formCharms  : danh sách charm đã nhập (id, name, price, image, imageUrl, error)
    error       : thông báo lỗi chung

  Mở form:  GET  /pages/seller/seller-products?action=form[&id=...]
  Lưu:      POST /pages/seller/seller-products (multipart)
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="productsUrl" value="${ctx}/pages/seller/seller-products"/>
<c:set var="isEdit" value="${empty editing ? not empty product : editing}"/>
<c:set var="hasForm" value="${not empty f}"/>
<c:set var="hasBase" value="${isEdit and not empty product.baseImage}"/>
<c:set var="fe" value="${fieldErrors}"/>

<%-- Giá trị hiển thị: ưu tiên dữ liệu người dùng vừa nhập (khi lỗi), nếu không thì lấy từ sản phẩm --%>
<c:set var="vName"      value="${hasForm ? f.name : product.name}"/>
<c:set var="vDesc"      value="${hasForm ? f.description : product.description}"/>
<c:set var="vPrice"     value="${hasForm ? f.price : product.price}"/>
<c:set var="vStock"     value="${hasForm ? f.stock : product.stock}"/>
<c:set var="vCat"       value="${hasForm ? f.categoryId : product.categoryId}"/>
<c:set var="vStatus"    value="${hasForm ? f.status : (isEdit ? product.status : 'active')}"/>
<c:set var="vCustom"    value="${hasForm ? f.customizable eq '1' : (isEdit and product.customizable)}"/>
<c:set var="vBaseType"  value="${hasForm ? f.baseType : product.baseType}"/>
<c:set var="vMainUrl"   value="${hasForm ? f.mainImageUrl : ''}"/>
<c:set var="vExtraUrls" value="${hasForm ? f.extraImageUrls : ''}"/>
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

<main class="seller-layout container">
    <c:set var="sellerTab" value="products" scope="request"/>
    <%@ include file="/components/seller-sidebar.jsp" %>

    <!-- Content -->
    <section class="seller-content">
        <%-- novalidate: để trang tự kiểm tra và đánh dấu đỏ thay vì bong bóng mặc định của trình duyệt --%>
        <form id="productForm" action="${productsUrl}" method="POST" enctype="multipart/form-data" novalidate>
            <c:if test="${isEdit}"><input type="hidden" name="id" value="<c:out value='${not empty editId ? editId : product.id}'/>"></c:if>

            <div class="content-header">
                <h1 class="page-title">${isEdit ? 'Sửa Sản phẩm' : 'Thêm Sản phẩm'}</h1>
                <div>
                    <a href="${productsUrl}" class="btn btn-outline" style="margin-right: 10px;">Hủy bỏ</a>
                    <button type="submit" class="btn btn-primary"><i class="bi bi-check-lg"></i> Lưu Sản phẩm</button>
                </div>
            </div>

            <div id="formAlert" class="alert alert-danger" ${empty error ? 'style="display:none"' : ''}>
                <i class="bi bi-exclamation-triangle-fill"></i>
                <span id="formAlertText"><c:out value="${error}"/></span>
            </div>

            <div class="dashboard-section form-section">
                <div class="form-row">
                    <!-- Cột trái: Thông tin cơ bản -->
                    <div class="form-col col-main">
                        <div class="form-group ${not empty fe.name ? 'has-error' : ''}" data-field="name">
                            <label for="name">Tên sản phẩm <span class="req">*</span></label>
                            <input type="text" id="name" name="name" class="form-control"
                                   placeholder="Nhập tên sản phẩm (Ví dụ: Vòng tay đan dây macrame...)"
                                   value="<c:out value='${vName}'/>">
                            <div class="field-error"><c:out value="${fe.name}"/></div>
                        </div>

                        <div class="form-group" data-field="description">
                            <label for="description">Mô tả chi tiết</label>
                            <textarea id="description" name="description" class="form-control desc-box" rows="12"
                                      placeholder="Mô tả về chất liệu, nguồn gốc đá, ý nghĩa phong thủy, hướng dẫn bảo quản..."><c:out value="${vDesc}"/></textarea>
                            <div class="field-error"></div>
                        </div>

                        <div class="form-row">
                            <div class="form-col">
                                <div class="form-group ${not empty fe.price ? 'has-error' : ''}" data-field="price">
                                    <label for="price">Giá bán (VND) <span class="req">*</span></label>
                                    <input type="number" id="price" name="price" class="form-control" min="0" placeholder="0"
                                           value="<c:out value='${vPrice}'/>">
                                    <div class="field-error"><c:out value="${fe.price}"/></div>
                                </div>
                            </div>
                            <div class="form-col">
                                <div class="form-group ${not empty fe.stock ? 'has-error' : ''}" data-field="stock">
                                    <label for="stock">Kho hàng <span class="req">*</span></label>
                                    <input type="number" id="stock" name="stock" class="form-control" min="0" placeholder="Số lượng"
                                           value="<c:out value='${vStock}'/>">
                                    <div class="field-error"><c:out value="${fe.stock}"/></div>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- Cột phải: Hình ảnh & Phân loại -->
                    <div class="form-col col-side">

                        <%-- Ảnh hiện có (khi sửa): ảnh đầu tiên là ảnh chính. Bấm × để đánh dấu xóa (checkbox ẩn name=removeImage) --%>
                        <c:if test="${not empty product.images}">
                            <div class="form-group ${not empty fe.existingImages ? 'has-error' : ''}" data-field="existingImages">
                                <label>Ảnh hiện có</label>
                                <div class="image-preview-list" id="existing-images">
                                    <c:forEach var="img" items="${product.images}" varStatus="st">
                                        <c:choose>
                                            <c:when test="${fn:startsWith(img, 'http')}"><c:set var="imgSrc" value="${img}"/></c:when>
                                            <c:otherwise><c:set var="imgSrc" value="${ctx}/${img}"/></c:otherwise>
                                        </c:choose>
                                        <div class="image-preview-item">
                                            <img src="${imgSrc}" class="image-preview" alt="Ảnh sản phẩm">
                                            <span class="tag-main ${st.first ? 'is-main' : ''}">${st.first ? 'Ảnh chính' : 'Ảnh phụ'}</span>
                                            <input type="checkbox" name="removeImage" value="<c:out value='${img}'/>" hidden
                                                ${not empty removedImages and removedImages.contains(img) ? 'checked' : ''}>
                                            <button type="button" class="preview-remove existing-remove" title="Xóa ảnh">&times;</button>
                                        </div>
                                    </c:forEach>
                                </div>
                                <p class="img-note">Bấm × để xóa ảnh (bấm lại để hoàn tác). Ảnh chỉ bị xóa thật khi bạn bấm "Lưu Sản phẩm". Ảnh tải thêm bên dưới sẽ được thêm vào các ảnh còn lại.</p>
                                <div class="field-error"><c:out value="${fe.existingImages}"/></div>
                            </div>
                        </c:if>

                        <%-- Ảnh chính: tải lên HOẶC dán link --%>
                        <div class="form-group ${not empty fe.mainImage ? 'has-error' : ''}" data-field="mainImage">
                            <label>Ảnh chính <c:if test="${not isEdit}"><span class="req">*</span></c:if></label>
                            <div class="image-upload-area compact" onclick="document.getElementById('main-upload').click()">
                                <i class="bi bi-cloud-arrow-up"></i>
                                <p>Tải ảnh chính lên</p>
                                <span style="font-size: 0.8rem; color: #999;">PNG, JPG, tối đa 5MB</span>
                            </div>
                            <input type="file" id="main-upload" name="mainImage" accept="image/png,image/jpeg" style="display: none;">
                            <div class="image-preview-list" id="main-preview"></div>

                            <div class="img-or">hoặc dán link ảnh</div>
                            <input type="text" id="mainImageUrl" name="mainImageUrl" class="form-control"
                                   placeholder="https://example.com/anh-chinh.jpg" value="<c:out value='${vMainUrl}'/>">
                            <p class="img-note">Nếu chọn cả hai, ảnh tải lên sẽ được ưu tiên.</p>
                            <div class="field-error"><c:out value="${fe.mainImage}"/></div>
                        </div>

                        <%-- Ảnh phụ: tải nhiều ảnh HOẶC dán nhiều link --%>
                        <div class="form-group ${not empty fe.extraImages ? 'has-error' : ''}" data-field="extraImages">
                            <label>Ảnh phụ (tối đa 8)</label>
                            <div class="image-upload-area compact" onclick="document.getElementById('file-upload').click()">
                                <i class="bi bi-images"></i>
                                <p>Tải các ảnh phụ lên</p>
                                <span style="font-size: 0.8rem; color: #999;">PNG, JPG, tối đa 5MB mỗi ảnh</span>
                            </div>
                            <input type="file" id="file-upload" name="images" accept="image/png,image/jpeg" style="display: none;" multiple>
                            <div class="image-preview-list" id="preview-list"></div>

                            <div class="img-or">hoặc dán link ảnh (mỗi dòng một link)</div>
                            <textarea id="extraImageUrls" name="extraImageUrls" class="form-control" rows="3"
                                      placeholder="https://example.com/anh-phu-1.jpg&#10;https://example.com/anh-phu-2.jpg"><c:out value="${vExtraUrls}"/></textarea>
                            <div class="field-error"><c:out value="${fe.extraImages}"/></div>
                        </div>

                        <div class="form-group ${not empty fe.categoryId ? 'has-error' : ''}" data-field="categoryId">
                            <label for="categoryId">Danh mục <span class="req">*</span></label>
                            <select id="categoryId" name="categoryId" class="form-control">
                                <option value="">-- Chọn danh mục --</option>
                                <c:forEach var="cat" items="${categories}">
                                    <option value="${cat.id}" ${vCat eq cat.id ? 'selected' : ''}>
                                        <c:out value="${cat.name}"/>
                                    </option>
                                </c:forEach>
                            </select>
                            <div class="field-error"><c:out value="${fe.categoryId}"/></div>
                        </div>

                        <div class="form-group" data-field="status">
                            <label for="status">Trạng thái</label>
                            <select id="status" name="status" class="form-control">
                                <option value="active" ${vStatus ne 'hidden' ? 'selected' : ''}>Đang bán</option>
                                <option value="hidden" ${vStatus eq 'hidden' ? 'selected' : ''}>Nháp (Ẩn khỏi cửa hàng)</option>
                            </select>
                            <div class="field-error"></div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- ===== Tùy chỉnh charm ===== -->
            <div class="dashboard-section form-section">
                <label class="custom-toggle">
                    <input type="checkbox" id="customizable" name="customizable" value="1" ${vCustom ? 'checked' : ''}>
                    Cho phép khách tùy chỉnh charm
                </label>
                <p class="custom-hint">Khách chọn charm bạn đăng và gắn lên sản phẩm này.</p>

                <fieldset id="customFields" ${vCustom ? '' : 'disabled'}>
                    <div class="form-row">
                        <div class="form-col">
                            <div class="form-group ${not empty fe.baseType ? 'has-error' : ''}" data-field="baseType">
                                <label for="baseType">Loại sản phẩm để gắn charm</label>
                                <select id="baseType" name="baseType" class="form-control">
                                    <option value="bracelet" ${vBaseType eq 'bracelet' ? 'selected' : ''}>Vòng tay</option>
                                    <option value="ring"     ${vBaseType eq 'ring' ? 'selected' : ''}>Nhẫn</option>
                                    <option value="necklace" ${vBaseType eq 'necklace' ? 'selected' : ''}>Dây chuyền</option>
                                    <option value="other"    ${vBaseType eq 'other' ? 'selected' : ''}>Khác</option>
                                </select>
                                <div class="field-error"><c:out value="${fe.baseType}"/></div>
                            </div>
                        </div>
                        <div class="form-col">
                            <div class="form-group ${not empty fe.baseImage ? 'has-error' : ''}" data-field="baseImage">
                                <span class="group-label">Ảnh nền để gắn charm <c:if test="${not hasBase}"><span class="req">*</span></c:if></span>
                                <div class="base-box">
                                    <label class="thumb" title="Chọn ảnh nền">
                                        <c:if test="${hasBase}">
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
                                <div class="field-error"><c:out value="${fe.baseImage}"/></div>
                            </div>
                        </div>
                    </div>

                    <div class="charm-block ${not empty fe.charms ? 'has-error' : ''}" data-field="charms">
                        <div class="charm-head">
                            <h3>Charm khách có thể chọn</h3>
                            <button type="button" class="btn btn-outline" id="addCharm"><i class="bi bi-plus-lg"></i> Thêm charm</button>
                        </div>
                        <div class="field-error"><c:out value="${fe.charms}"/></div>

                        <div id="charmList">
                            <c:choose>
                                <%-- Lưu lỗi: đổ lại đúng những gì người dùng đã nhập --%>
                                <c:when test="${hasForm}">
                                    <c:forEach var="ch" items="${formCharms}">
                                        <c:set var="chSrc" value=""/>
                                        <c:if test="${not empty ch.image}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(ch.image, 'http')}"><c:set var="chSrc" value="${ch.image}"/></c:when>
                                                <c:otherwise><c:set var="chSrc" value="${ctx}/${ch.image}"/></c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        <div class="charm-row ${not empty ch.error ? 'has-error' : ''}" data-existing="<c:out value='${chSrc}'/>">
                                            <input type="hidden" name="charmId" value="${ch.id gt 0 ? ch.id : ''}">
                                            <div class="thumb-wrap">
                                                <label class="thumb" title="Chọn ảnh charm">
                                                    <i class="bi bi-image"></i>
                                                    <input type="file" name="charmImage" accept="image/png,image/jpeg" data-thumb>
                                                </label>
                                                <button type="button" class="thumb-remove" title="Xóa ảnh" hidden>&times;</button>
                                            </div>
                                            <input type="text" name="charmName" class="form-control" placeholder="Tên charm" value="<c:out value='${ch.name}'/>">
                                            <input type="number" name="charmPrice" class="form-control charm-price" min="0" placeholder="Giá (VND)" value="<c:out value='${ch.price}'/>">
                                            <input type="text" name="charmImageUrl" class="form-control charm-url" placeholder="hoặc dán link ảnh" value="<c:out value='${ch.imageUrl}'/>">
                                            <button type="button" class="btn btn-outline charm-remove">Xóa</button>
                                            <div class="field-error"><c:out value="${ch.error}"/></div>
                                        </div>
                                    </c:forEach>
                                </c:when>
                                <%-- Mở form sửa: charm đã có trong DB --%>
                                <c:otherwise>
                                    <c:forEach var="ch" items="${product.charms}">
                                        <c:set var="chSrc" value=""/>
                                        <c:if test="${not empty ch.image}">
                                            <c:choose>
                                                <c:when test="${fn:startsWith(ch.image, 'http')}"><c:set var="chSrc" value="${ch.image}"/></c:when>
                                                <c:otherwise><c:set var="chSrc" value="${ctx}/${ch.image}"/></c:otherwise>
                                            </c:choose>
                                        </c:if>
                                        <div class="charm-row" data-existing="<c:out value='${chSrc}'/>">
                                            <input type="hidden" name="charmId" value="${ch.id}">
                                            <div class="thumb-wrap">
                                                <label class="thumb" title="Đổi ảnh charm">
                                                    <i class="bi bi-image"></i>
                                                    <input type="file" name="charmImage" accept="image/png,image/jpeg" data-thumb>
                                                </label>
                                                <button type="button" class="thumb-remove" title="Xóa ảnh" hidden>&times;</button>
                                            </div>
                                            <input type="text" name="charmName" class="form-control" placeholder="Tên charm" value="<c:out value='${ch.name}'/>">
                                            <input type="number" name="charmPrice" class="form-control charm-price" min="0" placeholder="Giá (VND)" value="<c:out value='${ch.price}'/>">
                                            <input type="text" name="charmImageUrl" class="form-control charm-url" placeholder="hoặc dán link ảnh" value="">
                                            <button type="button" class="btn btn-outline charm-remove">Xóa</button>
                                            <div class="field-error"></div>
                                        </div>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <p class="charm-empty" id="charmEmpty">Chưa có charm nào. Bấm "Thêm charm" để đăng charm đầu tiên.</p>
                    </div>
                </fieldset>
            </div>
        </form>
    </section>
</main>

<%-- Mẫu 1 dòng charm mới (không dùng EL/template literal vì đây là JSP) --%>
<template id="charmTpl">
    <div class="charm-row" data-existing="">
        <input type="hidden" name="charmId" value="">
        <div class="thumb-wrap">
            <label class="thumb" title="Chọn ảnh charm">
                <i class="bi bi-image"></i>
                <input type="file" name="charmImage" accept="image/png,image/jpeg" data-thumb>
            </label>
            <button type="button" class="thumb-remove" title="Xóa ảnh" hidden>&times;</button>
        </div>
        <input type="text" name="charmName" class="form-control" placeholder="Tên charm">
        <input type="number" name="charmPrice" class="form-control charm-price" min="0" placeholder="Giá (VND)">
        <input type="text" name="charmImageUrl" class="form-control charm-url" placeholder="hoặc dán link ảnh" value="">
        <button type="button" class="btn btn-outline charm-remove">Xóa</button>
        <div class="field-error"></div>
    </div>
</template>

<jsp:include page="/components/footer.html"/>
<script>
    (function () {
        var MAX_BYTES = 5 * 1024 * 1024;
        var MAX_EXTRA = 8;
        var IS_EDIT = ${isEdit ? 'true' : 'false'};
        var HAS_BASE = ${hasBase ? 'true' : 'false'};
        var URL_RE = /^https?:\/\/\S+$/i;

        var form = document.getElementById('productForm');
        var alertBox = document.getElementById('formAlert');
        var alertText = document.getElementById('formAlertText');

        // ---------- tiện ích ----------
        function val(name) { var el = form.elements[name]; return el ? (el.value || '').trim() : ''; }
        function groupOf(field) { return form.querySelector('[data-field="' + field + '"]'); }
        function setError(field, msg) {
            var g = groupOf(field);
            if (!g) return;
            var box = g.querySelector('.field-error');
            g.classList.toggle('has-error', !!msg);
            if (box) box.textContent = msg || '';
        }
        function setRowError(row, msg) {
            row.classList.toggle('has-error', !!msg);
            var b = row.querySelector('.field-error');
            if (b) b.textContent = msg || '';
        }
        function fileError(f) {
            if (!/\.(png|jpe?g)$/i.test(f.name)) return 'Ảnh phải là PNG hoặc JPG.';
            if (f.size > MAX_BYTES) return 'Ảnh vượt quá 5MB.';
            return '';
        }
        function urlError(u) {
            if (u.length > 255) return 'Link tối đa 255 ký tự.';
            if (!URL_RE.test(u)) return 'Link ảnh phải bắt đầu bằng http:// hoặc https://';
            return '';
        }
        function short(s) { return s.length > 50 ? s.slice(0, 50) + '…' : s; }
        function showAlert(msg) { alertText.textContent = msg; alertBox.style.display = ''; }
        function scrollToFirstError() {
            var first = form.querySelector('.has-error');
            if (first) first.scrollIntoView({ behavior: 'smooth', block: 'center' });
        }

        // ---------- kiểm tra trước khi gửi (không mất dữ liệu, không mất file đã chọn) ----------
        function validate() {
            function chk(field, msg) { setError(field, msg || ''); }

            chk('name', val('name') ? '' : 'Vui lòng nhập tên sản phẩm.');
            var price = val('price');
            chk('price', (price !== '' && /^\d+$/.test(price)) ? '' : 'Giá bán không hợp lệ (số nguyên từ 0).');
            var stock = val('stock');
            chk('stock', (stock !== '' && /^\d+$/.test(stock)) ? '' : 'Số lượng kho không hợp lệ (số nguyên từ 0).');
            chk('categoryId', val('categoryId') ? '' : 'Vui lòng chọn danh mục.');

            // Ảnh chính
            var mainMsg = '';
            var mainFile = form.elements['mainImage'].files[0];
            if (mainFile) mainMsg = fileError(mainFile);
            else if (val('mainImageUrl')) mainMsg = urlError(val('mainImageUrl'));
            else if (!IS_EDIT) mainMsg = 'Vui lòng chọn ảnh chính (tải lên hoặc dán link).';
            chk('mainImage', mainMsg);

            // Ảnh phụ
            var extraMsg = '';
            var extraFiles = form.elements['images'].files;
            for (var i = 0; i < extraFiles.length && !extraMsg; i++) {
                var m = fileError(extraFiles[i]);
                if (m) extraMsg = extraFiles[i].name + ': ' + m;
            }
            var lines = (form.elements['extraImageUrls'].value || '').split(/\r?\n/).map(function (s) { return s.trim(); })
                .filter(function (s) { return s; });
            for (var j = 0; j < lines.length && !extraMsg; j++) {
                var um = urlError(lines[j]);
                if (um) extraMsg = 'Link ảnh phụ không hợp lệ: ' + short(lines[j]) + ' (' + um + ')';
            }
            if (!extraMsg && extraFiles.length + lines.length > MAX_EXTRA) {
                extraMsg = 'Tối đa ' + MAX_EXTRA + ' ảnh phụ mỗi lần lưu (hiện có ' + (extraFiles.length + lines.length) + ').';
            }
            chk('extraImages', extraMsg);

            // Khi sửa: sau khi xóa phải còn ít nhất 1 ảnh
            if (IS_EDIT && groupOf('existingImages')) {
                var kept = form.querySelectorAll('#existing-images input[name="removeImage"]:not(:checked)').length;
                var added = (mainFile || val('mainImageUrl') ? 1 : 0) + extraFiles.length + lines.length;
                chk('existingImages', (kept + added) === 0
                    ? 'Sản phẩm cần có ít nhất 1 ảnh. Hãy giữ lại một ảnh hoặc thêm ảnh mới.' : '');
            }

            // Charm (chỉ khi bật tùy chỉnh)
            if (form.elements['customizable'].checked) {
                var bf = form.elements['baseImage'].files[0];
                var baseMsg = bf ? fileError(bf) : (HAS_BASE ? '' : 'Vui lòng tải ảnh nền để gắn charm.');
                chk('baseImage', baseMsg);

                var rowsBad = false;
                var rows = document.querySelectorAll('#charmList .charm-row');
                Array.prototype.forEach.call(rows, function (row) {
                    var msg = '';
                    var cname = row.querySelector('[name="charmName"]').value.trim();
                    var cprice = row.querySelector('[name="charmPrice"]').value.trim();
                    var curl = row.querySelector('[name="charmImageUrl"]').value.trim();
                    var fileIn = row.querySelector('[name="charmImage"]');
                    var picked = fileIn.files && fileIn.files[0];
                    var hasImg = picked || curl || row.getAttribute('data-existing');
                    if (!cname) msg = 'Vui lòng nhập tên charm.';
                    else if (cprice !== '' && !/^\d+$/.test(cprice)) msg = 'Giá charm không hợp lệ.';
                    else if (picked && fileError(picked)) msg = fileError(picked);
                    else if (!picked && curl && urlError(curl)) msg = urlError(curl);
                    else if (!hasImg) msg = 'Vui lòng chọn ảnh hoặc dán link ảnh cho charm.';
                    setRowError(row, msg);
                    if (msg) rowsBad = true;
                });
                chk('charms', rowsBad ? 'Một số charm chưa hợp lệ, vui lòng kiểm tra các dòng được đánh dấu đỏ.' : '');
            } else {
                chk('baseImage', ''); chk('charms', ''); chk('baseType', '');
            }
            return !form.querySelector('.has-error');
        }

        form.addEventListener('submit', function (e) {
            if (!validate()) {
                e.preventDefault();
                showAlert('Vui lòng kiểm tra lại các mục được đánh dấu đỏ bên dưới.');
                scrollToFirstError();
            }
        });

        // Sửa ô nào thì bỏ đánh dấu lỗi ô đó
        function clearOnEdit(e) {
            if (e.target.type === 'file') return;      // file đã được kiểm tra trong handler riêng
            var row = e.target.closest('.charm-row');
            if (row) { setRowError(row, ''); return; }
            var g = e.target.closest('[data-field]');
            if (g) {
                g.classList.remove('has-error');
                var b = g.querySelector('.field-error');
                if (b) b.textContent = '';
            }
        }
        form.addEventListener('input', clearOnEdit);
        form.addEventListener('change', clearOnEdit);

        // ---------- xem trước ảnh (có nút xóa) ----------
        function makeItem(src, label, onRemove) {
            var item = document.createElement('div');
            item.className = 'image-preview-item';
            var img = document.createElement('img');
            img.className = 'image-preview';
            img.alt = '';
            img.onerror = function () { item.classList.add('broken'); };
            img.src = src;
            var tag = document.createElement('span');
            tag.className = 'tag-main';
            tag.textContent = label;
            var bad = document.createElement('span');
            bad.className = 'img-broken';
            bad.textContent = 'Không tải được ảnh';
            var rm = document.createElement('button');
            rm.type = 'button';
            rm.className = 'preview-remove';
            rm.title = 'Xóa ảnh';
            rm.innerHTML = '&times;';
            rm.addEventListener('click', onRemove);
            item.appendChild(img); item.appendChild(bad); item.appendChild(tag); item.appendChild(rm);
            return item;
        }
        function debounce(fn, ms) { var t; return function () { clearTimeout(t); t = setTimeout(fn, ms); }; }

        // --- Ảnh hiện có: bấm × để đánh dấu xóa / hoàn tác ---
        var existingBox = document.getElementById('existing-images');
        function syncExisting(item) {
            var cb = item.querySelector('input[name="removeImage"]');
            var btn = item.querySelector('.existing-remove');
            item.classList.toggle('removed', cb.checked);
            btn.innerHTML = cb.checked ? '&#8634;' : '&times;';
            btn.title = cb.checked ? 'Hoàn tác' : 'Xóa ảnh';
        }
        if (existingBox) {
            Array.prototype.forEach.call(existingBox.querySelectorAll('.image-preview-item'), syncExisting);
            existingBox.addEventListener('click', function (e) {
                var btn = e.target.closest('.existing-remove');
                if (!btn) return;
                var item = btn.closest('.image-preview-item');
                var cb = item.querySelector('input[name="removeImage"]');
                cb.checked = !cb.checked;
                syncExisting(item);
                setError('existingImages', '');
            });
        }

        // --- Ảnh chính ---
        var mainInput = document.getElementById('main-upload');
        var mainUrlInput = document.getElementById('mainImageUrl');
        var mainBox = document.getElementById('main-preview');
        function renderMain() {
            mainBox.innerHTML = '';
            var f = mainInput.files[0];
            if (f) mainBox.appendChild(makeItem(URL.createObjectURL(f), 'Tải lên', function () {
                mainInput.value = ''; setError('mainImage', ''); renderMain();
            }));
            var u = mainUrlInput.value.trim();
            if (u && !urlError(u)) mainBox.appendChild(makeItem(u, 'Link', function () {
                mainUrlInput.value = ''; setError('mainImage', ''); renderMain();
            }));
        }
        mainInput.addEventListener('change', function () {
            var f = this.files[0];
            if (f) {
                var m = fileError(f);
                setError('mainImage', m);
                if (m) this.value = '';
            }
            renderMain();
        });
        mainUrlInput.addEventListener('input', debounce(renderMain, 300));

        // --- Ảnh phụ: cộng dồn nhiều lần chọn, xóa được từng ảnh ---
        var extraInput = document.getElementById('file-upload');
        var extraUrlsEl = document.getElementById('extraImageUrls');
        var extraBox = document.getElementById('preview-list');
        var extraDT = new DataTransfer();
        function renderExtra() {
            extraBox.innerHTML = '';
            Array.prototype.forEach.call(extraDT.files, function (f, i) {
                extraBox.appendChild(makeItem(URL.createObjectURL(f), 'Tải lên', function () {
                    var dt = new DataTransfer();
                    Array.prototype.forEach.call(extraDT.files, function (x, j) { if (j !== i) dt.items.add(x); });
                    extraDT = dt;
                    extraInput.files = extraDT.files;
                    renderExtra();
                }));
            });
            extraUrlsEl.value.split(/\r?\n/).forEach(function (line, idx) {
                var u = line.trim();
                if (!u || urlError(u)) return;
                extraBox.appendChild(makeItem(u, 'Link', function () {
                    var arr = extraUrlsEl.value.split(/\r?\n/);
                    arr.splice(idx, 1);
                    extraUrlsEl.value = arr.join('\n');
                    renderExtra();
                }));
            });
        }
        extraInput.addEventListener('change', function () {
            var msg = '';
            Array.prototype.forEach.call(this.files, function (f) {
                var m = fileError(f);
                if (m) { msg = msg || (f.name + ': ' + m); return; }
                extraDT.items.add(f);
            });
            extraInput.files = extraDT.files;      // giữ lại cả các ảnh đã chọn trước đó
            setError('extraImages', msg);
            renderExtra();
        });
        extraUrlsEl.addEventListener('input', debounce(renderExtra, 300));

        // ---------- tùy chỉnh charm ----------
        var toggle = document.getElementById('customizable');
        var fields = document.getElementById('customFields');
        toggle.addEventListener('change', function () { fields.disabled = !toggle.checked; });

        var list = document.getElementById('charmList');
        var empty = document.getElementById('charmEmpty');
        function refreshEmpty() { empty.style.display = list.children.length ? 'none' : ''; }

        // Ưu tiên: file chọn > link > ảnh đã lưu trước đó
        function updateThumb(row) {
            var thumb = row.querySelector('.thumb');
            var fileIn = row.querySelector('[name="charmImage"]');
            var urlIn = row.querySelector('[name="charmImageUrl"]');
            var rm = row.querySelector('.thumb-remove');
            var src = '', custom = false;
            if (fileIn.files && fileIn.files[0]) { src = URL.createObjectURL(fileIn.files[0]); custom = true; }
            else {
                var u = urlIn.value.trim();
                if (u && !urlError(u)) { src = u; custom = true; }
                else src = row.getAttribute('data-existing') || '';
            }
            var img = thumb.querySelector('img');
            if (!src) { if (img) img.remove(); }
            else {
                if (!img) { img = document.createElement('img'); thumb.insertBefore(img, thumb.firstChild); }
                img.onerror = function () { img.remove(); };
                img.src = src;
            }
            rm.hidden = !custom;
        }

        document.getElementById('addCharm').addEventListener('click', function () {
            var frag = document.getElementById('charmTpl').content.cloneNode(true);
            var row = frag.querySelector('.charm-row');
            list.appendChild(frag);
            updateThumb(row);
            setError('charms', '');
            refreshEmpty();
        });

        fields.addEventListener('click', function (e) {
            var btn = e.target.closest('.charm-remove');
            if (btn) { btn.closest('.charm-row').remove(); refreshEmpty(); return; }
            var rm = e.target.closest('.thumb-remove');
            if (rm) {
                var row = rm.closest('.charm-row');
                row.querySelector('[name="charmImage"]').value = '';
                row.querySelector('[name="charmImageUrl"]').value = '';
                updateThumb(row);
            }
        });
        fields.addEventListener('input', function (e) {
            if (e.target.name === 'charmImageUrl') updateThumb(e.target.closest('.charm-row'));
        });
        fields.addEventListener('change', function (e) {
            var input = e.target;
            if (!input.matches('input[data-thumb]') || !input.files[0]) return;
            var f = input.files[0];
            var row = input.closest('.charm-row');
            var m = fileError(f);
            if (row) setRowError(row, m);
            else setError('baseImage', m);
            if (m) { input.value = ''; if (row) updateThumb(row); return; }
            if (row) { updateThumb(row); return; }
            // ảnh nền
            var box = input.closest('.thumb');
            var img = box.querySelector('img');
            if (!img) { img = document.createElement('img'); box.insertBefore(img, box.firstChild); }
            img.src = URL.createObjectURL(f);
        });

        // Dựng lại xem trước khi form được trả về (lỗi) hoặc mở form sửa
        renderMain();
        renderExtra();
        Array.prototype.forEach.call(document.querySelectorAll('#charmList .charm-row'), updateThumb);
        refreshEmpty();

        // Server trả lại form có lỗi: cuộn tới ô lỗi đầu tiên
        if (form.querySelector('.has-error')) scrollToFirstError();
    })();
</script>
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>
