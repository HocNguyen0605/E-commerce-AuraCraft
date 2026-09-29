document.addEventListener("DOMContentLoaded", () => {
  const list = document.getElementById("adminCustomOrderList");
  if (!list || !window.AuraCraftCustom) return;

  const search = document.getElementById("customOrderSearch");
  const statusFilter = document.getElementById("customOrderStatus");
  const feedback = document.getElementById("customOrderFeedback");
  const labels = {
    pending_payment: "Chờ thanh toán",
    crafting: "Đang chế tác",
    late: "Trễ hạn",
    completed: "Hoàn thành",
    handed_to_carrier: "Đã bàn giao vận chuyển",
    in_transit: "Đang vận chuyển",
    delivered: "Đã giao",
    cancelled: "Đã hủy"
  };

  function isLate(order) {
    return ["crafting", "late"].includes(order.status) && order.deadlineAt
      && new Date(order.deadlineAt).getTime() < Date.now();
  }

  function isPaymentExpired(order) {
    return order.status === "pending_payment" && order.paymentDeadlineAt
      && new Date(order.paymentDeadlineAt).getTime() < Date.now();
  }

  function dateLabel(value) {
    if (!value) return "Chưa có deadline";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Chưa có deadline" : date.toLocaleString("vi-VN");
  }

  function addActionButton(container, label, action, variant) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `btn ${variant || "btn-outline"}`;
    button.textContent = label;
    button.addEventListener("click", action);
    container.append(button);
  }

  function runAction(action) {
    try {
      action();
      feedback.textContent = "Đã cập nhật dữ liệu vận hành.";
      render();
    } catch (error) {
      feedback.textContent = error.message;
    }
  }

  function buildOrderCard(order) {
    const card = document.createElement("article");
    const header = document.createElement("div");
    const orderId = document.createElement("strong");
    const badge = document.createElement("span");
    const summary = document.createElement("div");
    const product = document.createElement("h3");
    const buyer = document.createElement("p");
    const seller = document.createElement("p");
    const payment = document.createElement("p");
    const deadline = document.createElement("p");
    const actions = document.createElement("div");
    const visibleStatus = isLate(order) ? "late" : isPaymentExpired(order) ? "payment_expired" : order.status;

    card.className = "order-card admin-custom-order-card";
    card.dataset.orderId = order.id;
    header.className = "order-header";
    orderId.className = "order-id";
    orderId.textContent = order.id;
    badge.className = `badge ${visibleStatus === "late" ? "late" : visibleStatus === "crafting" ? "crafting" : visibleStatus === "delivered" ? "done" : "pending"}`;
    badge.textContent = visibleStatus === "payment_expired" ? "Quá hạn thanh toán" : labels[visibleStatus] || visibleStatus;
    header.append(orderId, badge);

    summary.className = "admin-custom-order-summary";
    product.textContent = order.productType;
    buyer.textContent = `Người mua: ${order.shippingName || order.buyerName} · ${order.buyerEmail}`;
    seller.textContent = `Thợ: ${order.sellerName} · ${order.sellerEmail}`;
    payment.textContent = `Thanh toán: ${order.paymentStatus} · Giá trị: ${Number(order.price).toLocaleString("vi-VN")} đ`;
    deadline.textContent = order.status === "pending_payment"
      ? `Hạn thanh toán: ${dateLabel(order.paymentDeadlineAt)}`
      : `Deadline: ${dateLabel(order.deadlineAt)} · Cam kết ${order.days} ngày`;
    if (visibleStatus === "payment_expired") deadline.classList.add("admin-order-warning");
    summary.append(product, buyer, seller, payment, deadline);

    actions.className = "admin-custom-order-actions";
    if (isLate(order) && order.status !== "late") {
      addActionButton(actions, "Ghi nhận trễ & vi phạm", () => runAction(() => window.AuraCraftCustom.reviewLateOrder(order.id, "Admin ghi nhận trễ deadline.")), "btn-primary");
    } else if (["crafting", "late"].includes(order.status)) {
      addActionButton(actions, "Gia hạn deadline", () => {
        const days = prompt("Số ngày gia hạn (1-30):", "2");
        if (days === null) return;
        const reason = prompt("Lý do gia hạn:");
        if (!reason) return;
        runAction(() => window.AuraCraftCustom.extendOrderDeadline(order.id, days, reason));
      });
      addActionButton(actions, "Đánh dấu hoàn thành", () => runAction(() => window.AuraCraftCustom.advanceOrderStatus(order.id, "completed", { note: "Admin cập nhật." })), "btn-primary");
    }
    if (order.status === "completed") {
      addActionButton(actions, "Bàn giao vận chuyển", () => {
        const carrier = prompt("Đơn vị vận chuyển:");
        if (!carrier) return;
        const trackingNumber = prompt("Mã vận đơn:");
        if (!trackingNumber) return;
        runAction(() => window.AuraCraftCustom.advanceOrderStatus(order.id, "handed_to_carrier", { carrier, trackingNumber }));
      }, "btn-primary");
    }
    if (order.status === "handed_to_carrier") {
      addActionButton(actions, "Xác nhận đang vận chuyển", () => runAction(() => window.AuraCraftCustom.advanceOrderStatus(order.id, "in_transit", {})));
    }
    if (order.status === "in_transit") {
      addActionButton(actions, "Xác nhận đã giao", () => runAction(() => window.AuraCraftCustom.advanceOrderStatus(order.id, "delivered", {})), "btn-primary");
    }
    if (["pending_payment", "crafting", "late", "completed"].includes(order.status)) {
      addActionButton(actions, "Hủy đơn", () => {
        if (!confirm("Xác nhận hủy đơn? Đơn đã thanh toán sẽ chuyển sang chờ hoàn tiền.")) return;
        const reason = prompt("Lý do hủy:");
        if (!reason) return;
        runAction(() => window.AuraCraftCustom.cancelOrder(order.id, reason));
      }, "btn-outline");
    }
    if (order.paymentStatus === "refund_due") {
      addActionButton(actions, "Ghi nhận hoàn tiền", () => {
        const reference = prompt("Mã tham chiếu hoàn tiền (nếu có):", "");
        if (reference === null) return;
        if (!confirm("Xác nhận gateway đã hoàn tiền thành công?")) return;
        runAction(() => window.AuraCraftCustom.processRefund(order.id, reference));
      }, "btn-primary");
    }
    const disputeLink = document.createElement("a");
    disputeLink.className = "btn btn-outline";
    disputeLink.href = `admin-disputes.html?orderId=${encodeURIComponent(order.id)}`;
    disputeLink.textContent = "Mở hồ sơ khiếu nại";
    actions.append(disputeLink);

    const history = document.createElement("details");
    const historySummary = document.createElement("summary");
    const historyList = document.createElement("ul");
    historySummary.textContent = "Lịch sử can thiệp";
    window.AuraCraftCustom.listAdminActions(order.id).slice().reverse().forEach((entry) => {
      const item = document.createElement("li");
      item.textContent = `${dateLabel(entry.createdAt)} · ${entry.action}${entry.note ? ` · ${entry.note}` : ""}`;
      historyList.append(item);
    });
    if (!historyList.children.length) {
      const item = document.createElement("li");
      item.textContent = "Chưa có thao tác admin.";
      historyList.append(item);
    }
    history.append(historySummary, historyList);
    card.append(header, summary, actions, history);
    return card;
  }

  function render() {
    const keyword = search.value.trim().toLowerCase();
    const orders = window.AuraCraftCustom.listOrders().filter((order) => {
      const status = isLate(order) ? "late" : isPaymentExpired(order) ? "payment_expired" : order.status;
      const searchable = `${order.id} ${order.buyerName} ${order.buyerEmail} ${order.sellerName} ${order.sellerEmail} ${order.productType}`.toLowerCase();
      return (statusFilter.value === "all" || status === statusFilter.value) && searchable.includes(keyword);
    });
    list.replaceChildren();
    orders.forEach((order) => list.append(buildOrderCard(order)));
  }

  search.addEventListener("input", render);
  statusFilter.addEventListener("change", render);
  window.addEventListener("storage", render);
  render();
});