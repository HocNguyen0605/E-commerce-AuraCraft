<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="dao.ProductDAO, com.google.gson.Gson, com.google.gson.reflect.TypeToken, java.util.*, java.io.BufferedReader" %>
<%
    ProductDAO dao = new ProductDAO();
    String ajax = request.getParameter("ajax");

    if ("search".equals(ajax)) {
        response.setContentType("application/json;charset=UTF-8");
        String q = request.getParameter("q");
        List<Map<String, Object>> list = dao.searchActiveProducts(q, 50);
        response.getWriter().write(new Gson().toJson(list));
        return;
    }

    if ("save".equals(ajax) && "POST".equalsIgnoreCase(request.getMethod())) {
        response.setContentType("application/json;charset=UTF-8");
        try {
            StringBuilder sb = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) sb.append(line);

            List<Integer> productIds = new ArrayList<>();
            Map<String, Object> map = new Gson().fromJson(sb.toString(), new TypeToken<Map<String, Object>>(){}.getType());
            if (map != null && map.containsKey("productIds")) {
                List<?> rawList = (List<?>) map.get("productIds");
                for (Object item : rawList) {
                    if (item instanceof Number) productIds.add(((Number) item).intValue());
                }
            }
            boolean ok = dao.updateFeaturedProducts(productIds);
            response.getWriter().write("{\"success\":" + ok + "}");
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().write("{\"success\":false,\"message\":\"" + e.getMessage() + "\"}");
        }
        return;
    }

    List<Map<String, Object>> initialFeatured = Collections.emptyList();
    List<Map<String, Object>> initialSearch = Collections.emptyList();
    try {
        initialFeatured = dao.getFeaturedProducts();
        initialSearch = dao.searchActiveProducts("", 50);
    } catch (Exception e) {
        // Fallback to empty list
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Quản lý Sản phẩm nổi bật - AuraCraft Admin</title>
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../../assets/css/style.css">
    <link rel="stylesheet" href="../../assets/css/admin-layout.css">
    <link rel="stylesheet" href="../../assets/css/pages/styleOrders.css">
    <link rel="stylesheet" href="../../assets/css/pages/styleAdminOrders.css">
    <style>
        .featured-grid {
            display: grid;
            grid-template-columns: 1.2fr 1fr;
            gap: 24px;
            margin-top: 20px;
        }
        @media (max-width: 992px) {
            .featured-grid { grid-template-columns: 1fr; }
        }
        .panel-card {
            background: var(--white);
            border: 1px solid var(--gray-light);
            border-radius: 12px;
            padding: 24px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.03);
        }
        .panel-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 16px;
            padding-bottom: 12px;
            border-bottom: 1px solid var(--gray-light);
        }
        .panel-header h2 {
            margin: 0;
            font-size: 18px;
            color: var(--text-dark);
            font-family: 'Baloo 2', cursive;
        }
        .item-row {
            display: flex;
            align-items: center;
            gap: 12px;
            padding: 12px;
            background: #fff;
            border: 1px solid var(--gray-light);
            border-radius: 8px;
            margin-bottom: 10px;
            transition: all 0.2s ease;
        }
        .item-row:hover {
            box-shadow: 0 2px 8px rgba(0,0,0,0.06);
            border-color: #cbd5e1;
        }
        .item-thumb {
            width: 52px;
            height: 52px;
            border-radius: 6px;
            object-fit: cover;
            background: #f1f5f9;
            flex-shrink: 0;
        }
        .item-info {
            flex-grow: 1;
            min-width: 0;
            overflow: hidden;
        }
        .item-name {
            font-size: 14px;
            font-weight: 700;
            color: var(--text-dark);
            margin: 0 0 4px 0;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .item-meta {
            font-size: 13px;
            color: var(--text-muted);
            display: flex;
            gap: 12px;
        }
        .item-price {
            font-weight: 700;
            color: var(--primary-blue);
        }
        .order-badge {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: var(--primary-blue);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-weight: 700;
            font-size: 13px;
            flex-shrink: 0;
        }
        .btn-action-icon {
            background: none;
            border: 1px solid var(--gray-light);
            border-radius: 6px;
            padding: 6px 10px;
            cursor: pointer;
            color: var(--text-dark);
            transition: 0.2s;
        }
        .btn-action-icon:hover:not(:disabled) {
            background: #f8fafc;
            color: var(--primary-blue);
            border-color: var(--primary-blue);
        }
        .btn-action-icon:disabled {
            opacity: 0.3;
            cursor: not-allowed;
        }
        .btn-remove {
            color: #ef4444;
            border-color: #fecaca;
        }
        .btn-remove:hover {
            background: #fee2e2 !important;
            color: #b91c1c !important;
            border-color: #f87171 !important;
        }
        .btn-add {
            background: var(--primary-blue);
            color: #fff;
            border: none;
            padding: 6px 14px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 13px;
            cursor: pointer;
            transition: 0.2s;
        }
        .btn-add:hover {
            opacity: 0.9;
        }
        .badge-added {
            font-size: 12px;
            padding: 4px 8px;
            background: #e2e8f0;
            color: #64748b;
            border-radius: 4px;
            font-weight: 600;
        }
        .feedback-banner {
            padding: 12px 16px;
            border-radius: 8px;
            margin-bottom: 16px;
            display: none;
            font-size: 14px;
            font-weight: 600;
        }
        .feedback-success {
            background: #dcfce7;
            color: #166534;
            border: 1px solid #bbf7d0;
        }
        .feedback-error {
            background: #fee2e2;
            color: #991b1b;
            border: 1px solid #fecaca;
        }
    </style>
</head>
<body>
    <div class="admin-wrapper">
        <div id="admin-sidebar-placeholder"></div>

        <main class="admin-main-content">
            <div class="page-header" style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 16px;">
                <div>
                    <h1 class="page-title"><i class="fa-solid fa-star" style="color: #f59e0b;"></i> Quản lý Sản phẩm nổi bật</h1>
                    <p style="margin: 4px 0 0 0; color: var(--text-muted); font-size: 14px;">Chọn và sắp xếp các sản phẩm hiển thị tại section "Sản phẩm nổi bật" trang chủ</p>
                </div>
                <div>
                    <button id="btnSave" class="btn btn-primary" style="padding: 10px 24px; font-weight: 700; display: flex; align-items: center; gap: 8px;">
                        <i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi
                    </button>
                </div>
            </div>

            <div id="feedbackBanner" class="feedback-banner"></div>

            <div class="featured-grid">
                <!-- Cột trái: Danh sách đang nổi bật -->
                <div class="panel-card">
                    <div class="panel-header">
                        <h2>Sản phẩm hiển thị trang chủ (<span id="featuredCount">0</span>)</h2>
                        <span style="font-size: 13px; color: var(--text-muted);">Tối đa nên để 3 - 6 sản phẩm</span>
                    </div>

                    <div id="featuredList">
                        <!-- Rendered by JS -->
                    </div>

                    <div id="emptyFeatured" style="display: none; text-align: center; padding: 40px 10px; color: var(--text-muted);">
                        <i class="fa-regular fa-folder-open" style="font-size: 36px; margin-bottom: 10px; display: block;"></i>
                        Chưa có sản phẩm nào được chọn. Hãy tìm và thêm sản phẩm từ danh sách bên phải.
                    </div>
                </div>

                <!-- Cột phải: Tìm và chọn sản phẩm -->
                <div class="panel-card">
                    <div class="panel-header">
                        <h2>Tìm & Thêm sản phẩm</h2>
                    </div>

                    <div class="search-box" style="margin-bottom: 16px;">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <input id="searchInput" type="text" placeholder="Tìm theo tên sản phẩm hoặc gian hàng...">
                    </div>

                    <div id="searchResults" style="max-height: 520px; overflow-y: auto; padding-right: 4px;">
                        <!-- Rendered by JS -->
                    </div>
                </div>
            </div>
        </main>
    </div>

    <!-- Scripts -->
    <script>
        window.CTX = "${pageContext.request.contextPath}";
        var INITIAL_FEATURED = <%= new Gson().toJson(initialFeatured) %>;
        var INITIAL_SEARCH = <%= new Gson().toJson(initialSearch) %>;
    </script>
    <script src="../../assets/js/admin/admin-common.js"></script>
    <script>
        (function() {
            let featuredProducts = Array.isArray(INITIAL_FEATURED) ? INITIAL_FEATURED : [];
            let searchProducts = Array.isArray(INITIAL_SEARCH) ? INITIAL_SEARCH : [];
            const CTX = window.CTX || '';

            const featuredListEl = document.getElementById('featuredList');
            const searchResultsEl = document.getElementById('searchResults');
            const featuredCountEl = document.getElementById('featuredCount');
            const emptyFeaturedEl = document.getElementById('emptyFeatured');
            const searchInput = document.getElementById('searchInput');
            const btnSave = document.getElementById('btnSave');
            const feedbackBanner = document.getElementById('feedbackBanner');

            function showFeedback(msg, isSuccess) {
                feedbackBanner.textContent = msg;
                feedbackBanner.className = 'feedback-banner ' + (isSuccess ? 'feedback-success' : 'feedback-error');
                feedbackBanner.style.display = 'block';
                setTimeout(function() {
                    feedbackBanner.style.display = 'none';
                }, 4000);
            }

            function getProductImage(img, catId, id) {
                if (!img) {
                    return CTX + '/assets/img/products/bracelet_01.jpg';
                }
                if (img.startsWith('http://') || img.startsWith('https://')) {
                    return img;
                }
                let clean = img.replace(/^[/\\]+/, '');
                if (clean.startsWith('assets/')) {
                    return CTX + '/' + encodeURI(clean);
                }
                return CTX + '/assets/img/' + encodeURI(clean);
            }

            function escapeText(str) {
                if (!str) return '';
                return String(str)
                    .replace(/&/g, '&amp;')
                    .replace(/</g, '&lt;')
                    .replace(/>/g, '&gt;')
                    .replace(/"/g, '&quot;')
                    .replace(/'/g, '&#39;');
            }

            // Tìm kiếm sản phẩm active trong shop
            let searchTimeout = null;
            function loadSearch(keyword) {
                const url = CTX + '/pages/admin/admin-featured.jsp?ajax=search&q=' + encodeURIComponent(keyword || '');
                fetch(url)
                    .then(function(res) { return res.json(); })
                    .then(function(data) {
                        searchProducts = data || [];
                        renderSearch();
                    })
                    .catch(function(err) {
                        console.error('Search error:', err);
                    });
            }

            searchInput.addEventListener('input', function() {
                clearTimeout(searchTimeout);
                searchTimeout = setTimeout(function() {
                    loadSearch(searchInput.value.trim());
                }, 250);
            });

            // Render cột sản phẩm nổi bật
            function renderFeatured() {
                featuredCountEl.textContent = featuredProducts.length;
                if (featuredProducts.length === 0) {
                    featuredListEl.innerHTML = '';
                    emptyFeaturedEl.style.display = 'block';
                    return;
                }
                emptyFeaturedEl.style.display = 'none';

                let html = '';
                for (let idx = 0; idx < featuredProducts.length; idx++) {
                    const p = featuredProducts[idx];
                    const priceFormatted = Number(p.price || 0).toLocaleString('vi-VN') + 'đ';
                    const isFirst = (idx === 0);
                    const isLast = (idx === featuredProducts.length - 1);
                    const imgSrc = getProductImage(p.image, p.categoryId, p.id);

                    html += '<div class="item-row" data-id="' + p.id + '">' +
                        '<div class="order-badge">' + (idx + 1) + '</div>' +
                        '<img class="item-thumb" src="' + imgSrc + '" onerror="this.onerror=null;this.src=\'' + CTX + '/assets/img/products/bracelet_01.jpg\';" alt="' + escapeText(p.name) + '">' +
                        '<div class="item-info">' +
                            '<h4 class="item-name" title="' + escapeText(p.name) + '">' + escapeText(p.name) + '</h4>' +
                            '<div class="item-meta">' +
                                '<span class="item-price">' + priceFormatted + '</span>' +
                                '<span><i class="fa-solid fa-store"></i> ' + escapeText(p.shopName || '') + '</span>' +
                            '</div>' +
                        '</div>' +
                        '<div style="display: flex; gap: 4px;">' +
                            '<button type="button" class="btn-action-icon" onclick="moveItem(' + idx + ', -1)" ' + (isFirst ? 'disabled' : '') + ' title="Lên trên">' +
                                '<i class="fa-solid fa-arrow-up"></i>' +
                            '</button>' +
                            '<button type="button" class="btn-action-icon" onclick="moveItem(' + idx + ', 1)" ' + (isLast ? 'disabled' : '') + ' title="Xuống dưới">' +
                                '<i class="fa-solid fa-arrow-down"></i>' +
                            '</button>' +
                            '<button type="button" class="btn-action-icon btn-remove" onclick="removeItem(' + idx + ')" title="Xóa khỏi nổi bật">' +
                                '<i class="fa-solid fa-trash-can"></i>' +
                            '</button>' +
                        '</div>' +
                    '</div>';
                }
                featuredListEl.innerHTML = html;
                renderSearch();
            }

            // Render cột tìm kiếm
            function renderSearch() {
                const featuredIds = new Set(featuredProducts.map(function(p) { return p.id; }));
                let html = '';
                if (!searchProducts || searchProducts.length === 0) {
                    searchResultsEl.innerHTML = '<p style="text-align: center; color: var(--text-muted); padding: 20px;">Không tìm thấy sản phẩm nào.</p>';
                    return;
                }

                for (let i = 0; i < searchProducts.length; i++) {
                    const p = searchProducts[i];
                    const isAdded = featuredIds.has(p.id);
                    const priceFormatted = Number(p.price || 0).toLocaleString('vi-VN') + 'đ';
                    const imgSrc = getProductImage(p.image, p.categoryId, p.id);

                    html += '<div class="item-row">' +
                        '<img class="item-thumb" src="' + imgSrc + '" onerror="this.onerror=null;this.src=\'' + CTX + '/assets/img/products/bracelet_01.jpg\';" alt="' + escapeText(p.name) + '">' +
                        '<div class="item-info">' +
                            '<h4 class="item-name" title="' + escapeText(p.name) + '">' + escapeText(p.name) + '</h4>' +
                            '<div class="item-meta">' +
                                '<span class="item-price">' + priceFormatted + '</span>' +
                                '<span><i class="fa-solid fa-store"></i> ' + escapeText(p.shopName || '') + '</span>' +
                            '</div>' +
                        '</div>' +
                        '<div>' +
                            (isAdded 
                                ? '<span class="badge-added"><i class="fa-solid fa-check"></i> Đang chọn</span>' 
                                : '<button type="button" class="btn-add" onclick="addItem(' + p.id + ')"><i class="fa-solid fa-plus"></i> Thêm</button>') +
                        '</div>' +
                    '</div>';
                }
                searchResultsEl.innerHTML = html;
            }

            // Thao tác danh sách
            window.moveItem = function(index, direction) {
                const targetIndex = index + direction;
                if (targetIndex < 0 || targetIndex >= featuredProducts.length) return;
                const temp = featuredProducts[index];
                featuredProducts[index] = featuredProducts[targetIndex];
                featuredProducts[targetIndex] = temp;
                renderFeatured();
            };

            window.removeItem = function(index) {
                featuredProducts.splice(index, 1);
                renderFeatured();
            };

            window.addItem = function(id) {
                const prod = searchProducts.find(function(p) { return p.id === id; });
                if (prod && !featuredProducts.some(function(p) { return p.id === id; })) {
                    featuredProducts.push(prod);
                    renderFeatured();
                }
            };

            // Lưu dữ liệu vào Server
            btnSave.addEventListener('click', function() {
                btnSave.disabled = true;
                btnSave.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';

                const productIds = featuredProducts.map(function(p) { return p.id; });
                const saveUrl = CTX + '/pages/admin/admin-featured.jsp?ajax=save';

                fetch(saveUrl, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ productIds: productIds })
                })
                .then(function(res) { return res.json(); })
                .then(function(res) {
                    if (res && res.success) {
                        showFeedback('Đã lưu danh sách sản phẩm nổi bật thành công!', true);
                    } else {
                        showFeedback('Lỗi: ' + ((res && res.message) ? res.message : 'Không thể lưu'), false);
                    }
                })
                .catch(function(err) {
                    showFeedback('Lỗi kết nối: ' + err.message, false);
                })
                .finally(function() {
                    btnSave.disabled = false;
                    btnSave.innerHTML = '<i class="fa-solid fa-floppy-disk"></i> Lưu thay đổi';
                });
            });

            // Khởi chạy ban đầu ngay lập tức từ dữ liệu server đã nạp sẵn
            renderFeatured();
            renderSearch();
        })();
    </script>
</body>
</html>
