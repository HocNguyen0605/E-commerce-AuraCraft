<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="controller.GoogleAuthServlet" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng ký tài khoản - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleLogin.css">
    <style>
        .google-block { margin: 4px 0 0; }
        #googleBtn { display: flex; justify-content: center; min-height: 44px; }
        .auth-divider { display: flex; align-items: center; gap: 12px; margin: 18px 0; color: #8a8a8a; font-size: .9rem; }
        .auth-divider::before, .auth-divider::after { content: ""; flex: 1; height: 1px; background: #e3e3e3; }
        .form-alert { padding: 10px 14px; border-radius: 8px; font-size: .92rem; margin-bottom: 14px; }
        .form-alert[hidden] { display: none; }
        .form-alert.error { background: #fdecec; color: #b3261e; }
        .form-alert.success { background: #e8f5e9; color: #1b5e20; }
        .input-group[hidden] { display: none; }
        .field-note { display: block; margin-top: 4px; font-size: .84rem; color: #6b6b6b; }
        .field-note.ok { color: #1b5e20; }
        .field-note.warn { color: #b3261e; }
        .field-note:empty { display: none; }

        /* Ô email / OTP có nút bên cạnh */
        .inline-row { display: flex; gap: 8px; align-items: stretch; }
        .inline-row .input-wrapper { flex: 1; min-width: 0; }
        .btn-inline { white-space: nowrap; padding-left: 16px; padding-right: 16px; }
        .btn-inline:disabled { opacity: .6; cursor: not-allowed; }
        input[readonly] { background: #f4f4f4; }
    </style>
</head>

<body>
<main class="form-page">
    <div class="container">
        <div class="auth-wrapper">

            <div class="banner-side">
                <div class="banner-overlay"></div>
                <img src="https://i.pinimg.com/736x/97/fb/75/97fb75451a18c257802dac4efab0065b.jpg"
                     alt="AuraCraft Handmade" class="banner-img">
                <div class="banner-content">
                    <h3>Bắt đầu sáng tạo</h3>
                    <p>Tham gia AuraCraft để sở hữu những bộ sưu tập vòng tay mang dấu ấn cá nhân.</p>
                </div>
            </div>

            <div class="form-side">
                <div class="top-header">
                    <a href="${pageContext.request.contextPath}/index.jsp" class="brand-logo">
                        <span class="logo-icon">A</span>
                        <span class="logo-text">uraCraft</span>
                    </a>
                    <a href="${pageContext.request.contextPath}/pages/login.jsp" class="auth-nav-back">Đã có tài khoản? Đăng nhập</a>
                </div>

                <div class="form-header">
                    <h2>Tạo tài khoản mới</h2>
                    <p class="auth-subtitle">Chọn loại tài khoản, sau đó đăng ký bằng Google hoặc tên đăng nhập.</p>
                </div>

                <p id="formAlert" class="form-alert" role="alert" hidden></p>

                <form id="registerForm" novalidate>
                    <div class="input-group">
                        <label for="accountRole">Bạn đăng ký với vai trò</label>
                        <select id="accountRole" name="role" class="auth-select">
                            <option value="buyer">Người mua</option>
                            <option value="artisan">Thợ thủ công</option>
                        </select>
                    </div>

                    <div class="input-group">
                        <label for="fullName">Họ và tên</label>
                        <div class="input-wrapper">
                            <i class="fa-regular fa-user field-icon"></i>
                            <input type="text" id="fullName" name="fullName" placeholder="Ví dụ: Nguyễn Văn A" maxlength="100" required>
                        </div>
                        <span class="error-msg">Vui lòng nhập họ và tên.</span>
                    </div>

                    <div class="input-group">
                        <label for="username">Tên đăng nhập</label>
                        <div class="input-wrapper">
                            <i class="fa-solid fa-at field-icon"></i>
                            <input type="text" id="username" name="username" placeholder="vd: nguyenvana" maxlength="30"
                                   autocomplete="username" required>
                        </div>
                        <span class="error-msg">Tên đăng nhập gồm 4–30 ký tự: chữ thường, số hoặc dấu gạch dưới.</span>
                    </div>

                    <!-- Email + nút gửi mã OTP -->
                    <div class="input-group">
                        <label for="email">Địa chỉ Email</label>
                        <div class="inline-row">
                            <div class="input-wrapper">
                                <i class="fa-regular fa-envelope field-icon"></i>
                                <input type="email" id="email" name="email" placeholder="name@example.com" maxlength="150"
                                       autocomplete="email" required>
                            </div>
                            <button type="button" class="btn btn-outline btn-inline" id="sendOtpBtn">Gửi mã</button>
                        </div>
                        <span class="error-msg">Vui lòng nhập email hợp lệ.</span>
                        <span class="field-note" id="emailNote"></span>
                    </div>

                    <!-- Ô nhập OTP: chỉ hiện sau khi gửi mã thành công -->
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

                    <div class="input-group">
                        <label for="password">Mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fa-solid fa-lock field-icon"></i>
                            <input type="password" id="password" name="password" placeholder="Tối thiểu 6 ký tự"
                                   autocomplete="new-password" required>
                            <button type="button" class="btn-toggle-pass" data-toggle-for="password"
                                    aria-label="Hiện/ẩn mật khẩu">
                                <i class="fa-regular fa-eye"></i>
                            </button>
                        </div>
                        <span class="error-msg">Mật khẩu phải từ 6 ký tự trở lên.</span>
                    </div>

                    <div class="input-group">
                        <label for="confirmPassword">Xác nhận mật khẩu</label>
                        <div class="input-wrapper">
                            <i class="fa-solid fa-circle-check field-icon"></i>
                            <input type="password" id="confirmPassword" placeholder="Nhập lại mật khẩu"
                                   autocomplete="new-password" required>
                            <button type="button" class="btn-toggle-pass" data-toggle-for="confirmPassword"
                                    aria-label="Hiện/ẩn mật khẩu">
                                <i class="fa-regular fa-eye"></i>
                            </button>
                        </div>
                        <span class="error-msg">Mật khẩu xác nhận không trùng khớp.</span>
                    </div>

                    <button type="submit" class="btn btn-primary btn-submit" id="btnSubmit">
                        <span class="btn-text">Đăng ký tài khoản</span>
                        <div class="spinner" id="btnSpinner"></div>
                    </button>
                </form>
                <div class="auth-divider">hoặc</div>

                <div class="google-block">
                    <div id="googleBtn"></div>
                </div>

                <div class="terms-text">
                    Bằng việc đăng ký, bạn đồng ý với <a href="#">Điều khoản & Chính sách</a> của AuraCraft.
                </div>
            </div>

        </div>
    </div>
</main>

<%-- Lưu ý: script nội tuyến trong JSP không dùng template literal, vì Tomcat sẽ hiểu nhầm dấu dollar-ngoặc nhọn là EL --%>
<script>
    var CTX = '${pageContext.request.contextPath}';
    var GOOGLE_CLIENT_ID = '<%= GoogleAuthServlet.CLIENT_ID %>';
    var EMAIL_RE = /^[^@\s]+@[^@\s]+\.[^@\s]+$/;

    var form = document.getElementById('registerForm');
    var alertBox = document.getElementById('formAlert');
    var submitBtn = document.getElementById('btnSubmit');

    var emailInput = document.getElementById('email');
    var emailNote = document.getElementById('emailNote');
    var sendBtn = document.getElementById('sendOtpBtn');
    var otpGroup = document.getElementById('otpGroup');
    var otpInput = document.getElementById('otp');
    var otpNote = document.getElementById('otpNote');
    var verifyBtn = document.getElementById('verifyOtpBtn');

    function showAlert(type, msg) {
        alertBox.className = 'form-alert ' + type;
        alertBox.textContent = msg;
        alertBox.hidden = false;
    }

    function clearErrors() {
        alertBox.hidden = true;
        form.querySelectorAll('.input-group.invalid').forEach(function (g) { g.classList.remove('invalid'); });
    }

    // Nếu không truyền msg thì dùng lại câu lỗi mặc định ban đầu của ô đó
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

    // ===== Xác thực email bằng OTP =====
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

    // Quay về trạng thái ban đầu của phần email / OTP
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

    sendBtn.addEventListener('click', async function () {
        if (emailVerified) {          // đang là nút "Đổi email"
            resetOtpUi();
            emailInput.focus();
            return;
        }
        clearErrors();
        var email = emailInput.value.trim();
        if (!EMAIL_RE.test(email)) { fieldError('email'); return; }

        sendBtn.disabled = true;
        sendBtn.textContent = 'Đang gửi...';
        try {
            var res = await postForm('/register/send-otp', {email: email});
            var data = await res.json();
            if (data.ok) {
                otpGroup.hidden = false;
                otpInput.value = '';
                emailNote.textContent = 'Đã gửi mã tới ' + email + '. Nếu không thấy, hãy kiểm tra mục Spam.';
                emailNote.className = 'field-note';
                startTimer(data.expiresIn, data.resendIn);
                otpInput.focus();
            } else {
                fieldError('email', data.message || 'Không gửi được mã.');
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
            fieldError('email', 'Không kết nối được với máy chủ. Vui lòng thử lại.');
            sendBtn.disabled = false;
            sendBtn.textContent = otpGroup.hidden ? 'Gửi mã' : 'Gửi lại';
        }
    });

    async function verifyOtp() {
        clearErrors();
        var code = otpInput.value.trim();
        if (!/^\d{6}$/.test(code)) { fieldError('otp'); return; }

        verifyBtn.disabled = true;
        try {
            var res = await postForm('/register/verify-otp', {email: emailInput.value.trim(), otp: code});
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
            } else {
                fieldError('otp', data.message || 'Mã không đúng.');
                // Hết lượt thử hoặc mã hết hạn: bắt gửi mã mới
                if (res.status === 410 || res.status === 429) expiresAt = 0;
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

    // ===== Đăng ký thường =====
    form.addEventListener('submit', async function (e) {
        e.preventDefault();
        clearErrors();

        var name = document.getElementById('fullName').value.trim();
        var username = document.getElementById('username').value.trim().toLowerCase();
        var email = emailInput.value.trim();
        var password = document.getElementById('password').value;
        var confirm = document.getElementById('confirmPassword').value;

        var ok = true;
        if (!name) { fieldError('fullName'); ok = false; }
        if (!/^[a-z0-9_]{4,30}$/.test(username)) { fieldError('username'); ok = false; }
        if (!EMAIL_RE.test(email)) { fieldError('email'); ok = false; }
        else if (!emailVerified) { fieldError('email', 'Vui lòng xác thực email bằng mã OTP.'); ok = false; }
        if (password.length < 6) { fieldError('password'); ok = false; }
        if (confirm !== password) { fieldError('confirmPassword'); ok = false; }
        if (!ok) return;

        submitBtn.disabled = true;
        try {
            var res = await postForm('/register', {
                fullName: name,
                username: username,
                email: email,
                password: password,
                role: document.getElementById('accountRole').value
            });
            var data = await res.json();
            if (data.ok) {
                showAlert('success', 'Đăng ký thành công! Đang chuyển tới trang đăng nhập...');
                setTimeout(function () { location.href = CTX + '/pages/login.jsp'; }, 1200);
            } else {
                showAlert('error', data.message || 'Đăng ký không thành công.');
                if (res.status === 403) resetOtpUi();   // xác thực hết hạn: yêu cầu xác thực lại
                submitBtn.disabled = false;
            }
        } catch (err) {
            console.error(err);
            showAlert('error', 'Không kết nối được với máy chủ. Vui lòng thử lại.');
            submitBtn.disabled = false;
        }
    });

    // ===== Đăng ký / đăng nhập bằng Google (Google đã xác thực email nên không cần OTP) =====
    async function onGoogleCredential(response) {
        clearErrors();
        try {
            var res = await postForm('/auth/google', {
                credential: response.credential,
                role: document.getElementById('accountRole').value
            });
            var data = await res.json();
            if (!data.ok) {
                showAlert('error', data.message || 'Đăng ký bằng Google không thành công.');
                return;
            }
            var routes = {
                admin: '/pages/admin/admin-users.jsp',
                seller: '/pages/seller/seller-dashboard.jsp'
            };
            var role = String(data.role || '').toLowerCase();
            location.href = CTX + (routes[role] || '/index.jsp');
        } catch (err) {
            console.error(err);
            showAlert('error', 'Không kết nối được với máy chủ. Vui lòng thử lại.');
        }
    }

    function initGoogle() {
        google.accounts.id.initialize({
            client_id: GOOGLE_CLIENT_ID,
            callback: onGoogleCredential
        });
        google.accounts.id.renderButton(document.getElementById('googleBtn'), {
            theme: 'outline',
            size: 'large',
            shape: 'pill',
            text: 'signup_with',
            locale: 'vi',
            width: 320
        });
    }
</script>
<script src="https://accounts.google.com/gsi/client" async defer onload="initGoogle()"></script>
</body>

</html>
