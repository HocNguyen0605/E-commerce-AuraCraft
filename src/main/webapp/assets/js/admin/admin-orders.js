/**
 * Admin Orders Management - Dynamic rendering from DB via /api/admin/orders
 */
document.addEventListener("DOMContentLoaded", function () {
    const orderList = document.getElementById("orderList");
    const searchInput = document.getElementById("searchInput");
    const filterShop = document.getElementById("filterShop");
    const filterSort = document.getElementById("filterSort");
    const emptyState = document.getElementById("emptyOrders");
    const feedback = document.getElementById("orderFeedback");
    const orderModal = document.getElementById("orderModal");

    const statusLabels = {
        pending: "Chờ xử lý",
        paid: "Đã thanh toán",
        processing: "Đang xử lý",
        shipping: "Đang giao hàng",
        completed: "Đã hoàn thành",
        cancelled: "Đã hủy",
        refunded: "Đã hoàn tiền"
    };

    const statusBadgeClass = {
        pending: "pending",
        paid: "pending",
        processing: "crafting",
        shipping: "shipping",
        completed: "done",
        cancelled: "late",
        refunded: "late"
    };

    // Map statuses to tab filter groups
    const statusToTab = {
        pending: "pending",
        paid: "pending",
        processing: "processing",
        shipping: "shipping",
        completed: "completed",
        cancelled: "cancelled",
        refunded: "cancelled"
    };

    let ordersData = [];
    let currentModalOrderId = null;

    // --- LOAD DATA FROM API ---
    function loadOrders() {
        fetch("../api/admin/orders?t=" + new Date().getTime())
            .then(function (res) {
                if (!res.ok) throw new Error("Lỗi khi tải dữ liệu đơn hàng: " + res.statusText);
                return res.json();
            })
            .then(function (data) {
                ordersData = data.orders || [];
                updateTabs(data);
                populateShopFilter(data.shops || []);
                renderOrders();
            })
            .catch(function (err) {
                console.error("Lỗi:", err);
                if (feedback) feedback.textContent = "Lỗi khi tải dữ liệu. Vui lòng thử lại.";
            });
    }

    // --- UPDATE TAB COUNTS ---
    function updateTabs(data) {
        var tabAll = document.getElementById("tabAll");
        var tabPending = document.getElementById("tabPending");
        var tabProcessing = document.getElementById("tabProcessing");
        var tabShipping = document.getElementById("tabShipping");
        var tabCompleted = document.getElementById("tabCompleted");
        var tabCancelled = document.getElementById("tabCancelled");

        if (tabAll) tabAll.textContent = data.totalOrders || 0;
        if (tabPending) tabPending.textContent = data.pendingOrders || 0;
        if (tabProcessing) tabProcessing.textContent = data.processingOrders || 0;
        if (tabShipping) tabShipping.textContent = data.shippingOrders || 0;
        if (tabCompleted) tabCompleted.textContent = data.completedOrders || 0;
        if (tabCancelled) tabCancelled.textContent = data.cancelledOrders || 0;
    }

    // --- POPULATE SHOP FILTER FROM DB ---
    function populateShopFilter(shops) {
        if (!filterShop) return;
        // Keep first option "Tất cả"
        filterShop.innerHTML = '<option value="all">Tất cả Cửa hàng / Thợ</option>';
        shops.forEach(function (shop) {
            var opt = document.createElement("option");
            opt.value = String(shop.shopId);
            opt.textContent = shop.ownerName + " (" + shop.shopName + ")";
            filterShop.appendChild(opt);
        });
    }

    // --- HELPER: SEARCH MATCHING ---
    function matchSearchKeyword(order, keyword) {
        if (!keyword) return true;
        var searchable = [
            "#ORD-" + order.orderId,
            order.buyerName || "",
            order.buyerEmail || "",
            order.sellerName || "",
            order.shopName || "",
            order.productSummary || "",
            order.shippingAddress || ""
        ].join(" ").toLowerCase();
        return searchable.indexOf(keyword) !== -1;
    }

    // --- BUILD ORDER CARD ---
    function buildOrderCard(order) {
        var card = document.createElement("div");
        card.className = "order-card" + (order.custom ? " admin-custom-order-card" : "");
        card.setAttribute("data-status", statusToTab[order.status] || order.status);

        // Special styling for cancelled/refunded
        if (order.status === "cancelled" || order.status === "refunded") {
            card.style.borderLeft = "4px solid var(--status-late)";
            card.style.opacity = "0.85";
        } else if (order.status === "completed") {
            card.style.opacity = "0.85";
        }

        // Header
        var header = document.createElement("div");
        header.className = "order-header";

        var idSpan = document.createElement("span");
        idSpan.className = "order-id";
        if (order.status === "cancelled" || order.status === "refunded") {
            idSpan.style.color = "var(--status-late)";
        }
        idSpan.textContent = "Mã ĐH: #ORD-" + order.orderId;

        var badge = document.createElement("span");
        badge.className = "badge " + (statusBadgeClass[order.status] || "pending");
        badge.textContent = statusLabels[order.status] || order.status;

        header.appendChild(idSpan);
        header.appendChild(badge);

        // Body
        var body = document.createElement("div");
        body.className = "order-body";
        body.style.cursor = "pointer";
        body.title = "Nhấn để xem chi tiết";
        body.addEventListener("click", function () {
            openOrderDetail(order);
        });

        var info = document.createElement("div");
        info.className = order.custom ? "admin-custom-order-summary" : "order-info";

        var title = document.createElement("h3");
        title.className = "hover-title";
        title.textContent = order.productSummary || "Đơn hàng #" + order.orderId;
        if (order.status === "completed") {
            title.style.color = "var(--text-muted)";
        }

        var participants = document.createElement("div");
        participants.className = "order-participants";

        var sellerSpan = document.createElement("span");
        sellerSpan.className = "participant";
        sellerSpan.innerHTML = '<i class="fa-solid fa-hammer"></i> Thợ: <strong>' +
            escapeHtml(order.sellerName || "N/A") + '</strong>';

        var buyerSpan = document.createElement("span");
        buyerSpan.className = "participant";
        buyerSpan.innerHTML = '<i class="fa-solid fa-user"></i> Người mua: <strong>' +
            escapeHtml(order.buyerName || "N/A") + '</strong>';

        participants.appendChild(sellerSpan);
        participants.appendChild(buyerSpan);

        // Extra info line
        var extraInfo = document.createElement("span");
        extraInfo.className = "deadline";
        if (order.status === "shipping") {
            extraInfo.innerHTML = '<i class="fa-solid fa-truck"></i> Đang giao - ' +
                escapeHtml(order.paymentMethod || "");
            extraInfo.style.color = "var(--primary-blue)";
        } else if (order.status === "pending") {
            extraInfo.innerHTML = '<i class="fa-solid fa-money-bill"></i> Chờ xử lý thanh toán qua ' +
                escapeHtml(order.paymentMethod || "");
            extraInfo.style.color = "var(--text-muted)";
            extraInfo.style.background = "var(--gray-light)";
        } else if (order.status === "processing") {
            extraInfo.innerHTML = '<i class="fa-regular fa-clock"></i> Đang xử lý - Đặt ngày: ' +
                formatDate(order.dateCreate);
        } else if (order.status === "completed") {
            extraInfo.innerHTML = '<i class="fa-solid fa-check-circle"></i> Hoàn thành - Thanh toán: ' +
                formatDate(order.datePaid);
            extraInfo.style.color = "var(--text-muted)";
        } else if (order.status === "cancelled" || order.status === "refunded") {
            extraInfo.innerHTML = '<i class="fa-solid fa-triangle-exclamation"></i> ' +
                statusLabels[order.status];
            extraInfo.classList.add("late-alert");
        }

        info.appendChild(title);
        info.appendChild(participants);
        if (extraInfo.innerHTML) info.appendChild(extraInfo);
        body.appendChild(info);

        // Footer
        var footer = document.createElement("div");
        footer.className = "order-footer";

        var price = document.createElement("div");
        price.className = "total-price";
        price.textContent = formatMoney(order.amount);

        var actions = document.createElement("div");
        actions.className = order.custom ? "admin-custom-order-actions" : "order-actions";

        var detailBtn = document.createElement("button");
        detailBtn.className = "btn btn-outline";
        detailBtn.textContent = "Xem chi tiết";
        detailBtn.addEventListener("click", function (e) {
            e.stopPropagation();
            openOrderDetail(order);
        });
        actions.appendChild(detailBtn);

        // Cancel button for active orders
        if (["pending", "paid", "processing"].includes(order.status)) {
            var cancelBtn = document.createElement("button");
            cancelBtn.className = "action-btn-danger";
            cancelBtn.textContent = "Hủy đơn";
            cancelBtn.addEventListener("click", function (e) {
                e.stopPropagation();
                cancelOrder(order.orderId);
            });
            actions.appendChild(cancelBtn);
        }

        footer.appendChild(price);
        footer.appendChild(actions);

        card.appendChild(header);
        card.appendChild(body);
        card.appendChild(footer);

        return card;
    }

    // --- RENDER ORDERS ---
    function renderOrders() {
        if (!orderList) return;

        var keyword = searchInput ? searchInput.value.trim().toLowerCase() : "";
        var shopFilter = filterShop ? filterShop.value : "all";
        var sortValue = filterSort ? filterSort.value : "newest";

        // Get active tab
        var activeTab = document.querySelector(".tab-item.active");
        var tabFilter = activeTab ? activeTab.getAttribute("data-tab") : "all";

        var customSearch = document.getElementById("customOrderSearch");
        var customStatusFilter = document.getElementById("customOrderStatus");
        var customList = document.getElementById("adminCustomOrderList");

        var customKeyword = customSearch ? customSearch.value.trim().toLowerCase() : "";
        var customStatus = customStatusFilter ? customStatusFilter.value : "all";

        var normalFiltered = [];
        var customFiltered = [];

        ordersData.forEach(function(order) {
            if (order.custom) {
                // Filter custom orders
                var matchStatus = (customStatus === "all" || order.status === customStatus);
                var matchSearch = matchSearchKeyword(order, customKeyword);
                if (matchStatus && matchSearch) {
                    customFiltered.push(order);
                }
            } else {
                // Filter normal orders
                var matchTab = true;
                var orderTab = statusToTab[order.status] || order.status;
                if (tabFilter !== "all" && orderTab !== tabFilter) matchTab = false;

                var matchShop = true;
                if (shopFilter !== "all") {
                    var filterOpt = filterShop.options[filterShop.selectedIndex];
                    if (filterOpt && filterOpt.textContent) {
                        var filterText = filterOpt.textContent.toLowerCase();
                        matchShop = (order.sellerName && order.sellerName.toLowerCase().indexOf(filterText.split("(")[0].trim().toLowerCase()) !== -1) ||
                                    (order.shopName && order.shopName.toLowerCase().indexOf(filterText.toLowerCase()) !== -1);
                    }
                }

                var matchSearch = matchSearchKeyword(order, keyword);

                if (matchTab && matchShop && matchSearch) {
                    normalFiltered.push(order);
                }
            }
        });

        // Sort normal orders
        normalFiltered.sort(function (a, b) {
            if (sortValue === "oldest") {
                return new Date(a.dateCreate) - new Date(b.dateCreate);
            } else if (sortValue === "high_price") {
                return b.amount - a.amount;
            }
            return new Date(b.dateCreate) - new Date(a.dateCreate);
        });

        // Render Normal Orders
        orderList.innerHTML = "";
        normalFiltered.forEach(function (order) {
            orderList.appendChild(buildOrderCard(order));
        });

        if (emptyState) {
            emptyState.hidden = normalFiltered.length > 0;
        }

        // Render Custom Orders
        if (customList) {
            customList.innerHTML = "";
            customFiltered.forEach(function(order) {
                customList.appendChild(buildOrderCard(order));
            });
        }
    }

    // --- SEARCH & FILTER EVENTS ---
    if (searchInput) searchInput.addEventListener("input", renderOrders);
    if (filterShop) filterShop.addEventListener("change", renderOrders);
    if (filterSort) filterSort.addEventListener("change", renderOrders);
    var customSearch = document.getElementById("customOrderSearch");
    if (customSearch) customSearch.addEventListener("input", renderOrders);
    var customStatusFilter = document.getElementById("customOrderStatus");
    if (customStatusFilter) customStatusFilter.addEventListener("change", renderOrders);

    // --- OPEN ORDER DETAIL MODAL ---
    function openOrderDetail(order) {
        currentModalOrderId = order.orderId;

        document.getElementById("modalOrderId").textContent = "#ORD-" + order.orderId;
        document.getElementById("modalProduct").textContent = order.productSummary || "Đơn hàng #" + order.orderId;
        document.getElementById("modalSeller").textContent = order.sellerName || "N/A";
        document.getElementById("modalShop").textContent = order.shopName || "N/A";
        document.getElementById("modalBuyer").textContent = order.buyerName || "N/A";
        document.getElementById("modalBuyerEmail").textContent = order.buyerEmail || "N/A";
        document.getElementById("modalDate").textContent = formatDate(order.dateCreate);
        document.getElementById("modalDatePaid").textContent = order.datePaid ? formatDate(order.datePaid) : "Chưa thanh toán";
        document.getElementById("modalPayment").textContent = order.paymentMethod || "N/A";
        document.getElementById("modalAddress").textContent = order.shippingAddress || "N/A";
        document.getElementById("modalItemCount").textContent = order.itemCount || 0;
        document.getElementById("modalPrice").textContent = formatMoney(order.amount);
        document.getElementById("modalShippingFee").textContent = "+" + formatMoney(order.shippingFee);
        document.getElementById("modalDiscount").textContent = order.discount > 0 ? "-" + formatMoney(order.discount) : "0 đ";
        document.getElementById("modalFee").textContent = "-" + formatMoney(order.totalFee);

        // Set current status in select
        var statusSelect = document.getElementById("modalStatusSelect");
        if (statusSelect) {
            statusSelect.value = order.status;
        }

        orderModal.classList.add("active");
    }

    // --- CLOSE MODAL ---
    window.closeOrderDetail = function () {
        orderModal.classList.remove("active");
        currentModalOrderId = null;
    };

    // Close on overlay click
    if (orderModal) {
        orderModal.addEventListener("click", function (e) {
            if (e.target === orderModal) closeOrderDetail();
        });
    }

    // --- UPDATE ORDER STATUS ---
    var btnUpdateStatus = document.getElementById("btnUpdateStatus");
    if (btnUpdateStatus) {
        btnUpdateStatus.addEventListener("click", function () {
            if (!currentModalOrderId) return;

            var newStatus = document.getElementById("modalStatusSelect").value;

            // Confirm dangerous actions
            if (newStatus === "cancelled" || newStatus === "refunded") {
                if (!confirm("CẢNH BÁO: Bạn có chắc chắn muốn chuyển đơn #ORD-" + currentModalOrderId +
                    " sang trạng thái \"" + statusLabels[newStatus] + "\"?")) {
                    return;
                }
            }

            fetch("../api/admin/orders", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    orderId: currentModalOrderId,
                    status: newStatus
                })
            })
            .then(function (res) { return res.json(); })
            .then(function (data) {
                if (data.success) {
                    if (feedback) feedback.textContent = "Đã cập nhật trạng thái đơn #ORD-" +
                        currentModalOrderId + " thành \"" + statusLabels[newStatus] + "\".";
                    closeOrderDetail();
                    loadOrders(); // Reload data
                } else {
                    throw new Error(data.message || "Lỗi cập nhật");
                }
            })
            .catch(function (err) {
                alert("Cập nhật thất bại: " + err.message);
            });
        });
    }

    // --- CANCEL ORDER (from list button) ---
    window.cancelOrder = function (orderId) {
        if (!confirm("CẢNH BÁO: Bạn có chắc chắn muốn HỦY đơn #ORD-" + orderId + "?")) return;

        fetch("../api/admin/orders", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                orderId: orderId,
                status: "cancelled"
            })
        })
        .then(function (res) { return res.json(); })
        .then(function (data) {
            if (data.success) {
                if (feedback) feedback.textContent = "Đã hủy đơn #ORD-" + orderId + " thành công.";
                loadOrders();
            } else {
                throw new Error(data.message || "Lỗi hủy đơn");
            }
        })
        .catch(function (err) {
            alert("Hủy đơn thất bại: " + err.message);
        });
    };

    // --- TAB CLICK ---
    var tabs = document.querySelectorAll(".tab-item");
    tabs.forEach(function (tab) {
        tab.addEventListener("click", function () {
            tabs.forEach(function (t) { t.classList.remove("active"); });
            tab.classList.add("active");
            renderOrders();
        });
    });



    // --- INITIAL LOAD ---
    loadOrders();
});
