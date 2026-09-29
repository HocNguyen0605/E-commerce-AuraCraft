// Dữ liệu tài khoản mặc định phục vụ kiểm thử phân quyền
const MOCK_USERS = [
  {
    email: "admin@auracraft.com",
    password: "password123",
    role: "admin",
    fullName: "Quản Trị Viên",
  },
  {
    email: "artisan@auracraft.com",
    password: "password123",
    role: "artisan",
    fullName: "Thợ Thủ Công",
  },
  {
    email: "buyer@auracraft.com",
    password: "password123",
    role: "buyer",
    fullName: "Khách Hàng",
  },
];

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

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

document.addEventListener("DOMContentLoaded", () => {
  const loginForm = document.getElementById("loginForm");
  if (loginForm) {
    loginForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const emailInput = document.getElementById("email");
      const passInput = document.getElementById("password");
      let isValid = true;

      const emailVal = emailInput.value.trim();
      const passVal = passInput.value;

      // Validate email/username
      if (!emailVal) {
        emailInput.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        emailInput.closest(".input-group").classList.remove("invalid");
      }

      // Validate password length
      if (passVal.length < 6) {
        passInput.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        passInput.closest(".input-group").classList.remove("invalid");
      }

      if (!isValid) return;

      // Lấy danh sách user đã đăng ký (từ localStorage) kết hợp với tài khoản mặc định
      const registeredUsers =
        JSON.parse(localStorage.getItem("AuraCraftUsers")) || [];
      const allUsers = [...MOCK_USERS, ...registeredUsers];

      // Tìm kiếm user phù hợp
      const matchedUser = allUsers.find(
        (u) =>
          u.email.toLowerCase() === emailVal.toLowerCase() &&
          u.password === passVal,
      );

      if (!matchedUser) {
        emailInput.closest(".input-group").classList.add("invalid");
        const errorMsg = emailInput
          .closest(".input-group")
          .querySelector(".error-msg");
        if (errorMsg)
          errorMsg.textContent = "Tài khoản hoặc mật khẩu không chính xác.";
        return;
      }

      // Lưu thông tin phiên đăng nhập (Session)
      const currentUser = {
        fullName: matchedUser.fullName,
        email: matchedUser.email,
        role: matchedUser.role,
      };
      localStorage.setItem("currentUser", JSON.stringify(currentUser));

      alert(`Đăng nhập thành công! Chào mừng ${currentUser.fullName}.`);

      // Điều hướng dựa trên vai trò (Role-based Routing)
      switch (currentUser.role) {
        case "admin":
          window.location.href = "../pages/admin-orders.html";
          break;
        case "artisan":
          window.location.href = "../pages/seller-dashboard.html";
          break;
        case "buyer":
        default:
          window.location.href = "../index.html";
          break;
      }
    });
  }

  // Khởi tạo hệ thống quản lý user giả lập cho trang đăng ký
  window.AuraCraftUsers = {
    create: (newUser) => {
      const existingUsers =
        JSON.parse(localStorage.getItem("AuraCraftUsers")) || [];
      const isExist = [...MOCK_USERS, ...existingUsers].some(
        (u) => u.email.toLowerCase() === newUser.email.toLowerCase(),
      );

      if (isExist) {
        throw new Error("Email này đã được sử dụng.");
      }

      existingUsers.push(newUser);
      localStorage.setItem("AuraCraftUsers", JSON.stringify(existingUsers));
      return newUser;
    },
  };

  const registerForm = document.getElementById("registerForm");
  if (registerForm) {
    const accountRole = document.getElementById("accountRole");
    const artisanFields = document.getElementById("artisanFields");
    const updateArtisanFields = () => {
      if (!accountRole || !artisanFields) return;
      const isArtisan = accountRole.value === "artisan";
      artisanFields.hidden = !isArtisan;
      const intro = document.getElementById("introduction");
      const port = document.getElementById("portfolio");
      if (intro) intro.required = isArtisan;
      if (port) port.required = isArtisan;
    };

    if (accountRole) {
      accountRole.addEventListener("change", updateArtisanFields);
      updateArtisanFields();
    }

    registerForm.addEventListener("submit", (e) => {
      e.preventDefault();
      const fullName = document.getElementById("fullName");
      const email = document.getElementById("email");
      const password = document.getElementById("password");
      const confirmPassword = document.getElementById("confirmPassword");
      const accountRole = document.getElementById("accountRole");
      const introduction = document.getElementById("introduction");
      const portfolio = document.getElementById("portfolio");
      let isValid = true;

      if (!fullName.value.trim()) {
        fullName.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        fullName.closest(".input-group").classList.remove("invalid");
      }

      if (!isValidEmail(email.value.trim())) {
        email.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        email.closest(".input-group").classList.remove("invalid");
        email.closest(".input-group").querySelector(".error-msg").textContent = "Vui lòng nhập email hợp lệ.";
      }

      if (password.value.length < 6) {
        password.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        password.closest(".input-group").classList.remove("invalid");
      }

      if (!confirmPassword.value || confirmPassword.value !== password.value) {
        confirmPassword.closest(".input-group").classList.add("invalid");
        isValid = false;
      } else {
        confirmPassword.closest(".input-group").classList.remove("invalid");
      }

      if (accountRole && accountRole.value === "artisan") {
        const introductionGroup = introduction.closest(".input-group");
        const portfolioGroup = portfolio.closest(".input-group");
        const validPortfolio = portfolio.checkValidity();
        introductionGroup.classList.toggle(
          "invalid",
          !introduction.value.trim(),
        );
        portfolioGroup.classList.toggle("invalid", !validPortfolio);
        isValid =
          Boolean(introduction.value.trim()) && validPortfolio && isValid;
      }

      if (isValid) {
        try {
          const user = window.AuraCraftUsers.create({
            fullName: fullName.value,
            email: email.value,
            password: password.value,
            role: accountRole ? accountRole.value : "buyer",
            introduction: introduction ? introduction.value : "",
            portfolio: portfolio ? portfolio.value : "",
          });
          const successMessage =
            user.role === "artisan"
              ? "Đăng ký thành công. Tài khoản thợ đang chờ admin duyệt trước khi nhận đơn."
              : "Đăng ký thành công. Vui lòng đăng nhập.";
          alert(successMessage);
          window.location.href = "login.html";
        } catch (error) {
          const emailGroup = email.closest(".input-group");
          emailGroup.classList.add("invalid");
          const errEl = emailGroup.querySelector(".error-msg");
          if (errEl) errEl.textContent = error.message;
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
