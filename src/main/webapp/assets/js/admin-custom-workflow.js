document.addEventListener("DOMContentLoaded", () => {
  const tabs = [...document.querySelectorAll(".custom-admin-tab")];
  const search = document.getElementById("customSearch");
  const statusFilter = document.getElementById("customStatusFilter");
  const tableHead = document.getElementById("customTableHead");
  const tableBody = document.getElementById("customTableBody");
  const emptyState = document.getElementById("customAdminEmpty");
  const feedback = document.getElementById("customAdminFeedback");
  let activeView = "requests";

  const labels = {
    open: "Đang mở",
    quoted: "Đã có báo giá",
    awarded: "Đã chọn thợ",
    cancelled: "Đã hủy",
    pending_payment: "Chờ thanh toán",
    crafting: "Đang chế tác",
    unpaid: "Chưa thanh toán",
    paid: "Đã thanh toán",
    adjustment_due: "Chờ thu chênh lệch",
    refund_due: "Chờ hoàn tiền",
    success: "Thành công",
    failure: "Thất bại",
    pending: "Đang chờ xử lý"
  };

  const columns = {
    requests: ["Mã yêu cầu", "Người mua", "Thiết kế", "Giá dự kiến", "Báo giá", "Trạng thái", "Ngày tạo"],
    orders: ["Mã đơn", "Thiết kế", "Người mua", "Thợ", "Tổng giá", "Đơn hàng", "Thanh toán"],
    transactions: ["Mã giao dịch", "Mã đơn", "Phương thức", "Số tiền", "Kết quả", "Thời gian"]
  };

  function formatDate(value) {
    if (!value) return "Không rõ";
    const date = new Date(value);
    return Number.isNaN(date.getTime()) ? "Không rõ" : new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium", timeStyle: "short" }).format(date);
  }

  function money(value) {
    return `${Number(value || 0).toLocaleString("vi-VN")} đ`;
  }

  function cell(row, value, className) {
    const element = document.createElement("td");
    if (className) element.className = className;
    if (value instanceof Node) element.append(value);
    else element.textContent = value == null || value === "" ? "—" : String(value);
    row.append(element);
    return element;
  }

  const statusBadgeMap = {
    open: "badge shipping",
    quoted: "badge pending",
    awarded: "badge crafting",
    cancelled: "badge rejected",
    pending_payment: "badge pending",
    crafting: "badge crafting",
    unpaid: "badge rejected",
    paid: "badge done",
    adjustment_due: "badge late",
    refund_due: "badge late",
    success: "badge done",
    failure: "badge rejected",
    pending: "badge pending"
  };

  function statusCell(row, status) {
    const badge = document.createElement("span");
    badge.className = statusBadgeMap[status] || "badge pending";
    badge.textContent = labels[status] || status;
    return cell(row, badge);
  }

  function quoteDetails(request, quotes) {
    const details = document.createElement("details");
    const summary = document.createElement("summary");
    const list = document.createElement("ul");
    details.className = "admin-quote-details";
    summary.textContent = `Xem ${quotes.length} báo giá`;
    quotes.forEach((quote) => {
      const item = document.createElement("li");
      const seller = document.createElement("strong");
      const info = document.createElement("span");
      seller.textContent = quote.sellerName;
      info.textContent = ` · ${money(quote.price)} · ${quote.days} ngày · ${labels[quote.status] || quote.status}`;
      item.append(seller, info);
      if (quote.portfolio) {
        const link = document.createElement("a");
        link.href = quote.portfolio;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        link.textContent = "Portfolio";
        item.append(document.createTextNode(" · "), link);
      }
      list.append(item);
    });
    if (!quotes.length) {
      const empty = document.createElement("li");
      empty.textContent = "Chưa nhận báo giá.";
      list.append(empty);
    }
    details.append(summary, list);
    return details;
  }

  function buildRows(state) {
    if (activeView === "requests") {
      return state.requests.map((request) => ({
        id: request.id,
        status: request.status,
        search: `${request.id} ${request.buyerName} ${request.buyerEmail} ${request.productType} ${request.materials}`,
        render(row) {
          cell(row, request.id, "custom-mono");
          cell(row, `${request.buyerName}\n${request.buyerEmail}`);
          const design = cell(row, `${request.productType}\n${request.materials}\n${request.charmDescription}`, "custom-design");
          const imageCount = (request.designImages || []).length + (request.charmImages || []).length;
          if (imageCount) {
            const imageNote = document.createElement("small");
            imageNote.textContent = `${imageCount} ảnh đính kèm`;
            design.append(document.createElement("br"), imageNote);
          }
          cell(row, money(request.budget));
          cell(row, quoteDetails(request, state.quotes.filter((quote) => quote.requestId === request.id)));
          statusCell(row, request.status);
          cell(row, formatDate(request.createdAt));
        }
      }));
    }
    if (activeView === "orders") {
      return state.orders.map((order) => ({
        id: order.id,
        status: order.paymentStatus,
        search: `${order.id} ${order.requestId} ${order.buyerName} ${order.buyerEmail} ${order.sellerName} ${order.sellerEmail} ${order.productType}`,
        render(row) {
          cell(row, order.id, "custom-mono");
          cell(row, `${order.productType}\n${order.days} ngày chế tác`, "custom-design");
          cell(row, `${order.shippingName || order.buyerName}\n${order.buyerEmail}`);
          cell(row, `${order.sellerName}\n${order.sellerEmail}`);
          const amount = order.priceDifference && order.paymentStatus === "adjustment_due"
            ? `${money(order.price)} (chênh lệch +${money(order.priceDifference)})`
            : money(order.price);
          cell(row, amount);
          statusCell(row, order.status);
          statusCell(row, order.paymentStatus);
        }
      }));
    }
    return state.transactions.map((transaction) => {
      const order = state.orders.find((item) => item.id === transaction.orderId);
      return {
        id: transaction.id,
        status: transaction.result,
        search: `${transaction.id} ${transaction.orderId} ${transaction.method} ${order?.buyerEmail || ""} ${order?.sellerEmail || ""}`,
        render(row) {
          cell(row, transaction.id, "custom-mono");
          cell(row, transaction.orderId, "custom-mono");
          cell(row, transaction.method === "refund" ? "Hoàn tiền" : transaction.method);
          cell(row, money(transaction.amount));
          statusCell(row, transaction.result);
          cell(row, formatDate(transaction.createdAt));
        }
      };
    });
  }

  function updateStats(state) {
    document.getElementById("requestCount").textContent = state.requests.length;
    document.getElementById("quoteCount").textContent = state.requests.filter((request) => request.status === "quoted").length;
    document.getElementById("orderCount").textContent = state.orders.length;
    const ordersNeedAttention = new Set(state.orders
      .filter((order) => ["unpaid", "adjustment_due", "refund_due"].includes(order.paymentStatus))
      .map((order) => order.id));
    state.transactions
      .filter((transaction) => ["failure", "pending"].includes(transaction.result))
      .forEach((transaction) => ordersNeedAttention.add(transaction.orderId));
    document.getElementById("attentionCount").textContent = ordersNeedAttention.size;
  }

  function updateStatusOptions(records) {
    const previousValue = statusFilter.value;
    const statuses = [...new Set(records.map((record) => record.status).filter(Boolean))];
    statusFilter.replaceChildren();
    const allOption = document.createElement("option");
    allOption.value = "all";
    allOption.textContent = "Tất cả trạng thái";
    statusFilter.append(allOption);
    statuses.forEach((status) => {
      const option = document.createElement("option");
      option.value = status;
      option.textContent = labels[status] || status;
      statusFilter.append(option);
    });
    statusFilter.value = statuses.includes(previousValue) ? previousValue : "all";
  }

  function render() {
    const state = {
      requests: window.AuraCraftCustom.listRequests(),
      quotes: [],
      orders: window.AuraCraftCustom.listOrders(),
      transactions: window.AuraCraftCustom.listTransactions()
    };
    state.quotes = state.requests.flatMap((request) => window.AuraCraftCustom.listQuotes(request.id));
    updateStats(state);
    const records = buildRows(state);
    updateStatusOptions(records);
    const keyword = search.value.trim().toLowerCase();
    const visibleRecords = records.filter((record) =>
      (statusFilter.value === "all" || record.status === statusFilter.value)
      && record.search.toLowerCase().includes(keyword)
    );

    tableHead.replaceChildren();
    const headerRow = document.createElement("tr");
    columns[activeView].forEach((label) => {
      const heading = document.createElement("th");
      heading.scope = "col";
      heading.textContent = label;
      headerRow.append(heading);
    });
    tableHead.append(headerRow);

    tableBody.replaceChildren();
    visibleRecords.forEach((record) => {
      const row = document.createElement("tr");
      record.render(row);
      tableBody.append(row);
    });
    emptyState.hidden = visibleRecords.length > 0;
    feedback.textContent = `${visibleRecords.length} / ${records.length} mục`;
  }

  function setView(view) {
    activeView = view;
    tabs.forEach((tab) => {
      const isActive = tab.dataset.view === view;
      tab.classList.toggle("active", isActive);
      tab.setAttribute("aria-selected", String(isActive));
    });
    render();
  }



  tabs.forEach((tab) => tab.addEventListener("click", () => setView(tab.dataset.view)));
  search.addEventListener("input", render);
  statusFilter.addEventListener("change", render);
  window.addEventListener("storage", render);
  render();
});