let myBarChart = null;
let myPieChart = null;

document.addEventListener("DOMContentLoaded", function() {
    loadChartsData();

    //  SETUP MODAL CẤU HÌNH
    const configModal = document.getElementById('configModal');
    if (configModal) {
        configModal.style.display = 'none';
    }
});

// ─── Helper: format tiền VND ──────────────────────────────────────────────────
function formatVND(amount) {
    if (amount == null || isNaN(amount)) return '0 đ';
    return new Intl.NumberFormat('vi-VN').format(Math.round(amount)) + ' đ';
}

// ─── Load dữ liệu từ Servlet ──────────────────────────────────────────────────
function loadChartsData() {
    const startDate = document.getElementById('startDate') ? document.getElementById('startDate').value : '';
    const endDate   = document.getElementById('endDate')   ? document.getElementById('endDate').value   : '';

    // Đường dẫn tương đối từ thư mục pages/
    const url = `../api/admin/finance/charts?startDate=${startDate}&endDate=${endDate}`;

    fetch(url)
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }
            return response.json();
        })
        .then(data => {
            console.log('[Finance] Dữ liệu nhận được:', data);
            updateStats(data);
            renderBarChart(data);
            renderPieChart(data);
        })
        .catch(error => {
            console.error('[Finance] Lỗi khi lấy dữ liệu:', error);
            // Hiển thị lỗi lên stat cards thay vì alert
            ['statTotalGmv','statTotalFee','statTotalPaid','statTotalPending'].forEach(id => {
                const el = document.getElementById(id);
                if (el) el.textContent = 'Lỗi tải dữ liệu';
            });
        });
}

// ─── Cập nhật 4 stat cards ────────────────────────────────────────────────────
function updateStats(data) {
    const gmvEl     = document.getElementById('statTotalGmv');
    const feeEl     = document.getElementById('statTotalFee');
    const paidEl    = document.getElementById('statTotalPaid');
    const pendingEl = document.getElementById('statTotalPending');

    if (gmvEl)     gmvEl.textContent     = formatVND(data.totalGmv);
    if (feeEl)     feeEl.textContent     = formatVND(data.totalFee);
    if (paidEl)    paidEl.textContent    = formatVND(data.totalPaidToSeller);
    if (pendingEl) pendingEl.textContent = formatVND(data.totalPendingBalance);
}

// ─── Bar Chart ────────────────────────────────────────────────────────────────
function renderBarChart(data) {
    const canvasBar = document.getElementById('revenueBarChart');
    if (!canvasBar) return;

    if (myBarChart) {
        myBarChart.destroy();
    }

    // Nếu không có dữ liệu → hiển thị thông báo thay vì chart rỗng
    const labels  = (data.barLabels  && data.barLabels.length  > 0) ? data.barLabels  : ['Chưa có dữ liệu'];
    const gmvData = (data.barGmvData && data.barGmvData.length > 0) ? data.barGmvData : [0];
    const feeData = (data.barFeeData && data.barFeeData.length > 0) ? data.barFeeData : [0];

    const ctxBar = canvasBar.getContext('2d');
    myBarChart = new Chart(ctxBar, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Giá trị giao dịch (GMV - Triệu đ)',
                    data: gmvData,
                    backgroundColor: '#425B9A',
                    borderRadius: 4
                },
                {
                    label: 'Phí sàn thu được (Triệu đ)',
                    data: feeData,
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
                            const val = context.raw || 0;
                            return context.dataset.label + ': ' + val.toFixed(2) + ' Triệu đ';
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

// ─── Pie / Doughnut Chart ─────────────────────────────────────────────────────
function renderPieChart(data) {
    const canvasPie = document.getElementById('revenuePieChart');
    if (!canvasPie) return;

    if (myPieChart) {
        myPieChart.destroy();
    }

    const hasPieData = data.pieLabels && data.pieLabels.length > 0;
    const labels     = hasPieData ? data.pieLabels : ['Chưa có dữ liệu'];
    const pieData    = hasPieData ? data.pieData   : [1];
    const colors     = hasPieData
        ? ['#425B9A', '#F48FB1', '#D4AF37', '#2ECC71', '#95A5A6', '#E74C3C', '#9B59B6']
        : ['#e0e0e0'];

    const ctxPie = canvasPie.getContext('2d');
    myPieChart = new Chart(ctxPie, {
        type: 'doughnut',
        data: {
            labels: labels,
            datasets: [{
                data: pieData,
                backgroundColor: colors,
                borderWidth: 0
            }]
        },
        options: {
            responsive: true,
            cutout: '60%',
            plugins: {
                legend: { position: 'bottom' },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            if (!hasPieData) return 'Không có dữ liệu';
                            const val = context.raw || 0;
                            return context.label + ': ' + formatVND(val);
                        }
                    }
                }
            }
        }
    });
}

// ─── XỬ LÝ RÚT TIỀN ──────────────────────────────────────────────────────────
function approveWithdrawal(id) {
    if (confirm("Xác nhận đã chuyển khoản thành công cho yêu cầu " + id + "?\nHành động này sẽ thay đổi trạng thái và trừ số dư của Thợ.")) {
        alert("Đã duyệt thành công Payout " + id + "!");
    }
}

// ─── XỬ LÝ CẤU HÌNH PHÍ SÀN ─────────────────────────────────────────────────
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
    if (newFee === '' || newFee < 0 || newFee > 100) {
        alert("Vui lòng nhập tỷ lệ % hợp lệ (0-100).");
        return;
    }
    alert("Đã cập nhật tỷ lệ Phí sàn toàn hệ thống thành: " + newFee + "%");
    closeConfigModal();
}
