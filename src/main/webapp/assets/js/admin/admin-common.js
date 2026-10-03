// --- LOAD ADMIN SIDEBAR & CHỨC NĂNG CHUNG ---
document.addEventListener("DOMContentLoaded", function() {
    const sidebarPlaceholder = document.getElementById('admin-sidebar-placeholder');
    if (sidebarPlaceholder) {
        fetch('../components/admin-sidebar.html')
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
