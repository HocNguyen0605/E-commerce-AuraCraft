
document.addEventListener('DOMContentLoaded', () => {
  const requestId = new URLSearchParams(window.location.search).get('requestId');
  const quoteList = document.getElementById('quoteList');
  const infoOverlay = document.getElementById('csInfoOverlay');
  const infoClose = document.getElementById('csInfoClose');
  const infoNameEl = document.getElementById('csInfoName');
  const infoExperienceEl = document.getElementById('csInfoExperience');
  const infoYearsEl = document.getElementById('csInfoYears');
  const infoSpecialtyEl = document.getElementById('csInfoSpecialty');

  function renderQuotes() {
    const request = window.AuraCraftCustom.getRequest(requestId);
    if (!request) return;
    const summary = document.getElementById('requestSummary');
    summary.replaceChildren();
    const title = document.createElement('h2');
    const product = document.createElement('p');
    const materials = document.createElement('p');
    const price = document.createElement('p');
    title.textContent = 'Yêu cầu của bạn';
    product.textContent = request.productType;
    materials.textContent = `${request.materials}. ${request.charmDescription}`;
    price.textContent = `Giá cơ sở dự kiến: ${Number(request.budget).toLocaleString('vi-VN')} đ`;
    summary.append(title, product, materials, price);

    quoteList.replaceChildren();
    window.AuraCraftCustom.listQuotes(requestId).forEach((quote) => {
      const card = document.createElement('article');
      const info = document.createElement('div');
      const nameButton = document.createElement('button');
      const meta = document.createElement('div');
      const note = document.createElement('div');
      const quoteActions = document.createElement('div');
      const amount = document.createElement('div');
      const status = document.createElement('span');
      card.className = `seller-card${quote.status === 'selected' ? ' selected' : quote.status === 'rejected' ? ' rejected' : ''}`;
      card.dataset.quoteId = quote.id;
      card.dataset.experience = quote.note || 'Thợ thủ công nhận đơn theo yêu cầu.';
      card.dataset.years = 'Hồ sơ thợ';
      card.dataset.specialty = quote.note || request.materials;
      info.className = 'seller-info';
      nameButton.type = 'button';
      nameButton.className = 'seller-name-btn';
      nameButton.innerHTML = `<h3>${escapeText(quote.sellerName)}</h3>`;
      nameButton.addEventListener('click', () => {
        infoNameEl.textContent = quote.sellerName;
        infoExperienceEl.textContent = quote.note || 'Chưa có thông tin.';
        infoYearsEl.textContent = `${quote.completedOrders} đơn hoàn thành`;
        infoSpecialtyEl.textContent = request.materials;
        if (quote.portfolio) {
          const portfolio = document.createElement('a');
          portfolio.href = quote.portfolio;
          portfolio.target = '_blank';
          portfolio.rel = 'noopener noreferrer';
          portfolio.textContent = 'Xem portfolio';
          infoSpecialtyEl.append(' ', portfolio);
        }
        infoOverlay.classList.add('cs-open');
      });
      meta.className = 'meta-info';
      meta.textContent = `★ ${quote.rating} · ${quote.completedOrders} đơn · Chế tác: ${quote.days} ngày`;
      note.className = 'quote-details';
      note.textContent = quote.note || 'Thợ chưa thêm ghi chú.';
      status.className = 'status-badge approved';
      status.textContent = quote.status === 'selected' ? 'Đã chọn' : quote.status === 'rejected' ? 'Không được chọn' : 'Đang chờ';
      amount.className = 'quote-price';
      amount.textContent = `${Number(quote.price).toLocaleString('vi-VN')} ₫`;
      quoteActions.className = 'seller-quote';
      quoteActions.append(amount, status);
      if (quote.status === 'pending' && request.status !== 'awarded') {
        const selectButton = document.createElement('button');
        selectButton.type = 'button';
        selectButton.className = 'btn btn-primary btn-sm';
        selectButton.textContent = 'Chọn thợ này';
        selectButton.addEventListener('click', () => {
          try {
            const order = window.AuraCraftCustom.selectQuote(quote.id);
            window.location.href = `checkout.html?orderId=${encodeURIComponent(order.id)}`;
          } catch (error) {
            alert(error.message);
          }
        });
        quoteActions.append(selectButton);
      }
      if (quote.status === 'selected') {
        const chatLink = document.createElement('a');
        chatLink.className = 'btn btn-outline btn-sm';
        chatLink.href = `chat-buyer.html?requestId=${encodeURIComponent(request.id)}`;
        chatLink.textContent = 'Mở chat';
        quoteActions.append(chatLink);
      }
      info.append(nameButton, meta, note);
      card.append(info, quoteActions);
      quoteList.append(card);
    });
    if (!quoteList.children.length) {
      const empty = document.createElement('p');
      empty.textContent = 'Chưa có báo giá nào. Hãy chờ thợ gửi báo giá từ chợ yêu cầu.';
      quoteList.append(empty);
    }
  }

  function escapeText(value) {
    const element = document.createElement('span');
    element.textContent = value;
    return element.innerHTML;
  }

  if (requestId) renderQuotes();

  function openSellerInfo(card, sellerName) {
    infoNameEl.textContent = sellerName;
    infoExperienceEl.textContent = card.dataset.experience || 'Chưa có thông tin.';
    infoYearsEl.textContent = card.dataset.years || 'Chưa có thông tin.';
    infoSpecialtyEl.textContent = card.dataset.specialty || 'Chưa có thông tin.';
    infoOverlay.classList.add('cs-open');
  }
  function closeSellerInfo() {
    infoOverlay.classList.remove('cs-open');
  }
  infoClose.addEventListener('click', closeSellerInfo);
  infoOverlay.addEventListener('click', e => {
    if (e.target === infoOverlay) closeSellerInfo();
  });

  if (!requestId) {
    const cards = document.querySelectorAll('.seller-card');
    cards.forEach(card => {
      const chooseBtn = card.querySelector('.quote-choose-btn');
      const chatBtn = card.querySelector('.quote-chat-btn');
      const nameBtn = card.querySelector('.seller-name-btn');
      const sellerName = card.querySelector('.seller-info h3').textContent;

      nameBtn.addEventListener('click', () => openSellerInfo(card, sellerName));
      chooseBtn.addEventListener('click', () => {
        const alreadyChosen = document.querySelector('.seller-card.selected');
        if (alreadyChosen) return;
        cards.forEach(c => {
          const cChooseBtn = c.querySelector('.quote-choose-btn');
          if (c === card) {
            c.classList.add('selected');
            c.classList.remove('rejected');
            cChooseBtn.textContent = 'Đã chọn';
            cChooseBtn.disabled = true;
            
            // Redirect to checkout for the demo flow
            const virtualOrderId = "ORD-" + Math.floor(1000 + Math.random() * 9000);
            const total = parseInt(c.querySelector('.quote-price').textContent.replace(/[^\d]/g, ''), 10);
            
            // Simulate saving an order
            const orderData = {
                id: virtualOrderId,
                productType: `Custom Mẫu Yêu Cầu`,
                sellerName: sellerName,
                price: total,
                days: parseInt(c.querySelector('.meta-info span:nth-child(3)').textContent.replace(/[^\d]/g, ''), 10) || 5,
                paymentStatus: 'unpaid',
                buyerName: 'Khách hàng',
                buyerEmail: 'khachhang@example.com'
            };
            try {
                const stateStr = localStorage.getItem("auracraft_custom_workflow");
                if (stateStr) {
                    const state = JSON.parse(stateStr);
                    state.orders.push(orderData);
                    localStorage.setItem("auracraft_custom_workflow", JSON.stringify(state));
                }
            } catch (e) {
                console.error(e);
            }
            
            setTimeout(() => {
                window.location.href = `checkout.html?orderId=${virtualOrderId}`;
            }, 500);
          } else {
            c.classList.add('rejected');
            cChooseBtn.textContent = 'Không được chọn';
            cChooseBtn.disabled = true;
          }
        });
      });
      chatBtn.addEventListener('click', () => {
        alert(`Đang mở đoạn chat với ${sellerName}...`);
      });
    });
  }
});