<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Khôi phục mật khẩu - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleLogin.css">
    <style>
        .form-alert { padding: 10px 14px; border-radius: 8px; font-size: .92rem; margin-bottom: 14px; }
        .form-alert[hidden] { display: none; }
        .form-alert.error { background: #fdecec; color: #b3261e; }
        .form-alert.success { background: #e8f5e9; color: #1b5e20; }
        .input-group[hidden], .success-state[hidden] { display: none; }
        .field-note { display: block; margin-top: 4px; font-size: .84rem; color: #6b6b6b; }
        .field-note.ok { color: #1b5e20; }
        .field-note.warn { color: #b3261e; }
        .field-note:empty { display: none; }

        .inline-row { display: flex; gap: 8px; align-items: stretch; }
        .inline-row .input-wrapper { flex: 1; min-width: 0; }
        .btn-inline { white-space: nowrap; padding-left: 16px; padding-right: 16px; }
        .btn-inline:disabled { opacity: .6; cursor: not-allowed; }
        input[readonly] { background: #f4f4f4; }

        /* Modal đặt mật khẩu mới */
        .fp-modal-overlay { position: fixed; inset: 0; z-index: 1000; display: flex; align-items: center;
            justify-content: center; padding: 16px; background: rgba(20, 16, 18, .55); }
        .fp-modal-overlay[hidden] { display: none; }
        .fp-modal { width: 100%; max-width: 420px; max-height: 92vh; overflow-y: auto; background: #fff;
            border-radius: 16px; padding: 28px 26px; box-shadow: 0 20px 50px rgba(0, 0, 0, .25); }
        .fp-modal h3 { margin: 0 0 6px; font-size: 1.35rem; }
        .fp-modal .auth-subtitle { margin-bottom: 16px; }
        .fp-cancel { display: block; width: 100%; margin-top: 10px; padding: 10px; background: none; border: 0;
            color: #6b6b6b; cursor: pointer; font: inherit; }
        .fp-cancel:hover { text-decoration: underline; }
    </style>
</head>

<body>
<main class="form-page">
    <div class="container">
        <div class="auth-wrapper">

            <div class="banner-side">
                <div class="banner-overlay"></div>
                <img src="https://i.pinimg.com/1200x/c0/95/08/c095086cdb736e0df54ad2bbe1831360.jpg"
                     alt="AuraCraft Security" class="banner-img">
                <div class="banner-content">
                    <h3>An toàn & Bảo mật</h3>
                    <p>Hỗ trợ khôi phục quyền truy cập tài khoản của bạn nhanh chóng.</p>
                </div>
            </div>

            <div class="form-side">
                <div class="top-header">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="brand-logo">
                        <span class="logo-icon">A</span>
                        <span class="logo-text">uraCraft</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/pages/login.jsp" class="auth-nav-back">← Quay lại Đăng nhập</a>
                </div>

                <div id="requestState">
                    <div class="form-header">
                        <div class="icon-box">
                            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
                                 fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"
                                 stroke-linejoin="round">
                                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                                <path d="M7 11V7a5 5 0 0 1 9.9-1"></path>
                            </svg>
                        </div>
                        <h2>Quên mật khẩu?</h2>
                        <p class="auth-subtitle">Đừng lo lắng! Hãy nhập địa chỉ email đã đăng ký. AuraCraft sẽ gửi
                            cho bạn mã xác thực (OTP) để thiết lập lại mật khẩu.</p>
                    </div>

                    <p id="formAlert" class="form-alert" role="alert" hidden></p>

                    <form id="forgotForm" novalidate>
                        <div class="input-group" id="emailGroup">
                            <label for="recoveryEmail">Địa chỉ Email</label>
                            <div class="inline-row">
                                <div class="input-wrapper">
                                    <i class="fa-regular fa-envelope field-icon"></i>
                                    <input type="email" id="recoveryEmail" placeholder="example@auracraft.com"
                                           maxlength="150" autocomplete="email" required>
                                </div>
                                <button type="submit" class="btn btn-outline btn-inline" id="sendOtpBtn">Gửi mã</button>
                            </div>
                            <span class="error-msg">Vui lòng nhập địa chỉ email hợp lệ.</span>
                            <span class="field-note" id="emailNote"></span>
                        </div>

                        <!-- Ô nhập OTP: hiện sau khi gửi mã -->
                        <div class="input-group" id="otpGroup" hidden>
                            <label for="otp">Mã xác thực (OTP)</label>
                            <div class="inline-row">
                                <div class="input-wrapper">
                                    <i class="fa-solid fa-key field-icon"></i>
                                    <input type="text" id="otp" inputmode="numeric" autocomplete="one-time-code"
                                           maxlength="6" placeholder="Nhập 6 chữ số">
                                </div>
                                <button type="button" class="btn btn-primary btn-inline" id="verifyOtpBtn">Xác nhận</button>
                            </div>
                            <span class="error-msg">Mã OTP gồm 6 chữ số.</span>
                            <span class="field-note" id="otpNote"></span>
                        </div>
                    </form>
                </div>

                <div id="successState" class="success-state" hidden>
                    <i class="fa-solid fa-circle-check success-icon"></i>
                    <h2>Đổi mật khẩu thành công!</h2>
                    <p class="auth-subtitle">Bạn có thể đăng nhập bằng mật khẩu mới.</p>
                    <a href="${pageContext.request.contextPath}/pages/login.jsp" class="btn btn-primary">Đăng nhập ngay</a>
                </div>

                <div class="auth-footer-text">
                    Bạn gặp khó khăn? <a href="#">Liên hệ Bộ phận hỗ trợ AuraCraft</a>
                </div>
            </div>

        </div>
    </div>
</main>

<!-- Modal nhập mật khẩu mới: mở sau khi xác thực OTP thành công -->
<div class="fp-modal-overlay" id="resetModal" role="dialog" aria-modal="true" aria-labelledby="resetTitle" hidden>
    <div class="fp-modal">
        <h3 id="resetTitle">Đặt lại mật khẩu</h3>
        <p class="auth-subtitle">Email <strong id="resetEmailLabel"></strong> đã được xác thực. Hãy nhập mật khẩu mới cho tài khoản của bạn.</p>

        <p id="modalAlert" class="form-alert error" role="alert" hidden></p>

        <form id="resetForm" novalidate>
            <div class="input-group">
                <label for="newPassword">Mật khẩu mới</label>
                <div class="input-wrapper">
                    <i class="fa-solid fa-lock field-icon"></i>
                    <input type="password" id="newPassword" placeholder="Tối thiểu 6 ký tự" maxlength="72"
                           autocomplete="new-password" required>
                    <button type="button" class="btn-toggle-pass" data-toggle-for="newPassword"
                            aria-label="Hiện/ẩn mật khẩu"><i class="fa-regular fa-eye"></i></button>
                </div>
                <span class="error-msg">Mật khẩu phải từ 6 ký tự trở lên.</span>
            </div>

            <div class="input-group">
                <label for="confirmNewPassword">Nhập lại mật khẩu mới</label>
                <div class="input-wrapper">
                    <i class="fa-solid fa-circle-check field-icon"></i>
                    <input type="password" id="confirmNewPassword" placeholder="Nhập lại mật khẩu" maxlength="72"
                           autocomplete="new-password" required>
                    <button type="button" class="btn-toggle-pass" data-toggle-for="confirmNewPassword"
                            aria-label="Hiện/ẩn mật khẩu"><i class="fa-regular fa-eye"></i></button>
                </div>
                <span class="error-msg">Mật khẩu xác nhận không trùng khớp.</span>
            </div>

            <button type="submit" class="btn btn-primary btn-submit" id="resetBtn">
                <span class="btn-text">Cập nhật mật khẩu</span>
            </button>
            <button type="button" class="fp-cancel" id="resetCancel">Hủy</button>
        </form>
    </div>
</div>

<%-- Lưu ý: script nội tuyến trong JSP không dùng template literal, vì Tomcat sẽ hiểu nhầm dấu dollar-ngoặc nhọn là EL --%>
<script>
    var CTX = '${pageContext.request.contextPath}';
    var EMAIL_RE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

    var forgotForm = document.getElementById('forgotForm');
    var alertBox = document.getElementById('formAlert');
    var emailInput = document.getElementById('recoveryEmail');
    var emailNote = document.getElementById('emailNote');
    var sendBtn = document.getElementById('sendOtpBtn');
    var otpGroup = document.getElementById('otpGroup');
    var otpInput = document.getElementById('otp');
    var otpNote = document.getElementById('otpNote');
    var verifyBtn = document.getElementById('verifyOtpBtn');

    var resetModal = document.getElementById('resetModal');
    var resetForm = document.getElementById('resetForm');
    var modalAlert = document.getElementById('modalAlert');
    var newPass = document.getElementById('newPassword');
    var confirmPass = document.getElementById('confirmNewPassword');
    var resetBtn = document.getElementById('resetBtn');

    function showAlert(type, msg) {
        alertBox.className = 'form-alert ' + type;
        alertBox.textContent = msg;
        alertBox.hidden = false;
    }

    function clearErrors() {
        alertBox.hidden = true;
        modalAlert.hidden = true;
        document.querySelectorAll('.input-group.invalid').forEach(function (g) { g.classList.remove('invalid'); });
    }

    // Không truyền msg thì dùng lại câu lỗi mặc định ban đầu của ô đó
    function fieldError(id, msg) {
        var group = document.getElementById(id).closest('.input-group');
        var err = group.querySelector('.error-msg');
        if (!group.dataset.defaultError) group.dataset.defaultError = err.textContent;
        err.textContent = msg || group.dataset.defaultError;
        group.classList.add('invalid');
    }

    function postForm(path, params) {
        return fetch(CTX + path, {
            method: 'POST',
            headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
            body: new URLSearchParams(params)
        });
    }

    // Hiện / ẩn mật khẩu
    document.querySelectorAll('[data-toggle-for]').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var input = document.getElementById(btn.dataset.toggleFor);
            var show = input.type === 'password';
            input.type = show ? 'text' : 'password';
            btn.querySelector('i').className = show ? 'fa-regular fa-eye-slash' : 'fa-regular fa-eye';
        });
    });

    // ===== Đếm ngược OTP =====
    var emailVerified = false;
    var timerId = null;
    var expiresAt = 0;   // thời điểm mã hết hạn (ms)
    var resendAt = 0;    // thời điểm được gửi lại (ms)

    function fmt(sec) {
        var m = Math.floor(sec / 60), s = sec % 60;
        return m + ':' + (s < 10 ? '0' : '') + s;
    }

    function stopTimer() {
        if (timerId) { clearInterval(timerId); timerId = null; }
    }

    function tick() {
        var now = Date.now();
        var left = Math.max(0, Math.ceil((expiresAt - now) / 1000));
        var wait = Math.max(0, Math.ceil((resendAt - now) / 1000));

        if (!otpGroup.hidden) {
            if (left > 0) {
                otpNote.textContent = 'Mã còn hiệu lực: ' + fmt(left);
                otpNote.className = 'field-note';
                verifyBtn.disabled = false;
            } else {
                otpNote.textContent = 'Mã đã hết hạn. Hãy bấm "Gửi lại" để nhận mã mới.';
                otpNote.className = 'field-note warn';
                verifyBtn.disabled = true;
            }
        }

        var label = otpGroup.hidden ? 'Gửi mã' : 'Gửi lại';
        if (wait > 0) {
            sendBtn.disabled = true;
            sendBtn.textContent = label + ' (' + wait + 's)';
        } else {
            sendBtn.disabled = false;
            sendBtn.textContent = label;
        }
        if (left === 0 && wait === 0) stopTimer();
    }

    function startTimer(expiresIn, resendIn) {
        var now = Date.now();
        expiresAt = now + expiresIn * 1000;
        resendAt = now + resendIn * 1000;
        stopTimer();
        tick();
        timerId = setInterval(tick, 250);
    }

    function resetOtpUi() {
        stopTimer();
        expiresAt = 0;
        resendAt = 0;
        emailVerified = false;
        emailInput.readOnly = false;
        otpGroup.hidden = true;
        otpInput.value = '';
        otpNote.textContent = '';
        emailNote.textContent = '';
        sendBtn.disabled = false;
        sendBtn.textContent = 'Gửi mã';
    }

    // Sửa email khi chưa xác thực thì mã cũ không còn dùng được
    emailInput.addEventListener('input', function () {
        if (!emailVerified && (timerId || !otpGroup.hidden)) resetOtpUi();
    });

    // ===== Modal =====
    function openModal() {
        document.getElementById('resetEmailLabel').textContent = emailInput.value.trim();
        newPass.value = '';
        confirmPass.value = '';
        modalAlert.hidden = true;
        resetBtn.disabled = false;
        resetModal.hidden = false;
        document.body.style.overflow = 'hidden';
        newPass.focus();
    }

    function closeModal() {
        resetModal.hidden = true;
        document.body.style.overflow = '';
    }

    document.getElementById('resetCancel').addEventListener('click', function () {
        closeModal();
        resetOtpUi();
    });
    document.addEventListener('keydown', function (e) {
        if (e.key === 'Escape' && !resetModal.hidden) { closeModal(); resetOtpUi(); }
    });

    // ===== Gửi mã (bấm nút hoặc Enter trong ô email) =====
    forgotForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        if (emailVerified) {          // đang là nút "Đổi email"
            resetOtpUi();
            emailInput.focus();
            return;
        }
        clearErrors();
        var email = emailInput.value.trim();
        if (!EMAIL_RE.test(email)) { fieldError('recoveryEmail'); return; }

        sendBtn.disabled = true;
        sendBtn.textContent = 'Đang gửi...';
        try {
            var res = await postForm('/forgot/send-otp', {email: email});
            var data = await res.json();
            if (data.ok) {
                otpGroup.hidden = false;
                otpInput.value = '';
                emailNote.textContent = 'Nếu email đã đăng ký, mã xác thực đã được gửi tới ' + email
                    + '. Hãy kiểm tra cả mục Spam.';
                emailNote.className = 'field-note';
                startTimer(data.expiresIn, data.resendIn);
                otpInput.focus();
            } else {
                fieldError('recoveryEmail', data.message || 'Không gửi được mã.');
                if (data.retryAfter) {
                    resendAt = Date.now() + data.retryAfter * 1000;
                    stopTimer();
                    tick();
                    timerId = setInterval(tick, 250);
                } else {
                    sendBtn.disabled = false;
                    sendBtn.textContent = otpGroup.hidden ? 'Gửi mã' : 'Gửi lại';
                }
            }
        } catch (err) {
            console.error(err);
            fieldError('recoveryEmail', 'Không kết nối được với máy chủ. Vui lòng thử lại.');
            sendBtn.disabled = false;
            sendBtn.textContent = otpGroup.hidden ? 'Gửi mã' : 'Gửi lại';
        }
    });

    // ===== Xác nhận OTP =====
    async function verifyOtp() {
        clearErrors();
        var code = otpInput.value.trim();
        if (!/^\d{6}$/.test(code)) { fieldError('otp'); return; }

        verifyBtn.disabled = true;
        try {
            var res = await postForm('/forgot/verify-otp', {email: emailInput.value.trim(), otp: code});
            var data = await res.json();
            if (data.ok) {
                stopTimer();
                emailVerified = true;
                emailInput.readOnly = true;
                otpGroup.hidden = true;
                emailNote.textContent = '✓ Email đã được xác thực';
                emailNote.className = 'field-note ok';
                sendBtn.disabled = false;
                sendBtn.textContent = 'Đổi email';
                openModal();
            } else {
                fieldError('otp', data.message || 'Mã không đúng.');
                if (res.status === 410 || res.status === 429) expiresAt = 0;   // bắt gửi mã mới
                tick();
                if (expiresAt !== 0) verifyBtn.disabled = false;
            }
        } catch (err) {
            console.error(err);
            fieldError('otp', 'Không kết nối được với máy chủ. Vui lòng thử lại.');
            verifyBtn.disabled = false;
        }
    }

    verifyBtn.addEventListener('click', verifyOtp);
    otpInput.addEventListener('keydown', function (e) {
        if (e.key === 'Enter') { e.preventDefault(); verifyOtp(); }
    });
    otpInput.addEventListener('input', function () {
        otpInput.value = otpInput.value.replace(/\D/g, '');
    });

    // ===== Cập nhật mật khẩu mới =====
    resetForm.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearErrors();

        var ok = true;
        if (newPass.value.length < 6) { fieldError('newPassword'); ok = false; }
        if (confirmPass.value !== newPass.value) { fieldError('confirmNewPassword'); ok = false; }
        if (!ok) return;

        resetBtn.disabled = true;
        try {
            var res = await postForm('/forgot/reset', {
                newPassword: newPass.value,
                confirmPassword: confirmPass.value
            });
            var data = await res.json();
            if (data.ok) {
                closeModal();
                document.getElementById('requestState').hidden = true;
                document.getElementById('successState').hidden = false;
            } else if (res.status === 403) {
                // Phiên xác thực hết hạn: quay lại bước nhập email
                closeModal();
                resetOtpUi();
                showAlert('error', data.message || 'Phiên xác thực đã hết hạn. Vui lòng xác thực lại.');
            } else {
                modalAlert.textContent = data.message || 'Không cập nhật được mật khẩu.';
                modalAlert.hidden = false;
                resetBtn.disabled = false;
            }
        } catch (err) {
            console.error(err);
            modalAlert.textContent = 'Không kết nối được với máy chủ. Vui lòng thử lại.';
            modalAlert.hidden = false;
            resetBtn.disabled = false;
        }
    });
</script>
</body>

</html>
