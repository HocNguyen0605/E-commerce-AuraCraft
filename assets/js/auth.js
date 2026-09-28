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
    const accountRole = document.getElementById("accountRole");
    const artisanFields = document.getElementById("artisanFields");
    const updateArtisanFields = () => {
      const isArtisan = accountRole.value === "artisan";
      artisanFields.hidden = !isArtisan;
      document.getElementById("introduction").required = isArtisan;
      document.getElementById("portfolio").required = isArtisan;
    };
    accountRole.addEventListener("change", updateArtisanFields);
    updateArtisanFields();

    registerForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const fullName = document.getElementById("fullName");
      const email = document.getElementById("email");
      const password = document.getElementById("password");
      const confirmPassword = document.getElementById("confirmPassword");
      const introduction = document.getElementById("introduction");
      const portfolio = document.getElementById("portfolio");
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
        email.closest(".input-group").querySelector(".error-msg").textContent = "Vui lòng nhập email hợp lệ.";
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

      if (accountRole.value === "artisan") {
        const introductionGroup = introduction.closest(".input-group");
        const portfolioGroup = portfolio.closest(".input-group");
        const validPortfolio = portfolio.checkValidity();
        introductionGroup.classList.toggle("invalid", !introduction.value.trim());
        portfolioGroup.classList.toggle("invalid", !validPortfolio);
        isValid = Boolean(introduction.value.trim()) && validPortfolio && isValid;
      }

      if (isValid) {
        try {
          const user = window.AuraCraftUsers.create({
            fullName: fullName.value,
            email: email.value,
            role: accountRole ? accountRole.value : "buyer",
            introduction: introduction ? introduction.value : "",
            portfolio: portfolio ? portfolio.value : ""
          });
          const successMessage = user.role === "artisan"
            ? "Đăng ký thành công. Tài khoản thợ đang chờ admin duyệt trước khi nhận đơn."
            : "Đăng ký thành công. Vui lòng đăng nhập.";
          alert(successMessage);
          window.location.href = "login.html";
        } catch (error) {
          const emailGroup = email.closest(".input-group");
          emailGroup.classList.add("invalid");
          emailGroup.querySelector(".error-msg").textContent = error.message;
          email.focus();
        }
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
