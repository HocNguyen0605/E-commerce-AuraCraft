document.addEventListener("DOMContentLoaded", () => {
  const checkoutForm = document.getElementById("checkoutForm");
  if (checkoutForm) {
    const savedAddressField = document.getElementById("address");
    const savedProvinceSelect = document.getElementById("province");
    if (savedAddressField && savedProvinceSelect && savedAddressField.value.trim()) {
      const provinceNames = Array.from(savedProvinceSelect.options)
        .map(option => option.value || option.textContent.trim())
        .filter(Boolean)
        .sort((a, b) => b.length - a.length);
      let cleanedAddress = savedAddressField.value.trim();
      const detectedProvinces = [];
      for (let pass = 0; pass < provinceNames.length; pass++) {
        const match = provinceNames.find(name => cleanedAddress.toLocaleLowerCase("vi").endsWith(name.toLocaleLowerCase("vi")));
        if (!match) break;
        if (!detectedProvinces.includes(match)) detectedProvinces.push(match);
        cleanedAddress = cleanedAddress.slice(0, -match.length).replace(/[\s,;|-]+$/g, "").trim();
      }
      if (detectedProvinces.length) {
        savedAddressField.value = cleanedAddress;
        savedProvinceSelect.value = detectedProvinces.length === 1 ? detectedProvinces[0] : "";
      }
    }
    const customOrderId = new URLSearchParams(window.location.search).get("orderId");
    const customOrder = customOrderId && window.AuraCraftCustom.getOrder(customOrderId);
    let checkoutCart = [];
    try { checkoutCart = JSON.parse(localStorage.getItem("AuraCraftCheckoutCart") || "[]"); } catch { checkoutCart = []; }
    const cartCheckoutMode = !customOrder && (new URLSearchParams(window.location.search).get("fromCart") === "1"
      || checkoutCart.length > 0 || Boolean(checkoutForm.dataset.productIds));
    const cartSubtotal = checkoutCart.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 1), 0);
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
      const selectedMaterials = (customOrder.design || []).filter(Boolean).map(piece => piece.name).join(", ");
      const designSizeText = customOrder.designSize ? ` · ${customOrder.designSize} vị trí` : "";
      meta.textContent = `Thợ: ${customOrder.sellerName} · Chế tác: ${customOrder.days} ngày${designSizeText}${selectedMaterials ? ` · ${selectedMaterials}` : ""}`;
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
      if (customerName && customOrder.buyerName
          && customOrder.buyerName !== "Khách hàng AuraCraft"
          && !customerName.value.trim()) customerName.value = customOrder.buyerName;
      const customerEmail = document.getElementById("email");
      if (customerEmail && customOrder.buyerEmail) customerEmail.value = customOrder.buyerEmail;
      document.querySelector(".checkout-breadcrumb .active").textContent = "Thanh toán đơn Custom";
    }

    if (!customOrder && checkoutForm.dataset.databaseCheckout === "true") {
      const serverItems = document.querySelectorAll("#summaryProductsList .summary-product-item");
      if (!serverItems.length) {
        const localIds = checkoutCart.map(item => Number(item.id)).filter(id => Number.isInteger(id) && id > 0);
        if (!checkoutForm.dataset.productIds && localIds.length) {
          const url = new URL(checkoutForm.dataset.context + "/pages/checkout", window.location.origin);
          url.searchParams.set("fromCart", "1");
          url.searchParams.set("productIds", localIds.join(","));
          window.location.replace(url.href);
          return;
        }
        const warning = document.createElement("p");
        warning.className = "checkout-products-error";
        warning.setAttribute("role", "alert");
        warning.textContent = "Không tải được sản phẩm đã chọn từ giỏ hàng. Hãy quay lại giỏ và chọn sản phẩm cần thanh toán.";
        document.querySelector(".summary-products-list").replaceChildren(warning);
        document.getElementById("btnConfirmOrder").disabled = true;
        return;
      }
    }

    if (cartCheckoutMode) {
      const databaseProductIds = checkoutForm.dataset.productIds || "";
      if (!checkoutCart.length && !(checkoutForm.dataset.databaseCheckout === "true" && databaseProductIds)) {
        checkoutForm.hidden = true;
        alert("Giỏ hàng chưa có sản phẩm được chọn.");
        window.location.href = `${checkoutForm.dataset.context || ""}/pages/cart`;
        return;
      }
      if (checkoutForm.dataset.databaseCheckout !== "true") {
        const list = document.querySelector(".summary-products-list");
        list.replaceChildren(...checkoutCart.map(item => {
          const row = document.createElement("div"); row.className = "summary-product-item";
          const image = document.createElement("img"); image.className = "summary-product-thumb"; image.src = item.image || "../assets/img/products/bracelet_01.jpg"; image.alt = item.name || "Sản phẩm";
          const details = document.createElement("div"); details.className = "summary-product-details";
          const name = document.createElement("h3"); name.className = "summary-product-title"; name.textContent = item.name || "Sản phẩm AuraCraft";
          const meta = document.createElement("p"); meta.className = "summary-product-meta"; meta.textContent = `Số lượng: ${Number(item.quantity || 1)}`;
          details.append(name, meta);
          const price = document.createElement("span"); price.className = "summary-product-price"; price.textContent = `${(Number(item.price || 0) * Number(item.quantity || 1)).toLocaleString("vi-VN")}đ`;
          row.append(image, details, price); return row;
        }));
        const sellerCount = Math.max(1, new Set(checkoutCart.map(item => item.shopName).filter(Boolean)).size);
        const shipping = 25300 * sellerCount;
        document.getElementById("summarySubtotal").textContent = `${cartSubtotal.toLocaleString("vi-VN")}đ`;
        document.getElementById("summaryShipping").textContent = `${shipping.toLocaleString("vi-VN")}đ`;
        document.getElementById("summaryTotal").textContent = `${(cartSubtotal + shipping).toLocaleString("vi-VN")}đ`;
      }
    }

    let hasCustomItem = checkoutForm.dataset.hasCustom === "true"
      || Array.from(document.querySelectorAll('.summary-product-title')).some(el => el.textContent.toLowerCase().includes('custom'))
      || !!customOrder;

    const paymentCards = document.querySelectorAll(".payment-method-card");
    
    function updateDepositUI(radio) {
        if (!radio) {
            radio = document.querySelector('input[name="paymentMethod"]:checked') || { value: 'cod' };
        }
        const depositAlert = document.getElementById('customDepositAlert');
        const depositRow = document.getElementById('depositRow');
        const remainingRow = document.getElementById('remainingRow');
        const btnConfirmText = document.getElementById('btnConfirmText');
        
        let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
        if (customOrder) totalStr = (customAmountDue + 25300).toString();
        let totalVal = parseInt(totalStr, 10) || 0;
        let depositVal = Math.round(totalVal / 2);
        let remainingVal = totalVal - depositVal;

        if (!hasCustomItem) {
            // Sản phẩm CÓ SẴN: COD được thanh toán luôn khi nhận hàng, không cọc!
            if (depositAlert) depositAlert.hidden = true;
            if (depositRow) depositRow.hidden = true;
            if (remainingRow) remainingRow.hidden = true;
            if (btnConfirmText) {
                btnConfirmText.textContent = radio.value === 'vnpay' ? 'Thanh toán qua VNPay' : 'Xác nhận đặt hàng';
            }
            return;
        }

        // Sản phẩm CUSTOM:
        if (depositAlert) depositAlert.hidden = false;
        if (radio.value === 'vnpay') {
            // VNPay cho custom: Thanh toán 100%
            if (depositRow) depositRow.hidden = true;
            if (remainingRow) remainingRow.hidden = true;
            if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán 100% qua VNPay';
            depositAlert.innerHTML = '<i class="fa-solid fa-circle-info"></i> <strong>Sản phẩm Custom:</strong> Bạn đang chọn thanh toán trực tuyến toàn bộ 100% qua VNPay.';
        } else {
            // COD cho custom: Cọc 50%, 50% còn lại thanh toán khi nhận hàng (COD)
            if (depositRow) depositRow.hidden = false;
            if (remainingRow) remainingRow.hidden = false;
            const depositEl = document.getElementById('summaryDeposit');
            const remainingEl = document.getElementById('summaryRemaining');
            if (depositEl) depositEl.textContent = depositVal.toLocaleString('vi-VN') + 'đ';
            if (remainingEl) remainingEl.textContent = remainingVal.toLocaleString('vi-VN') + 'đ';
            if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán cọc 50% (Mô phỏng)';
            depositAlert.innerHTML = '<i class="fa-solid fa-circle-exclamation"></i> <strong>Lưu ý:</strong> Đơn hàng có sản phẩm Custom chế tác riêng, vui lòng thanh toán cọc trước <strong>50%</strong>. Phần còn lại (50%) sẽ thanh toán khi nhận hàng (COD).';
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

    // Voucher and loyalty coin previews are calculated by the server against the
    // selected database cart, so the displayed total matches the order transaction.
    if (checkoutForm.dataset.databaseCheckout === "true" && !customOrder && checkoutForm.dataset.productIds) {
      const voucherInput = document.getElementById("voucherCode");
      const voucherMessage = document.getElementById("voucherMessage");
      const coinsMessage = document.getElementById("coinsMessage");
      const useCoinsButton = document.getElementById("btnUseCoins");
      let useCoins = false;
      const money = value => `${Number(value || 0).toLocaleString("vi-VN")}đ`;
      const preview = async () => {
        const params = new URLSearchParams({
          action: "preview-benefits",
          productIds: checkoutForm.dataset.productIds,
          voucherCode: voucherInput?.value.trim() || "",
          useCoins: String(useCoins)
        });
        try {
          const response = await fetch(checkoutForm.dataset.context + "/pages/checkout", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json" },
            body: params
          });
          const quote = await response.json();
          if (!response.ok) throw new Error(quote.message || "Không thể kiểm tra ưu đãi.");
          document.getElementById("summarySubtotal").textContent = money(quote.subtotal);
          document.getElementById("summaryShipping").textContent = money(quote.shipping);
          document.getElementById("summaryTotal").textContent = money(quote.total);
          const voucherRow = document.getElementById("voucherDiscountRow");
          const coinRow = document.getElementById("coinDiscountRow");
          voucherRow.hidden = Number(quote.voucherDiscount || 0) <= 0;
          coinRow.hidden = Number(quote.coinDiscount || 0) <= 0;
          document.getElementById("summaryVoucherDiscount").textContent = `-${money(quote.voucherDiscount)}`;
          document.getElementById("summaryCoinDiscount").textContent = `-${money(quote.coinDiscount)}`;
          document.getElementById("summaryCoinsUsed").textContent = quote.coinsUsed || 0;
          document.getElementById("availableCoins").textContent = `${quote.availableCoins || 0} xu`;
          if (useCoinsButton) useCoinsButton.disabled = Number(quote.coinsAvailableToUse || 0) <= 0;
          if (voucherMessage) voucherMessage.textContent = quote.voucherDiscount > 0
            ? `Đã áp dụng voucher, giảm ${money(quote.voucherDiscount)}.`
            : (voucherInput?.value.trim() ? "Mã voucher hợp lệ nhưng không tạo ưu đãi." : "Nhập mã voucher để kiểm tra điều kiện.");
          if (coinsMessage) coinsMessage.textContent = useCoins
            ? `Đang dùng ${quote.coinsUsed || 0} xu, giảm ${money(quote.coinDiscount)}; còn ${quote.remainingCoins || 0} xu khả dụng. 100 xu giảm 1.000đ. ${quote.pendingCoins || 0} xu từ đơn chưa hoàn tất đang chờ.`
            : `Bạn có ${quote.availableCoins || 0} xu khả dụng. ${quote.pendingCoins || 0} xu từ đơn chưa hoàn tất đang chờ. 100 xu giảm 1.000đ; tích 1 xu mỗi 10.000đ thực trả.`;
          if (quote.hasCustomItem !== undefined) {
            hasCustomItem = Boolean(quote.hasCustomItem);
          }
          updateDepositUI();
        } catch (error) {
          if (voucherMessage) voucherMessage.textContent = error.message;
          if (coinsMessage && useCoins) coinsMessage.textContent = error.message;
        }
      };
      let voucherTimer;
      voucherInput?.addEventListener("input", () => {
        clearTimeout(voucherTimer);
        voucherTimer = setTimeout(preview, 250);
      });
      document.getElementById("btnClearVoucher")?.addEventListener("click", () => {
        if (voucherInput) voucherInput.value = "";
        preview();
      });
      voucherInput?.addEventListener("keydown", event => {
        if (event.key === "Enter") { event.preventDefault(); preview(); }
      });
      useCoinsButton?.addEventListener("click", () => { useCoins = !useCoins; useCoinsButton.textContent = useCoins ? "Bỏ dùng xu" : "Dùng xu"; preview(); });
      document.querySelectorAll(".checkout-voucher-option").forEach(option => option.addEventListener("click", () => {
        voucherInput.value = option.dataset.voucherCode || "";
        preview();
      }));
      preview();
    }

    // Submit form đặt hàng
    checkoutForm.addEventListener("submit", async (e) => {
      e.preventDefault();

      const fullName = document.getElementById("fullName");
      const phone = document.getElementById("phone");
      const address = document.getElementById("address");
      const province = document.getElementById("province");
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

      if (province && !province.value) {
        province.closest(".form-group").classList.add("invalid");
        isValid = false;
      } else if (province) {
        province.closest(".form-group").classList.remove("invalid");
      }

      if (!isValid) {
        const firstInvalid = document.querySelector(".form-group.invalid");
        if (firstInvalid) {
          firstInvalid.scrollIntoView({ behavior: "smooth", block: "center" });
        }
        return;
      }

      // Signed-in buyers submit the selected cart item IDs to the server. Product names,
      // prices, stock and quantities are reloaded from the database before orders are saved.
      if (checkoutForm.dataset.databaseCheckout === "true" && !customOrder) {
        const productIds = (checkoutForm.dataset.productIds || "").split(",").map(id => Number(id)).filter(id => Number.isInteger(id) && id > 0);
        const button = document.getElementById("btnConfirmOrder");
        button.disabled = true;
        const isSelectedVnPay = document.querySelector('input[name="paymentMethod"]:checked')?.value === "vnpay";
        button.innerHTML = isSelectedVnPay
          ? '<i class="fa-solid fa-spinner fa-spin"></i> Đang kết nối VNPay...'
          : '<i class="fa-solid fa-spinner fa-spin"></i> Đang tạo đơn hàng...';
        try {
          const response = await fetch(checkoutForm.dataset.context + "/pages/checkout", {
            method: "POST",
            headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json" },
            signal: AbortSignal.timeout(20000),
            body: new URLSearchParams({
              productIds: productIds.join(","),
              fullName: fullName.value.trim(),
              phone: phone.value.trim(),
              address: [address.value.trim(), province?.value].filter(Boolean).join(", "),
              note: note ? note.value.trim() : "",
              paymentMethod: document.querySelector('input[name="paymentMethod"]:checked')?.value || "cod",
              voucherCode: document.getElementById("voucherCode")?.value.trim() || "",
              useCoins: document.getElementById("coinDiscountRow")?.hidden === false ? "true" : "false"
            })
          });
          const result = await response.json();
          if (!response.ok) throw new Error(result.message || "Không thể tạo đơn hàng.");

          const isOnlinePayment = result.paymentMethod === "VNPay";
          const isCustomOrder = Boolean(result.hasCustomItem || hasCustomItem);
          const total = Number(result.total || 0);
          const subtotal = Number(result.subtotal || 0);
          const shipping = Number(result.shipping || 0);
          const depositAmount = Math.round(total / 2);
          const remainingAmount = total - depositAmount;

          // Quy tắc thanh toán:
          // 1. VNPay: luôn yêu cầu thanh toán trực tuyến 100% mô phỏng.
          // 2. COD sản phẩm Custom: yêu cầu thanh toán cọc 50% mô phỏng.
          // 3. COD sản phẩm có sẵn: ĐƯỢC THANH TOÁN LUÔN khi nhận hàng, KHÔNG mở modal thanh toán cọc!
          const needsImmediatePayment = isOnlinePayment || (isCustomOrder && !isOnlinePayment);
          const modal = document.getElementById("qrPaymentModal");
          if (needsImmediatePayment && modal) {
            document.getElementById("mockPaymentTitle").textContent = isOnlinePayment
              ? "Mô phỏng thanh toán VNPay (100%)"
              : "Mô phỏng thanh toán cọc COD (50%)";
            document.getElementById("mockPaymentDescription").innerHTML = isOnlinePayment
              ? `Xác nhận thanh toán mô phỏng <strong>${total.toLocaleString("vi-VN")}đ</strong> qua VNPay (100%). Không có giao dịch ngân hàng thật.`
              : `Sản phẩm Custom yêu cầu cọc 50%. Xác nhận cọc mô phỏng 50% (<strong>${depositAmount.toLocaleString("vi-VN")}đ</strong>) để thợ bắt đầu chế tác. Phần còn lại <strong>${remainingAmount.toLocaleString("vi-VN")}đ</strong> sẽ thanh toán khi nhận hàng (COD).`;
            
            const qrPlaceholder = modal.querySelector(".qr-code-placeholder");
            if (qrPlaceholder) {
              qrPlaceholder.innerHTML = '<i class="fa-solid fa-laptop-code"></i><p id="mockPaymentDemoNote">CHẾ ĐỘ DEMO · KHÔNG GIAO DỊCH NGÂN HÀNG</p>';
            }
            document.getElementById("mockPaymentReference").textContent = result.paymentReference || "";

            const confirmButton = document.getElementById("btnSuccessQr");
            const cancelButton = document.getElementById("btnCancelQr");

            modal.hidden = false;
            modal.style.display = "flex";
            confirmButton.disabled = false;
            cancelButton.disabled = false;
            confirmButton.innerHTML = "Xác nhận giả lập thanh toán";
            cancelButton.innerHTML = "Quay lại";

            const paymentConfirmed = await new Promise((resolve) => {
              const onConfirm = async () => {
                confirmButton.removeEventListener("click", onConfirm);
                cancelButton.removeEventListener("click", onCancel);
                confirmButton.disabled = true;
                cancelButton.disabled = true;
                confirmButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xác thực giao dịch...';
                try {
                  const paymentAction = isOnlinePayment ? "confirm" : "confirm-cod-deposit";
                  const paymentResponse = await fetch(checkoutForm.dataset.context + "/vnpay/simulate", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json" },
                    body: new URLSearchParams({ action: paymentAction, paymentReference: result.paymentReference || "" })
                  });
                  const paymentResult = await paymentResponse.json();
                  if (!paymentResponse.ok || !paymentResult.success) {
                    throw new Error(paymentResult.message || "Không thể xác nhận giao dịch mô phỏng.");
                  }

                  if (qrPlaceholder) {
                    qrPlaceholder.innerHTML = '<i class="fa-solid fa-circle-check" style="color: #10b981; font-size: 72px;"></i>' +
                      '<p style="color: #10b981; font-size: 13px; margin-top: 10px; font-weight: 700;">GIAO DỊCH THÀNH CÔNG</p>';
                  }
                  confirmButton.innerHTML = '<i class="fa-solid fa-check"></i> Đang chuyển hướng...';
                  await new Promise(r => setTimeout(r, 350));
                  modal.hidden = true;
                  modal.style.display = "none";
                  resolve(true);
                } catch (err) {
                  window.alert(err.message || "Lỗi xác thực thanh toán.");
                  confirmButton.disabled = false;
                  cancelButton.disabled = false;
                  confirmButton.innerHTML = "Xác nhận giả lập thanh toán";
                  if (qrPlaceholder) {
                    qrPlaceholder.innerHTML = '<i class="fa-solid fa-laptop-code"></i><p id="mockPaymentDemoNote">CHẾ ĐỘ DEMO · KHÔNG GIAO DỊCH NGÂN HÀNG</p>';
                  }
                  resolve(false);
                }
              };

              const onCancel = async () => {
                confirmButton.removeEventListener("click", onConfirm);
                cancelButton.removeEventListener("click", onCancel);
                confirmButton.disabled = true;
                cancelButton.disabled = true;
                cancelButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang hủy...';
                try {
                  const paymentAction = isOnlinePayment ? "cancel" : "cancel-cod-deposit";
                  await fetch(checkoutForm.dataset.context + "/vnpay/simulate", {
                    method: "POST",
                    headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8", "Accept": "application/json" },
                    body: new URLSearchParams({ action: paymentAction, paymentReference: result.paymentReference || "" })
                  });
                } catch (_) {}
                modal.hidden = true;
                modal.style.display = "none";
                resolve(false);
              };

              confirmButton.addEventListener("click", onConfirm, { once: true });
              cancelButton.addEventListener("click", onCancel, { once: true });
            });

            if (!paymentConfirmed) {
              window.alert("Đã hủy đơn hàng và khôi phục sản phẩm vào giỏ hàng.");
              window.location.href = checkoutForm.dataset.context + "/pages/cart";
              return;
            }
          }

          let paymentMethodDisplay = "Thanh toán khi nhận hàng (COD)";
          let paidDisplay = undefined;
          let remainingDisplay = undefined;

          if (isOnlinePayment) {
            paymentMethodDisplay = "VNPay (Đã thanh toán trực tuyến 100%)";
            paidDisplay = total.toLocaleString("vi-VN") + "đ";
            remainingDisplay = "0đ";
          } else if (isCustomOrder) {
            paymentMethodDisplay = "COD (Đã thanh toán cọc 50% qua VNPay)";
            paidDisplay = depositAmount.toLocaleString("vi-VN") + "đ";
            remainingDisplay = remainingAmount.toLocaleString("vi-VN") + "đ";
          }

          sessionStorage.setItem("auracraft_latest_order", JSON.stringify({
            orderId: "#AC-" + result.orderId,
            orderIds: result.orderIds,
            fullName: fullName.value.trim(), phone: phone.value.trim(),
            address: [address.value.trim(), province?.value].filter(Boolean).join(", "), note: note ? note.value.trim() : "",
            email: document.getElementById("email")?.value || "",
            paymentMethod: paymentMethodDisplay,
            shippingFee: shipping.toLocaleString("vi-VN") + "đ",
            subtotal: subtotal.toLocaleString("vi-VN") + "đ",
            total: total.toLocaleString("vi-VN") + "đ",
            paid: paidDisplay,
            remaining: remainingDisplay,
            createdAt: new Date().toLocaleString("vi-VN"),
            rewardCoins: Number(result.rewardCoinsPending || 0)
          }));
          const boughtIds = new Set(productIds.map(String));
          try {
            const localCart = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]");
            localStorage.setItem("AuraCraftCart", JSON.stringify(localCart.filter(item => !boughtIds.has(String(item.id)))));
          } catch (_) { }
          localStorage.removeItem("AuraCraftCheckoutCart");
          localStorage.removeItem("AuraCraftCheckoutSource");
          sessionStorage.removeItem("AuraCraftCartSelected");
          window.location.href = checkoutForm.dataset.context + "/pages/success?orderId=" + encodeURIComponent("#AC-" + result.orderId);
        } catch (error) {
          window.alert(error.message);
          button.disabled = false;
          button.innerHTML = '<span id="btnConfirmText">Xác nhận đặt hàng</span><i class="fa-solid fa-arrow-right"></i>';
          updateDepositUI();
        }
        return;
      }

      const paymentRadioChecked = document.querySelector('input[name="paymentMethod"]:checked');
      const isVNPay = paymentRadioChecked && paymentRadioChecked.value === 'vnpay';
      const hasCustomItemSubmit = checkoutForm.dataset.hasCustom === "true"
        || Array.from(document.querySelectorAll('.summary-product-title')).some(el => el.textContent.toLowerCase().includes('custom'))
        || !!customOrder;
      
      let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
      if (customOrder) totalStr = (customAmountDue + 25300).toString();
      const totalNumber = parseInt(totalStr, 10) || 415300;
      const depositToPay = Math.round(totalNumber / 2);
      const remainingToPay = totalNumber - depositToPay;

      function processCheckoutFinal(paymentResult) {
        if (!paymentResult) {
          alert('Thao tác thanh toán bị hủy hoặc không thành công.');
          const btnConfirm = document.getElementById("btnConfirmOrder");
          if (btnConfirm) {
            btnConfirm.disabled = false;
            btnConfirm.innerHTML = '<span id="btnConfirmText">Xác nhận đặt hàng</span><i class="fa-solid fa-arrow-right"></i>';
            updateDepositUI(paymentRadioChecked);
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
              method: isVNPay ? "vnpay" : "vnpay_deposit",
              amount: customOrder.paymentStatus === "adjustment_due"
                ? customAmountDue
                : (isVNPay ? customAmountDue + 25300 : depositToPay)
            });
            if (customOrder.customDesignId) {
              try {
                const cart = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]");
                localStorage.setItem("AuraCraftCart", JSON.stringify(cart.filter(item =>
                  String(item.customDesignId || "") !== String(customOrder.customDesignId)
                )));
                window.dispatchEvent(new Event("auracraft:cart-updated"));
              } catch { }
            }
            
            let paymentMethodText = isVNPay
              ? "VNPay (Đã thanh toán trực tuyến 100%)"
              : "COD (Đã cọc 50% qua VNPay)";

            const customOrderData = {
              orderId: result.order.id,
              fullName: fullName.value.trim(),
              phone: phone.value.trim(),
              address: address.value.trim(),
              email: document.getElementById("email")?.value || "",
              note: note ? note.value.trim() : "",
              paymentMethod: paymentMethodText,
              shippingFee: "25.300đ",
              subtotal: `${customAmountDue.toLocaleString("vi-VN")}đ`,
              total: `${(customAmountDue + 25300).toLocaleString("vi-VN")}đ`,
              paid: isVNPay ? `${(customAmountDue + 25300).toLocaleString("vi-VN")}đ` : `${depositToPay.toLocaleString("vi-VN")}đ`,
              remaining: isVNPay ? "0đ" : `${remainingToPay.toLocaleString("vi-VN")}đ`,
              createdAt: new Date().toLocaleString("vi-VN"),
              sellerName: result.order.sellerName,
              paymentStatus: result.order.paymentStatus
            };
            sessionStorage.setItem("auracraft_latest_order", JSON.stringify(customOrderData));
            window.location.href = `${checkoutForm.dataset.context || ""}/pages/success?orderId=${encodeURIComponent(result.order.id)}`;
          } catch (error) {
            alert(error.message);
          }
          return;
        }

        // Tạo mã đơn hàng cho hàng có sẵn hoặc đơn giỏ hàng
        const randomCode = Math.floor(100000 + Math.random() * 900000);
        const virtualOrderId = `#AC-${randomCode}`;

        let paymentMethodText = "Thanh toán khi nhận hàng (COD)";
        let paidVal = undefined;
        let remainingVal = undefined;

        if (isVNPay) {
          paymentMethodText = "VNPay (Đã thanh toán trực tuyến 100%)";
          paidVal = totalNumber.toLocaleString("vi-VN") + "đ";
          remainingVal = "0đ";
        } else if (hasCustomItemSubmit) {
          paymentMethodText = "COD (Đã cọc 50% qua VNPay)";
          paidVal = depositToPay.toLocaleString("vi-VN") + "đ";
          remainingVal = remainingToPay.toLocaleString("vi-VN") + "đ";
        }

        const orderData = {
          orderId: virtualOrderId,
          fullName: fullName.value.trim(),
          phone: phone.value.trim(),
          address: address.value.trim(),
          email: document.getElementById("email")?.value || "",
          note: note ? note.value.trim() : "",
          paymentMethod: paymentMethodText,
          shippingFee: "25.300đ",
          subtotal: `${(cartCheckoutMode ? cartSubtotal : 390000).toLocaleString("vi-VN")}đ`,
          total: `${totalNumber.toLocaleString("vi-VN")}đ`,
          paid: paidVal,
          remaining: remainingVal,
          createdAt: new Date().toLocaleString("vi-VN"),
        };

        sessionStorage.setItem(
          "auracraft_latest_order",
          JSON.stringify(orderData),
        );

        if (cartCheckoutMode) {
          const checkedOutIds = new Set(checkoutCart.map(item => String(item.id)));
          const currentCart = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]");
          localStorage.setItem("AuraCraftCart", JSON.stringify(currentCart.filter(item => !checkedOutIds.has(String(item.id)))));
          window.dispatchEvent(new Event("auracraft:cart-updated"));
          if (localStorage.getItem("AuraCraftCheckoutSource") === "database") {
            fetch(new URL("../api/cart", window.location.href), {
              method: "POST",
              headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
              body: new URLSearchParams({ action: "remove-many", productIds: [...checkedOutIds].join(",") })
            }).catch(error => console.error("Could not sync checked out items with database cart", error));
          }
          localStorage.removeItem("AuraCraftCheckoutCart");
          localStorage.removeItem("AuraCraftCheckoutSource");
        }

        setTimeout(() => {
          window.location.href = `${checkoutForm.dataset.context || ""}/pages/success?orderId=${encodeURIComponent(virtualOrderId)}`;
        }, 400);
      }

      const btnConfirm = document.getElementById("btnConfirmOrder");
      if (btnConfirm) {
        btnConfirm.disabled = true;
        btnConfirm.innerHTML = isVNPay
          ? '<i class="fa-solid fa-spinner fa-spin"></i> Đang kết nối VNPay...'
          : '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
      }

      const needsImmediatePaymentBrowser = isVNPay || (hasCustomItemSubmit && !isVNPay);
      const modal = document.getElementById("qrPaymentModal");

      if (needsImmediatePaymentBrowser && modal) {
        document.getElementById("mockPaymentTitle").textContent = isVNPay
          ? "Mô phỏng thanh toán VNPay (100%)"
          : "Mô phỏng thanh toán cọc COD (50%)";
        document.getElementById("mockPaymentDescription").innerHTML = isVNPay
          ? `Xác nhận thanh toán mô phỏng <strong>${totalNumber.toLocaleString("vi-VN")}đ</strong> qua VNPay (100%). Không có giao dịch ngân hàng thật.`
          : `Sản phẩm Custom yêu cầu cọc 50%. Xác nhận cọc mô phỏng 50% (<strong>${depositToPay.toLocaleString("vi-VN")}đ</strong>) để thợ bắt đầu chế tác. Phần còn lại <strong>${remainingToPay.toLocaleString("vi-VN")}đ</strong> sẽ thanh toán khi nhận hàng (COD).`;
        
        const qrPlaceholder = modal.querySelector(".qr-code-placeholder");
        if (qrPlaceholder) {
          qrPlaceholder.innerHTML = '<i class="fa-solid fa-laptop-code"></i><p id="mockPaymentDemoNote">CHẾ ĐỘ DEMO · KHÔNG GIAO DỊCH NGÂN HÀNG</p>';
        }
        const virtualRef = "DEMO" + Math.floor(10000000 + Math.random() * 90000000);
        document.getElementById("mockPaymentReference").textContent = virtualRef;

        modal.hidden = false;
        modal.style.display = "flex";
        const confirmButton = document.getElementById("btnSuccessQr");
        const cancelButton = document.getElementById("btnCancelQr");
        confirmButton.disabled = false;
        cancelButton.disabled = false;
        confirmButton.innerHTML = "Xác nhận giả lập thanh toán";
        cancelButton.innerHTML = "Quay lại";

        const onConfirmBrowser = async () => {
          confirmButton.removeEventListener("click", onConfirmBrowser);
          cancelButton.removeEventListener("click", onCancelBrowser);
          confirmButton.disabled = true;
          cancelButton.disabled = true;
          confirmButton.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xác thực...';

          if (qrPlaceholder) {
            qrPlaceholder.innerHTML = '<i class="fa-solid fa-circle-check" style="color: #10b981; font-size: 72px;"></i>' +
              '<p style="color: #10b981; font-size: 13px; margin-top: 10px; font-weight: 700;">GIAO DỊCH THÀNH CÔNG</p>';
          }
          confirmButton.innerHTML = '<i class="fa-solid fa-check"></i> Đang chuyển hướng...';
          await new Promise(r => setTimeout(r, 350));
          modal.hidden = true;
          modal.style.display = "none";
          processCheckoutFinal(true);
        };

        const onCancelBrowser = () => {
          confirmButton.removeEventListener("click", onConfirmBrowser);
          cancelButton.removeEventListener("click", onCancelBrowser);
          modal.hidden = true;
          modal.style.display = "none";
          if (btnConfirm) {
            btnConfirm.disabled = false;
            btnConfirm.innerHTML = '<span id="btnConfirmText">Xác nhận đặt hàng</span><i class="fa-solid fa-arrow-right"></i>';
            updateDepositUI(paymentRadioChecked);
          }
        };

        confirmButton.addEventListener("click", onConfirmBrowser, { once: true });
        cancelButton.addEventListener("click", onCancelBrowser, { once: true });
      } else {
        // Đơn mua sản phẩm có sẵn chọn COD: hoàn tất đặt hàng ngay lập tức!
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

      const emailEl = document.getElementById("detailEmail");
      if (emailEl) emailEl.textContent = orderData.email || "—";
      const rewardEl = document.getElementById("detailRewardCoins");
      if (rewardEl) rewardEl.textContent = `${Number(orderData.rewardCoins || 0)} xu (cộng khi đơn hoàn tất${orderData.remaining && orderData.remaining !== "0đ" ? " và thanh toán đủ" : ""})`;

      const noteEl = document.getElementById("detailNote");
      if (noteEl) noteEl.textContent = orderData.note || "Không có ghi chú";

      const paymentEl = document.getElementById("detailPayment");
      if (paymentEl) paymentEl.textContent = orderData.paymentMethod;

      const shippingEl = document.getElementById("detailShipping");
      if (shippingEl) shippingEl.textContent = orderData.shippingFee;

      const totalEl = document.getElementById("detailTotal");
      if (totalEl) {
          totalEl.textContent = orderData.total;
          if (orderData.paid && orderData.remaining && orderData.remaining !== "0đ") {
              const totalParent = totalEl.closest('.detail-line');
              if (totalParent && !document.getElementById("detailPaid")) {
                  totalParent.insertAdjacentHTML('afterend', `
                      <div class="detail-line">
                          <span class="detail-label">Đã thanh toán (Cọc 50%):</span>
                          <span class="detail-val" id="detailPaid" style="color:var(--accent-gold); font-weight:700;">${orderData.paid}</span>
                      </div>
                      <div class="detail-line">
                          <span class="detail-label">Cần thanh toán khi nhận hàng (COD):</span>
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
