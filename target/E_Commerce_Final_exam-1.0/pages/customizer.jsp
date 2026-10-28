<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>Thiết kế Custom — AuraCraft</title>
<link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleCustomizer.css?v=customizer-layout-v16">
</head>
<body>

 <div id="header-placeholder"></div>

<main class="cz-wrap" data-context="${pageContext.request.contextPath}">
  <div class="container cz-headbar">
    <div>
      <a href="${pageContext.request.contextPath}/pages/products" class="cz-back-link" id="czBackLink">← Quay lại sản phẩm</a>
      <span class="tag" id="czTag">Thiết kế Custom</span>
      <h1 id="czHeading">Vòng tay của bạn</h1>
      <p class="cz-hint" id="czHint">Tự phối dây nền, hạt và charm cho thiết kế mới. Bạn có thể kéo thả để đổi vị trí hoặc xóa từng thành phần.</p>
    </div>
  </div>

  <div class="container cz-grid">

    <section class="cz-canvas-panel card">
      <h3>Bản thiết kế</h3>

      <div class="cz-tools">
        <div class="cz-size-selector card cz-design-mode-only" id="czDesignTypeCard">
          <label for="czProductType">Danh mục thiết kế:</label>
          <select id="czProductType" class="form-control">
            <option value="">Chọn danh mục thiết kế</option>
            <option value="bracelet">Vòng tay</option>
            <option value="necklace">Dây chuyền</option>
            <option value="ring">Nhẫn</option>
            <option value="charm">Móc khóa điện thoại</option>
          </select>
        </div>
        <div class="cz-size-selector card cz-design-mode-only" id="czDesignSizeCard">
          <label for="czSlotSize">Số vị trí thiết kế:</label>
          <select id="czSlotSize" class="form-control">
            <option value="">Chọn danh mục trước</option>
            <option value="14" selected>14 vị trí (Cơ bản)</option>
            <option value="16">16 vị trí (Lớn)</option>
            <option value="18">18 vị trí (Rất lớn)</option>
            <option value="20">20 vị trí (Đặc biệt)</option>
          </select>
        </div>
        <div class="cz-size-selector card cz-design-mode-only" id="czSourceCard">
          <div id="czSourcePicker" class="cz-design-mode-only">
            <label for="czShopSelect">Chọn shop thiết kế:</label>
            <select id="czShopSelect" class="form-control">
              <option value="">Chọn shop</option>
            </select>
            <label for="czSourceProduct">Chọn dây/mẫu nền:</label>
            <select id="czSourceProduct" class="form-control">
              <option value="">Chọn shop trước</option>
            </select>
            <small class="cz-source-help" id="czSourceHelp">Chọn shop, danh mục và mẫu nền; vật liệu lấy từ các sản phẩm đang bán của shop.</small>
          </div>
          <div class="cz-base-preview" id="czBasePreview" hidden>
            <img id="czBaseMaterialImage" alt="Ảnh minh họa dây nền">
            <span id="czBaseMaterialName"></span>
          </div>
        </div>
        <div class="cz-size-selector card cz-design-mode-only" id="czSavedDesignsCard">
          <label for="czSavedDesigns">Thiết kế đã lưu:</label>
          <select id="czSavedDesigns" class="form-control"><option value="">Chọn thiết kế để tải</option></select>
        </div>

        <div class="cz-text-autofill card">
            <label for="czTextInput">Xếp chữ tự động:</label>
          <div class="cz-text-inputs">
            <input type="text" id="czTextInput" placeholder="Nhập tên (Vd: THUONG)" class="form-control cz-uppercase">
            <select id="czTextFont" class="form-control cz-font-select" aria-label="Màu hạt chữ">
              <option value="Chữ Đen">Chữ Đen</option>
              <option value="Chữ Vàng">Chữ Vàng</option>
            </select>
            <button type="button" class="btn btn-primary" id="czAutoFillBtn">Xếp chữ</button>
          </div>
        </div>
      </div>

      <p class="cz-slot-counter" id="czSlotCounter" aria-live="polite"></p>

      <div class="cz-strip" id="czStrip" aria-label="Các vị trí vật liệu trong thiết kế"></div>

      <p class="cz-strip-hint">Chọn vật liệu để thêm vào ô trống tiếp theo. Kéo vật liệu vào một ô cụ thể hoặc kéo các ô đã đặt để đổi chỗ.</p>
      <p class="cz-feedback" id="czFeedback" role="status" aria-live="polite"></p>

      <div class="cz-library-inline">
        <h3>Thư viện vật liệu</h3>
        <div class="cz-library-filters cz-design-mode-only" id="czLibraryFilters" role="group" aria-label="Lọc loại vật liệu">
          <button type="button" class="is-active" data-material-filter="all" aria-pressed="true">Tất cả</button>
          <button type="button" data-material-filter="base" aria-pressed="false">Dây nền</button>
          <button type="button" data-material-filter="bead" aria-pressed="false">Charm &amp; hạt</button>
          <button type="button" data-material-filter="charm" aria-pressed="false">Charm</button>
        </div>
        <div class="cz-lib-strip" id="czLibrary"></div>
      </div>
    </section>

    <section class="cz-price card">
      <h3>Chi phí thiết kế</h3>
      <div class="cz-price-body">
        <div class="cz-price-cost">
          <div class="cz-shop-info" id="czShopInfo"></div>
          <ul class="cz-price-list" id="czPriceList">
            <li class="cz-price-empty">Chưa có thành phần nào</li>
          </ul>
          <div class="cz-total-row">
            <span>Tổng tiền</span>
            <strong id="czTotal">0đ</strong>
          </div>
        </div>
        <div class="cz-price-actions">
          <button type="button" class="btn btn-outline cz-save-btn" id="czSaveDesignBtn">Lưu thiết kế</button>
          <button type="button" class="btn btn-outline cz-cart-btn" id="czAddToCartBtn">Thêm vào giỏ hàng</button>
          <button type="button" class="btn btn-primary cz-request-btn" id="czSendShopBtn">Tiếp tục thanh toán</button>
          <a href="${pageContext.request.contextPath}/pages/products" class="cz-cancel-link" id="czCancelLink">Không muốn custom nữa? Quay lại sản phẩm</a>
        </div>
      </div>
    </section>

  </div>
</main>
<div id="footer-placeholder"></div>
<div id="czToast" class="cz-toast" role="status" aria-live="polite" hidden></div>

<script type="application/json" id="czInitialSources">${customizerSourcesJson}</script>
<script src="${pageContext.request.contextPath}/assets/js/custom-workflow.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/customizer.js?v=customizer-v16"></script>
<script>
        fetch('../components/header.html').then(response => response.text()).then(data => { document.getElementById('header-placeholder').innerHTML = data; }).catch(error => console.error('Error loading header:', error));

        fetch('../components/footer.html').then(response => response.text()).then(data => { document.getElementById('footer-placeholder').innerHTML = data; }).catch(error => console.error('Error loading footer:', error));
    </script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
 <script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
</body>
</html>
