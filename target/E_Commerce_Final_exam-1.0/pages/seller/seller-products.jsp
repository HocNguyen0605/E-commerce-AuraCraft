<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%--
  Đặt tại: webapp/pages/seller/seller-products.jsp  (servlet forward tới /pages/seller/seller-products.jsp)

  Servlet cần set (request attribute):
    shop        : Shop (SellerSupport đã set sẵn: name, avatar)
    products    : List<Product>  mỗi phần tử cần getter: id, name, image, categoryName, price, stock, status
    categories  : List<Category> mỗi phần tử cần getter: id, name, productCount
    page        : trang hiện tại (mặc định 1)
    totalPages  : tổng số trang
  Tham số GET: q, category, sort (newest | price_asc | price_desc | best), page
  Xóa: POST /pages/seller/seller-products  (action=delete, id=...)
  Flash: success / error (Flash.transfer)
--%>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="curPage" value="${empty page ? 1 : page}"/>
<%-- URL của servlet controller.SellerProductServlet.SellerProductServlet --%>
<c:set var="productsUrl" value="${ctx}/pages/seller/seller-products"/>
<%-- Đang có bộ lọc nào không (để hiện nút "Xóa lọc") --%>
<c:set var="hasFilter" value="${not empty param.q or not empty param.category or not empty param.status or (not empty param.sort and param.sort ne 'newest')}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Kênh Thợ - Quản lý Sản phẩm</title>
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
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title">Quản lý Sản phẩm</h1>
            <a href="${productsUrl}?action=form" class="btn btn-primary"><i class="bi bi-plus-lg"></i> Thêm Sản phẩm mới</a>
        </div>

        <!-- Toolbar: tìm kiếm, lọc, sắp xếp (GET) -->
        <div class="filter-bar">
            <form action="${productsUrl}" method="GET">
                <%-- Hàng 1: ô tìm kiếm + nút Tìm kiếm + nút Xóa lọc (luôn nằm cạnh nhau) --%>
                <div class="filter-search-row">
                    <div class="search-input">
                        <i class="bi bi-search"></i>
                        <input type="text" name="q" value="<c:out value='${param.q}'/>" placeholder="Tìm kiếm tên sản phẩm...">
                    </div>
                    <button type="submit" class="btn btn-primary filter-btn"><i class="bi bi-search"></i> Tìm kiếm</button>
                    <c:if test="${hasFilter}">
                        <a href="${productsUrl}" class="btn btn-outline filter-btn"><i class="bi bi-x-lg"></i> Xóa lọc</a>
                    </c:if>
                </div>
                <%-- Hàng 2: các ô lọc --%>
                <div class="filter-actions">
                    <select class="custom-select" name="category" onchange="this.form.submit()">
                        <option value="">Tất cả danh mục</option>
                        <c:forEach var="cat" items="${categories}">
                            <option value="${cat.id}" ${param.category eq cat.id ? 'selected' : ''}>
                                <c:out value="${cat.name}"/> (${cat.productCount})
                            </option>
                        </c:forEach>
                    </select>
                    <select class="custom-select" name="status" onchange="this.form.submit()">
                        <option value="">Tất cả trạng thái</option>
                        <option value="active" ${param.status eq 'active' ? 'selected' : ''}>Đang bán</option>
                        <option value="out"    ${param.status eq 'out' ? 'selected' : ''}>Hết hàng</option>
                        <option value="hidden" ${param.status eq 'hidden' ? 'selected' : ''}>Đã ẩn</option>
                    </select>
                    <select class="custom-select" name="sort" onchange="this.form.submit()">
                        <option value="newest"      ${empty param.sort or param.sort eq 'newest' ? 'selected' : ''}>Mới nhất</option>
                        <option value="price_asc"   ${param.sort eq 'price_asc' ? 'selected' : ''}>Giá: Thấp đến Cao</option>
                        <option value="price_desc"  ${param.sort eq 'price_desc' ? 'selected' : ''}>Giá: Cao xuống Thấp</option>
                        <option value="best"        ${param.sort eq 'best' ? 'selected' : ''}>Bán chạy nhất</option>
                    </select>
                </div>
            </form>
        </div>

        <!-- Bảng sản phẩm -->
        <div class="dashboard-section mt-4 table-section">
            <table class="data-table">
                <thead>
                <tr>
                    <th width="45%">Sản phẩm</th>
                    <th>Giá bán</th>
                    <th>Tồn kho</th>
                    <th>Trạng thái</th>
                    <th>Hành động</th>
                </tr>
                </thead>
                <tbody>
                <c:choose>
                    <c:when test="${empty products}">
                        <tr><td colspan="5" style="text-align:center;">Chưa có sản phẩm nào.</td></tr>
                    </c:when>
                    <c:otherwise>
                        <c:forEach var="p" items="${products}">
                            <c:choose>
                                <c:when test="${empty p.image}"><c:set var="pImg" value="${ctx}/assets/img/products/belly_02.jpg"/></c:when>
                                <c:when test="${fn:startsWith(p.image, 'http')}"><c:set var="pImg" value="${p.image}"/></c:when>
                                <c:otherwise><c:set var="pImg" value="${ctx}/${p.image}"/></c:otherwise>
                            </c:choose>
                            <tr class="${p.status eq 'hidden' ? 'row-hidden' : ''}">
                                <td>
                                    <div class="product-cell">
                                        <img src="${pImg}" alt="<c:out value='${p.name}'/>">
                                        <div>
                                            <h4><c:out value="${p.name}"/></h4>
                                            <span class="category">Danh mục: <c:out value="${p.categoryName}"/></span>
                                        </div>
                                    </div>
                                </td>
                                <td class="price-cell"><fmt:formatNumber value="${p.price}" pattern="#,##0"/>đ</td>
                                <td>${p.stock}</td>
                                <td>
                                    <c:choose>
                                        <c:when test="${p.status eq 'hidden'}"><span class="status-badge muted">Đã ẩn</span></c:when>
                                        <c:when test="${p.stock le 0}"><span class="status-badge danger">Hết hàng</span></c:when>
                                        <c:otherwise><span class="status-badge success">Đang bán</span></c:otherwise>
                                    </c:choose>
                                </td>
                                <td>
                                    <div class="action-btns">
                                        <a href="${productsUrl}?action=form&id=${p.id}" class="btn-icon text-primary" title="Sửa"><i class="bi bi-pencil-fill"></i></a>
                                        <form action="${productsUrl}" method="POST" style="display:inline;"
                                              onsubmit="return confirm('Bạn có chắc muốn xóa sản phẩm này?');">
                                            <input type="hidden" name="action" value="delete">
                                            <input type="hidden" name="id" value="${p.id}">
                                            <button type="submit" class="btn-icon text-danger" title="Xóa"><i class="bi bi-trash-fill"></i></button>
                                        </form>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>
                    </c:otherwise>
                </c:choose>
                </tbody>
            </table>
        </div>

        <!-- Phân trang (giữ nguyên bộ lọc) -->
        <c:if test="${totalPages > 1}">
            <c:url var="baseUrl" value="/pages/seller/seller-products">
                <c:if test="${not empty param.q}"><c:param name="q" value="${param.q}"/></c:if>
                <c:if test="${not empty param.category}"><c:param name="category" value="${param.category}"/></c:if>
                <c:if test="${not empty param.status}"><c:param name="status" value="${param.status}"/></c:if>
                <c:if test="${not empty param.sort}"><c:param name="sort" value="${param.sort}"/></c:if>
            </c:url>
            <c:set var="sep" value="${fn:contains(baseUrl, '?') ? '&' : '?'}"/>
            <div class="pagination">
                <c:if test="${curPage > 1}">
                    <a class="btn-page" href="${baseUrl}${sep}page=${curPage - 1}"><i class="bi bi-chevron-left"></i></a>
                </c:if>
                <c:forEach begin="1" end="${totalPages}" var="i">
                    <a class="btn-page ${i == curPage ? 'active' : ''}" href="${baseUrl}${sep}page=${i}">${i}</a>
                </c:forEach>
                <c:if test="${curPage < totalPages}">
                    <a class="btn-page" href="${baseUrl}${sep}page=${curPage + 1}"><i class="bi bi-chevron-right"></i></a>
                </c:if>
            </div>
        </c:if>
    </section>
</main>

<jsp:include page="/components/footer.html"/>
<script src="${ctx}/assets/js/auth.js"></script>
<script src="${ctx}/assets/js/popup.js"></script>
</body>
</html>
