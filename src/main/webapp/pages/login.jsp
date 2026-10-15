<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="controller.GoogleAuthServlet" %>
<!DOCTYPE html>
<html lang="vi">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Đăng nhập - AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="../assets/css/header-footer.css">
    <link rel="stylesheet" href="../assets/css/pages/styleLogin.css">
    <style>
        #googleBtn { display: flex; justify-content: center; min-height: 44px; }
        .auth-divider { display: flex; align-items: center; gap: 12px; margin: 18px 0; color: #8a8a8a; font-size: .9rem; }
        .auth-divider::before, .auth-divider::after { content: ""; flex: 1; height: 1px; background: #e3e3e3; }
    </style>
</head>

<body>
<main class="form-page">
    <div class="container">
        <div class="auth-wrapper">

            <!-- Banner bên trái -->
            <div class="banner-side">
                <div class="banner-overlay"></div>
                <img src="https://i.pinimg.com/736x/0a/57/18/0a571878c98d882e140d54d302010359.jpg"
                     alt="AuraCraft Crafting" class="banner-img">
                <div class="banner-content">
                    <h3>AuraCraft</h3>
                    <p>Mỗi món đồ là một câu chuyện được tạo nên từ sự tỉ mỉ và tình yêu.</p>
                </div>
            </div>

            <!-- Form đăng nhập bên phải -->
            <div class="form-side">
                <div class="top-header">
                    <a href="../index.jsp" class="brand-logo">
                        <span class="logo-icon">A</span>
                        <span class="logo-text">uraCraft</span>
                    </a>
                    <a href="../index.jsp" class="auth-nav-back">← Trang chủ</a>
                </div>

                <div class="form-header">
                    <h2>Chào mừng trở lại!</h2>
                    <p class="auth-subtitle">Đăng nhập để tiếp tục khám phá và sáng tạo các mẫu vòng tay độc bản.</p>
                </div>

                <!-- Giữ nguyên ID loginForm, sử dụng thuộc tính name cho từng input -->
                <form id="loginForm" novalidate>
                    <div class="input-group" id="emailGroup">
                        <label for="email">Tên đăng nhập hoặc Email</label>
                        <div class="input-wrapper">
                            <i class="fa-regular fa-envelope field-icon"></i>
                            <input type="text" id="email" name="email" placeholder="Nhập email hoặc tên tài khoản" required>
                        </div>
                        <span class="error-msg" id="emailError">Vui lòng nhập email hoặc tên đăng nhập.</span>
                    </div>

                    <div class="input-group" id="passGroup">
                        <div class="form-label-row">
                            <label for="password">Mật khẩu</label>
                            <a href="forgot.jsp" class="forgot-pass-link">Quên mật khẩu?</a>
                        </div>
                        <div class="input-wrapper">
                            <i class="fa-solid fa-lock field-icon"></i>
                            <input type="password" id="password" name="password" placeholder="••••••••" required>
                            <button type="button" class="btn-toggle-pass" onclick="togglePass('password', this)"
                                    aria-label="Hiện/ẩn mật khẩu">
                                <i class="fa-regular fa-eye"></i>
                            </button>
                        </div>
                        <span class="error-msg" id="passError">Mật khẩu phải từ 6 ký tự trở lên.</span>
                    </div>

                    <div class="form-options">
                        <label class="checkbox-container">
                            <input type="checkbox" id="rememberMe" name="rememberMe">
                            <span class="checkmark"></span>
                            Ghi nhớ đăng nhập
                        </label>
                    </div>

                    <button type="submit" class="btn btn-primary btn-submit" id="btnSubmit">
                        <span class="btn-text">Đăng nhập</span>
                        <div class="spinner" id="btnSpinner"></div>
                    </button>
                </form>

                <div class="auth-divider">hoặc</div>
                <div id="googleBtn"></div>

                <div class="auth-footer-text">
                    Chưa có tài khoản? <a href="register.jsp">Tạo tài khoản mới</a>
                </div>
            </div>

        </div>
    </div>
</main>

<!-- auth.js chứa biến CTX, hàm logout, initAuth... -->
<script src="../assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>

<!-- Lưu ý: script nội tuyến trong JSP không dùng template literal (dấu đô-la + ngoặc nhọn) vì Tomcat sẽ hiểu là EL -->
<script>
    var ctxPath = '${pageContext.request.contextPath}';
    var GOOGLE_CLIENT_ID = '<%= GoogleAuthServlet.CLIENT_ID %>';

    var emailGroup = document.getElementById('emailGroup');
    var emailError = document.getElementById('emailError');

    function showLoginError(msg) {
        emailGroup.classList.add('invalid');
        emailError.textContent = msg;
    }

    // Chuyển trang theo vai trò, dùng chung cho đăng nhập thường và Google
    function redirectByRole(roleValue) {
        var base = (typeof CTX !== 'undefined' ? CTX : ctxPath);
        var role = String(roleValue || '').toLowerCase();
        var routes = {
            admin:  '/pages/admin/admin-users.jsp',
            seller: '/pages/seller/seller-dashboard.jsp'
        };
        location.href = base + (routes[role] || '/index.jsp');
    }

    // Đăng nhập thường
    document.getElementById('loginForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        var emailInput = document.getElementById('email');
        var passInput = document.getElementById('password');
        var passGroup = document.getElementById('passGroup');

        // Reset trạng thái lỗi giao diện
        emailGroup.classList.remove('invalid');
        passGroup.classList.remove('invalid');

        // Validate nhanh ở Client
        var isValid = true;
        if (!emailInput.value.trim()) {
            showLoginError('Vui lòng nhập email hoặc tên đăng nhập.');
            isValid = false;
        }
        if (!passInput.value || passInput.value.length < 6) {
            passGroup.classList.add('invalid');
            isValid = false;
        }
        if (!isValid) return;

        try {
            var res = await fetch(ctxPath + '/login', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                body: new URLSearchParams(new FormData(e.target))
            });
            var data = await res.json();
            if (data.ok) {
                redirectByRole(data.role);
            } else {
                showLoginError(data.message || 'Tài khoản hoặc mật khẩu không chính xác.');
            }
        } catch (ex) {
            console.error('Lỗi kết nối:', ex);
            showLoginError('Không kết nối được với máy chủ. Vui lòng thử lại!');
        }
    });

    // Đăng nhập bằng Google: mode=login nên servlet chỉ đăng nhập, không tự tạo tài khoản
    async function onGoogleCredential(response) {
        emailGroup.classList.remove('invalid');
        try {
            var res = await fetch(ctxPath + '/auth/google', {
                method: 'POST',
                headers: {'Content-Type': 'application/x-www-form-urlencoded; charset=UTF-8'},
                body: new URLSearchParams({
                    credential: response.credential,
                    mode: 'login'
                })
            });
            var data = await res.json();
            if (data.ok) {
                redirectByRole(data.role);
            } else {
                showLoginError(data.message || 'Đăng nhập bằng Google không thành công.');
            }
        } catch (ex) {
            console.error('Lỗi kết nối:', ex);
            showLoginError('Không kết nối được với máy chủ. Vui lòng thử lại!');
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
            text: 'signin_with',
            locale: 'vi',
            width: 320
        });
    }
</script>
<script src="https://accounts.google.com/gsi/client" async defer onload="initGoogle()"></script>
</body>

</html>
