document.addEventListener("DOMContentLoaded", () => {
  const checkoutForm = document.getElementById("checkoutForm");
  if (checkoutForm) {
    const customOrderId = new URLSearchParams(window.location.search).get("orderId");
    const customOrder = customOrderId && window.AuraCraftCustom.getOrder(customOrderId);
    const customAmountDue = customOrder && customOrder.paymentStatus === "adjustment_due"
      ? Math.abs(customOrder.priceDifference)
      : customOrder && customOrder.price;
    if (customOrderId && !customOrder) {
      checkoutForm.hidden = true;
      alert("Không tìm thấy đơn Custom cần thanh toán.");
      return;
    }

    if (customOrder) {
      const codCard = document.getElementById("paymentCodCard");
      if (codCard) codCard.style.display = "none";
      const onlinePayment = document.getElementById("paymentVNPay");
      onlinePayment.checked = true;
      onlinePayment.closest(".payment-method-card").classList.add("active");
      const items = document.querySelector(".summary-products-list");
      const product = document.createElement("div");
      product.className = "summary-product-item";
      const details = document.createElement("div");
      const title = document.createElement("h3");
      const meta = document.createElement("p");
      const itemPrice = document.createElement("span");
      details.className = "summary-product-details";
      title.className = "summary-product-title";
      meta.className = "summary-product-meta";
      itemPrice.className = "summary-product-price";
      title.textContent = customOrder.productType;
      meta.textContent = `Thợ: ${customOrder.sellerName} · Chế tác: ${customOrder.days} ngày`;
      itemPrice.textContent = `${customOrder.price.toLocaleString("vi-VN")}đ`;
      details.append(title, meta);
      product.append(details, itemPrice);
      items.replaceChildren(product);
      document.querySelector(".cost-row span").textContent = customOrder.paymentStatus === "adjustment_due"
        ? "Khoản chênh lệch cần thanh toán"
        : "Giá báo được duyệt";
      document.querySelector(".cost-row .cost-value").textContent = `${customAmountDue.toLocaleString("vi-VN")}đ`;
      const shippingRow = document.querySelector(".shipping-fee-row .cost-value");
      shippingRow.textContent = "Tính khi giao hàng";
      document.querySelector(".total-amount").textContent = `${customAmountDue.toLocaleString("vi-VN")}đ`;
      const customerName = document.getElementById("fullName");
      if (customerName && customOrder.buyerName) customerName.value = customOrder.buyerName;
      const customerEmail = document.getElementById("email");
      if (customerEmail && customOrder.buyerEmail) customerEmail.value = customOrder.buyerEmail;
      document.querySelector(".checkout-breadcrumb .active").textContent = "Thanh toán đơn Custom";
      const buttonText = document.querySelector("#btnConfirmOrder span");
      if (buttonText) buttonText.textContent = "Thanh toán qua VNPay";
    }

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

      if (customOrder) {
        const paymentResult = window.confirm(
          `Mô phỏng kết quả cổng VNPay cho ${customAmountDue.toLocaleString("vi-VN")}đ.\nOK = thanh toán thành công, Hủy = giao dịch thất bại.`
        );
        try {
          window.AuraCraftCustom.saveOrderDetails(customOrder.id, {
            fullName: fullName.value,
            phone: phone.value,
            address: address.value,
            note: note ? note.value : ""
          });
          const result = window.AuraCraftCustom.recordPayment(customOrder.id, paymentResult ? "success" : "failure", {
            method: "vnpay"
          });
          if (!paymentResult) {
            alert(`Thanh toán thất bại. Đơn ${result.order.id} vẫn chưa được chế tác.`);
            return;
          }
          const customOrderData = {
            orderId: result.order.id,
            fullName: fullName.value.trim(),
            phone: phone.value.trim(),
            address: address.value.trim(),
            note: note ? note.value.trim() : "",
            paymentMethod: "VNPay (mô phỏng thành công)",
            shippingFee: "Tính khi giao hàng",
            subtotal: `${customAmountDue.toLocaleString("vi-VN")}đ`,
            total: `${customAmountDue.toLocaleString("vi-VN")}đ`,
            createdAt: new Date().toLocaleString("vi-VN"),
            sellerName: result.order.sellerName,
            paymentStatus: result.order.paymentStatus
          };
          sessionStorage.setItem("auracraft_latest_order", JSON.stringify(customOrderData));
          window.location.href = `success.html?orderId=${encodeURIComponent(result.order.id)}`;
        } catch (error) {
          alert(error.message);
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
