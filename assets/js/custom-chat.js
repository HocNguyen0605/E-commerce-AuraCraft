document.addEventListener("DOMContentLoaded", () => {
  const requestId = new URLSearchParams(window.location.search).get("requestId");
  if (!requestId || !window.AuraCraftCustom.getRequest(requestId)) return;

  const isSeller = window.location.pathname.endsWith("chat-seller.html");
  const role = isSeller ? "artisan" : "buyer";
  const name = isSeller ? "Thợ thủ công" : "Người mua";
  const messageList = document.getElementById("customMessages");
  const messageInput = document.getElementById("customMessageInput");
  const changeForm = document.getElementById("customChangeForm");
  const changeList = document.getElementById("customChangeRequests") || document.getElementById("buyerChangeRequests");

  function renderMessages() {
    messageList.replaceChildren();
    window.AuraCraftCustom.listMessages(requestId).forEach((message) => {
      const row = document.createElement("div");
      const bubble = document.createElement("div");
      const text = document.createElement("span");
      const time = document.createElement("span");
      row.className = `message ${message.senderRole === role ? "sent" : "received"}`;
      bubble.className = "msg-bubble";
      text.textContent = `${message.senderName}: ${message.text}`;
      time.className = "msg-time";
      time.textContent = new Date(message.createdAt).toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
      bubble.append(text, time);
      row.append(bubble);
      messageList.append(row);
    });
    messageList.scrollTop = messageList.scrollHeight;
  }

  function renderChanges() {
    if (!changeList) return;
    changeList.replaceChildren();
    window.AuraCraftCustom.listChanges(requestId).forEach((change) => {
      const item = document.createElement("article");
      const description = document.createElement("p");
      const price = document.createElement("p");
      const status = document.createElement("p");
      item.className = "custom-change-item";
      description.textContent = change.description;
      price.textContent = [
        change.proposedPrice !== null ? `Giá mới: ${change.proposedPrice.toLocaleString("vi-VN")}đ` : "",
        change.proposedDays !== null ? `Thời gian mới: ${change.proposedDays} ngày` : ""
      ].filter(Boolean).join(" · ");
      status.textContent = `Trạng thái: ${change.status === "pending" ? "Chờ thợ xác nhận" : change.status === "accepted" ? "Đã đồng ý" : "Đã từ chối"}`;
      item.append(description, price, status);
      if (isSeller && change.status === "pending") {
        [true, false].forEach((accepted) => {
          const button = document.createElement("button");
          button.type = "button";
          button.className = accepted ? "btn btn-primary" : "btn btn-outline";
          button.textContent = accepted ? "Đồng ý" : "Từ chối";
          button.addEventListener("click", () => {
            try {
              let revisedDays;
              if (accepted && change.proposedDays !== null) {
                revisedDays = prompt("Xác nhận thời gian chế tác mới (ngày):", change.proposedDays);
                if (revisedDays === null) return;
              }
              window.AuraCraftCustom.respondToChange(change.id, accepted, revisedDays);
              renderChanges();
              alert(accepted ? "Đã xác nhận thay đổi; giá và thời gian đơn hàng đã được cập nhật." : "Đã từ chối; thiết kế cũ được giữ nguyên.");
            } catch (error) {
              alert(error.message);
            }
          });
          item.append(button);
        });
      }
      if (!isSeller && change.status === "accepted" && change.proposedPrice !== null) {
        const order = window.AuraCraftCustom.getOrderForRequest(requestId);
        if (order && order.paymentStatus === "adjustment_due") {
          const paymentLink = document.createElement("a");
          paymentLink.className = "btn btn-primary";
          paymentLink.href = `checkout.html?orderId=${encodeURIComponent(order.id)}`;
          paymentLink.textContent = `Thanh toán phần chênh lệch ${Math.abs(order.priceDifference).toLocaleString("vi-VN")}đ`;
          item.append(paymentLink);
        }
        if (order && order.paymentStatus === "refund_due") {
          const refundNotice = document.createElement("p");
          refundNotice.textContent = `Giá giảm ${Math.abs(order.priceDifference).toLocaleString("vi-VN")}đ; khoản hoàn tiền đang chờ xử lý.`;
          item.append(refundNotice);
        }
      }
      changeList.append(item);
    });
  }

  function sendMessage() {
    try {
      window.AuraCraftCustom.addMessage(requestId, role, name, messageInput.value);
      messageInput.value = "";
      renderMessages();
    } catch (error) {
      alert(error.message);
    }
  }

  document.getElementById("sendCustomMessage").addEventListener("click", sendMessage);
  messageInput.addEventListener("keydown", (event) => {
    if (event.key === "Enter") {
      event.preventDefault();
      sendMessage();
    }
  });

  if (changeForm) {
    changeForm.addEventListener("submit", (event) => {
      event.preventDefault();
      try {
        window.AuraCraftCustom.proposeChange(requestId, {
          description: document.getElementById("changeDescription").value,
          proposedPrice: document.getElementById("changePrice").value,
          proposedDays: document.getElementById("changeDays").value
        });
        changeForm.reset();
        renderChanges();
        alert("Đã gửi đề xuất. Thiết kế chỉ cập nhật sau khi thợ xác nhận.");
      } catch (error) {
        alert(error.message);
      }
    });
  }

  window.addEventListener("storage", () => {
    renderMessages();
    renderChanges();
  });
  renderMessages();
  renderChanges();
});