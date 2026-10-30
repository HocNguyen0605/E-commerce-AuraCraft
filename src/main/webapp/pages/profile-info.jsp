<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Thông tin cá nhân</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/profile-content.css">
    <style>
        /* Thông báo */
        .flash { padding: 10px 14px; border-radius: 6px; margin-bottom: 14px; font-size: 14px; }
        .flash-success { background: #e8f7ee; color: #1e7e46; border: 1px solid #b9e4c9; }
        .flash-error { background: #fdecea; color: #b3261e; border: 1px solid #f5c2be; }
        /* Hành động trên địa chỉ */
        .address-actions { display: flex; gap: 6px; align-items: center; flex-wrap: wrap; }
        .address-actions form { display: inline; margin: 0; }
        .link-btn { background: none; border: none; padding: 0; color: #2a6fdb; cursor: pointer; font: inherit; }
        .link-btn:hover { text-decoration: underline; }
        .link-btn.danger { color: #E74C3C; }
        /* Modal địa chỉ */
        .modal-overlay { display: none; position: fixed; inset: 0; background: rgba(0,0,0,.45); z-index: 1000;
            align-items: center; justify-content: center; padding: 16px; }
        .modal-overlay.open { display: flex; }
        .modal-box { background: #fff; border-radius: 10px; width: 100%; max-width: 480px; padding: 22px; max-height: 90vh; overflow-y: auto; }
        .modal-box h3 { margin: 0 0 14px; }
        .modal-box .form-group { margin-bottom: 12px; }
        .modal-box .form-group label { display: block; margin-bottom: 4px; }
        .modal-box .form-input { width: 100%; box-sizing: border-box; }
        .modal-check { display: flex; gap: 8px; align-items: center; margin-bottom: 14px; }
        .modal-actions { display: flex; justify-content: flex-end; gap: 8px; }
        .modal-box select.form-input { background: #fff; }
        .tag-warn { background: #fff4e5; color: #b26a00; border: 1px solid #f3d3a2; border-radius: 4px; padding: 1px 6px; font-size: 12px; margin-left: 6px; }
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
            <!--Thanh menu -->
            <div class="col-3 over_content">
                <ul class="menu_function">
                    <li class="funciton_item active"><a href="${ctx}/pages/profile-info">Thông tin cá nhân</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-orders">Đơn hàng</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-designs">Thiết kế đã lưu</a></li>
                    <li class="funciton_item"><a href="${ctx}/pages/profile-quotes">Báo giá</a></li>
                    <li class="funciton_item">
                        <a href="#" data-logout style="color: #E74C3C;">
                            <i class="fa-solid fa-arrow-right-from-bracket"></i> Đăng xuất
                        </a>
                    </li>
                </ul>
            </div>
            <!--Màn hình hiển thị thông tin -->
            <div class="col-9 over_content">

                <c:if test="${not empty flashSuccess}"><div class="flash flash-success"><c:out value="${flashSuccess}"/></div></c:if>
                <c:if test="${not empty flashError}"><div class="flash flash-error"><c:out value="${flashError}"/></div></c:if>

                <div class="col-12 panel">
                    <h2 class="panel-title">Thông tin cá nhân</h2>
                    <form method="post" action="${ctx}/pages/profile-info">
                        <input type="hidden" name="action" value="updateProfile">
                        <div class="form-grid">
                            <div class="form-group"><label for="fullname">Họ và tên</label>
                                <input class="form-input" id="fullname" name="name" type="text" required maxlength="100"
                                       value="<c:out value='${profile.name}'/>"></div>
                            <div class="form-group"><label for="phone">Số điện thoại</label>
                                <input class="form-input" id="phone" name="phone" type="tel" placeholder="0901 234 567"
                                       pattern="[0-9 .\-]{10,14}" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"
                                       value="<c:out value='${profile.phone}'/>"></div>
                            <div class="form-group"><label for="email">Email</label>
                                <input class="form-input" id="email" type="email" value="<c:out value='${profile.email}'/>" readonly></div>
                            <div class="form-group"><label for="dob">Ngày sinh</label>
                                <input class="form-input" id="dob" name="birthday" type="date" value="${profile.birthday}"></div>
                        </div>
                        <div class="form-actions"><button type="submit" class="btn btn-primary btn-sm">Lưu thay đổi</button></div>
                    </form>
                </div>

                <div class="col-12 panel">
                    <h2 class="panel-title">Địa chỉ nhận hàng</h2>
                    <c:forEach var="a" items="${addresses}">
                        <div class="address-item">
                            <div><b><c:out value="${a.receiver}"/></b> | <c:out value="${a.phone}"/>
                                <c:if test="${a.defaultAddress}"><span class="tag">Mặc định</span></c:if>
                                <c:if test="${empty a.provinceCode or empty a.ward}"><span class="tag-warn">Địa chỉ cũ - vui lòng cập nhật tỉnh / xã</span></c:if>
                                <p><c:out value="${a.fullAddress}"/></p></div>
                            <div class="address-actions">
                                <button type="button" class="link-btn js-edit-address"
                                        data-id="${a.id}"
                                        data-receiver="<c:out value='${a.receiver}'/>"
                                        data-phone="<c:out value='${a.phone}'/>"
                                        data-province-code="<c:out value='${a.provinceCode}'/>"
                                        data-ward-code="<c:out value='${a.wardCode}'/>"
                                        data-ward="<c:out value='${a.ward}'/>"
                                        data-address="<c:out value='${a.address}'/>"
                                        data-default="${a.defaultAddress}">Sửa</button>
                                <span>·</span>
                                <form method="post" action="${ctx}/pages/profile-info"
                                      onsubmit="return confirm('Bạn có chắc muốn xóa địa chỉ này?');">
                                    <input type="hidden" name="action" value="deleteAddress">
                                    <input type="hidden" name="addressId" value="${a.id}">
                                    <button type="submit" class="link-btn danger">Xóa</button>
                                </form>
                                <c:if test="${not a.defaultAddress}">
                                    <span>·</span>
                                    <form method="post" action="${ctx}/pages/profile-info">
                                        <input type="hidden" name="action" value="setDefaultAddress">
                                        <input type="hidden" name="addressId" value="${a.id}">
                                        <button type="submit" class="link-btn">Đặt mặc định</button>
                                    </form>
                                </c:if>
                            </div>
                        </div>
                    </c:forEach>
                    <c:if test="${empty addresses}"><p>Bạn chưa có địa chỉ nhận hàng nào.</p></c:if>
                    <div class="form-actions"><a href="#" id="btnAddAddress" class="btn btn-outline btn-sm">Thêm địa chỉ mới</a></div>
                </div>

            </div>
        </div>
    </div>
</main>

<!-- Modal thêm / sửa địa chỉ -->
<div class="modal-overlay" id="addressModal" aria-hidden="true">
    <div class="modal-box" role="dialog" aria-modal="true" aria-labelledby="addressModalTitle">
        <h3 id="addressModalTitle">Thêm địa chỉ mới</h3>
        <form method="post" action="${ctx}/pages/profile-info" id="addressForm">
            <input type="hidden" name="action" id="addrAction" value="addAddress">
            <input type="hidden" name="addressId" id="addrId" value="">
            <div class="form-group"><label for="addrReceiver">Người nhận</label>
                <input class="form-input" id="addrReceiver" name="receiver" type="text" required maxlength="100"></div>
            <div class="form-group"><label for="addrPhone">Số điện thoại</label>
                <input class="form-input" id="addrPhone" name="phone" type="tel" required
                       pattern="[0-9 .\-]{10,14}" title="Số điện thoại gồm 10 chữ số, bắt đầu bằng 0"></div>
            <div class="form-group"><label for="addrProvince">Tỉnh / thành phố</label>
                <select class="form-input" id="addrProvince" name="provinceCode" required>
                    <option value="">Chọn tỉnh / thành phố</option>
<option value="01">Hà Nội</option><option value="04">Cao Bằng</option><option value="08">Tuyên Quang</option><option value="11">Điện Biên</option><option value="12">Lai Châu</option><option value="14">Sơn La</option><option value="15">Lào Cai</option><option value="19">Thái Nguyên</option><option value="20">Lạng Sơn</option><option value="22">Quảng Ninh</option><option value="24">Bắc Ninh</option><option value="25">Phú Thọ</option><option value="31">Hải Phòng</option><option value="33">Hưng Yên</option><option value="37">Ninh Bình</option><option value="38">Thanh Hóa</option><option value="40">Nghệ An</option><option value="42">Hà Tĩnh</option><option value="44">Quảng Trị</option><option value="46">Huế</option><option value="48">Đà Nẵng</option><option value="51">Quảng Ngãi</option><option value="52">Gia Lai</option><option value="56">Khánh Hòa</option><option value="66">Đắk Lắk</option><option value="68">Lâm Đồng</option><option value="75">Đồng Nai</option><option value="79">Thành phố Hồ Chí Minh</option><option value="80">Tây Ninh</option><option value="82">Đồng Tháp</option><option value="86">Vĩnh Long</option><option value="91">An Giang</option><option value="92">Cần Thơ</option><option value="96">Cà Mau</option>
                </select>
                <input type="hidden" name="province" id="addrProvinceName"></div>
            <div class="form-group"><label for="addrWard">Xã / phường</label>
                <select class="form-input" id="addrWard" name="wardCode" required disabled>
                    <option value="">Vui lòng chọn tỉnh / thành phố trước</option>
                </select>
                <input class="form-input" id="addrWardFallback" type="text" maxlength="120"
                       placeholder="Nhập xã, phường" hidden>
                <input type="hidden" name="ward" id="addrWardName"></div>
            <div class="form-group"><label for="addrAddress">Số nhà, tên đường / địa chỉ chi tiết</label>
                <input class="form-input" id="addrAddress" name="address" type="text" required maxlength="250"
                       placeholder="Số nhà, tên đường, thôn / tổ dân phố..."></div>
            <label class="modal-check"><input type="checkbox" name="isDefault" id="addrDefault"> Đặt làm địa chỉ mặc định</label>
            <div class="modal-actions">
                <button type="button" class="btn btn-outline btn-sm" id="btnCancelAddress">Hủy</button>
                <button type="submit" class="btn btn-primary btn-sm">Lưu</button>
            </div>
        </form>
    </div>
</div>

<div id="footer-placeholder"></div>

<script src="${ctx}/assets/js/vn-address.js"></script>
<script>
    const CTX = '${ctx}';
    fetch(CTX + '/components/header.html').then(r => r.text())
        .then(html => document.getElementById('header-placeholder').innerHTML = html);
    fetch(CTX + '/components/footer.html').then(r => r.text())
        .then(html => document.getElementById('footer-placeholder').innerHTML = html);

    // ===== Modal địa chỉ =====
    (function () {
        const modal = document.getElementById('addressModal');
        const $ = id => document.getElementById(id);
        const picker = VnAddressPicker.init({
            province: $('addrProvince'),
            ward: $('addrWard'),
            fallback: $('addrWardFallback'),
            provinceName: $('addrProvinceName'),
            wardName: $('addrWardName')
        });

        function openModal(data) {
            const isEdit = !!data;
            $('addressModalTitle').textContent = isEdit ? 'Sửa địa chỉ' : 'Thêm địa chỉ mới';
            $('addrAction').value = isEdit ? 'updateAddress' : 'addAddress';
            $('addrId').value = isEdit ? data.id : '';
            $('addrReceiver').value = isEdit ? data.receiver : '';
            $('addrPhone').value = isEdit ? data.phone : '';
            $('addrAddress').value = isEdit ? data.address : '';
            if (isEdit) picker.setValues(data.provinceCode, data.wardCode, data.ward);
            else picker.reset();
            $('addrDefault').checked = isEdit ? data.isDefault : false;
            // Địa chỉ đang mặc định thì không cho bỏ tick (phải đặt địa chỉ khác làm mặc định)
            $('addrDefault').disabled = isEdit && data.isDefault;
            modal.classList.add('open');
            modal.setAttribute('aria-hidden', 'false');
            $('addrReceiver').focus();
        }
        function closeModal() {
            modal.classList.remove('open');
            modal.setAttribute('aria-hidden', 'true');
        }

        $('btnAddAddress').addEventListener('click', e => { e.preventDefault(); openModal(null); });
        $('btnCancelAddress').addEventListener('click', closeModal);
        modal.addEventListener('click', e => { if (e.target === modal) closeModal(); });
        document.addEventListener('keydown', e => { if (e.key === 'Escape') closeModal(); });

        document.querySelectorAll('.js-edit-address').forEach(btn => {
            btn.addEventListener('click', () => openModal({
                id: btn.dataset.id,
                receiver: btn.dataset.receiver,
                phone: btn.dataset.phone,
                provinceCode: btn.dataset.provinceCode,
                wardCode: btn.dataset.wardCode,
                ward: btn.dataset.ward,
                address: btn.dataset.address,
                isDefault: btn.dataset.default === 'true'
            }));
        });

        // checkbox bị disabled sẽ không được gửi -> bật lại trước khi submit
        $('addressForm').addEventListener('submit', () => { $('addrDefault').disabled = false; });
    })();
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
</body>
</html>
