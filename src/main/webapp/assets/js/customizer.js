// AuraCraft — Customizer (standalone page): numbered charm-slot strip, live total, save + send-to-shop
document.addEventListener('DOMContentLoaded', () => {
  const TYPE_CONFIG = {
    bracelet: { label: 'Vòng tay', heading: 'Vòng tay của bạn', slotCount: 14 },
    necklace: { label: 'Dây chuyền', heading: 'Dây chuyền của bạn', slotCount: 10 },
    ring: { label: 'Nhẫn', heading: 'Nhẫn của bạn', slotCount: 6 },
    charm: { label: 'Móc khóa điện thoại', heading: 'Móc khóa điện thoại của bạn', slotCount: 3 }
  };


  const CHARM_IMAGES = (typeof AURA_CHARM_LIBRARY_IMAGES !== 'undefined') ? AURA_CHARM_LIBRARY_IMAGES : {};
  const EXTRA_CHARMS = (typeof AURA_CHARM_LIBRARY_EXTRA !== 'undefined') ? AURA_CHARM_LIBRARY_EXTRA : [];

  const params = new URLSearchParams(window.location.search);
  const productId = parseInt(params.get('id'), 10);
  const product = (typeof AURA_PRODUCTS !== 'undefined') ? AURA_PRODUCTS.find(p => p.id === productId) : null;
  const type = product ? product.type : (TYPE_CONFIG[params.get('type')] ? params.get('type') : 'bracelet');
  const cfg = TYPE_CONFIG[type];
  const shopName = product ? product.shop : 'Xưởng Mộc An';

  const backHref = 'shop-profile.html';
  document.getElementById('czBackLink').href = backHref;
  document.getElementById('czBackLink').textContent = `← Quay lại cửa hàng (${shopName})`;
  document.getElementById('czCancelLink').href = backHref;
  document.getElementById('czCancelLink').textContent = `Không muốn custom nữa? Quay lại cửa hàng`;

  document.getElementById('czTag').textContent = `Thiết kế Custom · ${cfg.label}`;
  document.getElementById('czHeading').textContent = product ? `Custom: ${product.name}` : cfg.heading;
  document.getElementById('czShopInfo').innerHTML =
    `Yêu cầu Custom này sẽ gửi trực tiếp đến <strong>${shopName}</strong> — shop sẽ báo giá thực tế cho riêng bạn.`;
  const LIBRARY_ITEMS = [
    { name: 'Hạt chữ', price: 10000, image: 'https://myumemories.com/wp-content/uploads/2025/11/3-65-300x300.png' },
    { name: 'Charm hoa', price: 40000, image: 'https://myumemories.com/wp-content/uploads/2026/01/3-10-300x300.png' },
    { name: 'Charm tim', price: 35000, image: 'https://myumemories.com/wp-content/uploads/2026/01/8-10-300x300.png' },
    { name: 'Charm cỏ 4 lá', price: 38000, image: 'https://myumemories.com/wp-content/uploads/2025/11/8-28-300x300.png' },
    ...EXTRA_CHARMS.map(c => ({ name: c.name, price: c.price, image: c.image }))
  ];

  const stripEl = document.getElementById('czStrip');
  const slotCounterEl = document.getElementById('czSlotCounter');
  const libraryEl = document.getElementById('czLibrary');
  const priceList = document.getElementById('czPriceList');
  const totalEl = document.getElementById('czTotal');

  let totalSlots = cfg.slotCount;
  let slots = new Array(totalSlots).fill(null);

  // Demo: Tự động fill đầy charm nếu người dùng click từ product detail (có productId)
  if (productId) {
    for (let i = 0; i < totalSlots; i++) {
      slots[i] = LIBRARY_ITEMS[i % Math.min(4, LIBRARY_ITEMS.length)];
    }
  }

  const sizeSelect = document.getElementById('czSlotSize');
  if (sizeSelect) {
    if ([14, 16, 18, 20].includes(totalSlots)) {
      sizeSelect.value = totalSlots;
    }
    sizeSelect.addEventListener('change', (e) => {
      const newSize = parseInt(e.target.value, 10);
      if (newSize > totalSlots) {
        slots = slots.concat(new Array(newSize - totalSlots).fill(null));
      } else if (newSize < totalSlots) {
        slots = slots.slice(0, newSize);
      }
      totalSlots = newSize;
      refreshAll();
    });
  }

  const autoFillBtn = document.getElementById('czAutoFillBtn');
  const textInput = document.getElementById('czTextInput');
  const textFontSelect = document.getElementById('czTextFont');

  if (autoFillBtn && textInput && textFontSelect) {
    autoFillBtn.addEventListener('click', () => {
      const text = textInput.value.trim().toUpperCase();
      if (!text) {
        alert('Vui lòng nhập chữ cần xếp.');
        return;
      }
      const font = textFontSelect.value;
      const price = font === 'Chữ Đen' ? 10000 : 15000;
      const color = font === 'Chữ Đen' ? '#333333' : '#E5C158';
      let charIndex = 0;
      for (let i = 0; i < totalSlots; i++) {
        if (slots[i] === null) {
          if (charIndex < text.length) {
            const char = text[charIndex];
            const charImage = `../assets/img/products/letter_${char}.png`;
            slots[i] = {
              name: `${font} - ${char}`,
              price: price,
              color: color,
              image: charImage
            };
            charIndex++;
          } else {
            break;
          }
        }
      }
      if (charIndex < text.length) {
        alert(`Không đủ ô trống! Chỉ xếp được ${charIndex}/${text.length} chữ. Hãy đổi sang cỡ vòng lớn hơn.`);
      }
      refreshAll();
    });
  }

  const formatVND = n => n.toLocaleString('vi-VN') + ' ₫';

  function pieceColor(name) {
    const map = {
      'Hạt chữ': '#C9A24B',
      'Charm hoa': '#D9789A',
      'Charm tim': '#C0524F',
      'Charm cỏ 4 lá': '#5C9668'
    };
    return map[name] || '#425B9A';
  }

  function filledCount() {
    return slots.filter(s => s !== null).length;
  }

  function renderTotal() {
    const total = slots.reduce((sum, p) => sum + (p ? p.price : 0), 0);
    totalEl.textContent = formatVND(total);
    totalEl.classList.remove('cz-bump');
    void totalEl.offsetWidth;
    totalEl.classList.add('cz-bump');
  }

  function renderPriceList() {
    priceList.innerHTML = '';
    const placed = slots.filter(s => s !== null);
    if (placed.length === 0) {
      priceList.innerHTML = '<li class="cz-price-empty">Chưa có thành phần nào</li>';
      return;
    }
    placed.forEach(p => {
      const li = document.createElement('li');
      li.innerHTML = `<span>${p.name}</span><span>${formatVND(p.price)}</span>`;
      priceList.appendChild(li);
    });
  }

  function renderSlotCounter() {
    const filled = filledCount();
    slotCounterEl.innerHTML = `Charm tùy chỉnh: <strong>${filled}/${totalSlots}</strong> · Còn ${totalSlots - filled} vị trí`;
  }

  function removeFromSlot(index) {
    slots[index] = null;
    refreshAll();
  }
  function handleDropOnSlot(e, index) {
    e.preventDefault();
    e.currentTarget.classList.remove('cz-slot-dragover');

    const fromSlotIndex = e.dataTransfer.getData('text/slot-index');
    if (fromSlotIndex !== '') {
      const fi = parseInt(fromSlotIndex, 10);
      if (fi === index) return;
      const tmp = slots[index];
      slots[index] = slots[fi];
      slots[fi] = tmp;
      refreshAll();
      return;
    }

    const name = e.dataTransfer.getData('text/name');
    if (!name) return;
    const price = parseInt(e.dataTransfer.getData('text/price'), 10) || 0;
    const image = e.dataTransfer.getData('text/image') || null;
    const color = e.dataTransfer.getData('text/color') || null;
    slots[index] = { name, price, image, color };
    refreshAll();
  }

  function renderStrip() {
    stripEl.innerHTML = '';
    slots.forEach((piece, index) => {
      const slot = document.createElement('div');
      slot.className = 'cz-slot ' + (piece ? 'cz-slot-filled' : 'cz-slot-empty');
      slot.dataset.index = index + 1;

      if (piece) {
        if (piece.image) {
          slot.style.background = `url('${piece.image}') center/cover no-repeat, ${piece.color || pieceColor(piece.name)}`;
        } else {
          slot.style.background = piece.color || pieceColor(piece.name);
          slot.textContent = piece.name.charAt(0);
        }
        slot.title = piece.name + ' — kéo để đổi vị trí, bấm × để xóa';
        slot.draggable = true;

        slot.addEventListener('dragstart', e => {
          e.dataTransfer.setData('text/slot-index', String(index));
        });

        const removeBtn = document.createElement('button');
        removeBtn.type = 'button';
        removeBtn.className = 'cz-slot-remove';
        removeBtn.setAttribute('aria-label', `Xóa ${piece.name}`);
        removeBtn.textContent = '×';
        removeBtn.addEventListener('click', e => {
          e.stopPropagation();
          removeFromSlot(index);
        });
        slot.appendChild(removeBtn);
      } else {
        slot.style.background = '';
        slot.title = `Vị trí ${index + 1} — còn trống`;
      }

      slot.addEventListener('dragover', e => {
        e.preventDefault();
        slot.classList.add('cz-slot-dragover');
      });
      slot.addEventListener('dragleave', () => slot.classList.remove('cz-slot-dragover'));
      slot.addEventListener('drop', e => handleDropOnSlot(e, index));

      stripEl.appendChild(slot);
    });
  }

  function refreshAll() {
    renderStrip();
    renderPriceList();
    renderSlotCounter();
    renderTotal();
    updateLibraryFullState();
  }
  function updateLibraryFullState() {
    const isFull = filledCount() >= totalSlots;
    libraryEl.querySelectorAll('.cz-lib-strip-item').forEach(el => {
      el.classList.toggle('is-full', isFull);
    });
  }

  function renderLibrary() {
    libraryEl.innerHTML = '';
    LIBRARY_ITEMS.forEach(item => {
      const el = document.createElement('div');
      el.className = 'cz-lib-strip-item';
      el.draggable = true;

      const photo = document.createElement('div');
      photo.className = 'cz-lib-strip-photo';
      photo.style.background = item.image
        ? `url('${item.image}') center/cover no-repeat, ${item.color || pieceColor(item.name)}`
        : (item.color || pieceColor(item.name));

      const name = document.createElement('span');
      name.className = 'cz-lib-strip-name';
      name.textContent = item.name;

      const price = document.createElement('small');
      price.className = 'cz-lib-strip-price';
      price.textContent = formatVND(item.price);

      el.appendChild(photo);
      el.appendChild(name);
      el.appendChild(price);

      el.addEventListener('dragstart', e => {
        e.dataTransfer.setData('text/name', item.name);
        e.dataTransfer.setData('text/price', String(item.price));
        if (item.image) e.dataTransfer.setData('text/image', item.image);
        if (item.color) e.dataTransfer.setData('text/color', item.color);
      });

      el.addEventListener('click', () => {
        const emptyIndex = slots.findIndex(s => s === null);
        if (emptyIndex === -1) {
          alert('Dải đã đủ vị trí. Vui lòng xóa bớt charm trước khi thêm charm mới.');
          return;
        }
        slots[emptyIndex] = { name: item.name, price: item.price, image: item.image || null, color: item.color || null };
        refreshAll();
      });

      libraryEl.appendChild(el);
    });
  }

  renderLibrary();
  refreshAll();

  const sendShopBtn = document.getElementById('czSendShopBtn');
  const saveDesignBtn = document.getElementById('czSaveDesignBtn');
  const sendOverlay = document.getElementById('czSendModalOverlay');
  const sendModalClose = document.getElementById('czSendModalClose');
  const sendShopNameEl = document.getElementById('czSendShopName');

  if (saveDesignBtn) {
    saveDesignBtn.addEventListener('click', () => {
        if (filledCount() === 0) {
            alert('Vui lòng thêm ít nhất một charm vào thiết kế trước khi lưu.');
            return;
        }
        alert('Thiết kế của bạn đã được lưu vào Bộ sưu tập cá nhân!');
    });
  }

  sendShopBtn.addEventListener('click', () => {
    if (filledCount() === 0) {
      alert('Vui lòng thêm ít nhất một charm vào thiết kế trước khi đặt hàng.');
      return;
    }
    
    // Tạo đơn hàng ảo
    const total = slots.reduce((sum, p) => sum + (p ? p.price : 0), 0);
    const virtualOrderId = "ORD-" + Math.floor(1000 + Math.random() * 9000);
    const orderData = {
        id: virtualOrderId,
        productType: product ? `Custom: ${product.name}` : `Custom: ${cfg.label}`,
        sellerName: shopName,
        price: total,
        days: 5,
        paymentStatus: 'unpaid',
        buyerName: 'Khách hàng',
        buyerEmail: 'khachhang@example.com'
    };
    
    // Lưu vào AuraCraftCustom DB giả lập
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
    
    window.location.href = `checkout.html?orderId=${orderData.id}`;
  });
  sendModalClose.addEventListener('click', () => { window.location.href = backHref; });
});