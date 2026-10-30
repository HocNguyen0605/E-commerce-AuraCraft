<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thiết kế đã lưu</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-content.css">
    <style>
        .saved-design-grid {
            display: grid;
            grid-template-columns:repeat(3, minmax(0, 1fr));
            gap: 16px;
        }

        .saved-design-card {
            display: flex;
            flex-direction: column;
            overflow: hidden;
            border: 1px solid #e8e4e2;
            border-radius: 14px;
            background: #fff;
            box-shadow: var(--shadow);
        }

        .saved-design-card img {
            display: block;
            width: 100%;
            height: 170px;
            object-fit: contain;
            background: #fff;
        }

        .saved-design-info {
            display: flex;
            flex: 1;
            flex-direction: column;
            padding: 14px;
        }

        .saved-design-info h3 {
            display: -webkit-box;
            min-height: 2.8em;
            margin: 0 0 8px;
            overflow: hidden;
            font-size: 1rem;
            line-height: 1.4;
            overflow-wrap: anywhere;
            -webkit-box-orient: vertical;
            -webkit-line-clamp: 2;
        }

        .saved-design-info p {
            margin: 5px 0;
            color: #6e6260;
        }

        .saved-design-actions {
            display: flex;
            gap: 8px;
            margin-top: auto;
            padding-top: 12px;
        }

        .saved-design-actions .btn {
            flex: 1;
            text-align: center;
        }

        @media (max-width: 1024px) {
            .saved-design-grid {
                grid-template-columns:repeat(2, minmax(0, 1fr));
            }
        }

        @media (max-width: 640px) {
            .saved-design-grid {
                grid-template-columns:1fr;
            }
        }
    </style>
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
            <div class="col-3 over_content">
                <ul class="menu_function">
                    <li class="funciton_item"><a href="${ctx}/pages/profile-info">Thông tin cá nhân</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-orders">Đơn hàng</a></li>
                    <li class="funciton_item active"><a href="${ctx}/pages/profile-designs">Thiết kế đã lưu</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-quotes">Báo giá</a></li>
                    <li class="funciton_item"><a href="#" data-logout style="color:#E74C3C"><i
                            class="fa-solid fa-arrow-right-from-bracket"></i> Đăng xuất</a></li>
                </ul>
            </div>
            <div class="col-9 over_content">
                <div class="col-12 panel">
                    <h2 class="panel-title">Thiết kế đã lưu</h2>
                    <div class="saved-design-grid" id="savedDesignGrid"></div>
                    <div class="empty-state" id="savedDesignEmpty" hidden>
                        <p>Bạn chưa lưu thiết kế nào.</p>
                        <a class="btn btn-primary" href="${ctx}/pages/customizer">Bắt đầu thiết kế</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</main>
<div id="footer-placeholder"></div>
<script>
    const CTX = '${ctx}';
    fetch(CTX + '/components/header.html').then(r => r.text()).then(html => document.getElementById('header-placeholder').innerHTML = html);
    fetch(CTX + '/components/footer.html').then(r => r.text()).then(html => document.getElementById('footer-placeholder').innerHTML = html);
</script>
<script src="${ctx}/assets/js/custom-workflow.js"></script>
<script>
    (() => {
        const grid = document.getElementById('savedDesignGrid');
        const empty = document.getElementById('savedDesignEmpty');
        const designs = window.AuraCraftCustom ? window.AuraCraftCustom.listCustomDesigns() : [];
        const money = value => (Number(value) || 0).toLocaleString('vi-VN') + 'đ';
        const escapeHtml = value => String(value || '').replace(/[&<>"']/g, ch => ({
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#39;'
        }[ch]));
        const imageUrl = design => {
            const item = design.baseMaterial || (design.slots || []).find(Boolean);
            const image = item && (item.image || item.imageUrl);
            return image ? (image.startsWith('/') ? CTX + image : image) : CTX + '/assets/img/products/belly_02.jpg';
        };
        if (!designs.length) {
            empty.hidden = false;
            return;
        }
        designs.forEach(design => {
            const card = document.createElement('article');
            card.className = 'saved-design-card';
            const title = design.name || design.productType || 'Thiết kế của tôi';
            const materials = (design.slots || []).filter(Boolean).length + (design.baseMaterial ? 1 : 0);
            const url = CTX + '/pages/customizer?designId=' + encodeURIComponent(design.id);
            card.innerHTML = '<img src="' + escapeHtml(imageUrl(design)) + '" alt="' + escapeHtml(title) + '">' +
                '<div class="saved-design-info"><h3 title="' + escapeHtml(title) + '">' + escapeHtml(title) + '</h3>' +
                '<p>' + escapeHtml(design.designType || design.productType || 'Thiết kế sản phẩm') + ' · ' + materials + ' vật liệu</p>' +
                '<p>' + escapeHtml(design.sellerName || design.shopName || 'AuraCraft') + '</p>' +
                '<strong>' + money(design.price) + '</strong>' +
                '<div class="saved-design-actions"><a class="btn btn-primary btn-sm" href="' + escapeHtml(url) + '">Xem thiết kế</a></div></div>';
            grid.appendChild(card);
        });
    })();
</script>
<script src="${ctx}/assets/js/popup.js"></script>
<script src="${ctx}/assets/js/site-shell.js"></script>
<script src="${ctx}/assets/js/auth.js"></script>
</body>
</html>
