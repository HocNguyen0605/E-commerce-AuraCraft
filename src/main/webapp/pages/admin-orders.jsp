<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Đơn hàng (Admin) - AuraCraft</title>
    <!-- Fonts & Icons -->
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">

    <!-- Styles -->
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="../assets/css/header-footer.css">
    <link rel="stylesheet" href="../assets/css/pages/styleOrders.css">
    <link rel="stylesheet" href="../assets/css/pages/styleAdminOrders.css">
    <link rel="stylesheet" href="../assets/css/admin-layout.css">
</head>
<body>
<div class="admin-wrapper">
    <div id="admin-sidebar-placeholder"></div>

    <main class="admin-main-content">
    <div class="page-header">
        <h1 class="page-title"><i class="fa-solid fa-clipboard-list"></i> Giám sát Đơn hàng Toàn sàn</h1>
    </div>

    <!-- Thanh công cụ Admin -->
    <div class="admin-toolbar">
        <div class="search-box">
            <i class="fa-solid fa-magnifying-glass"></i>
            <input type="text" id="searchInput" placeholder="Tìm kiếm mã đơn, tên thợ, tên khách hàng...">
        </div>
        <select class="filter-select" id="filterShop">
            <option value="all">Tất cả Cửa hàng / Thợ</option>
            <!-- Shops will be populated dynamically from DB -->
        </select>
        <select class="filter-select" id="filterSort">
            <option value="newest">Mới nhất</option>
            <option value="oldest">Cũ nhất</option>
            <option value="high_price">Giá trị cao nhất</option>
        </select>
    </div>

    <!-- Bộ lọc Tabs -->
    <div class="tabs">
        <div class="tab-item active" data-tab="all">Tất cả (<span id="tabAll">0</span>)</div>
        <div class="tab-item" data-tab="pending">Chờ xử lý (<span id="tabPending">0</span>)</div>
        <div class="tab-item" data-tab="processing">Đang xử lý (<span id="tabProcessing">0</span>)</div>
        <div class="tab-item" data-tab="shipping">Đang giao (<span id="tabShipping">0</span>)</div>
        <div class="tab-item" data-tab="completed">Đã hoàn thành (<span id="tabCompleted">0</span>)</div>
        <div class="tab-item" data-tab="cancelled" style="color: var(--status-late); font-weight: bold;">Đã hủy (<span id="tabCancelled">0</span>)</div>
    </div>

    <section class="admin-custom-operations" aria-label="Đơn Custom cần vận hành">
        <div class="admin-custom-operations-header">
            <div>
                <h2>Vận hành đơn Custom</h2>
                <p>Theo dõi deadline, cập nhật trạng thái và xử lý hủy/hoàn tiền.</p>
            </div>
            <a class="btn btn-outline" href="admin-disputes.html"><i class="fa-solid fa-scale-balanced" aria-hidden="true"></i> Khiếu nại &amp; vi phạm</a>
        </div>
        <div class="admin-custom-toolbar">
            <label class="search-box">
                <i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i>
                <span class="visually-hidden">Tìm mã đơn, buyer hoặc thợ</span>
                <input type="search" id="customOrderSearch" placeholder="Tìm mã đơn, buyer hoặc thợ">
            </label>
            <label class="visually-hidden" for="customOrderStatus">Lọc trạng thái đơn</label>
            <select class="filter-select" id="customOrderStatus">
                <option value="all">Tất cả đơn Custom</option>
                <option value="pending">Chờ xử lý</option>
                <option value="paid">Đã thanh toán</option>
                <option value="processing">Đang chế tác</option>
                <option value="shipping">Đang giao</option>
                <option value="completed">Hoàn thành</option>
                <option value="cancelled">Đã hủy</option>
                <option value="refunded">Đã hoàn tiền</option>
            </select>
        </div>
        <p class="admin-custom-feedback" id="customOrderFeedback" role="status" aria-live="polite"></p>
        <div class="order-list" id="adminCustomOrderList"></div>
    </section>

    <!-- Danh sách đơn hàng - rendered dynamically from DB -->
    <div class="order-list" id="orderList"></div>

    <!-- Empty state -->
    <div id="emptyOrders" class="admin-custom-empty" hidden>
        <i class="fa-regular fa-folder-open" style="font-size: 48px; margin-bottom: 12px; display: block;"></i>
        <p>Không tìm thấy đơn hàng phù hợp với bộ lọc.</p>
    </div>

    </main>
