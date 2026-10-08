document.addEventListener("DOMContentLoaded", () => {
  const page = document.querySelector(".cart-page");
  const context = page.dataset.context;
  const databaseMode = page.dataset.databaseCart === "true";
  if (new URLSearchParams(window.location.search).get("checkout") === "invalid") {
    window.alert("Giỏ hàng đã thay đổi hoặc sản phẩm không còn bán. Vui lòng kiểm tra lại sản phẩm được chọn.");
  }
  const container = document.getElementById("cartItems");
  const selectAll = document.getElementById("selectAllCart");
  let items = [];

  const money = value => Number(value || 0).toLocaleString("vi-VN") + " ₫";
  const readLocal = () => {
    try { const result = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]"); return Array.isArray(result) ? result : []; }
    catch { return []; }
  };
  const saveLocal = () => localStorage.setItem("AuraCraftCart", JSON.stringify(items));
  const selectedIds = () => {
    try { return JSON.parse(sessionStorage.getItem("AuraCraftCartSelected") || "{}"); }
    catch { return {}; }
  };
  const saveSelectedIds = () => sessionStorage.setItem("AuraCraftCartSelected", JSON.stringify(
    Object.fromEntries(items.map(item => [String(item.id), item.selected !== false]))
  ));

  async function api(action, data = {}) {
    const response = await fetch(context + "/api/cart", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded;charset=UTF-8" },
      body: new URLSearchParams({ action, ...data })
    });
    const result = await response.json();
    if (!response.ok) throw new Error(result.message || "Không thể cập nhật giỏ hàng.");
    return result;
  }

  async function loadItems() {
    if (databaseMode) {
      try {
        const response = await fetch(context + "/api/cart", { headers: { Accept: "application/json" }, cache: "no-store" });
        const result = await response.json();
        if (!response.ok) throw new Error(result.message || "Không tải được giỏ hàng.");
        const savedSelection = selectedIds();
        items = (result.items || []).map(item => ({ ...item, selected: savedSelection[String(item.id)] !== false }));
        return;
      } catch (error) {
        window.alert(error.message + " Đang hiển thị giỏ hàng trên trình duyệt.");
      }
    }
    items = readLocal().map(item => ({ ...item, selected: item.selected !== false }));
  }

  function render() {
    container.replaceChildren();
    const empty = document.getElementById("cartEmpty");
    const summary = document.getElementById("cartSummary");
    const toolbar = document.querySelector(".cart-toolbar");
    empty.hidden = items.length > 0;
    summary.hidden = items.length === 0;
    toolbar.hidden = items.length === 0;

    items.forEach(item => {
      const card = document.createElement("article"); card.className = "cart-item"; card.dataset.id = item.id;
      const header = document.createElement("div"); header.className = "cart-shop-header";
      const icon = document.createElement("i"); icon.className = "fa-solid fa-store"; icon.setAttribute("aria-hidden", "true");
      const shop = document.createElement("span"); shop.textContent = item.shopName || "AuraCraft"; header.append(icon, shop);
      const content = document.createElement("div"); content.className = "cart-item-content";
      const left = document.createElement("div"); left.className = "cart-item-left";
      const check = document.createElement("input"); check.type = "checkbox"; check.className = "item-checkbox"; check.checked = item.selected !== false; check.setAttribute("aria-label", "Chọn " + (item.name || "sản phẩm"));
      const image = document.createElement("img"); image.src = item.image || context + "/assets/img/products/bracelet_01.jpg"; image.alt = item.name || "Sản phẩm";
      image.onerror = () => { image.onerror = null; image.src = context + "/assets/img/products/bracelet_01.jpg"; };
      const info = document.createElement("div"); info.className = "item-info";
      const name = document.createElement("h3"); name.textContent = item.name || "Sản phẩm AuraCraft";
      const category = document.createElement("p"); category.className = "item-category"; category.textContent = item.categoryName || "Sản phẩm handmade";
      const price = document.createElement("p"); price.className = "item-price"; price.textContent = money(item.price);
      info.append(name, category, price); left.append(check, image, info);
      const right = document.createElement("div"); right.className = "cart-item-right";
      const quantity = document.createElement("div"); quantity.className = "item-quantity";
      const minus = document.createElement("button"); minus.type = "button"; minus.className = "qty-btn"; minus.dataset.action = "decrease"; minus.textContent = "−"; minus.setAttribute("aria-label", "Giảm số lượng");
      const count = document.createElement("span"); count.className = "qty-value"; count.textContent = item.quantity || 1;
      const plus = document.createElement("button"); plus.type = "button"; plus.className = "qty-btn"; plus.dataset.action = "increase"; plus.textContent = "+"; plus.setAttribute("aria-label", "Tăng số lượng");
      quantity.append(minus, count, plus);
      const actions = document.createElement("div"); actions.className = "item-actions";
      const detail = document.createElement("a"); detail.className = "action-btn find-similar"; detail.href = context + "/pages/product-detail?id=" + encodeURIComponent(item.id); detail.textContent = "Xem sản phẩm";
      const remove = document.createElement("button"); remove.type = "button"; remove.className = "action-btn remove-btn"; remove.dataset.action = "remove"; remove.textContent = "Xóa";
      actions.append(detail, remove); right.append(quantity, actions); content.append(left, right); card.append(header, content); container.appendChild(card);
    });
    updateSummary();
  }

  function updateSummary() {
    const selected = items.filter(item => item.selected !== false);
    document.getElementById("cartTotal").textContent = money(selected.reduce((sum, item) => sum + Number(item.price || 0) * Number(item.quantity || 1), 0));
    document.getElementById("selectedCartCount").textContent = selected.reduce((sum, item) => sum + Number(item.quantity || 1), 0) + " sản phẩm được chọn";
    selectAll.checked = items.length > 0 && selected.length === items.length;
    selectAll.indeterminate = selected.length > 0 && selected.length < items.length;
    document.getElementById("checkoutCartBtn").disabled = selected.length === 0;
  }

  container.addEventListener("change", event => {
    if (!event.target.matches(".item-checkbox")) return;
    const item = items.find(entry => String(entry.id) === event.target.closest(".cart-item").dataset.id);
    if (item) item.selected = event.target.checked;
    databaseMode ? saveSelectedIds() : saveLocal();
    updateSummary();
  });

  container.addEventListener("click", async event => {
    const button = event.target.closest("button[data-action]"); if (!button) return;
    const id = button.closest(".cart-item").dataset.id;
    const item = items.find(entry => String(entry.id) === id); if (!item) return;
    try {
      if (button.dataset.action === "remove") {
        if (databaseMode) await api("remove", { productId: id });
        items = items.filter(entry => String(entry.id) !== id);
      } else {
        const nextQuantity = button.dataset.action === "increase"
          ? Math.min(Number(item.stock || 99), Number(item.quantity || 1) + 1)
          : Math.max(1, Number(item.quantity || 1) - 1);
        if (databaseMode) await api("quantity", { productId: id, quantity: nextQuantity });
        item.quantity = nextQuantity;
      }
      databaseMode ? saveSelectedIds() : saveLocal(); render();
    } catch (error) { window.alert(error.message); }
  });

  selectAll.addEventListener("change", () => {
    items.forEach(item => item.selected = selectAll.checked);
    databaseMode ? saveSelectedIds() : saveLocal(); render();
  });

  document.getElementById("checkoutCartBtn").addEventListener("click", () => {
    const selected = items.filter(item => item.selected !== false);
    if (!selected.length) return;
    localStorage.setItem("AuraCraftCheckoutCart", JSON.stringify(selected));
    localStorage.setItem("AuraCraftCheckoutSource", databaseMode ? "database" : "browser");
    const ids = selected.map(item => Number(item.id)).filter(id => Number.isInteger(id) && id > 0);
    window.location.href = context + "/pages/checkout?fromCart=1&productIds=" + encodeURIComponent(ids.join(","));
  });

  loadItems().then(() => {
    if (!databaseMode) saveLocal(); else saveSelectedIds();
    render();
  }).catch(error => window.alert(error.message));

  fetch(context + "/components/header.html").then(r => r.text()).then(html => {
    document.getElementById("header-placeholder").innerHTML = html.replace(/href="index\.html"/g, 'href="' + context + '/index.html"');
  });
  fetch(context + "/components/footer.html").then(r => r.text()).then(html => {
    document.getElementById("footer-placeholder").innerHTML = html.replace(/href="index\.html"/g, 'href="' + context + '/index.html"');
  });
});
