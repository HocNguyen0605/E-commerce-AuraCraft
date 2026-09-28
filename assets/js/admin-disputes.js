document.addEventListener("DOMContentLoaded", () => {
  const intake = document.getElementById("disputeIntake");
  const form = document.getElementById("disputeForm");
  const orderSelect = document.getElementById("disputeOrder");
  const disputeList = document.getElementById("disputeList");
  const violationBody = document.getElementById("violationTableBody");
  const search = document.getElementById("disputeSearch");
  const statusFilter = document.getElementById("disputeStatusFilter");
  const feedback = document.getElementById("disputeFeedback");
  const empty = document.getElementById("disputeEmpty");
  const statusLabels = {
    open: "Chờ tiếp nhận",
    investigating: "Đang điều tra",
    awaiting_information: "Chờ bổ sung thông tin",
    resolved_buyer: "Đã xử lý cho buyer",
    resolved_seller: "Đã xử lý cho thợ",
    closed: "Đã đóng"
  };

  function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Không rõ" : date.toLocaleString("vi-VN");
  }

  function makeButton(text, handler, variant) {
    const button = document.createElement("button");
    button.type = "button";
    button.className = `btn ${variant || "btn-outline"}`;
    button.textContent = text;
    button.addEventListener("click", handler);
    return button;
  }

  function updateStats() {
    const disputes = window.AuraCraftCustom.listDisputes();
    const open = disputes.filter((dispute) => dispute.status === "open").length;
    const investigating = disputes.filter((dispute) => ["investigating", "awaiting_information"].includes(dispute.status)).length;
    const activeViolations = window.AuraCraftCustom.listViolations().filter((violation) => !violation.clearedAt).length;
    const refundDue = window.AuraCraftCustom.listOrders().filter((order) => order.paymentStatus === "refund_due").length;
    document.getElementById("openDisputeCount").textContent = open;
    document.getElementById("investigatingCount").textContent = investigating;
    document.getElementById("activeViolationCount").textContent = activeViolations;
    document.getElementById("refundDueCount").textContent = refundDue;
    document.getElementById("violationSummary").textContent = `${window.AuraCraftCustom.listViolations().length} hồ sơ`;
  }

  function renderOrderOptions() {
    const orders = window.AuraCraftCustom.listOrders();
    const selectedId = new URLSearchParams(window.location.search).get("orderId");
    orderSelect.replaceChildren();
    orders.forEach((order) => {
      const option = document.createElement("option");
      option.value = order.id;
      option.textContent = `${order.id} · ${order.productType} · ${order.buyerEmail}`;
      option.selected = order.id === selectedId;
      orderSelect.append(option);
    });
    if (!orders.length) {
      const option = document.createElement("option");
      option.value = "";
      option.textContent = "Chưa có đơn hàng Custom";
      orderSelect.append(option);
    }
  }

  function resolve(dispute, outcome) {
    const note = prompt(outcome === "buyer" ? "Kết luận và căn cứ xử lý cho buyer:" : "Kết luận và căn cứ xử lý cho thợ:");
    if (!note) return;
    try {
      const result = window.AuraCraftCustom.resolveDispute(dispute.id, outcome, note);
      feedback.textContent = result.refund
        ? "Đã kết luận khiếu nại; đơn bị hủy và khoản tiền được đưa vào hàng chờ hoàn."
        : "Đã lưu kết luận xử lý khiếu nại.";
      render();
    } catch (error) {
      feedback.textContent = error.message;
    }
  }

  function renderDisputes() {
    const keyword = search.value.trim().toLowerCase();
    const disputes = window.AuraCraftCustom.listDisputes().filter((dispute) => {
      const text = `${dispute.id} ${dispute.orderId} ${dispute.buyerEmail} ${dispute.sellerEmail} ${dispute.subject}`.toLowerCase();
      return (statusFilter.value === "all" || dispute.status === statusFilter.value) && text.includes(keyword);
    });
    disputeList.replaceChildren();
    disputes.forEach((dispute) => {
      const order = window.AuraCraftCustom.getOrder(dispute.orderId);
      const card = document.createElement("article");
      const header = document.createElement("header");
      const title = document.createElement("h3");
      const status = document.createElement("span");
      const details = document.createElement("div");
      const description = document.createElement("p");
      const parties = document.createElement("p");
      const orderSummary = document.createElement("p");
      const adminNote = document.createElement("textarea");
      const controls = document.createElement("div");
      const closed = ["resolved_buyer", "resolved_seller", "closed"].includes(dispute.status);
      card.className = "dispute-card";
      header.className = "dispute-card-header";
      title.textContent = `${dispute.id} · ${dispute.subject}`;
      status.className = "dispute-status";
      status.dataset.status = dispute.status;
      status.textContent = statusLabels[dispute.status] || dispute.status;
      header.append(title, status);
      details.className = "dispute-card-details";
      description.textContent = dispute.description;
      parties.textContent = `Buyer: ${dispute.buyerEmail} · Thợ: ${dispute.sellerEmail}`;
      orderSummary.textContent = `Đơn ${dispute.orderId} · ${order?.productType || ""} · ${order ? Number(order.price).toLocaleString("vi-VN") : 0} đ · ${order?.status || "Không rõ trạng thái"}`;
      details.append(description, parties, orderSummary);
      adminNote.className = "dispute-admin-note";
      adminNote.rows = 2;
      adminNote.placeholder = "Ghi chú xử lý / thông tin yêu cầu bổ sung";
      adminNote.value = dispute.adminNote || "";
      adminNote.disabled = closed;
      controls.className = "dispute-actions";

      if (!closed) {
        const nextStatus = document.createElement("select");
        nextStatus.className = "dispute-next-status";
        ["open", "investigating", "awaiting_information"].forEach((value) => {
          const option = document.createElement("option");
          option.value = value;
          option.textContent = statusLabels[value];
          option.selected = dispute.status === value;
          nextStatus.append(option);
        });
        controls.append(nextStatus);
        controls.append(makeButton("Lưu cập nhật", () => {
          try {
            window.AuraCraftCustom.updateDispute(dispute.id, nextStatus.value, adminNote.value);
            feedback.textContent = `Đã cập nhật hồ sơ ${dispute.id}.`;
            render();
          } catch (error) { feedback.textContent = error.message; }
        }, "btn-outline"));
        controls.append(makeButton("Kết luận có lợi buyer", () => resolve(dispute, "buyer"), "btn-primary"));
        controls.append(makeButton("Kết luận có lợi thợ", () => resolve(dispute, "seller"), "btn-outline"));
      } else {
        const resolution = document.createElement("p");
        resolution.className = "dispute-resolution";
        resolution.textContent = `Kết luận: ${dispute.resolution || dispute.adminNote || "Đã đóng"}`;
        details.append(resolution);
      }

      if (order?.paymentStatus === "refund_due") {
        controls.append(makeButton("Ghi nhận đã hoàn tiền", () => {
          const reference = prompt("Mã tham chiếu hoàn tiền (nếu có):", "");
          if (reference === null || !confirm("Xác nhận cổng thanh toán đã hoàn tiền thành công?")) return;
          try {
            window.AuraCraftCustom.processRefund(order.id, reference);
            feedback.textContent = `Đã ghi nhận hoàn tiền cho đơn ${order.id}.`;
            render();
          } catch (error) { feedback.textContent = error.message; }
        }, "btn-primary"));
      }

      const created = document.createElement("small");
      created.className = "dispute-created-at";
      created.textContent = `Tiếp nhận: ${formatDate(dispute.createdAt)}`;
      card.append(header, details, adminNote, controls, created);
      disputeList.append(card);
    });
    empty.hidden = disputes.length > 0;
  }

  function renderViolations() {
    const violations = window.AuraCraftCustom.listViolations().slice().reverse();
    violationBody.replaceChildren();
    violations.forEach((violation) => {
      const row = document.createElement("tr");
      const order = window.AuraCraftCustom.getOrder(violation.orderId);
      [violation.orderId, violation.sellerEmail || order?.sellerEmail || "—", violation.type === "late_delivery" ? "Trễ deadline" : violation.type, formatDate(violation.createdAt), violation.clearedAt ? "Đã xử lý" : "Còn hiệu lực"].forEach((value) => {
        const cell = document.createElement("td");
        cell.textContent = value;
        row.append(cell);
      });
      const actionCell = document.createElement("td");
      if (!violation.clearedAt) {
        actionCell.append(makeButton("Ghi nhận xử lý", () => {
          const note = prompt("Ghi chú xử lý vi phạm:");
          if (!note) return;
          try {
            window.AuraCraftCustom.clearViolation(violation.id, note);
            feedback.textContent = "Đã cập nhật hồ sơ vi phạm.";
            render();
          } catch (error) { feedback.textContent = error.message; }
        }));
      } else {
        actionCell.textContent = violation.resolutionNote || "Đã xử lý";
      }
      row.append(actionCell);
      violationBody.append(row);
    });
  }

  function render() {
    updateStats();
    renderOrderOptions();
    renderDisputes();
    renderViolations();
  }

  function toggleIntake(show) {
    intake.hidden = !show;
    if (show) renderOrderOptions();
  }

  document.getElementById("openDisputeForm").addEventListener("click", () => toggleIntake(true));
  document.getElementById("closeDisputeForm").addEventListener("click", () => toggleIntake(false));
  document.getElementById("cancelDisputeForm").addEventListener("click", () => toggleIntake(false));
  form.addEventListener("submit", (event) => {
    event.preventDefault();
    try {
      const dispute = window.AuraCraftCustom.createDispute({
        orderId: orderSelect.value,
        buyerEmail: document.getElementById("disputeBuyerEmail").value,
        subject: document.getElementById("disputeSubject").value,
        description: document.getElementById("disputeDescription").value
      });
      form.reset();
      toggleIntake(false);
      feedback.textContent = `Đã tiếp nhận hồ sơ ${dispute.id}.`;
      render();
    } catch (error) { feedback.textContent = error.message; }
  });

  fetch("../components/admin-sidebar.html")
    .then((response) => {
      if (!response.ok) throw new Error("Không tải được menu admin.");
      return response.text();
    })
    .then((html) => {
      document.getElementById("admin-sidebar-placeholder").outerHTML = html
        .replace('href="admin-orders.html" class="menu-item active"', 'href="admin-orders.html" class="menu-item"')
        .replace('href="admin-disputes.html" class="menu-item"', 'href="admin-disputes.html" class="menu-item active"');
    })
    .catch((error) => { feedback.textContent = error.message; });

  document.getElementById("disputeSearch").addEventListener("input", renderDisputes);
  statusFilter.addEventListener("change", renderDisputes);
  window.addEventListener("storage", render);
  render();
});