</div>

<!-- Modal Chi Tiết Đơn Hàng Admin -->
<div class="modal-overlay" id="orderModal">
    <div class="modal-content" style="max-width: 600px;">
        <div class="modal-header">
            <h2>Chi Tiết Đơn Hàng <span id="modalOrderId" style="color: var(--primary-brown);"></span></h2>
            <button class="close-btn" onclick="closeOrderDetail()"><i class="fa-solid fa-times"></i></button>
        </div>
        <div class="detail-body">
            <h3 id="modalProduct" style="margin-top: 0; color: var(--primary-blue);"></h3>

            <div class="info-row">
                <strong>Thợ thực hiện:</strong> <span id="modalSeller" style="color: var(--primary-blue); font-weight: bold;"></span>
            </div>
            <div class="info-row">
                <strong>Cửa hàng:</strong> <span id="modalShop"></span>
            </div>
            <div class="info-row">
                <strong>Người mua:</strong> <span id="modalBuyer"></span>
            </div>
            <div class="info-row">
                <strong>Email:</strong> <span id="modalBuyerEmail"></span>
            </div>
            <div class="info-row">
                <strong>Ngày đặt:</strong> <span id="modalDate"></span>
            </div>
            <div class="info-row">
                <strong>Ngày thanh toán:</strong> <span id="modalDatePaid"></span>
            </div>
            <div class="info-row">
                <strong>Phương thức TT:</strong> <span id="modalPayment"></span>
            </div>
            <div class="info-row">
                <strong>Địa chỉ giao hàng:</strong> <span id="modalAddress"></span>
            </div>
            <div class="info-row">
                <strong>Số lượng SP:</strong> <span id="modalItemCount"></span>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 24px; border-top: 1px dashed var(--gray-light); padding-top: 16px;">
                <span style="font-size: 16px;">Tổng giá trị đơn:</span>
                <span id="modalPrice" class="total-price"></span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 8px;">
                <span style="font-size: 14px; color: var(--text-muted);">Phí vận chuyển:</span>
                <span id="modalShippingFee" style="font-size: 14px;"></span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                <span style="font-size: 14px; color: var(--text-muted);">Giảm giá:</span>
                <span id="modalDiscount" style="font-size: 14px; color: #27AE60;"></span>
            </div>
            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                <span style="font-size: 14px; color: var(--text-muted);">Phí sàn:</span>
                <span id="modalFee" style="font-size: 14px; color: #E74C3C; font-weight: bold;"></span>
            </div>

            <!-- Admin action in modal -->
            <div id="modalAdminActions" style="margin-top: 20px; padding-top: 16px; border-top: 1px dashed var(--gray-light);">
                <label for="modalStatusSelect" style="font-weight: bold; margin-bottom: 8px; display: block;">Cập nhật trạng thái:</label>
                <select id="modalStatusSelect" class="filter-select" style="width: 100%; padding: 10px;">
                    <option value="pending">Chờ xử lý</option>
                    <option value="paid">Đã thanh toán</option>
                    <option value="processing">Đang xử lý</option>
                    <option value="shipping">Đang giao hàng</option>
                    <option value="completed">Hoàn thành</option>
                    <option value="cancelled">Hủy đơn</option>
                    <option value="refunded">Hoàn tiền</option>
                </select>
            </div>
        </div>
        <div class="modal-actions" style="display: flex; gap: 10px;">
            <button type="button" class="btn btn-primary" id="btnUpdateStatus" style="flex: 1;">Cập nhật trạng thái</button>
            <button type="button" class="btn btn-outline" onclick="closeOrderDetail()" style="flex: 1;">Đóng</button>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="../assets/js/admin/admin-common.js"></script>
<script src="../assets/js/admin/admin-orders.js"></script>
<script src="../assets/js/popup.js"></script>
</body>
</html>
