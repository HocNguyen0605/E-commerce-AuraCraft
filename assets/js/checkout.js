document.addEventListener("DOMContentLoaded", () => {
  const checkoutForm = document.getElementById("checkoutForm");
  if (checkoutForm) {
    const paymentCards = document.querySelectorAll(".payment-method-card");
    paymentCards.forEach((card) => {
      const radio = card.querySelector('input[type="radio"]');
      radio.addEventListener("change", () => {
        paymentCards.forEach((c) => c.classList.remove("active"));
        if (radio.checked) {
          card.classList.add("active");
        }
      });
    });

    // Submit form đặt hàng
    checkoutForm.addEventListener("submit", (e) => {
      e.preventDefault();

      const fullName = document.getElementById("fullName");
      const phone = document.getElementById("phone");
      const address = document.getElementById("address");
      const note = document.getElementById("orderNote");
      const paymentRadio = document.querySelector(
        'input[name="paymentMethod"]:checked',
      );

      let isValid = true;

      // Kiểm tra họ và tên
      if (!fullName.value.trim()) {
        fullName.closest(".form-group").classList.add("invalid");
        isValid = false;
      } else {
        fullName.closest(".form-group").classList.remove("invalid");
      }

      // Kiểm tra số điện thoại
      const phoneRegex = /^(0[3|5|7|8|9])[0-9]{8}$/;
      if (
        !phone.value.trim() ||
        !phoneRegex.test(phone.value.trim().replace(/\s/g, ""))
      ) {
        phone.closest(".form-group").classList.add("invalid");
        isValid = false;
      } else {
        phone.closest(".form-group").classList.remove("invalid");
      }

      // Kiểm tra địa chỉ giao hàng
      if (!address.value.trim()) {
        address.closest(".form-group").classList.add("invalid");
        isValid = false;
      } else {
        address.closest(".form-group").classList.remove("invalid");
      }

      if (!isValid) {
        const firstInvalid = document.querySelector(".form-group.invalid");
        if (firstInvalid) {
          firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }

      // Tạo mã đơn hàng
      const randomCode = Math.floor(100000 + Math.random() * 900000);
      const virtualOrderId = `#AC-${randomCode}`;

      const paymentMethodText =
        paymentRadio && paymentRadio.value === "vnpay"
          ? "Thanh toán trực tuyến qua VNPay"
          : "Thanh toán khi nhận hàng (COD)";

      const orderData = {
        orderId: virtualOrderId,
        fullName: fullName.value.trim(),
        phone: phone.value.trim(),
        address: address.value.trim(),
        note: note ? note.value.trim() : "",
        paymentMethod: paymentMethodText,
        shippingFee: "30.000đ",
        subtotal: "390.000đ",
        total: "420.000đ",
        createdAt: new Date().toLocaleString("vi-VN"),
      };

      sessionStorage.setItem(
        "auracraft_latest_order",
        JSON.stringify(orderData),
      );

      const btnConfirm = document.getElementById("btnConfirmOrder");
      if (btnConfirm) {
        btnConfirm.disabled = true;
        btnConfirm.innerHTML =
          '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo đơn hàng...';
      }

      setTimeout(() => {
        window.location.href = `success.html?orderId=${encodeURIComponent(virtualOrderId)}`;
      }, 400);
    });
  }

  const successContainer = document.querySelector(".success-card");
  if (successContainer) {
    const urlParams = new URLSearchParams(window.location.search);
    const orderIdParam = urlParams.get("orderId");

    let orderData = null;
    try {
      const stored = sessionStorage.getItem("auracraft_latest_order");
      if (stored) {
        orderData = JSON.parse(stored);
      }
    } catch (err) {
      console.error("Lỗi đọc dữ liệu đơn hàng:", err);
    }

    const displayOrderId =
      orderData?.orderId ||
      orderIdParam ||
      "#AC-" + Math.floor(100000 + Math.random() * 900000);
    const orderCodeEl = document.getElementById("displayOrderCode");
    if (orderCodeEl) {
      orderCodeEl.textContent = displayOrderId;
    }

    // Điền các thông tin chi tiết
    if (orderData) {
      const customerEl = document.getElementById("detailCustomer");
      if (customerEl)
        customerEl.textContent = `${orderData.fullName} (${orderData.phone})`;

      const addressEl = document.getElementById("detailAddress");
      if (addressEl) addressEl.textContent = orderData.address;

      const noteEl = document.getElementById("detailNote");
      if (noteEl) noteEl.textContent = orderData.note || "Không có ghi chú";

      const paymentEl = document.getElementById("detailPayment");
      if (paymentEl) paymentEl.textContent = orderData.paymentMethod;

      const shippingEl = document.getElementById("detailShipping");
      if (shippingEl) shippingEl.textContent = orderData.shippingFee;

      const totalEl = document.getElementById("detailTotal");
      if (totalEl) totalEl.textContent = orderData.total;

      const timeEl = document.getElementById("detailTime");
      if (timeEl) timeEl.textContent = orderData.createdAt;
    }

    const formatDate = (d) => {
      const day = String(d.getDate()).padStart(2, "0");
      const month = String(d.getMonth() + 1).padStart(2, "0");
      const year = d.getFullYear();
      return `${day}/${month}/${year}`;
    };

    const deliveryEstimateDateEl = document.getElementById(
      "deliveryEstimateDate",
    );
    if (deliveryEstimateDateEl) {
      deliveryEstimateDateEl.textContent = `${formatDate(startEstimate)} - ${formatDate(endEstimate)}`;
    }

    const stepTime1El = document.getElementById("stepTime1");
    if (stepTime1El && orderData?.createdAt) {
      stepTime1El.textContent = orderData.createdAt.split(" ")[0] || "Vừa xong";
    }

    const logTime1El = document.getElementById("logTime1");
    if (logTime1El && orderData?.createdAt) {
      logTime1El.textContent = orderData.createdAt;
    }

    const btnCopy = document.getElementById("btnCopyCode");
    if (btnCopy) {
      btnCopy.addEventListener("click", () => {
        navigator.clipboard.writeText(displayOrderId).then(() => {
          const originalTitle = btnCopy.getAttribute("title");
          btnCopy.innerHTML = '<i class="fa-solid fa-check"></i>';
          setTimeout(() => {
            btnCopy.innerHTML = '<i class="fa-regular fa-copy"></i>';
          }, 2000);
        });
      });
    }
  }
});
