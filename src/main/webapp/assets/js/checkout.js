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
      shippingRow.textContent = "25.300đ";
      document.querySelector(".total-amount").textContent = `${(customAmountDue + 25300).toLocaleString("vi-VN")}đ`;
      const customerName = document.getElementById("fullName");
      if (customerName && customOrder.buyerName) customerName.value = customOrder.buyerName;
      const customerEmail = document.getElementById("email");
      if (customerEmail && customOrder.buyerEmail) customerEmail.value = customOrder.buyerEmail;
      document.querySelector(".checkout-breadcrumb .active").textContent = "Thanh toán đơn Custom";
    }

    const hasCustomItem = Array.from(document.querySelectorAll('.summary-product-title')).some(el => el.textContent.toLowerCase().includes('custom')) || !!customOrder;



    const paymentCards = document.querySelectorAll(".payment-method-card");
    
    function updateDepositUI(radio) {
        if (!hasCustomItem) return;
        let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
        if (customOrder) totalStr = (customAmountDue + 25300).toString();
        let totalVal = parseInt(totalStr);
        
        const btnConfirmText = document.getElementById('btnConfirmText');
        
        if (radio.value === 'vnpay') {
            // Thanh toán 100%
            document.getElementById('depositRow').style.display = 'none';
            document.getElementById('remainingRow').style.display = 'none';
            if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán trực tuyến (100%)';
            document.getElementById('customDepositAlert').innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> Sản phẩm Custom yêu cầu thanh toán 100% khi chọn VNPay.';
        } else {
            // COD - Cọc 50%
            document.getElementById('depositRow').style.display = 'flex';
            document.getElementById('remainingRow').style.display = 'flex';
            let depositVal = totalVal / 2;
            let remainingVal = totalVal - depositVal;
            document.getElementById('summaryDeposit').textContent = depositVal.toLocaleString('vi-VN') + 'đ';
            document.getElementById('summaryRemaining').textContent = remainingVal.toLocaleString('vi-VN') + 'đ';
            if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán cọc (VNPay)';
            document.getElementById('customDepositAlert').innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> <strong>Lưu ý:</strong> Sản phẩm Custom yêu cầu thanh toán cọc trước <strong>50%</strong>.';
        }
    }

    paymentCards.forEach((card) => {
      const radio = card.querySelector('input[type="radio"]');
      radio.addEventListener("change", () => {
        paymentCards.forEach((c) => c.classList.remove("active"));
        if (radio.checked) {
          card.classList.add("active");
          updateDepositUI(radio);
        }
      });
    });
    
    // Initialize UI on load
    const checkedRadio = document.querySelector('input[name="paymentMethod"]:checked');
    if (checkedRadio) updateDepositUI(checkedRadio);

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

      const paymentRadioChecked = document.querySelector('input[name="paymentMethod"]:checked');
      const isVNPay = paymentRadioChecked && paymentRadioChecked.value === 'vnpay';
      const hasCustomItemSubmit = Array.from(document.querySelectorAll('.summary-product-title')).some(el => el.textContent.toLowerCase().includes('custom')) || !!customOrder;
      
      let depositToPay = 0;
      let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
      if (customOrder) totalStr = (customAmountDue + 25300).toString();

      if (hasCustomItemSubmit) {
        depositToPay = isVNPay ? parseInt(totalStr) : parseInt(totalStr) / 2;
      } else if (isVNPay) {
        depositToPay = parseInt(totalStr); // For non-custom, VNPay still pays 100% online
      }

      function processCheckoutFinal(paymentResult) {
        if (!paymentResult) {
          alert('Thanh toán cọc thất bại hoặc bị hủy. Đơn hàng chưa được thực hiện.');
          const btnConfirm = document.getElementById("btnConfirmOrder");
          if (btnConfirm) {
            btnConfirm.disabled = false;
            btnConfirm.innerHTML = '<span>Xác nhận đặt hàng</span><i class="fa-solid fa-arrow-right"></i>';
          }
          return;
        }

        if (customOrder) {
          try {
            window.AuraCraftCustom.saveOrderDetails(customOrder.id, {
              fullName: fullName.value,
              phone: phone.value,
              address: address.value,
              note: note ? note.value : ""
            });
            const result = window.AuraCraftCustom.recordPayment(customOrder.id, "success", {
              method: "vnpay"
            });
            
            const customOrderData = {
              orderId: result.order.id,
              fullName: fullName.value.trim(),
              phone: phone.value.trim(),
              address: address.value.trim(),
              note: note ? note.value.trim() : "",
              paymentMethod: isVNPay ? "Thanh toán trực tuyến (VNPay)" : "COD (Đã cọc 50%)",
              shippingFee: "25.300đ",
              subtotal: `${customAmountDue.toLocaleString("vi-VN")}đ`,
              total: `${(customAmountDue + 25300).toLocaleString("vi-VN")}đ`,
              paid: isVNPay ? undefined : `${depositToPay.toLocaleString("vi-VN")}đ`,
              remaining: isVNPay ? undefined : `${depositToPay.toLocaleString("vi-VN")}đ`,
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

        // Tạo mã đơn hàng cho hàng có sẵn
        const randomCode = Math.floor(100000 + Math.random() * 900000);
        const virtualOrderId = `#AC-${randomCode}`;

        let paymentMethodText = isVNPay ? "Thanh toán trực tuyến qua VNPay" : "Thanh toán khi nhận hàng (COD)";
            
        if (hasCustomItemSubmit && !isVNPay) {
          paymentMethodText += ` (Đã cọc ${depositToPay.toLocaleString("vi-VN")}đ)`;
        }

        const orderData = {
          orderId: virtualOrderId,
          fullName: fullName.value.trim(),
          phone: phone.value.trim(),
          address: address.value.trim(),
          note: note ? note.value.trim() : "",
          paymentMethod: paymentMethodText,
          shippingFee: "25.300đ",
          subtotal: "390.000đ",
          total: "415.300đ",
          paid: (hasCustomItemSubmit && !isVNPay) ? `${depositToPay.toLocaleString("vi-VN")}đ` : undefined,
          remaining: (hasCustomItemSubmit && !isVNPay) ? `${depositToPay.toLocaleString("vi-VN")}đ` : undefined,
          createdAt: new Date().toLocaleString("vi-VN"),
        };

        sessionStorage.setItem(
          "auracraft_latest_order",
          JSON.stringify(orderData),
        );

        setTimeout(() => {
          window.location.href = `success.html?orderId=${encodeURIComponent(virtualOrderId)}`;
        }, 400);
      }

      if (depositToPay > 0) {
          // Hiển thị modal QR cho cả Custom cọc 50% HOẶC thanh toán VNPay 100%
          const qrModal = document.getElementById('qrPaymentModal');
          if (qrModal) {
              const modalTitle = qrModal.querySelector('h3');
              if (modalTitle) {
                  modalTitle.textContent = hasCustomItemSubmit && !isVNPay ? 'Thanh toán cọc 50% qua Mã QR' : 'Thanh toán qua Mã QR (VNPay)';
              }
              document.getElementById('qrDepositAmount').textContent = depositToPay.toLocaleString('vi-VN') + 'đ';
              qrModal.style.display = 'flex';
              
              // Remove old event listeners by replacing nodes
              const btnSuccess = document.getElementById('btnSuccessQr');
              const btnCancel = document.getElementById('btnCancelQr');
              
              const newSuccess = btnSuccess.cloneNode(true);
              const newCancel = btnCancel.cloneNode(true);
              btnSuccess.parentNode.replaceChild(newSuccess, btnSuccess);
              btnCancel.parentNode.replaceChild(newCancel, btnCancel);
              
              newCancel.addEventListener('click', () => {
                  qrModal.style.display = 'none';
                  processCheckoutFinal(false);
              });
              
              newSuccess.addEventListener('click', () => {
                  qrModal.style.display = 'none';
                  const btnConfirm = document.getElementById("btnConfirmOrder");
                  if (btnConfirm) {
                    btnConfirm.disabled = true;
                    btnConfirm.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
                  }
                  processCheckoutFinal(true);
              });
          }
      } else {
          const btnConfirm = document.getElementById("btnConfirmOrder");
          if (btnConfirm) {
            btnConfirm.disabled = true;
            btnConfirm.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo đơn hàng...';
          }
          processCheckoutFinal(true);
      }
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
      if (totalEl) {
          totalEl.textContent = orderData.total;
          if (orderData.paid && orderData.remaining) {
              const totalParent = totalEl.closest('.detail-line');
              if (totalParent && !document.getElementById("detailPaid")) {
                  totalParent.insertAdjacentHTML('afterend', `
                      <div class="detail-line">
                          <span class="detail-label">Đã thanh toán (Cọc):</span>
                          <span class="detail-val" id="detailPaid" style="color:var(--accent-gold); font-weight:700;">${orderData.paid}</span>
                      </div>
                      <div class="detail-line">
                          <span class="detail-label">Cần thanh toán thêm:</span>
                          <span class="detail-val" id="detailRemaining" style="color:var(--primary-brown); font-weight:700;">${orderData.remaining}</span>
                      </div>
                  `);
              }
          }
      }

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
