(function () {
  const storageKey = "auracraft_custom_workflow";

  function makeId(prefix) {
    const randomPart = window.crypto && window.crypto.randomUUID
      ? window.crypto.randomUUID()
      : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
    return `${prefix}-${randomPart}`;
  }

  function readState() {
    try {
      const stored = JSON.parse(localStorage.getItem(storageKey) || "{}");
      return {
        requests: Array.isArray(stored.requests) ? stored.requests : [],
        quotes: Array.isArray(stored.quotes) ? stored.quotes : [],
        orders: Array.isArray(stored.orders) ? stored.orders : [],
        transactions: Array.isArray(stored.transactions) ? stored.transactions : [],
        messages: Array.isArray(stored.messages) ? stored.messages : [],
        changes: Array.isArray(stored.changes) ? stored.changes : [],
        disputes: Array.isArray(stored.disputes) ? stored.disputes : [],
        violations: Array.isArray(stored.violations) ? stored.violations : [],
        adminActions: Array.isArray(stored.adminActions) ? stored.adminActions : []
      };
    } catch (error) {
      return { requests: [], quotes: [], orders: [], transactions: [], messages: [], changes: [], disputes: [], violations: [], adminActions: [] };
    }
  }

  function writeState(state) {
    localStorage.setItem(storageKey, JSON.stringify(state));
  }

  function requireRecord(records, id, label) {
    const record = records.find((item) => item.id === id);
    if (!record) throw new Error(`${label} không tồn tại.`);
    return record;
  }

  function listRequests() {
    return readState().requests;
  }

  function getRequest(requestId) {
    return readState().requests.find((request) => request.id === requestId) || null;
  }

  function createRequest(details) {
    const state = readState();
    const request = {
      id: makeId("REQ"),
      sourceId: details.sourceId || "",
      buyerName: details.buyerName.trim(),
      buyerEmail: details.buyerEmail.trim().toLowerCase(),
      productType: details.productType.trim(),
      materials: details.materials.trim(),
      charmDescription: details.charmDescription.trim(),
      charmImages: details.charmImages || [],
      designImages: details.designImages || [],
      budget: Number(details.budget),
      status: "open",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    if (!request.productType || !request.materials || !request.charmDescription || !Number.isFinite(request.budget) || request.budget < 1) {
      throw new Error("Thông tin yêu cầu hoặc giá dự kiến không hợp lệ.");
    }
    if (!request.buyerName || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(request.buyerEmail)) {
      throw new Error("Tên và email người mua không hợp lệ.");
    }
    state.requests.unshift(request);
    writeState(state);
    return request;
  }

  function updateRequest(requestId, details) {
    const state = readState();
    const request = requireRecord(state.requests, requestId, "Yêu cầu");
    if (!["open", "quoted"].includes(request.status)) {
      throw new Error("Chỉ có thể sửa yêu cầu khi chưa chọn thợ.");
    }
    Object.assign(request, {
      productType: details.productType.trim(),
      materials: details.materials.trim(),
      charmDescription: details.charmDescription.trim(),
      budget: Number(details.budget),
      updatedAt: new Date().toISOString()
    });
    writeState(state);
    return request;
  }

  function cancelRequest(requestId) {
    const state = readState();
    const request = requireRecord(state.requests, requestId, "Yêu cầu");
    if (!["open", "quoted"].includes(request.status)) {
      throw new Error("Không thể hủy yêu cầu sau khi đã chọn thợ.");
    }
    request.status = "cancelled";
    request.updatedAt = new Date().toISOString();
    writeState(state);
    return request;
  }

  function listOpenRequests() {
    return readState().requests.filter((request) => request.status === "open" || request.status === "quoted");
  }

  function listQuotes(requestId) {
    return readState().quotes.filter((quote) => quote.requestId === requestId);
  }

  function addQuote(requestId, details) {
    const state = readState();
    const request = requireRecord(state.requests, requestId, "Yêu cầu");
    if (!["open", "quoted"].includes(request.status)) throw new Error("Yêu cầu này không còn nhận báo giá.");
    const sellerEmail = details.sellerEmail.trim().toLowerCase();
    if (state.quotes.some((quote) => quote.requestId === requestId && quote.sellerEmail === sellerEmail)) {
      throw new Error("Bạn đã gửi báo giá cho yêu cầu này.");
    }
    let portfolio = "";
    if (details.portfolio.trim()) {
      try {
        const portfolioUrl = new URL(details.portfolio.trim());
        if (["http:", "https:"].includes(portfolioUrl.protocol)) portfolio = portfolioUrl.href;
      } catch (error) {
        throw new Error("Đường dẫn portfolio không hợp lệ.");
      }
    }
    const quote = {
      id: makeId("QUOTE"),
      requestId,
      sellerName: details.sellerName.trim(),
      sellerEmail,
      portfolio,
      rating: Number(details.rating) || 5,
      completedOrders: Number(details.completedOrders) || 0,
      price: Number(details.price),
      days: Number(details.days),
      note: details.note.trim(),
      status: "pending",
      createdAt: new Date().toISOString()
    };
    if (!quote.sellerName || !Number.isFinite(quote.price) || quote.price < 1 || !Number.isInteger(quote.days) || quote.days < 1 || quote.days > 15
      || !Number.isFinite(quote.rating) || quote.rating < 1 || quote.rating > 5 || !Number.isInteger(quote.completedOrders) || quote.completedOrders < 0) {
      throw new Error("Giá, thời gian chế tác hoặc thông tin hồ sơ thợ không hợp lệ.");
    }
    state.quotes.push(quote);
    request.status = "quoted";
    request.updatedAt = new Date().toISOString();
    writeState(state);
    return quote;
  }

  function selectQuote(quoteId) {
    const state = readState();
    const quote = requireRecord(state.quotes, quoteId, "Báo giá");
    const request = requireRecord(state.requests, quote.requestId, "Yêu cầu");
    if (request.status === "awarded" || request.status === "cancelled") throw new Error("Yêu cầu này đã được xử lý.");
    quote.status = "selected";
    state.quotes.filter((item) => item.requestId === request.id && item.id !== quote.id)
      .forEach((item) => { item.status = "rejected"; });
    request.status = "awarded";
    request.selectedQuoteId = quote.id;
    request.updatedAt = new Date().toISOString();
    const order = {
      id: makeId("AC"),
      requestId: request.id,
      quoteId: quote.id,
      buyerName: request.buyerName,
      buyerEmail: request.buyerEmail,
      sellerName: quote.sellerName,
      sellerEmail: quote.sellerEmail,
      productType: request.productType,
      price: quote.price,
      days: quote.days,
      status: "pending_payment",
      paymentStatus: "unpaid",
      createdAt: new Date().toISOString(),
      paymentDeadlineAt: new Date(Date.now() + 24 * 60 * 60 * 1000).toISOString()
    };
    state.orders.push(order);
    writeState(state);
    return order;
  }

  function getOrder(orderId) {
    return readState().orders.find((order) => order.id === orderId) || null;
  }

  function listOrders() {
    return readState().orders;
  }

  function listTransactions() {
    return readState().transactions;
  }

  function getOrderForRequest(requestId) {
    return readState().orders.find((order) => order.requestId === requestId) || null;
  }

  function saveOrderDetails(orderId, details) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    Object.assign(order, {
      shippingName: details.fullName.trim(),
      phone: details.phone.trim(),
      address: details.address.trim(),
      note: details.note.trim()
    });
    writeState(state);
    return order;
  }

  function recordPayment(orderId, result, details) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    if (order.paymentStatus === "paid") throw new Error("Đơn hàng này đã thanh toán.");
    const isAdjustment = order.paymentStatus === "adjustment_due";
    const transaction = {
      id: makeId("TXN"),
      orderId,
      method: details.method,
      result,
      amount: isAdjustment ? Math.abs(order.priceDifference || 0) : order.price,
      createdAt: new Date().toISOString()
    };
    state.transactions.push(transaction);
    if (result === "success") {
      order.paymentStatus = "paid";
      order.status = "crafting";
      order.paidAt = transaction.createdAt;
      order.deadlineAt = new Date(new Date(transaction.createdAt).getTime() + order.days * 24 * 60 * 60 * 1000).toISOString();
    }
    writeState(state);
    return { order, transaction };
  }

  function listMessages(requestId) {
    return readState().messages.filter((message) => message.requestId === requestId);
  }

  function addMessage(requestId, senderRole, senderName, text) {
    const state = readState();
    const request = requireRecord(state.requests, requestId, "Yêu cầu");
    if (!request.selectedQuoteId) throw new Error("Chat chỉ mở sau khi người mua chọn thợ.");
    const message = {
      id: makeId("MSG"), requestId, senderRole, senderName: senderName.trim(),
      text: text.trim(), createdAt: new Date().toISOString()
    };
    if (!message.text) throw new Error("Tin nhắn không được để trống.");
    state.messages.push(message);
    writeState(state);
    return message;
  }

  function proposeChange(requestId, proposal) {
    const state = readState();
    const request = requireRecord(state.requests, requestId, "Yêu cầu");
    if (!request.selectedQuoteId) throw new Error("Chỉ có thể đề xuất thay đổi sau khi chọn thợ.");
    if (state.changes.some((change) => change.requestId === requestId && change.status === "pending")) {
      throw new Error("Đang có một đề xuất chờ thợ xác nhận.");
    }
    const change = {
      id: makeId("CHANGE"), requestId,
      description: proposal.description.trim(),
      proposedPrice: proposal.proposedPrice ? Number(proposal.proposedPrice) : null,
      proposedDays: proposal.proposedDays ? Number(proposal.proposedDays) : null,
      status: "pending", createdAt: new Date().toISOString()
    };
    if (!change.description || (change.proposedPrice !== null && (!Number.isFinite(change.proposedPrice) || change.proposedPrice < 1)) || (change.proposedDays !== null && (!Number.isInteger(change.proposedDays) || change.proposedDays < 1 || change.proposedDays > 60))) {
      throw new Error("Nội dung hoặc giá/thời gian đề xuất không hợp lệ.");
    }
    state.changes.push(change);
    writeState(state);
    return change;
  }

  function listChanges(requestId) {
    return readState().changes.filter((change) => change.requestId === requestId);
  }

  function respondToChange(changeId, accepted, revisedDays) {
    const state = readState();
    const change = requireRecord(state.changes, changeId, "Đề xuất thay đổi");
    if (change.status !== "pending") throw new Error("Đề xuất này đã được xử lý.");
    change.status = accepted ? "accepted" : "rejected";
    change.respondedAt = new Date().toISOString();
    if (accepted) {
      const request = requireRecord(state.requests, change.requestId, "Yêu cầu");
      const order = state.orders.find((item) => item.requestId === request.id);
      request.charmDescription = `${request.charmDescription}\nThay đổi đã duyệt: ${change.description}`;
      request.updatedAt = change.respondedAt;
      if (order) {
        if (change.proposedPrice !== null) {
          const wasPaid = order.paymentStatus === "paid";
          order.priceDifference = change.proposedPrice - order.price;
          order.price = change.proposedPrice;
          if (wasPaid && order.priceDifference > 0) {
            order.paymentStatus = "adjustment_due";
            order.status = "pending_payment";
          } else if (wasPaid && order.priceDifference < 0) {
            order.paymentStatus = "refund_due";
          } else if (!wasPaid) {
            order.paymentStatus = "unpaid";
            order.status = "pending_payment";
          }
        }
        const updatedDays = revisedDays ? Number(revisedDays) : change.proposedDays;
        if (updatedDays !== null && updatedDays !== undefined) {
          if (!Number.isInteger(updatedDays) || updatedDays < 1 || updatedDays > 60) {
            throw new Error("Thời gian chế tác phải từ 1 đến 60 ngày.");
          }
          order.days = updatedDays;
          change.confirmedDays = updatedDays;
        }
        if (order.priceDifference < 0) {
          state.transactions.push({
            id: makeId("TXN"), orderId: order.id, method: "refund",
            result: "pending", amount: Math.abs(order.priceDifference),
            createdAt: change.respondedAt
          });
        }
      }
    }
    writeState(state);
    return change;
  }

  function recordAdminAction(state, order, action, note, metadata) {
    const entry = {
      id: makeId("ADMIN"),
      orderId: order.id,
      action,
      note: (note || "").trim(),
      metadata: metadata || {},
      createdAt: new Date().toISOString()
    };
    state.adminActions.push(entry);
    return entry;
  }

  function listDisputes() {
    return readState().disputes;
  }

  function listViolations() {
    return readState().violations;
  }

  function clearViolation(violationId, note) {
    const state = readState();
    const violation = requireRecord(state.violations, violationId, "Vi phạm");
    if (violation.clearedAt) throw new Error("Vi phạm này đã được xử lý.");
    violation.clearedAt = new Date().toISOString();
    violation.resolutionNote = (note || "").trim();
    const order = requireRecord(state.orders, violation.orderId, "Đơn hàng");
    recordAdminAction(state, order, "violation_cleared", violation.resolutionNote, { violationId });
    writeState(state);
    return violation;
  }

  function listAdminActions(orderId) {
    return readState().adminActions.filter((entry) => !orderId || entry.orderId === orderId);
  }

  function extendOrderDeadline(orderId, days, reason) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    const extensionDays = Number(days);
    if (!["crafting", "late"].includes(order.status) || !Number.isInteger(extensionDays) || extensionDays < 1 || extensionDays > 30) {
      throw new Error("Chỉ gia hạn đơn đang chế tác/trễ hạn từ 1 đến 30 ngày.");
    }
    const previousDeadline = order.deadlineAt || new Date(Date.now() + order.days * 86400000).toISOString();
    order.deadlineAt = new Date(new Date(previousDeadline).getTime() + extensionDays * 86400000).toISOString();
    order.deadlineExtensionDays = (order.deadlineExtensionDays || 0) + extensionDays;
    order.status = "crafting";
    order.lateReviewedAt = null;
    recordAdminAction(state, order, "deadline_extended", reason, { days: extensionDays, previousDeadline, deadlineAt: order.deadlineAt });
    writeState(state);
    return order;
  }

  function reviewLateOrder(orderId, note) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    const deadlineAt = order.deadlineAt || (order.paidAt ? new Date(new Date(order.paidAt).getTime() + order.days * 86400000).toISOString() : null);
    if (!deadlineAt || !["crafting", "late"].includes(order.status) || new Date(deadlineAt).getTime() >= Date.now()) {
      throw new Error("Đơn hàng chưa quá deadline hoặc không còn ở giai đoạn chế tác.");
    }
    order.status = "late";
    order.lateReviewedAt = new Date().toISOString();
    const activeViolation = state.violations.find((item) => item.orderId === order.id && item.type === "late_delivery" && !item.clearedAt);
    if (!activeViolation) {
      state.violations.push({
        id: makeId("VIOLATION"), orderId: order.id, sellerEmail: order.sellerEmail,
        type: "late_delivery", note: (note || "Quá hạn deadline cam kết.").trim(),
        createdAt: order.lateReviewedAt, clearedAt: null
      });
    }
    recordAdminAction(state, order, "late_reviewed", note, { deadlineAt });
    writeState(state);
    return order;
  }

  function advanceOrderStatus(orderId, nextStatus, details) {
    const transitions = {
      crafting: ["completed"], late: ["completed"], completed: ["handed_to_carrier"],
      handed_to_carrier: ["in_transit"], in_transit: ["delivered"]
    };
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    if (!transitions[order.status]?.includes(nextStatus)) throw new Error("Không thể chuyển đơn sang trạng thái này.");
    if (nextStatus === "handed_to_carrier" && (!details?.carrier?.trim() || !details?.trackingNumber?.trim())) {
      throw new Error("Cần có đơn vị vận chuyển và mã vận đơn trước khi bàn giao.");
    }
    order.status = nextStatus;
    order.updatedAt = new Date().toISOString();
    if (nextStatus === "handed_to_carrier") {
      order.carrier = details.carrier.trim();
      order.trackingNumber = details.trackingNumber.trim();
    }
    if (nextStatus === "delivered") order.deliveredAt = order.updatedAt;
    if (nextStatus === "completed") order.completedAt = order.updatedAt;
    recordAdminAction(state, order, "status_changed", details?.note, { status: nextStatus });
    writeState(state);
    return order;
  }

  function calculateRefundDue(state, order) {
    const transactions = state.transactions.filter((transaction) => transaction.orderId === order.id);
    const successfulPayments = transactions
      .filter((transaction) => transaction.method !== "refund" && transaction.result === "success")
      .reduce((total, transaction) => total + Number(transaction.amount || 0), 0);
    const settledRefunds = transactions
      .filter((transaction) => transaction.method === "refund" && transaction.result === "success")
      .reduce((total, transaction) => total + Number(transaction.amount || 0), 0);
    const pendingRefunds = transactions.filter((transaction) => transaction.method === "refund" && transaction.result === "pending");
    const pendingAmount = pendingRefunds.reduce((total, transaction) => total + Number(transaction.amount || 0), 0);
    const fallbackAmount = transactions.length === 0 && ["paid", "adjustment_due", "refund_due"].includes(order.paymentStatus)
      ? Number(order.price || 0)
      : successfulPayments;
    return {
      amount: Math.max(0, fallbackAmount - settledRefunds - pendingAmount),
      hasPendingRefund: pendingRefunds.length > 0
    };
  }

  function cancelOrder(orderId, reason) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    if (!["pending_payment", "crafting", "late", "completed"].includes(order.status)) {
      throw new Error("Không thể hủy đơn sau khi đã bàn giao cho vận chuyển.");
    }
    if (!reason || !reason.trim()) throw new Error("Cần nhập lý do hủy đơn.");
    order.status = "cancelled";
    order.cancelledAt = new Date().toISOString();
    order.cancellationReason = reason.trim();
    let refund = null;
    const refundDue = calculateRefundDue(state, order);
    if (refundDue.amount > 0 || refundDue.hasPendingRefund) {
      order.paymentStatus = "refund_due";
      if (refundDue.amount > 0) {
        refund = {
          id: makeId("TXN"), orderId: order.id, method: "refund", result: "pending",
          amount: refundDue.amount, reason: order.cancellationReason, createdAt: order.cancelledAt
        };
        state.transactions.push(refund);
      }
    }
    recordAdminAction(state, order, "order_cancelled", reason, { refundAmount: refund?.amount || 0 });
    writeState(state);
    return { order, refund };
  }

  function processRefund(orderId, reference) {
    const state = readState();
    const order = requireRecord(state.orders, orderId, "Đơn hàng");
    if (order.paymentStatus !== "refund_due") throw new Error("Đơn này chưa đủ điều kiện hoàn tiền.");
    const pendingRefunds = state.transactions.filter((item) => item.orderId === order.id && item.method === "refund" && item.result === "pending");
    if (!pendingRefunds.length) throw new Error("Không tìm thấy khoản hoàn tiền đang chờ.");
    pendingRefunds.forEach((refund) => {
      refund.result = "success";
      refund.reference = (reference || "").trim();
      refund.processedAt = new Date().toISOString();
    });
    order.paymentStatus = order.status === "cancelled" ? "refunded" : "paid";
    recordAdminAction(state, order, "refund_processed", "Hoàn tiền được ghi nhận đã xử lý.", { reference: (reference || "").trim() });
    writeState(state);
    return order;
  }

  function createDispute(details) {
    const state = readState();
    const order = requireRecord(state.orders, details.orderId, "Đơn hàng");
    const dispute = {
      id: makeId("DSP"),
      orderId: order.id,
      buyerEmail: (details.buyerEmail || order.buyerEmail).trim().toLowerCase(),
      sellerEmail: order.sellerEmail,
      subject: details.subject.trim(),
      description: details.description.trim(),
      status: "open",
      adminNote: "",
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    };
    if (!dispute.subject || !dispute.description) throw new Error("Cần nhập tiêu đề và nội dung khiếu nại.");
    state.disputes.unshift(dispute);
    recordAdminAction(state, order, "dispute_opened", dispute.subject, { disputeId: dispute.id });
    writeState(state);
    return dispute;
  }

  function updateDispute(disputeId, status, adminNote) {
    const validStatuses = new Set(["open", "investigating", "awaiting_information", "closed"]);
    if (!validStatuses.has(status)) throw new Error("Trạng thái khiếu nại không hợp lệ.");
    const state = readState();
    const dispute = requireRecord(state.disputes, disputeId, "Khiếu nại");
    if (["resolved_buyer", "resolved_seller", "closed"].includes(dispute.status)) throw new Error("Khiếu nại này đã được đóng.");
    dispute.status = status;
    dispute.adminNote = (adminNote || "").trim();
    dispute.updatedAt = new Date().toISOString();
    if (status === "closed") dispute.resolvedAt = dispute.updatedAt;
    const order = requireRecord(state.orders, dispute.orderId, "Đơn hàng");
    recordAdminAction(state, order, "dispute_updated", dispute.adminNote, { disputeId, status });
    writeState(state);
    return dispute;
  }

  function resolveDispute(disputeId, outcome, resolutionNote) {
    if (!["buyer", "seller"].includes(outcome)) throw new Error("Kết quả giải quyết không hợp lệ.");
    if (!resolutionNote || !resolutionNote.trim()) throw new Error("Cần ghi rõ kết luận xử lý.");
    const state = readState();
    const dispute = requireRecord(state.disputes, disputeId, "Khiếu nại");
    if (["resolved_buyer", "resolved_seller", "closed"].includes(dispute.status)) throw new Error("Khiếu nại này đã được đóng.");
    const order = requireRecord(state.orders, dispute.orderId, "Đơn hàng");
    dispute.status = outcome === "buyer" ? "resolved_buyer" : "resolved_seller";
    dispute.resolution = resolutionNote.trim();
    dispute.resolvedAt = new Date().toISOString();
    dispute.updatedAt = dispute.resolvedAt;
    dispute.adminNote = dispute.resolution;
    let refund = null;
    if (outcome === "buyer") {
      if (!["pending_payment", "crafting", "late", "completed"].includes(order.status)) {
        throw new Error("Không thể hoàn tiền sau khi đơn đã bàn giao vận chuyển.");
      }
      order.status = "cancelled";
      order.cancelledAt = dispute.resolvedAt;
      order.cancellationReason = `Giải quyết khiếu nại ${dispute.id}`;
      const refundDue = calculateRefundDue(state, order);
      if (refundDue.amount > 0 || refundDue.hasPendingRefund) {
        order.paymentStatus = "refund_due";
        if (refundDue.amount > 0) {
          refund = {
            id: makeId("TXN"), orderId: order.id, method: "refund", result: "pending",
            amount: refundDue.amount, reason: dispute.resolution, createdAt: dispute.resolvedAt
          };
          state.transactions.push(refund);
        }
      }
    }
    recordAdminAction(state, order, "dispute_resolved", dispute.resolution, { disputeId, outcome, refundAmount: refund?.amount || 0 });
    writeState(state);
    return { dispute, order, refund };
  }

  window.AuraCraftCustom = {
    listRequests, getRequest, createRequest, updateRequest, cancelRequest,
    listOpenRequests, listQuotes, addQuote, selectQuote, getOrder, listOrders, listTransactions, getOrderForRequest, saveOrderDetails, recordPayment,
    listDisputes, listViolations, clearViolation, listAdminActions, extendOrderDeadline, reviewLateOrder,
    advanceOrderStatus, cancelOrder, processRefund, createDispute, updateDispute, resolveDispute,
    listMessages, addMessage, proposeChange, listChanges, respondToChange
  };
})();