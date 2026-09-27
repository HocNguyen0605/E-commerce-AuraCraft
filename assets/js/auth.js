function togglePass(inputId, btn) {
  const input = document.getElementById(inputId);
  if (!input) return;
  const icon = btn.querySelector("i");

  if (input.type === "password") {
    input.type = "text";
    if (icon) icon.className = "fa-regular fa-eye-slash";
  } else {
    input.type = "password";
    if (icon) icon.className = "fa-regular fa-eye";
  }
}

// Kiểm tra định dạng Email
function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const email = document.getElementById("email");
      const pass = document.getElementById("password");
      let isValid = true;

      if (!email.value.trim()) {
        email.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        email.closest(".input-group").classList.remove("invalid");
      }

      if (pass.value.length < 6) {
        pass.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        pass.closest(".input-group").classList.remove("invalid");
      }

      if (isValid) {
        alert("Đăng nhập thành công vào AuraCraft!");
        window.location.href = "../index.html";
      }
    });
  }

  const registerForm = document.getElementById("registerForm");
  if (registerForm) {
    registerForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const fullName = document.getElementById("fullName");
      const email = document.getElementById("email");
      const password = document.getElementById("password");
      const confirmPassword = document.getElementById("confirmPassword");
      let isValid = true;

      // Kiểm tra họ và tên
      if (!fullName.value.trim()) {
        fullName.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        fullName.closest(".input-group").classList.remove("invalid");
      }

      // Kiểm tra email
      if (!isValidEmail(email.value.trim())) {
        email.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        email.closest(".input-group").classList.remove("invalid");
      }

      // Kiểm tra độ dài mật khẩu
      if (password.value.length < 6) {
        password.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        password.closest(".input-group").classList.remove("invalid");
      }

      // Kiểm tra mật khẩu xác nhận trùng khớp
      if (!confirmPassword.value || confirmPassword.value !== password.value) {
        confirmPassword.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        confirmPassword.closest(".input-group").classList.remove("invalid");
      }

      if (isValid) {
        alert("Đăng ký tài khoản AuraCraft thành công! Vui lòng đăng nhập.");
        window.location.href = "login.html";
      }
    });
  }

  const forgotForm = document.getElementById("forgotForm");
  if (forgotForm) {
    forgotForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const recoveryEmail = document.getElementById("recoveryEmail");
      const emailGroup = document.getElementById("emailGroup");

      if (!isValidEmail(recoveryEmail.value.trim())) {
        emailGroup.classList.add("invalid");
        return;
      }

      emailGroup.classList.remove("invalid");
      const requestState = document.getElementById("requestState");
      const successState = document.getElementById("successState");

      if (requestState) requestState.style.display = "none";
      if (successState) successState.style.display = "block";
    });
  }
});
