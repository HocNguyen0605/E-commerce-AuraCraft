// --- CHỨC NĂNG LỌC THEO TAB ---
document.addEventListener("DOMContentLoaded", function() {
    const tabs = document.querySelectorAll('.tab-item');
    const orders = document.querySelectorAll('.order-card');

    tabs.forEach(tab => {
        tab.addEventListener('click', () => {
            // Xóa active cũ, gắn active mới
            tabs.forEach(t => t.classList.remove('active'));
            tab.classList.add('active');

            // Lấy giá trị data-tab để lọc
            const filter = tab.getAttribute('data-tab');

            orders.forEach(order => {
                const status = order.getAttribute('data-status');
                if (filter === 'all' || filter === status) {
                    order.style.display = 'block';
                } else {
                    order.style.display = 'none';
                }
            });
        });
    });

    // --- CHỨC NĂNG TÌM KIẾM CƠ BẢN ---
    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const keyword = this.value.toLowerCase();
            orders.forEach(order => {
                const text = order.innerText.toLowerCase();
                if(text.includes(keyword)) {
                    order.style.display = 'block';
                } else {
                    order.style.display = 'none';
                }
            });
        });
    }

    // --- XỬ LÝ ĐÓNG MODAL KHI CLICK RA NGOÀI ---
    const orderModal = document.getElementById('orderModal');
    if (orderModal) {
        orderModal.addEventListener('click', function(e) {
            if (e.target === orderModal) closeOrderDetail();
        });
    }
});

// --- CHỨC NĂNG XEM CHI TIẾT MODAL ---
function openOrderDetail(cardElement) {
    document.getElementById('modalOrderId').innerText = cardElement.getAttribute('data-id');
    document.getElementById('modalProduct').innerText = cardElement.getAttribute('data-product');
    document.getElementById('modalSeller').innerText = cardElement.getAttribute('data-seller');
    document.getElementById('modalBuyer').innerText = cardElement.getAttribute('data-buyer');
    document.getElementById('modalDate').innerText = cardElement.getAttribute('data-date');
    document.getElementById('modalAddress').innerText = cardElement.getAttribute('data-address');
    
    const priceStr = cardElement.getAttribute('data-price');
    document.getElementById('modalPrice').innerText = priceStr;
    
    // Giả lập tính phí sàn 10%
    try {
        const priceVal = parseInt(priceStr.replace(/[^0-9]/g, ''));
        const fee = priceVal * 0.1;
        document.getElementById('modalFee').innerText = "-" + fee.toLocaleString('vi-VN') + " đ";
    } catch(e) {
        document.getElementById('modalFee').innerText = "Đang tính...";
    }

    document.getElementById('orderModal').classList.add('active');
}

function closeOrderDetail() {
    document.getElementById('orderModal').classList.remove('active');
}

// --- CÁC HÀM XỬ LÝ QUYỀN ADMIN (BR45, UC 3.8, UC 3.9) ---
function notifySeller(orderId) {
    alert("Hệ thống đã gửi cảnh báo đến Thợ thực hiện đơn " + orderId + " yêu cầu cập nhật tiến độ ngay lập tức! (UC 3.8)");
}

function extendDeadline(orderId) {
    let days = prompt("Nhập số ngày gia hạn cho đơn " + orderId + " (Ví dụ: 2):");
    if(days) {
        alert("Đã gia hạn thêm " + days + " ngày cho đơn " + orderId + ". Thông báo đã được gửi đến Người mua.");
    }
}

function cancelAndRefund(orderId) {
    if(confirm("CẢNH BÁO: Bạn có chắc chắn muốn HỦY đơn " + orderId + " và hoàn tiền cho Người mua? Hành động này sẽ ghi lỗi cho Thợ. (BR48)")) {
        alert("Đơn " + orderId + " đã bị hủy. Hệ thống đang tiến hành hoàn tiền qua cổng thanh toán VNPay/MoMo. (UC 3.10)");
    }
}

function trackShipping(orderId, partner) {
    alert("Đang kết nối cổng API của " + partner + " để kiểm tra vận đơn cho " + orderId + "... (UC 3.9)");
}
