document.addEventListener("DOMContentLoaded", () => {
  const checkoutForm = document.getElementById("checkoutForm");
  if (checkoutForm) {
    const savedAddressField = document.getElementById("address");
    const provinceSelect = document.getElementById("province");
    const wardSelect = document.getElementById("ward");
    const wardFallback = document.getElementById("wardFallback");
    const wardLoadingMessage = document.getElementById("wardLoadingMessage");
    const addressSeparator = /[\s,;|-]+$/g;
    const cleanAddressSuffix = (value, suffix) => {
      const text = String(value || "").trim();
      const name = String(suffix || "").trim();
      if (!name || !text.toLocaleLowerCase("vi").endsWith(name.toLocaleLowerCase("vi"))) return null;
      return text.slice(0, -name.length).replace(addressSeparator, "").trim();
    };
    let savedAddressRemainder = savedAddressField?.value.trim() || "";
    // Địa chỉ mặc định đã lưu theo cấu trúc mới: tỉnh / xã / địa chỉ chi tiết tách riêng
    const defaultProvinceCode = checkoutForm.dataset.defaultProvinceCode || "";
    const defaultWardCode = checkoutForm.dataset.defaultWardCode || "";
    const defaultWardName = checkoutForm.dataset.defaultWard || "";
    if (defaultProvinceCode && provinceSelect) {
      provinceSelect.value = defaultProvinceCode;
    }
    // Địa chỉ cũ (chỉ có 1 chuỗi): giữ cách đoán tỉnh từ đuôi chuỗi như trước
    if (!defaultProvinceCode && savedAddressRemainder && provinceSelect) {
      const options = Array.from(provinceSelect.options).filter(option => option.value);
      const match = options.sort((a, b) => b.textContent.length - a.textContent.length)
          .find(option => cleanAddressSuffix(savedAddressRemainder, option.textContent) !== null
              || ["Tỉnh ", "Thành phố ", "TP. ", "TP "].some(prefix => cleanAddressSuffix(savedAddressRemainder, prefix + option.textContent) !== null));
      if (match) {
        savedAddressRemainder = cleanAddressSuffix(savedAddressRemainder, match.textContent)
            ?? ["Tỉnh ", "Thành phố ", "TP. ", "TP "].map(prefix => cleanAddressSuffix(savedAddressRemainder, prefix + match.textContent)).find(value => value !== null);
        savedAddressField.value = savedAddressRemainder;
        provinceSelect.value = match.value;
      }
    }

    const wardDataUrl = "https://raw.githubusercontent.com/mantisvn/Vietnamese-Administrative-Units-Dataset/main/Vietnamese-Administrative-Units-Dataset.json";
    let administrativeWards = [];
    async function loadAdministrativeWards(provinceCode, restoreSavedWard = false) {
      if (!wardSelect) return;
      if (!provinceCode) {
        wardSelect.replaceChildren(new Option("Vui lòng chọn tỉnh / thành phố trước", ""));
        wardSelect.disabled = true;
        if (wardFallback) { wardFallback.hidden = true; wardFallback.required = false; }
        if (wardLoadingMessage) wardLoadingMessage.textContent = "";
        return;
      }
      wardSelect.disabled = true;
      wardSelect.replaceChildren(new Option("Đang tải danh sách xã / phường...", ""));
      if (wardLoadingMessage) wardLoadingMessage.textContent = "Đang tải danh sách đơn vị hành chính mới nhất.";
      try {
        if (!administrativeWards.length) {
          try { administrativeWards = JSON.parse(sessionStorage.getItem("AuraCraftAdministrativeWards2025") || "[]"); }
          catch (_) { administrativeWards = []; }
          if (!Array.isArray(administrativeWards) || administrativeWards.length !== 3321) {
            const response = await fetch(wardDataUrl, { cache: "force-cache" });
            if (!response.ok) throw new Error("Không tải được danh sách xã / phường.");
            const dataset = await response.json();
            administrativeWards = Array.isArray(dataset.wards) ? dataset.wards : [];
            if (administrativeWards.length !== 3321) throw new Error("Danh sách đơn vị hành chính không đầy đủ.");
            try { sessionStorage.setItem("AuraCraftAdministrativeWards2025", JSON.stringify(administrativeWards)); } catch (_) { }
          }
        }
        const options = administrativeWards.filter(item => String(item.province_id) === String(Number(provinceCode)))
            .sort((a, b) => a.name.localeCompare(b.name, "vi"));
        if (!options.length) throw new Error("Không có dữ liệu xã / phường cho tỉnh đã chọn.");
        wardSelect.replaceChildren(new Option("Chọn xã / phường", ""));
        options.forEach(item => wardSelect.add(new Option(item.name, item.code)));
        wardSelect.disabled = false;
        wardSelect.hidden = false;
        if (wardFallback) { wardFallback.hidden = true; wardFallback.required = false; }
        if (wardLoadingMessage) wardLoadingMessage.textContent = `${options.length} xã / phường`;
        if (restoreSavedWard && defaultProvinceCode) {
          // Địa chỉ mới: chọn lại xã theo mã, không có thì khớp theo tên
          const match = options.find(item => defaultWardCode && String(item.code) === String(defaultWardCode))
              || options.find(item => defaultWardName && item.name.toLocaleLowerCase("vi") === defaultWardName.toLocaleLowerCase("vi"));
          if (match) wardSelect.value = match.code;
        } else if (restoreSavedWard && savedAddressRemainder) {
          const match = options.find(item => cleanAddressSuffix(savedAddressRemainder, item.name) !== null);
          if (match) {
            savedAddressRemainder = cleanAddressSuffix(savedAddressRemainder, match.name);
            savedAddressField.value = savedAddressRemainder;
            wardSelect.value = match.code;
          }
        }
      } catch (error) {
        wardSelect.replaceChildren(new Option("Không tải được danh sách — vui lòng nhập bên dưới", ""));
        wardSelect.hidden = true;
        wardSelect.required = false;
        if (wardFallback) {
          wardFallback.hidden = false; wardFallback.required = true;
          if (restoreSavedWard && defaultWardName && !wardFallback.value) wardFallback.value = defaultWardName;
        }
        if (wardLoadingMessage) wardLoadingMessage.textContent = "Có thể nhập tên xã, phường thủ công nếu danh sách chưa tải được.";
      }
    }
    provinceSelect?.addEventListener("change", () => loadAdministrativeWards(provinceSelect.value));
    if (provinceSelect?.value) loadAdministrativeWards(provinceSelect.value, true);

    function completeDeliveryAddress() {
      const wardName = wardSelect && !wardSelect.hidden
          ? wardSelect.selectedOptions[0]?.textContent.trim()
          : wardFallback?.value.trim();
      return [savedAddressField?.value.trim(), wardName, provinceSelect?.selectedOptions[0]?.textContent.trim()]
          .filter(Boolean).join(", ");
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
      const isAdjustment = customOrder.paymentStatus === "adjustment_due";
      document.querySelector(".total-amount").textContent = `${(customAmountDue + (isAdjustment ? 0 : 25300)).toLocaleString("vi-VN")}đ`;
      if (isAdjustment) {
        shippingRow.textContent = "0đ";
        shippingRow.closest(".cost-row")?.setAttribute("hidden", "hidden");
        document.querySelector(".cost-row .cost-value").previousElementSibling.textContent = "Khoản chênh lệch cần thanh toán";
      }
      const customerName = document.getElementById("fullName");
      if (customerName && customOrder.buyerName) customerName.value = customOrder.buyerName;
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
        window.location.href = "cart.jsp";
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

    // Deposit is reserved for quote-marketplace and new shop-design orders.
    // A customization of an existing product still supports COD or 100% transfer.
    // Product/cart checkouts always use COD or full transfer. Only standalone
    // shop designs and selected quotes use the 50% deposit.
    let hasCustomItem = Boolean(customOrder && customOrder.orderType !== "product_customization");

    const paymentCards = document.querySelectorAll(".payment-method-card");
    const standardPaymentCard = document.getElementById("paymentCodStandardCard");
    const customPaymentCard = document.getElementById("paymentCodCustomCard");
    const transferPaymentCard = document.getElementById("paymentVnpayCard");
    const standardPaymentRadio = document.getElementById("paymentCODStandard");
    const depositPaymentRadio = document.getElementById("paymentCODCustom");
    const transferPaymentRadio = document.getElementById("paymentVNPay");

    function syncPaymentOptions() {
      const paymentCompleted = ["paid", "deposit_paid"].includes(customOrder?.paymentStatus);
      const standardDisabled = hasCustomItem || paymentCompleted;
      const depositDisabled = !hasCustomItem || paymentCompleted;
      const transferDisabled = hasCustomItem || paymentCompleted;
      if (standardPaymentRadio) standardPaymentRadio.disabled = standardDisabled;
      if (depositPaymentRadio) depositPaymentRadio.disabled = depositDisabled;
      if (transferPaymentRadio) transferPaymentRadio.disabled = transferDisabled;
      standardPaymentCard?.classList.toggle("disabled", standardDisabled);
      customPaymentCard?.classList.toggle("disabled", depositDisabled);
      transferPaymentCard?.classList.toggle("disabled", transferDisabled);

      const selected = document.querySelector('input[name="paymentMethod"]:checked');
      if (hasCustomItem && depositPaymentRadio) depositPaymentRadio.checked = true;
      else if (selected && selected.disabled && standardPaymentRadio) standardPaymentRadio.checked = true;
      paymentCards.forEach(card => card.classList.toggle("active", !!card.querySelector("input:checked")));
    }

    function updateDepositUI(radio) {
      if (!radio) {
        radio = document.querySelector('input[name="paymentMethod"]:checked') || { value: 'cod' };
      }
      const depositAlert = document.getElementById('customDepositAlert');
      const depositRow = document.getElementById('depositRow');
      const remainingRow = document.getElementById('remainingRow');
      const btnConfirmText = document.getElementById('btnConfirmText');

      if (["paid", "deposit_paid"].includes(customOrder?.paymentStatus)) {
        const isDepositPaid = customOrder.paymentStatus === "deposit_paid";
        depositAlert.hidden = false;
        depositAlert.innerHTML = isDepositPaid
            ? '<i class="fa-solid fa-circle-check"></i> Đơn này đã cọc 50%. Phần còn lại sẽ thanh toán khi nhận hàng.'
            : '<i class="fa-solid fa-circle-check"></i> Đơn này đã được thanh toán đầy đủ.';
        depositRow.hidden = !isDepositPaid;
        remainingRow.hidden = !isDepositPaid;
        document.getElementById('summaryDeposit').textContent = Number(customOrder.paidAmount || 0).toLocaleString('vi-VN') + 'đ';
        document.getElementById('summaryRemaining').textContent = Number(customOrder.remainingAmount || 0).toLocaleString('vi-VN') + 'đ';
        const submitButton = document.getElementById('btnConfirmOrder');
        submitButton.disabled = true;
        submitButton.querySelector('#btnConfirmText').textContent = 'Đã thanh toán cọc 50%';
        return;
      }

      let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
      if (customOrder) totalStr = (customAmountDue + (customOrder.paymentStatus === "adjustment_due" ? 0 : 25300)).toString();
      let totalVal = parseInt(totalStr, 10) || 0;
      let depositVal = customOrder?.paymentStatus === "adjustment_due" ? customAmountDue : Math.round(totalVal / 2);
      let remainingVal = totalVal - depositVal;

      if (customOrder?.paymentStatus === "adjustment_due") {
        if (depositAlert) {
          depositAlert.hidden = false;
          depositAlert.innerHTML = `<i class="fa-solid fa-circle-info"></i> Vui lòng thanh toán toàn bộ khoản chênh lệch <strong>${customAmountDue.toLocaleString('vi-VN')}đ</strong> qua VNPay.`;
        }
        if (depositRow) depositRow.hidden = true;
        if (remainingRow) remainingRow.hidden = true;
        if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán khoản chênh lệch';
        return;
      }

      if (!hasCustomItem) {
        // Sản phẩm CÓ SẴN: COD được thanh toán luôn khi nhận hàng, không cọc!
        if (depositAlert) depositAlert.hidden = true;
        if (depositRow) depositRow.hidden = true;
        if (remainingRow) remainingRow.hidden = true;
        if (btnConfirmText) {
          btnConfirmText.textContent = radio.value === 'vnpay' ? 'Chuyển khoản qua VNPay' : 'Xác nhận đặt hàng';
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
        // Đơn thiết kế cọc 50% bằng chuyển khoản, phần còn lại trả khi nhận hàng.
        if (depositRow) depositRow.hidden = false;
        if (remainingRow) remainingRow.hidden = false;
        const depositEl = document.getElementById('summaryDeposit');
        const remainingEl = document.getElementById('summaryRemaining');
        if (depositEl) depositEl.textContent = depositVal.toLocaleString('vi-VN') + 'đ';
        if (remainingEl) remainingEl.textContent = remainingVal.toLocaleString('vi-VN') + 'đ';
        if (btnConfirmText) btnConfirmText.textContent = 'Thanh toán cọc 50% qua VNPay';
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
    syncPaymentOptions();
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
      const ward = document.getElementById("ward");
      const wardFallback = document.getElementById("wardFallback");
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

      const wardControl = ward && !ward.hidden ? ward : wardFallback;
      if (wardControl && !(ward.hidden ? wardFallback?.value.trim() : ward.value)) {
        wardControl.closest(".form-group").classList.add("invalid");
        isValid = false;
      } else if (wardControl) {
        wardControl.closest(".form-group").classList.remove("invalid");
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
              address: completeDeliveryAddress(),
              note: note ? note.value.trim() : "",
              paymentMethod: document.querySelector('input[name="paymentMethod"]:checked')?.value || "cod",
              voucherCode: document.getElementById("voucherCode")?.value.trim() || "",
              useCoins: document.getElementById("coinDiscountRow")?.hidden === false ? "true" : "false"
            })
          });
          const result = await response.json();
          if (!response.ok) throw new Error(result.message || "Không thể tạo đơn hàng.");

          const chosenMethod = document.querySelector('input[name="paymentMethod"]:checked')?.value;
          const isDepositPayment = chosenMethod === "vnpay_deposit";
          const isOnlinePayment = String(result.paymentMethod || "").toLowerCase().startsWith("vnpay")
              || chosenMethod === "vnpay" || isDepositPayment;
          const isCustomOrder = Boolean(result.hasCustomItem || hasCustomItem);
          const total = Number(result.total || 0);
          const subtotal = Number(result.subtotal || 0);
          const shipping = Number(result.shipping || 0);
          const depositAmount = Math.round(total / 2);
          const remainingAmount = total - depositAmount;
// VNPay: chuyển sang cổng VNPay thật (sandbox), không mở modal giả lập nữa
          console.log("VNPAY check:", isOnlinePayment, result);
          if (isOnlinePayment) {
            sessionStorage.setItem("auracraft_latest_order", JSON.stringify({
              orderId: "#AC-" + result.orderId,
              orderIds: result.orderIds,
              fullName: fullName.value.trim(), phone: phone.value.trim(),
              address: completeDeliveryAddress(),
              note: note ? note.value.trim() : "",
              email: document.getElementById("email")?.value || "",
              paymentMethod: isDepositPayment ? "VNPay (Đã cọc 50% đơn thiết kế)" : "VNPay (Đã thanh toán trực tuyến 100%)",
              shippingFee: shipping.toLocaleString("vi-VN") + "đ",
              subtotal: subtotal.toLocaleString("vi-VN") + "đ",
              total: total.toLocaleString("vi-VN") + "đ",
              paid: (isDepositPayment ? depositAmount : total).toLocaleString("vi-VN") + "đ",
              remaining: isDepositPayment ? remainingAmount.toLocaleString("vi-VN") + "đ" : "0đ",
              createdAt: new Date().toLocaleString("vi-VN"),
              rewardCoins: Number(result.rewardCoinsPending || 0)
            }));
            const bought = new Set(productIds.map(String));
            try {
              const localCart = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]");
              localStorage.setItem("AuraCraftCart", JSON.stringify(localCart.filter(i => !bought.has(String(i.id)))));
            } catch (_) { }
            localStorage.removeItem("AuraCraftCheckoutCart");
            localStorage.removeItem("AuraCraftCheckoutSource");
            sessionStorage.removeItem("AuraCraftCartSelected");

            const ids = [].concat(result.orderIds || result.orderId).join(",");
            window.location.href = checkoutForm.dataset.context + "/payment/vnpay"
                + "?paymentReference=" + encodeURIComponent(result.paymentReference || "")
                + "&orderIds=" + encodeURIComponent(ids);
            return;
          }
          // Quy tắc thanh toán:
          // 1. VNPay: luôn yêu cầu thanh toán trực tuyến 100% mô phỏng.
          // 2. COD sản phẩm Custom: yêu cầu thanh toán cọc 50% mô phỏng.
          // 3. COD sản phẩm có sẵn: ĐƯỢC THANH TOÁN LUÔN khi nhận hàng, KHÔNG mở modal thanh toán cọc!
          const needsImmediatePayment = !isOnlinePayment && isCustomOrder;
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
              window.location.href = checkoutForm.dataset.context + "/pages/cart.jsp";
              return;
            }
          }

          let paymentMethodDisplay = "Thanh toán khi nhận hàng (COD)";
          let paidDisplay = undefined;
          let remainingDisplay = undefined;

          if (isOnlinePayment) {
            paymentMethodDisplay = isDepositPayment ? "VNPay (Đã cọc 50% đơn thiết kế)" : "VNPay (Đã thanh toán trực tuyến 100%)";
            paidDisplay = (isDepositPayment ? depositAmount : total).toLocaleString("vi-VN") + "đ";
            remainingDisplay = isDepositPayment ? remainingAmount.toLocaleString("vi-VN") + "đ" : "0đ";
          } else if (isCustomOrder) {
            paymentMethodDisplay = "Đã cọc 50% qua chuyển khoản VNPay";
            paidDisplay = depositAmount.toLocaleString("vi-VN") + "đ";
            remainingDisplay = remainingAmount.toLocaleString("vi-VN") + "đ";
          }

          sessionStorage.setItem("auracraft_latest_order", JSON.stringify({
            orderId: "#AC-" + result.orderId,
            orderIds: result.orderIds,
            fullName: fullName.value.trim(), phone: phone.value.trim(),
            address: completeDeliveryAddress(), note: note ? note.value.trim() : "",
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
          window.location.href = checkoutForm.dataset.context + "/pages/success.jsp?orderId=" + encodeURIComponent("#AC-" + result.orderId);
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
      const hasCustomItemSubmit = hasCustomItem;

      let totalStr = document.getElementById('summaryTotal') ? document.getElementById('summaryTotal').textContent.replace(/\D/g, '') : "415300";
      if (customOrder) totalStr = (customAmountDue + (customOrder.paymentStatus === "adjustment_due" ? 0 : 25300)).toString();
      const totalNumber = parseInt(totalStr, 10) || 415300;
      const depositToPay = customOrder?.paymentStatus === "adjustment_due" ? customAmountDue : Math.round(totalNumber / 2);
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
              address: completeDeliveryAddress(),
              note: note ? note.value : ""
            });
            const isAdjustmentPayment = customOrder.paymentStatus === "adjustment_due";
            const isCashOnDelivery = !isVNPay && !hasCustomItemSubmit && !isAdjustmentPayment;
            const result = window.AuraCraftCustom.recordPayment(customOrder.id, isCashOnDelivery ? "pending" : "success", {
              method: isVNPay ? "vnpay" : isCashOnDelivery ? "cod" : "vnpay_deposit",
              amount: isAdjustmentPayment
                  ? customAmountDue
                  : (isVNPay ? customAmountDue + 25300 : isCashOnDelivery ? 0 : depositToPay)
            });

            let paymentMethodText = isVNPay
                ? "VNPay (Đã thanh toán trực tuyến 100%)"
                : isCashOnDelivery ? "Thanh toán khi nhận hàng (COD)"
                    : isAdjustmentPayment ? "Đã thanh toán khoản chênh lệch qua VNPay" : "Đã cọc 50% qua chuyển khoản VNPay";

            const customOrderTotal = customAmountDue + (isAdjustmentPayment ? 0 : 25300);
            const customOrderData = {
              orderId: result.order.id,
              fullName: fullName.value.trim(),
              phone: phone.value.trim(),
              address: completeDeliveryAddress(),
              email: document.getElementById("email")?.value || "",
              note: note ? note.value.trim() : "",
              paymentMethod: paymentMethodText,
              shippingFee: isAdjustmentPayment ? "0đ" : "25.300đ",
              subtotal: `${customAmountDue.toLocaleString("vi-VN")}đ`,
              total: `${customOrderTotal.toLocaleString("vi-VN")}đ`,
              paid: isVNPay ? `${customOrderTotal.toLocaleString("vi-VN")}đ` : isCashOnDelivery ? "0đ" : `${depositToPay.toLocaleString("vi-VN")}đ`,
              remaining: isVNPay || isAdjustmentPayment ? "0đ" : isCashOnDelivery ? `${customOrderTotal.toLocaleString("vi-VN")}đ` : `${remainingToPay.toLocaleString("vi-VN")}đ`,
              createdAt: new Date().toLocaleString("vi-VN"),
              sellerName: result.order.sellerName,
              paymentStatus: result.order.paymentStatus
            };
            sessionStorage.setItem("auracraft_latest_order", JSON.stringify(customOrderData));
            window.location.href = `${checkoutForm.dataset.context || ""}/pages/success.jsp?orderId=${encodeURIComponent(result.order.id)}`;
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
          paymentMethodText = "Đã cọc 50% qua chuyển khoản VNPay";
          paidVal = depositToPay.toLocaleString("vi-VN") + "đ";
          remainingVal = remainingToPay.toLocaleString("vi-VN") + "đ";
        }

        const orderData = {
          orderId: virtualOrderId,
          fullName: fullName.value.trim(),
          phone: phone.value.trim(),
          address: completeDeliveryAddress(),
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
          window.location.href = `${checkoutForm.dataset.context || ""}/pages/success.jsp?orderId=${encodeURIComponent(virtualOrderId)}`;
        }, 400);
      }

      const btnConfirm = document.getElementById("btnConfirmOrder");
      if (btnConfirm) {
        btnConfirm.disabled = true;
        btnConfirm.innerHTML = isVNPay
            ? '<i class="fa-solid fa-spinner fa-spin"></i> Đang kết nối VNPay...'
            : '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
      }

      const needsImmediatePaymentBrowser = !isVNPay && hasCustomItemSubmit;
      const modal = document.getElementById("qrPaymentModal");

      if (needsImmediatePaymentBrowser && modal) {
        const isAdjustmentPayment = customOrder?.paymentStatus === "adjustment_due";
        document.getElementById("mockPaymentTitle").textContent = isVNPay
            ? "Mô phỏng thanh toán VNPay (100%)"
            : isAdjustmentPayment ? "Thanh toán khoản chênh lệch" : "Mô phỏng thanh toán cọc 50% qua chuyển khoản";
        document.getElementById("mockPaymentDescription").innerHTML = isVNPay
            ? `Xác nhận thanh toán mô phỏng <strong>${totalNumber.toLocaleString("vi-VN")}đ</strong> qua VNPay (100%). Không có giao dịch ngân hàng thật.`
            : isAdjustmentPayment
                ? `Thanh toán khoản chênh lệch <strong>${customAmountDue.toLocaleString("vi-VN")}đ</strong> qua VNPay.`
                : `Đơn thiết kế yêu cầu chuyển khoản cọc 50% (<strong>${depositToPay.toLocaleString("vi-VN")}đ</strong>) để shop bắt đầu chế tác. Phần còn lại <strong>${remainingToPay.toLocaleString("vi-VN")}đ</strong> sẽ thanh toán khi nhận hàng.`;

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
        // Nếu chọn VNPay hoặc thanh toán thông thường, gọi hàm hoàn tất thanh toán / chuyển hướng servlet luôn
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
            const isCodOrder = orderData.paymentMethod === "Thanh toán khi nhận hàng (COD)";
            totalParent.insertAdjacentHTML('afterend', isCodOrder ? `
                      <div class="detail-line">
                          <span class="detail-label">Cần thanh toán khi nhận hàng (COD):</span>
                          <span class="detail-val" id="detailRemaining" style="color:var(--primary-brown); font-weight:700;">${orderData.remaining}</span>
                      </div>
                  ` : `
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