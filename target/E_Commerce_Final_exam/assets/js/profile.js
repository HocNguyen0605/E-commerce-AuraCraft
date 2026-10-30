(function () {
    function setText(el, value, fallback) {
        // textContent (không dùng innerHTML) để tránh XSS
        el.textContent = (value !== null && value !== undefined && value !== '') ? value : (fallback || '');
    }

    fetch(CTX + '/api/profile', { credentials: 'same-origin' })
        .then(r => {
            if (r.status === 401) {
                location.href = CTX + '/pages/login';
                return null;
            }
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        })
        .then(u => {
            if (!u) return;

            // Phần đầu trang: tên + email
            setText(document.getElementById('profileName'), u.name);
            setText(document.getElementById('profileEmail'), u.email);

            // Các phần tử có data-profile="tên_trường" (dùng cho trang Thông tin cá nhân)
            document.querySelectorAll('[data-profile]').forEach(el => {
                const key = el.dataset.profile;
                const val = u[key];
                if (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA') {
                    el.value = val ?? '';
                } else {
                    setText(el, val, 'Chưa cập nhật');
                }
            });

            // Danh sách địa chỉ (nếu trang có #addressList)
            const list = document.getElementById('addressList');
            if (list) {
                list.innerHTML = '';
                if (!u.addresses.length) {
                    list.textContent = 'Chưa có địa chỉ nào.';
                }
                u.addresses.forEach(a => {
                    const div = document.createElement('div');
                    const head = document.createElement('b');
                    head.textContent = a.receiver + ' - ' + a.phone;
                    const addr = document.createElement('p');
                    addr.textContent = a.address + (a.isDefault ? ' [Mặc định]' : '');
                    div.append(head, addr);
                    list.appendChild(div);
                });
            }
        })
        .catch(e => {
            console.error('Lỗi tải profile:', e);
            setText(document.getElementById('profileName'), 'Không tải được thông tin');
        });
})();
