// --- LOAD ADMIN SIDEBAR & CHỨC NĂNG CHUNG ---
document.addEventListener("DOMContentLoaded", function() {
    const sidebarPlaceholder = document.getElementById('admin-sidebar-placeholder');
    if (sidebarPlaceholder) {
        const CTX = location.pathname.split('/')[1] ? '/' + location.pathname.split('/')[1] : '';
        fetch(CTX + '/components/admin-sidebar.html')
            .then(response => response.text())
            .then(data => {
                sidebarPlaceholder.outerHTML = data;
                
                // Cập nhật trạng thái active cho menu dựa trên URL hiện tại
                setTimeout(() => {
                    const currentPage = window.location.pathname.split('/').pop();
                    const menuItems = document.querySelectorAll('.menu-item');
                    menuItems.forEach(item => {
                        item.classList.remove('active');
                        // So sánh href với URL trang hiện tại
                        if (item.getAttribute('href') === currentPage) {
                            item.classList.add('active');
                        }
                    });
                }, 100);
            }).catch(e => console.log('Lỗi tải sidebar:', e));
    }
});

// --- CÁC HÀM TIỆN ÍCH CHUNG CHO TOÀN BỘ TRANG ADMIN ---

// Định dạng tiền tệ VNĐ
window.formatMoney = function(value) {
    if (!value && value !== 0) return "0 đ";
    return Number(value).toLocaleString("vi-VN") + " đ";
};

// Định dạng ngày tháng
window.formatDate = function(dateStr) {
    if (!dateStr) return "N/A";
    try {
        var d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString("vi-VN") + " - " +
            d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
    } catch (e) {
        return dateStr;
    }
};

// Xử lý chống XSS
window.escapeHtml = function(text) {
    if (!text) return "";
    var div = document.createElement("div");
    div.textContent = text;
    return div.innerHTML;
};
window.CTX = window.CTX || ('/' + location.pathname.split('/')[1]);
document.addEventListener('click', e => {
    const home = e.target.closest('[data-home]');
    if (home) {
        e.preventDefault();
        location.href = window.CTX + '/index.html';
        return;
    }
    const out = e.target.closest('[data-logout]');
    if (out) {
        e.preventDefault();
        fetch(window.CTX + '/logout', { method: 'POST' })
            .finally(() => location.href = window.CTX + '/index.html');
    }
});