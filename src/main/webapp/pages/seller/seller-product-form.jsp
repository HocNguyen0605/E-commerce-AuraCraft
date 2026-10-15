<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Đặt tại: webapp/pages/seller-product-form.jsp

  Servlet cần set (request attribute):
    shop       : Shop (SellerSupport đã set sẵn)
    categories : List<Category> getter: id, name
    product    : null khi THÊM MỚI; khi SỬA cần getter:
                 id, name, description, price, stock, categoryId, status, images (List<String>)
    error      : thông báo lỗi (tùy chọn)
  Mở form:  GET  /seller/products?action=form[&id=...]
  Lưu:      POST /seller/products (multipart): id rỗng = INSERT, có id = UPDATE
  status: active | hidden
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
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/pages/styleSeller.css">
    <style>
        .alert { padding: 15px; margin-bottom: 20px; border-radius: 4px; }
        .alert-danger { background:#f8d7da; color:#721c24; border:1px solid #f5c6cb; }
    </style>
</head>
<body>
<jsp:include page="/src/main/webapp/components/header.html"/>

<main class="seller-layout container">
    <!-- Sidebar -->
    <aside class="seller-sidebar">
        <div class="seller-profile">
            <c:choose>
                <c:when test="${empty shop.avatar}"><c:set var="shopImg" value="https://i.pravatar.cc/150?img=32"/></c:when>
                <c:when test="${fn:startsWith(shop.avatar, 'http')}"><c:set var="shopImg" value="${shop.avatar}"/></c:when>
                <c:otherwise><c:set var="shopImg" value="${ctx}/${shop.avatar}"/></c:otherwise>
            </c:choose>
            <img src="${shopImg}" alt="<c:out value='${shop.name}'/>" class="profile-img">
            <h3><c:out value="${shop.name}"/></h3>
        </div>
        <nav class="seller-nav">
            <a href="${ctx}/seller/dashboard"><i class="bi bi-pie-chart-fill"></i> Tổng quan Doanh thu</a>
            <a href="${ctx}/seller/products" class="active"><i class="bi bi-box-seam-fill"></i> Quản lý Sản phẩm</a>
            <a href="${ctx}/seller/orders"><i class="bi bi-card-list"></i> Quản lý Đơn hàng</a>
        </nav>
    </aside>

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
        </form>
    </section>
</main>

<jsp:include page="/src/main/webapp/components/footer.html"/>
<script>
    // Xem trước ảnh mới chọn
    document.getElementById('file-upload').addEventListener('change', function () {
        const list = document.getElementById('preview-list');
        Array.from(this.files).forEach(function (f) {
            if (f.size > 5 * 1024 * 1024) { alert(f.name + ' vượt quá 5MB'); return; }
            const img = document.createElement('img');
            img.className = 'image-preview';
            img.src = URL.createObjectURL(f);
            list.appendChild(img);
        });
    });
</script>
<script src="assets/js/auth.js"></script>
<script src="${ctx}/src/main/webapp/assets/js/popup.js"></script>
</body>
</html>
