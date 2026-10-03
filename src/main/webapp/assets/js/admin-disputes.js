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
    awaiting_information: "Chờ bổ sung",
    resolved_buyer: "Đã xử lý (Hoàn tiền)",
    resolved_seller: "Đã xử lý (Có lợi Thợ)",
    closed: "Đã đóng"
  };

  const statusBadgeClasses = {
    open: "pending",
    investigating: "crafting",
    awaiting_information: "late",
    resolved_buyer: "done",
    resolved_seller: "done",
    closed: "done"
  };

  function formatDate(value) {
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Không rõ" : date.toLocaleString("vi-VN");
  }

  function getStatusBadge(status) {
    const badgeClass = statusBadgeClasses[status] || "pending";
    const label = statusLabels[status] || status;
    return `<span class="badge ${badgeClass}">${label}</span>`;
  }

  function updateStats() {
    const disputes = window.AuraCraftCustom ? window.AuraCraftCustom.listDisputes() : [];
    const open = disputes.filter((d) => d.status === "open").length;
    const investigating = disputes.filter((d) => ["investigating", "awaiting_information"].includes(d.status)).length;
    const activeViolations = window.AuraCraftCustom ? window.AuraCraftCustom.listViolations().filter((v) => !v.clearedAt).length : 0;
    const refundDue = window.AuraCraftCustom ? window.AuraCraftCustom.listOrders().filter((o) => o.paymentStatus === "refund_due").length : 0;
    
    document.getElementById("openDisputeCount").textContent = open;
    document.getElementById("investigatingCount").textContent = investigating;
    document.getElementById("activeViolationCount").textContent = activeViolations;
    document.getElementById("refundDueCount").textContent = refundDue;
    
    const violationSummary = document.getElementById("violationSummary");
    if (violationSummary && window.AuraCraftCustom) {
      violationSummary.textContent = `${window.AuraCraftCustom.listViolations().length} hồ sơ`;
    }
  }

  function renderOrderOptions() {
    if (!window.AuraCraftCustom) return;
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
    const promptText = outcome === "buyer" 
      ? "Nhập kết luận và căn cứ giải quyết có lợi cho Người mua (hoàn tiền):"
      : "Nhập kết luận và căn cứ giải quyết có lợi cho Thợ:";
    const note = prompt(promptText);
    if (!note) return;

    try {
      const result = window.AuraCraftCustom.resolveDispute(dispute.id, outcome, note);
      feedback.textContent = result.refund
        ? `Đã kết luận hồ sơ ${dispute.id}: Đơn bị hủy và đã chuyển sang hàng chờ hoàn tiền.`
        : `Đã kết luận hồ sơ ${dispute.id} thành công.`;
      render();
    } catch (error) {
      feedback.textContent = error.message;
    }
  }

  function renderDisputes() {
    if (!window.AuraCraftCustom) return;
    const keyword = search.value.trim().toLowerCase();
    const disputes = window.AuraCraftCustom.listDisputes().filter((dispute) => {
      const text = `${dispute.id} ${dispute.orderId} ${dispute.buyerEmail} ${dispute.sellerEmail} ${dispute.subject}`.toLowerCase();
      return (statusFilter.value === "all" || dispute.status === statusFilter.value) && text.includes(keyword);
    });

    disputeList.replaceChildren();

    disputes.forEach((dispute) => {
      const order = window.AuraCraftCustom.getOrder(dispute.orderId);
      const closed = ["resolved_buyer", "resolved_seller", "closed"].includes(dispute.status);

      const card = document.createElement("article");
      card.className = `dispute-card status-${dispute.status}`;

      // Card Header
      const header = document.createElement("div");
      header.className = "dispute-card-header";
      header.innerHTML = `
        <h3>
          <span class="dispute-id">${dispute.id}</span>
          <span style="color: #999;">·</span>
          <span>${dispute.subject}</span>
        </h3>
        <div>${getStatusBadge(dispute.status)}</div>
      `;

      // Details Box
      const details = document.createElement("div");
      details.className = "dispute-card-details";
      
      const descBox = document.createElement("div");
      descBox.className = "dispute-desc-box";
      descBox.innerHTML = `<strong>Nội dung:</strong> ${dispute.description}`;
      
      const metaChips = document.createElement("div");
      metaChips.className = "dispute-meta-chips";
      metaChips.innerHTML = `
        <span><i class="fa-solid fa-user"></i> Người mua: <strong>${dispute.buyerEmail}</strong></span>
        <span><i class="fa-solid fa-hammer"></i> Thợ: <strong>${dispute.sellerEmail}</strong></span>
        <span><i class="fa-solid fa-receipt"></i> Đơn: <strong>${dispute.orderId}</strong> (${order?.productType || "Custom"} · ${order ? Number(order.price).toLocaleString("vi-VN") : 0} đ)</span>
        <span><i class="fa-solid fa-circle-info"></i> Tiến độ đơn: <strong>${order?.status || "N/A"}</strong></span>
      `;
      
      details.append(descBox, metaChips);

      if (closed) {
        const resolution = document.createElement("div");
        resolution.className = "dispute-resolution";
        resolution.innerHTML = `<i class="fa-solid fa-circle-check"></i> <strong>Kết luận:</strong> ${dispute.resolution || dispute.adminNote || "Đã đóng hồ sơ"}`;
        details.append(resolution);
      }

      // Admin Note Area
      const noteWrapper = document.createElement("div");
      noteWrapper.className = "dispute-admin-note-wrapper";
      noteWrapper.innerHTML = `<label><i class="fa-solid fa-pen-to-square"></i> Ghi chú & Đánh giá của Admin:</label>`;
      
      const adminNote = document.createElement("textarea");
      adminNote.className = "dispute-admin-note";
      adminNote.rows = 2;
      adminNote.placeholder = "Ghi chú tiến trình xử lý, yêu cầu thợ/buyer cung cấp thêm bằng chứng...";
      adminNote.value = dispute.adminNote || "";
      adminNote.disabled = closed;
      noteWrapper.append(adminNote);

      // Actions
      const actions = document.createElement("div");
      actions.className = "dispute-actions";

      const leftActions = document.createElement("div");
      leftActions.className = "dispute-action-group";

      if (!closed) {
        const nextStatus = document.createElement("select");
        nextStatus.className = "dispute-next-status filter-select";
        ["open", "investigating", "awaiting_information"].forEach((value) => {
          const option = document.createElement("option");
          option.value = value;
          option.textContent = statusLabels[value];
          option.selected = dispute.status === value;
          nextStatus.append(option);
        });

        const saveBtn = document.createElement("button");
        saveBtn.type = "button";
        saveBtn.className = "btn btn-outline";
        saveBtn.innerHTML = `<i class="fa-solid fa-floppy-disk"></i> Lưu cập nhật`;
        saveBtn.addEventListener("click", () => {
          try {
            window.AuraCraftCustom.updateDispute(dispute.id, nextStatus.value, adminNote.value);
            feedback.textContent = `Đã cập nhật trạng thái hồ sơ ${dispute.id}.`;
            render();
          } catch (error) {
            feedback.textContent = error.message;
          }
        });

        const buyerWinBtn = document.createElement("button");
        buyerWinBtn.type = "button";
        buyerWinBtn.className = "btn btn-primary";
        buyerWinBtn.innerHTML = `<i class="fa-solid fa-rotate-left"></i> Hoàn tiền Buyer`;
        buyerWinBtn.addEventListener("click", () => resolve(dispute, "buyer"));

        const sellerWinBtn = document.createElement("button");
        sellerWinBtn.type = "button";
        sellerWinBtn.className = "btn btn-outline";
        sellerWinBtn.innerHTML = `<i class="fa-solid fa-gavel"></i> Có lợi Thợ`;
        sellerWinBtn.addEventListener("click", () => resolve(dispute, "seller"));

        leftActions.append(nextStatus, saveBtn, buyerWinBtn, sellerWinBtn);
      }

      if (order?.paymentStatus === "refund_due") {
        const refundBtn = document.createElement("button");
        refundBtn.type = "button";
        refundBtn.className = "btn btn-primary";
        refundBtn.innerHTML = `<i class="fa-solid fa-money-bill-transfer"></i> Xác nhận đã hoàn tiền`;
        refundBtn.addEventListener("click", () => {
          const reference = prompt("Nhập mã giao dịch cổng thanh toán hoàn tiền (VNPay/MoMo):", "");
          if (reference === null || !confirm(`Xác nhận hoàn tiền cho đơn ${order.id}?`)) return;
          try {
            window.AuraCraftCustom.processRefund(order.id, reference);
            feedback.textContent = `Đã ghi nhận hoàn tiền thành công cho đơn ${order.id}.`;
            render();
          } catch (error) {
            feedback.textContent = error.message;
          }
        });
        leftActions.append(refundBtn);
      }

      const rightInfo = document.createElement("div");
      rightInfo.className = "dispute-created-at";
      rightInfo.innerHTML = `<i class="fa-regular fa-clock"></i> Tiếp nhận: ${formatDate(dispute.createdAt)}`;

      actions.append(leftActions, rightInfo);

      card.append(header, details, noteWrapper, actions);
      disputeList.append(card);
    });

    if (empty) {
      empty.hidden = disputes.length > 0;
    }
  }

  function renderViolations() {
    if (!window.AuraCraftCustom || !violationBody) return;
    const violations = window.AuraCraftCustom.listViolations().slice().reverse();
    violationBody.replaceChildren();

    violations.forEach((violation) => {
      const row = document.createElement("tr");
      const order = window.AuraCraftCustom.getOrder(violation.orderId);

      const typeLabel = violation.type === "late_delivery" 
        ? '<span class="badge late"><i class="fa-solid fa-clock-rotate-left"></i> Trễ deadline</span>'
        : `<span class="badge pending">${violation.type}</span>`;

      const statusBadge = violation.clearedAt
        ? '<span class="badge done"><i class="fa-solid fa-check"></i> Đã xử lý</span>'
        : '<span class="badge late"><i class="fa-solid fa-triangle-exclamation"></i> Còn hiệu lực</span>';

      row.innerHTML = `
        <td><strong>${violation.orderId}</strong></td>
        <td>${violation.sellerEmail || order?.sellerEmail || "—"}</td>
        <td>${typeLabel}</td>
        <td>${formatDate(violation.createdAt)}</td>
        <td>${statusBadge}</td>
        <td class="violation-action-cell"></td>
      `;

      const actionCell = row.querySelector(".violation-action-cell");
      if (!violation.clearedAt) {
        const clearBtn = document.createElement("button");
        clearBtn.type = "button";
        clearBtn.className = "btn btn-outline";
        clearBtn.style.padding = "6px 12px";
        clearBtn.style.fontSize = "12px";
        clearBtn.innerHTML = `<i class="fa-solid fa-check"></i> Ghi nhận xử lý`;
        clearBtn.addEventListener("click", () => {
          const note = prompt("Nhập ghi chú xử lý vi phạm của Thợ:");
          if (!note) return;
          try {
            window.AuraCraftCustom.clearViolation(violation.id, note);
            feedback.textContent = "Đã cập nhật hồ sơ vi phạm.";
            render();
          } catch (error) {
            feedback.textContent = error.message;
          }
        });
        actionCell.append(clearBtn);
      } else {
        actionCell.innerHTML = `<span style="font-size: 13px; color: var(--text-muted);">${violation.resolutionNote || "Đã xử lý"}</span>`;
      }

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
    if (intake) {
      intake.hidden = !show;
      if (show) renderOrderOptions();
    }
  }

  const openBtn = document.getElementById("openDisputeForm");
  const closeBtn = document.getElementById("closeDisputeForm");
  const cancelBtn = document.getElementById("cancelDisputeForm");

  if (openBtn) openBtn.addEventListener("click", () => toggleIntake(true));
  if (closeBtn) closeBtn.addEventListener("click", () => toggleIntake(false));
  if (cancelBtn) cancelBtn.addEventListener("click", () => toggleIntake(false));

  if (form) {
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
        feedback.textContent = `Đã tiếp nhận hồ sơ ${dispute.id} thành công.`;
        render();
      } catch (error) {
        feedback.textContent = error.message;
      }
    });
  }

  if (search) search.addEventListener("input", renderDisputes);
  if (statusFilter) statusFilter.addEventListener("change", renderDisputes);
  window.addEventListener("storage", render);

  render();
});