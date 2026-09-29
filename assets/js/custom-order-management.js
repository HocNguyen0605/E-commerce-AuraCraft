(function () {
  const list = document.getElementById("customOrderList");
  if (!list || !window.AuraCraftCustom) return;

  const statusLabels = {
    pending_payment: "Chờ thanh toán",
    crafting: "Đang chế tác",
    cancelled: "Đã hủy"
  };
  let activeFilter = "all";

  function renderOrders() {
    const orders = window.AuraCraftCustom.listOrders();
    const visibleOrders = orders.filter((order) => activeFilter === "all"
      || (activeFilter === "pending" && order.status === "pending_payment")
      || order.status === activeFilter);
    list.replaceChildren();
    visibleOrders.forEach((order) => {
      const card = document.createElement("article");
      const header = document.createElement("div");
      const orderId = document.createElement("span");
      const badge = document.createElement("span");
      const body = document.createElement("div");
      const title = document.createElement("h3");
      const buyer = document.createElement("p");
      const deadline = document.createElement("span");
      const footer = document.createElement("div");
      const amount = document.createElement("strong");
      card.className = "order-card";
      card.dataset.status = order.status === "pending_payment" ? "pending" : order.status;
      header.className = "order-header";
      orderId.className = "order-id";
      orderId.textContent = `Mã ĐH: ${order.id}`;
      badge.className = `badge ${order.status === "crafting" ? "crafting" : "pending"}`;
      badge.textContent = statusLabels[order.status] || order.status;
      header.append(orderId, badge);
      body.className = "order-body";
      title.className = "hover-title";
      title.textContent = order.productType;
      buyer.textContent = `Khách hàng: ${order.shippingName || order.buyerName} · ${order.phone || order.buyerEmail}`;
      deadline.className = "deadline";
      deadline.textContent = order.status === "crafting"
        ? `Đã thanh toán · Dự kiến ${order.days} ngày`
        : "Chưa thanh toán, chưa thể chế tác";
      body.append(title, buyer, deadline);
      footer.className = "order-footer";
      amount.className = "total-price";
      amount.textContent = `${Number(order.price).toLocaleString("vi-VN")} đ`;
      footer.append(amount);
      if (order.status === "crafting") {
        const chatLink = document.createElement("a");
        chatLink.className = "btn btn-outline";
        chatLink.href = `chat-seller.html?requestId=${encodeURIComponent(order.requestId)}`;
        chatLink.textContent = "Chat với người mua";
        footer.append(chatLink);
      }
      card.append(header, body, footer);
      list.append(card);
    });
    if (!visibleOrders.length) {
      const empty = document.createElement("p");
      empty.textContent = "Chưa có đơn Custom trong trạng thái này.";
      list.append(empty);
    }
  }

  document.querySelectorAll(".tab-item").forEach((tab) => {
    tab.addEventListener("click", () => {
      activeFilter = tab.dataset.tab || "all";
      renderOrders();
    });
  });
  window.addEventListener("storage", renderOrders);
  renderOrders();
})();