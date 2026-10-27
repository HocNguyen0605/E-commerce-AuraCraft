/* admin-portfolio.js — Hồ sơ Thợ (Admin View) */
(function () {
    'use strict';

    var CTX     = window.CTX || '';
    var params  = new URLSearchParams(window.location.search);
    var shopId  = params.get('shopId');
    var content = document.getElementById('portfolioContent');

    /* Tiện ích */
    function fmt(v)      { return Number(v || 0).toLocaleString('vi-VN'); }
    function fmtMoney(v) { return fmt(v) + 'đ'; }
    function escHtml(t) {
        if (!t) return '';
        var d = document.createElement('div');
        d.textContent = t;
        return d.innerHTML;
    }
    function fmtDate(s) {
        if (!s) return '-';
        try {
            var d = new Date(s);
            if (isNaN(d)) return s;
            return d.toLocaleDateString('vi-VN') + ' ' + d.toLocaleTimeString('vi-VN', { hour: '2-digit', minute: '2-digit' });
        } catch (e) { return s; }
    }

    /* Nhãn trạng thái */
    var statusLabelVN = {
        active: 'Hoạt động', pending: 'Chờ duyệt', locked: 'Tạm khóa',
        completed: 'Hoàn thành', processing: 'Đang xử lý',
        shipping: 'Đang giao', cancelled: 'Đã hủy', refunded: 'Hoàn tiền',
        paid: 'Đã thanh toán'
    };
    var orderStatusCls = {
        completed: 'completed', processing: 'processing', shipping: 'shipping',
        cancelled: 'cancelled', refunded: 'cancelled', pending: 'pending', paid: 'processing'
    };
    var acctStatusCls = { active: 'active', pending: 'pending', locked: 'locked' };

    /* Kiểm tra có shopId không */
    if (!shopId) {
        content.innerHTML =
            '<div class="pm-empty" style="margin-top:40px;">' +
            '<i class="fa-solid fa-triangle-exclamation" style="color:#E74C3C;"></i>' +
            '<p>Không có thông tin gian hàng. Vui lòng quay lại danh sách.</p>' +
            '<a href="admin-users.jsp" class="btn btn-primary" style="margin-top:16px;">' +
            '<i class="fa-solid fa-arrow-left"></i> Quay lại danh sách thợ</a></div>';
        return;
    }

    /* Fetch dữ liệu từ API */
    fetch(CTX + '/api/admin/portfolio?shopId=' + encodeURIComponent(shopId), {
        credentials: 'same-origin', cache: 'no-store'
    })
    .then(function(r) {
        if (!r.ok) throw new Error('HTTP ' + r.status);
        return r.json();
    })
    .then(function(data) { renderPortfolio(data); })
    .catch(function(err) {
        content.innerHTML =
            '<div class="pm-empty"><i class="fa-solid fa-circle-exclamation" style="color:#E74C3C;"></i>' +
            '<p>Không thể tải dữ liệu: ' + escHtml(err.message) + '</p>' +
            '<button class="btn btn-primary" onclick="location.reload()" style="margin-top:12px;">Thử lại</button></div>';
    });

    /* Render chính */
    function renderPortfolio(data) {
        var shop      = data.shop      || {};
        var stats     = data.stats     || {};
        var products  = data.products  || [];
        var templates = data.templates || [];
        var orders    = data.orders    || [];
        var acctCls   = acctStatusCls[shop.accountStatus] || 'pending';

        /* Avatar */
        var avatarHtml = shop.avatar
            ? '<div class="pm-avatar"><img src="' + escHtml(shop.avatar) + '" alt="Avatar" ' +
              'onerror="this.parentNode.innerHTML=\'<i class=\\\'fa-solid fa-store\\\'></i>\'"></div>'
            : '<div class="pm-avatar"><i class="fa-solid fa-store"></i></div>';

        /* Contact row */
        var contactParts = [];
        if (shop.ownerEmail) contactParts.push('<span class="pm-contact-item"><i class="fa-solid fa-envelope"></i> ' + escHtml(shop.ownerEmail) + '</span>');
        if (shop.shopPhone)  contactParts.push('<span class="pm-contact-item"><i class="fa-solid fa-phone"></i> ' + escHtml(shop.shopPhone) + '</span>');
        if (shop.address)    contactParts.push('<span class="pm-contact-item"><i class="fa-solid fa-location-dot"></i> ' + escHtml(shop.address) + '</span>');
        var contactHtml = contactParts.length ? '<div class="pm-contact-row">' + contactParts.join('') + '</div>' : '';

        /* Stars */
        var rating    = parseFloat(stats.avgRating || 0).toFixed(1);
        var starsFull = Math.floor(rating);
        var starsHtml = '';
        for (var i = 0; i < 5; i++) {
            starsHtml += '<i class="fa-' + (i < starsFull ? 'solid' : 'regular') + ' fa-star" style="color:#D4AF37;font-size:13px;"></i>';
        }

        /* Đặt HTML vào trang */
        content.innerHTML =
            '<div class="portfolio-modal" style="max-width:100%;border-radius:14px;overflow:hidden;box-shadow:0 4px 20px rgba(0,0,0,0.08);">' +
            /* HEADER */
            '<div class="pm-header">' +
            '<div class="pm-identity">' + avatarHtml +
            '<div class="pm-info-text">' +
            '<h2 class="pm-shop-name">' + escHtml(shop.shopName || 'Gian hàng chưa đặt tên') + '</h2>' +
            '<p class="pm-owner-line"><i class="fa-solid fa-user"></i> ' + escHtml(shop.ownerName || '-') +
            ' &nbsp;&middot;&nbsp; ID Thợ: #' + (shop.userId || '') + '</p>' +
            '<div class="pm-badges">' +
            '<span class="pm-badge ' + acctCls + '">' + (statusLabelVN[shop.accountStatus] || shop.accountStatus || 'Chờ duyệt') + '</span>' +
            '<span class="pm-badge">' + escHtml(shop.shopStatus || 'N/A') + '</span>' +
            '<span class="pm-badge" style="background:rgba(212,175,55,0.3);">Phí sàn: ' + parseFloat(shop.commissionRate || 0).toFixed(1) + '%</span>' +
            '</div></div></div>' + contactHtml + '</div>' +
            /* STATS */
            '<div class="pm-stats">' +
            '<div class="pm-stat-item"><span class="pm-stat-value">' + (shop.activeProducts || 0) + '</span>' +
            '<span class="pm-stat-label"><i class="fa-solid fa-box"></i> SP đang bán</span></div>' +
            '<div class="pm-stat-item"><span class="pm-stat-value">' + (stats.completedOrders || 0) + '</span>' +
            '<span class="pm-stat-label"><i class="fa-solid fa-check-circle"></i> Đơn hoàn thành</span></div>' +
            '<div class="pm-stat-item"><span class="pm-stat-value" style="font-size:17px;">' + fmtMoney(stats.totalRevenue) + '</span>' +
            '<span class="pm-stat-label"><i class="fa-solid fa-wallet"></i> Doanh thu</span></div>' +
            '<div class="pm-stat-item"><span class="pm-stat-value" style="display:flex;align-items:center;justify-content:center;gap:5px;">' + starsHtml + ' ' + rating + '</span>' +
            '<span class="pm-stat-label"><i class="fa-solid fa-comments"></i> ' + (stats.reviewCount || 0) + ' đánh giá</span></div>' +
            '</div>' +
            /* TABS */
            '<div class="pm-tabs" role="tablist">' +
            '<button class="pm-tab active" data-tab="products" role="tab"><i class="fa-solid fa-box-open"></i> Sản phẩm <span class="pm-tab-count">' + products.length + '</span></button>' +
            '<button class="pm-tab" data-tab="templates" role="tab"><i class="fa-solid fa-palette"></i> Mẫu Custom <span class="pm-tab-count">' + templates.length + '</span></button>' +
            '<button class="pm-tab" data-tab="orders" role="tab"><i class="fa-solid fa-receipt"></i> Đơn hàng <span class="pm-tab-count">' + orders.length + '</span></button>' +
            '</div>' +
            /* PANELS */
            '<div class="pm-panels">' +
            '<div class="pm-panel active" id="panel-products">' + renderDescription(shop.description) + renderProducts(products) + '</div>' +
            '<div class="pm-panel" id="panel-templates">' + renderTemplates(templates) + '</div>' +
            '<div class="pm-panel" id="panel-orders">' + renderOrders(orders) + '</div>' +
            '</div></div>';

        /* Gắn sự kiện chuyển tab */
        content.querySelectorAll('.pm-tab').forEach(function(btn) {
            btn.addEventListener('click', function() {
                content.querySelectorAll('.pm-tab').forEach(function(b) { b.classList.remove('active'); });
                content.querySelectorAll('.pm-panel').forEach(function(p) { p.classList.remove('active'); });
                btn.classList.add('active');
                var panel = content.querySelector('#panel-' + btn.dataset.tab);
                if (panel) panel.classList.add('active');
            });
        });
    }

    /* Các hàm phụ render */
    function renderDescription(desc) {
        if (!desc) return '';
        return '<div class="pm-description"><strong><i class="fa-solid fa-quote-left"></i> Giới thiệu cửa hàng</strong>' + escHtml(desc) + '</div>';
    }

    function renderProducts(products) {
        if (!products || products.length === 0) {
            return '<div class="pm-empty"><i class="fa-solid fa-box-open"></i><p>Chưa có sản phẩm nào.</p></div>';
        }
        var cards = products.map(function(p) {
            var imgHtml = p.image
                ? '<img class="pm-product-img" src="' + escHtml(p.image) + '" alt="' + escHtml(p.name) + '" loading="lazy" ' +
                  'onerror="this.outerHTML=\'<div class=\\\'pm-product-img-placeholder\\\'><i class=\\\'fa-solid fa-image\\\'></i></div>\'">'
                : '<div class="pm-product-img-placeholder"><i class="fa-solid fa-image"></i></div>';
            var stsCls  = p.status === 'active' ? 'active' : 'inactive';
            var stsText = p.status === 'active' ? 'Đang bán' : 'Ẩn';
            return '<div class="pm-product-card">' + imgHtml +
                '<div class="pm-product-info">' +
                '<p class="pm-product-name">' + escHtml(p.name) + '</p>' +
                '<p class="pm-product-price">' + fmtMoney(p.price) + '</p>' +
                '<div class="pm-product-meta"><span>Đã bán: ' + fmt(p.soldCount) + '</span>' +
                '<span class="pm-product-status ' + stsCls + '">' + stsText + '</span></div>' +
                '</div></div>';
        }).join('');
        return '<div class="pm-products-grid">' + cards + '</div>';
    }

    function renderTemplates(templates) {
        if (!templates || templates.length === 0) {
            return '<div class="pm-empty"><i class="fa-solid fa-palette"></i><p>Chưa có mẫu thiết kế custom nào.</p></div>';
        }
        var cards = templates.map(function(t) {
            return '<div class="pm-template-card">' +
                '<div class="pm-template-icon"><i class="fa-solid fa-wand-magic-sparkles"></i></div>' +
                '<div><p class="pm-template-name">' + escHtml(t.name) + '</p>' +
                '<p class="pm-template-price">' + fmtMoney(t.basePrice) + '</p>' +
                '<p class="pm-template-type">' + escHtml(t.type || 'Tùy chỉnh') + '</p></div></div>';
        }).join('');
        return '<div class="pm-templates-grid">' + cards + '</div>';
    }

    function renderOrders(orders) {
        if (!orders || orders.length === 0) {
            return '<div class="pm-empty"><i class="fa-solid fa-receipt"></i><p>Chưa có đơn hàng nào.</p></div>';
        }
        var rows = orders.map(function(o) {
            var stsCls = orderStatusCls[o.status] || 'pending';
            return '<tr>' +
                '<td><span class="pm-order-id">#' + o.orderId + '</span></td>' +
                '<td>' + escHtml(o.buyerName || '-') + '</td>' +
                '<td><span class="pm-order-summary">' + escHtml(o.productSummary || '-') + '</span></td>' +
                '<td>' + (o.itemCount || 1) + '</td>' +
                '<td class="pm-order-amount">' + fmtMoney(o.amount) + '</td>' +
                '<td><span class="pm-status ' + stsCls + '">' + (statusLabelVN[o.status] || o.status) + '</span></td>' +
                '<td style="font-size:12px;color:var(--text-muted);">' + fmtDate(o.dateCreate) + '</td>' +
                '</tr>';
        }).join('');
        return '<div class="pm-order-table-wrap"><table class="pm-order-table">' +
            '<thead><tr><th>Mã đơn</th><th>Khách hàng</th><th>Sản phẩm</th>' +
            '<th>SL</th><th>Giá trị</th><th>Trạng thái</th><th>Ngày đặt</th></tr></thead>' +
            '<tbody>' + rows + '</tbody></table></div>';
    }

    /* Nút quay lại */
    var btnBack = document.getElementById('btnBackToUsers');
    if (btnBack) {
        btnBack.addEventListener('click', function() {
            if (history.length > 1) { history.back(); }
            else { location.href = CTX + '/pages/admin/admin-users.jsp'; }
        });
    }

})();
