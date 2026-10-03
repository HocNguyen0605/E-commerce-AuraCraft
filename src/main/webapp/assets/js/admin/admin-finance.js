// --- CHART.JS CONFIGURATION ---
document.addEventListener("DOMContentLoaded", function() {
    // 1. Bar Chart (Doanh thu tổng & Phí sàn)
    const canvasBar = document.getElementById('revenueBarChart');
    if (canvasBar) {
        const ctxBar = canvasBar.getContext('2d');
        new Chart(ctxBar, {
            type: 'bar',
            data: {
                labels: ['Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9'],
                datasets: [
                    {
                        label: 'Giá trị giao dịch (GMV)',
                        data: [120, 150, 180, 140, 210, 250],
                        backgroundColor: '#425B9A',
                        borderRadius: 4
                    },
                    {
                        label: 'Phí sàn thu được',
                        data: [12, 15, 18, 14, 21, 25],
                        backgroundColor: '#D4AF37',
                        borderRadius: 4
                    }
                ]
            },
            options: {
                responsive: true,
                plugins: {
                    legend: { position: 'top' },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.dataset.label + ': ' + context.raw + ' Triệu VNĐ';
                            }
                        }
                    }
                },
                scales: {
                    y: { beginAtZero: true }
                }
            }
        });
    }

    // 2. Pie Chart (Tỷ trọng danh mục)
    const canvasPie = document.getElementById('revenuePieChart');
    if (canvasPie) {
        const ctxPie = canvasPie.getContext('2d');
        new Chart(ctxPie, {
            type: 'doughnut',
            data: {
                labels: ['Trang sức', 'Nến thơm', 'Gốm sứ', 'Đồ da', 'Khác'],
                datasets: [{
                    data: [40, 25, 15, 15, 5],
                    backgroundColor: ['#425B9A', '#F48FB1', '#D4AF37', '#2ECC71', '#95A5A6'],
                    borderWidth: 0
                }]
            },
            options: {
                responsive: true,
                cutout: '60%',
                plugins: {
                    legend: { position: 'bottom' }
                }
            }
        });
    }

    // --- SETUP MODAL CẤU HÌNH ---
    const configModal = document.getElementById('configModal');
    if (configModal) {
        configModal.style.display = 'none'; // Default hide modal
    }
});

// --- XỬ LÝ RÚT TIỀN (UC 3.11) ---
function approveWithdrawal(id) {
    if(confirm("Xác nhận đã chuyển khoản thành công cho yêu cầu " + id + "? Hành động này sẽ thay đổi trạng thái và trừ số dư của Thợ.")) {
        alert("Đã duyệt thành công Payout " + id + "!");
    }
}

// --- XỬ LÝ CẤU HÌNH PHÍ SÀN (BR49) ---
function openConfigModal() {
    const configModal = document.getElementById('configModal');
    if (configModal) {
        configModal.style.display = 'flex';
        configModal.style.alignItems = 'center';
        configModal.style.justifyContent = 'center';
    }
}

function closeConfigModal() {
    const configModal = document.getElementById('configModal');
    if (configModal) {
        configModal.style.display = 'none';
    }
}

function saveConfig() {
    const newFee = document.getElementById('feePercentage').value;
    if(newFee === '' || newFee < 0 || newFee > 100) {
        alert("Vui lòng nhập tỷ lệ % hợp lệ (0-100).");
        return;
    }
    alert("Đã cập nhật tỷ lệ Phí sàn toàn hệ thống thành: " + newFee + "%");
    closeConfigModal();
}
