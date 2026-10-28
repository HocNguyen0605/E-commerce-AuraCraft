// AuraCraft — Custom design page: material slots, base material pricing, save + checkout
document.addEventListener('DOMContentLoaded', () => {
  const TYPE_CONFIG = {
    bracelet: { label: 'Vòng tay', heading: 'Vòng tay của bạn', slotCount: 14 },
    necklace: { label: 'Dây chuyền', heading: 'Dây chuyền của bạn', slotCount: 10 },
    ring: { label: 'Nhẫn', heading: 'Nhẫn của bạn', slotCount: 6 },
    charm: { label: 'Móc khóa điện thoại', heading: 'Móc khóa điện thoại của bạn', slotCount: 3 }
  };


  const params = new URLSearchParams(window.location.search);
  const productId = parseInt(params.get('id'), 10);
  const productCustomizationMode = Number.isFinite(productId) && productId > 0;
  const productName = (params.get('name') || '').trim();
  let product = productId ? {
    id: productId,
    name: productName || `Sản phẩm #${productId}`,
    shop: (params.get('shop') || '').trim() || 'AuraCraft'
  } : null;
  let hasDesignType = productCustomizationMode || Boolean(TYPE_CONFIG[params.get('type')]);
  let type = TYPE_CONFIG[params.get('type')] ? params.get('type') : 'bracelet';
  let cfg = TYPE_CONFIG[type];
  let shopName = (product ? product.shop : (params.get('shop') || 'AuraCraft')).trim();
  let shopId = params.get('shopId') || '';
  const context = document.querySelector('.cz-wrap')?.dataset.context || params.get('context') || '';

  const backHref = productId ? `${context}/pages/product-detail?id=${encodeURIComponent(productId)}` : `${context}/pages/products`;
  document.getElementById('czBackLink').href = backHref;
  document.getElementById('czBackLink').textContent = `← Quay lại cửa hàng (${shopName})`;
  document.getElementById('czCancelLink').href = backHref;
  document.getElementById('czCancelLink').textContent = `Không muốn custom nữa? Quay lại cửa hàng`;

  document.getElementById('czTag').textContent = product ? `Tùy chỉnh charm · ${cfg.label}`
    : hasDesignType ? `Thiết kế Custom · ${cfg.label}` : 'Thiết kế sản phẩm';
  document.getElementById('czHeading').textContent = product ? product.name
    : hasDesignType ? cfg.heading : 'Bắt đầu thiết kế của bạn';
  document.getElementById('czHint').textContent = productCustomizationMode
    ? 'Mẫu dây và kích thước được giữ nguyên theo sản phẩm. Chọn, kéo thả hoặc xóa charm để tùy chỉnh.'
    : 'Chọn danh mục, shop, mẫu dây và kích thước để bắt đầu thiết kế.';
  document.getElementById('czShopInfo').textContent = product
    ? `Sản phẩm: ${product.name} · Gian hàng: ${shopName}. Giá thiết kế được tính theo các vật liệu bạn chọn.`
    : hasDesignType ? `Gian hàng: ${shopName}. Chọn shop và mẫu nền để bắt đầu.`
      : 'Chọn danh mục thiết kế để xem các shop và vật liệu phù hợp.';
  let LIBRARY_ITEMS = [];

  const stripEl = document.getElementById('czStrip');
  const slotCounterEl = document.getElementById('czSlotCounter');
  const libraryEl = document.getElementById('czLibrary');
  const shopSelect = document.getElementById('czShopSelect');
  const sourceProductSelect = document.getElementById('czSourceProduct');
  const sourceHelp = document.getElementById('czSourceHelp');
  const draftSelect = document.getElementById('czSavedDesigns');
  const basePreview = document.getElementById('czBasePreview');
  const baseMaterialImage = document.getElementById('czBaseMaterialImage');
  const baseMaterialName = document.getElementById('czBaseMaterialName');
  const priceList = document.getElementById('czPriceList');
  const totalEl = document.getElementById('czTotal');
  const feedbackEl = document.getElementById('czFeedback');
  const toastEl = document.getElementById('czToast');
  let toastTimer;
  if (productCustomizationMode) {
    document.getElementById('czDesignTypeCard').hidden = true;
    document.getElementById('czDesignSizeCard').hidden = true;
    document.getElementById('czSourcePicker').hidden = true;
    document.getElementById('czLibraryFilters').hidden = true;
    document.getElementById('czSavedDesignsCard').hidden = true;
  }
  const setFeedback = (message, kind = '') => {
    if (!feedbackEl) return;
    feedbackEl.textContent = message;
    feedbackEl.dataset.kind = kind;
  };
  const showNotice = (message, kind = 'success') => {
    if (!toastEl) return;
    window.clearTimeout(toastTimer);
    toastEl.textContent = message;
    toastEl.dataset.kind = kind;
    toastEl.hidden = false;
    toastTimer = window.setTimeout(() => { toastEl.hidden = true; }, 3600);
  };
  const shortenName = (value, limit = 34) => {
    const chars = Array.from(String(value || ''));
    return chars.length > limit ? `${chars.slice(0, limit - 1).join('')}…` : chars.join('');
  };

  let totalSlots = hasDesignType ? cfg.slotCount : 0;
  let slots = new Array(totalSlots).fill(null);
  let baseMaterial = null;
  let customizerSources = [];
  let selectedSource = null;
  let currentDesignId = params.get('designId') || '';

  function readBaseMaterial() {
    const option = sourceProductSelect?.selectedOptions[0];
    if (!option?.value) return null;
    return {
      id: option.value,
      name: option.dataset.baseName || '',
      price: Number(option.dataset.basePrice) || 0,
      color: '#C8CDD3',
      image: option.dataset.baseImage || '',
      sourceProductId: Number(option.value),
      sourceProductName: option.dataset.productName || '',
      shopName: option.dataset.shopName || '',
      shopId: option.dataset.shopId || ''
    };
  }
  function updateBaseMaterialPreview() {
    if (!basePreview || !baseMaterialImage || !baseMaterialName) return;
    basePreview.hidden = !baseMaterial;
    if (!baseMaterial) return;
    baseMaterialImage.src = baseMaterial.image;
    baseMaterialImage.alt = `Ảnh minh họa ${baseMaterial.name.toLowerCase()}`;
    baseMaterialName.textContent = shortenName(baseMaterial.name, 38);
    baseMaterialName.title = baseMaterial.name;
  }
  if (sourceProductSelect) sourceProductSelect.addEventListener('change', () => {
    const source = customizerSources.find(item => String(item.id) === sourceProductSelect.value);
    applySource(source || null);
  });
  if (shopSelect) shopSelect.addEventListener('change', () => {
    if (filledCount() && selectedSource && !window.confirm('Đổi shop sẽ xóa các vật liệu đang xếp. Bạn có muốn tiếp tục không?')) {
      shopSelect.value = String(selectedSource.shopId);
      return;
    }
    shopId = shopSelect.value;
    const selectedShop = customizerSources.find(source => String(source.shopId) === String(shopId));
    shopName = selectedShop?.shopName || 'AuraCraft';
    sourceProductSelect.value = '';
    selectedSource = null;
    slots = new Array(totalSlots).fill(null);
    applySource(null);
    refreshSourceOptions();
    refreshBaseChoices();
    renderLibrary();
  });

  const typeSelect = document.getElementById('czProductType');
  const sizeSelect = document.getElementById('czSlotSize');
  const sizesByType = { bracelet: [14, 16, 18, 20], necklace: [10, 12, 14, 16], ring: [4, 6, 8], charm: [3, 4, 5] };
  function configureSizeSelect(selectedSize = cfg.slotCount) {
    if (!sizeSelect) return;
    const sizes = sizesByType[type];
    sizeSelect.innerHTML = sizes.map(value => `<option value="${value}">${value} vị trí</option>`).join('');
    const nextSize = sizes.includes(Number(selectedSize)) ? Number(selectedSize) : sizes[0];
    sizeSelect.value = String(nextSize);
    return nextSize;
  }

  if (typeSelect) typeSelect.value = hasDesignType ? type : '';
  if (sizeSelect) {
    if (hasDesignType) {
      configureSizeSelect(totalSlots);
      totalSlots = Number(sizeSelect.value);
      slots = new Array(totalSlots).fill(null);
    } else {
      sizeSelect.innerHTML = '<option value="">Chọn danh mục trước</option>';
      sizeSelect.disabled = true;
      slots = [];
    }
    sizeSelect.addEventListener('change', (e) => {
      const newSize = parseInt(e.target.value, 10);
      const removedPieces = slots.slice(newSize).filter(Boolean).length;
      if (removedPieces && !window.confirm(`Thu nhỏ thiết kế sẽ xóa ${removedPieces} vật liệu ở cuối dải. Bạn có muốn tiếp tục không?`)) {
        sizeSelect.value = String(totalSlots);
        return;
      }
      if (newSize > totalSlots) {
        slots = slots.concat(new Array(newSize - totalSlots).fill(null));
      } else if (newSize < totalSlots) {
        slots = slots.slice(0, newSize);
      }
      totalSlots = newSize;
      refreshAll();
    });
  }

  if (typeSelect) typeSelect.addEventListener('change', () => {
    const nextType = typeSelect.value;
    if (!TYPE_CONFIG[nextType] || (hasDesignType && nextType === type)) return;
    if (filledCount() && !window.confirm('Đổi danh mục sẽ xóa các vật liệu đang đặt. Bạn có muốn tiếp tục không?')) {
      typeSelect.value = type;
      return;
    }
    type = nextType;
    hasDesignType = true;
    cfg = TYPE_CONFIG[type];
    currentDesignId = '';
    slots = [];
    totalSlots = configureSizeSelect(cfg.slotCount) || cfg.slotCount;
    slots = new Array(totalSlots).fill(null);
    sizeSelect.disabled = false;
    if (draftSelect) draftSelect.value = '';
    selectedSource = null;
    sourceProductSelect.value = '';
    baseMaterial = null;
    product = null;
    updateBaseMaterialPreview();
    refreshSourceOptions();
    refreshBaseChoices();
    renderLibrary();
    document.getElementById('czTag').textContent = `Thiết kế Custom · ${cfg.label}`;
    document.getElementById('czHeading').textContent = cfg.heading;
    document.getElementById('czShopInfo').textContent = shopId
      ? `Gian hàng: ${shopName}. Chọn mẫu nền để thiết kế với vật liệu của shop.`
      : 'Chọn shop, sau đó chọn mẫu nền và vật liệu thiết kế.';
    setFeedback(`Đã chuyển sang danh mục ${cfg.label}.`, 'success');
    refreshAll();
  });

  const autoFillBtn = document.getElementById('czAutoFillBtn');
  const textInput = document.getElementById('czTextInput');
  const textFontSelect = document.getElementById('czTextFont');

  if (autoFillBtn && textInput && textFontSelect) {
    autoFillBtn.addEventListener('click', () => {
      const text = Array.from(textInput.value.trim().toLocaleUpperCase('vi').replace(/\s+/g, ''));
      if (!text) {
        setFeedback('Vui lòng nhập chữ cần xếp.', 'error');
        return;
      }
      const letterBead = LIBRARY_ITEMS.find(item => item.category === 'charm'
        && item.name.toLocaleLowerCase('vi').includes('hạt chữ'));
      if (!letterBead) {
        setFeedback('Mẫu đã chọn chưa có hạt chữ trong danh sách vật liệu của shop.', 'error');
        return;
      }
      const font = textFontSelect.value;
      const price = Number(letterBead.price) || 0;
      const color = font === 'Chữ Đen' ? '#333333' : '#E5C158';
      let charIndex = 0;
      for (let i = 0; i < totalSlots; i++) {
        if (slots[i] === null) {
          if (charIndex < text.length) {
            const char = text[charIndex];
            slots[i] = {
              name: `${font} - ${char}`,
              letter: char,
              price: price,
              color: color
            };
            charIndex++;
          } else {
            break;
          }
        }
      }
      if (charIndex < text.length) {
        setFeedback(`Chỉ xếp được ${charIndex}/${text.length} chữ vì thiết kế không còn đủ vị trí. Hãy tăng số vị trí rồi thử lại.`, 'error');
      } else {
        setFeedback(`Đã xếp ${charIndex} hạt chữ vào các vị trí còn trống.`, 'success');
      }
      refreshAll();
    });
  }

  const formatVND = n => n.toLocaleString('vi-VN') + 'đ';

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
    const total = (baseMaterial?.price || 0) + slots.reduce((sum, p) => sum + (p ? p.price : 0), 0);
    totalEl.textContent = formatVND(total);
    totalEl.classList.remove('cz-bump');
    void totalEl.offsetWidth;
    totalEl.classList.add('cz-bump');
  }

  function renderPriceList() {
    priceList.innerHTML = '';
    const placed = slots.filter(s => s !== null);
    if (baseMaterial) {
      const li = document.createElement('li');
      const name = document.createElement('span');
      name.textContent = `${shortenName(baseMaterial.name, 34)} (dây nền)`;
      name.title = `${baseMaterial.name} (dây nền)`;
      const price = document.createElement('span'); price.textContent = formatVND(baseMaterial.price);
      li.append(name, price);
      priceList.appendChild(li);
    }
    if (placed.length === 0 && !baseMaterial) {
      priceList.innerHTML = '<li class="cz-price-empty">Chưa có thành phần nào</li>';
      return;
    }
    placed.forEach(p => {
      const li = document.createElement('li');
      const name = document.createElement('span');
      name.textContent = shortenName(p.name, 34);
      name.title = p.name;
      const price = document.createElement('span'); price.textContent = formatVND(p.price);
      li.append(name, price);
      priceList.appendChild(li);
    });
  }

  function renderSlotCounter() {
    if (!hasDesignType) {
      slotCounterEl.textContent = 'Chọn danh mục thiết kế để bắt đầu.';
      slotCounterEl.title = '';
      return;
    }
    const filled = filledCount();
    const baseName = baseMaterial?.name || 'chưa chọn';
    slotCounterEl.textContent = `Thành phần trang trí đã chọn: ${filled}/${totalSlots} · Còn ${totalSlots - filled} vị trí · Dây nền: ${shortenName(baseName, 34)}`;
    slotCounterEl.title = baseName;
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
    const letter = e.dataTransfer.getData('text/letter') || null;
    const symbol = e.dataTransfer.getData('text/symbol') || null;
    slots[index] = { name, price, image, color, letter, symbol };
    refreshAll();
  }

  function renderStrip() {
    stripEl.innerHTML = '';
    slots.forEach((piece, index) => {
      const slot = document.createElement('div');
      slot.className = 'cz-slot ' + (piece ? 'cz-slot-filled' : 'cz-slot-empty');
      slot.dataset.index = index + 1;
      slot.setAttribute('role', 'group');
      slot.setAttribute('aria-label', piece ? `Vị trí ${index + 1}: ${piece.name}` : `Vị trí ${index + 1}: còn trống`);

      if (piece) {
        if (piece.image) {
          slot.style.background = piece.color || pieceColor(piece.name);
          const image = document.createElement('img');
          image.className = 'cz-slot-image';
          image.alt = '';
          image.src = piece.image;
          const fallbackLabel = document.createElement('span');
          fallbackLabel.className = 'cz-slot-fallback-label';
          fallbackLabel.textContent = piece.symbol || piece.letter || piece.name.charAt(0);
          fallbackLabel.hidden = true;
          image.addEventListener('error', () => {
            image.hidden = true;
            fallbackLabel.hidden = false;
          }, { once: true });
          slot.append(image, fallbackLabel);
        } else {
          slot.style.background = piece.color || pieceColor(piece.name);
          slot.textContent = piece.symbol || piece.letter || piece.name.charAt(0);
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
      const isBase = el.dataset.category === 'base';
      el.classList.toggle('is-full', isFull && !isBase);
      el.setAttribute('aria-disabled', String(isFull && !isBase));
    });
  }

  function renderLibrary() {
    libraryEl.innerHTML = '';
    if (!LIBRARY_ITEMS.length) {
      const empty = document.createElement('p');
      empty.className = 'cz-price-empty';
      empty.textContent = !hasDesignType
        ? 'Chọn danh mục thiết kế để bắt đầu.'
        : !shopId ? 'Chọn shop để xem dây nền và vật liệu.'
          : 'Chọn mẫu nền để tải thư viện charm của shop.';
      libraryEl.appendChild(empty);
      return;
    }
    LIBRARY_ITEMS.forEach(item => {
      const el = document.createElement('div');
      el.className = 'cz-lib-strip-item';
      el.dataset.category = item.category || 'charm';
      el.draggable = item.category !== 'base';
      el.tabIndex = 0;
      el.setAttribute('role', 'button');
      el.setAttribute('aria-label', `Thêm ${item.name}, ${formatVND(item.price)}`);

      const photo = document.createElement('div');
      photo.className = 'cz-lib-strip-photo';
      if (item.image) {
        const image = document.createElement('img');
        image.alt = '';
        image.loading = 'lazy';
        image.src = item.image;
        image.addEventListener('error', () => image.remove(), { once: true });
        photo.appendChild(image);
      } else {
        photo.textContent = item.symbol || item.letter || '✧';
        photo.classList.add('cz-lib-strip-symbol');
      }

      const name = document.createElement('span');
      name.className = 'cz-lib-strip-name';
      name.textContent = shortenName(item.name, item.category === 'base' ? 22 : 18);
      name.title = item.name;

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
        if (item.letter) e.dataTransfer.setData('text/letter', item.letter);
        if (item.symbol) e.dataTransfer.setData('text/symbol', item.symbol);
      });

      const addItem = () => {
        if (item.category === 'base') {
          const source = customizerSources.find(entry => String(entry.id) === String(item.baseId));
          if (source) {
            sourceProductSelect.value = String(source.id);
            applySource(source);
          }
          return;
        }
        const emptyIndex = slots.findIndex(s => s === null);
        if (emptyIndex === -1) {
          setFeedback('Thiết kế đã đủ vị trí. Hãy xóa một vật liệu hoặc tăng số vị trí để thêm vật liệu khác.', 'error');
          return;
        }
        slots[emptyIndex] = { name: item.name, price: item.price, image: item.image || null, color: item.color || null, letter: item.letter || null, symbol: item.symbol || null };
        setFeedback(`${item.name} đã được thêm vào vị trí ${emptyIndex + 1}.`, 'success');
        refreshAll();
      };
      el.addEventListener('click', addItem);
      el.addEventListener('keydown', event => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault();
          addItem();
        }
      });

      libraryEl.appendChild(el);
    });
  }

  function sourceType(source) {
    if (productCustomizationMode && String(source.id) === String(productId)) return type;
    if (TYPE_CONFIG[source.baseType]) return source.baseType;
    if (source.categoryId === 3 && /vòng|vong|bracelet/i.test(source.name || '')) return 'bracelet';
    if (source.categoryId === 2) return 'necklace';
    if (source.categoryId === 3) return 'charm';
    return 'bracelet';
  }

  function refreshSourceOptions() {
    if (!sourceProductSelect || !customizerSources.length) return;
    const currentId = sourceProductSelect.value;
    const matches = customizerSources.filter(source =>
      hasDesignType && shopId && String(source.shopId) === String(shopId) && sourceType(source) === type);
    sourceProductSelect.innerHTML = '<option value="">Chọn dây/mẫu nền</option>';
    matches.forEach(source => {
      const option = document.createElement('option');
      option.value = source.id;
      option.dataset.baseName = `${source.categoryName}: ${source.name}`;
      option.dataset.basePrice = source.price;
      option.dataset.baseImage = source.baseImage || '';
      option.dataset.productName = source.name;
      option.dataset.shopName = source.shopName;
      option.dataset.shopId = source.shopId;
      option.dataset.baseType = source.baseType || '';
      option.textContent = `${shortenName(source.name, 24)} · ${formatVND(Number(source.price) || 0)}`;
      option.title = `${source.name} · ${formatVND(Number(source.price) || 0)}`;
      sourceProductSelect.appendChild(option);
    });
    if (matches.some(source => String(source.id) === String(currentId))) sourceProductSelect.value = currentId;
    else sourceProductSelect.value = '';
    if (!hasDesignType) sourceHelp.textContent = 'Chọn danh mục thiết kế trước.';
    else if (!shopId) sourceHelp.textContent = 'Chọn shop để xem mẫu nền có sẵn.';
    else if (!matches.length) sourceHelp.textContent = `Shop chưa có mẫu nền thuộc danh mục ${cfg.label}.`;
  }

  function refreshBaseChoices() {
    LIBRARY_ITEMS = productCustomizationMode ? [] : customizerSources.filter(source =>
      hasDesignType && shopId && String(source.shopId) === String(shopId) && sourceType(source) === type
    ).map(source => ({
      baseId: source.id,
      name: `${source.categoryName}: ${source.name}`,
      price: Number(source.price) || 0,
      image: source.baseImage || '',
      category: 'base'
    }));
  }

  function applySource(source) {
    if (source && selectedSource && String(source.id) === String(selectedSource.id)) {
      setFeedback(`Đang dùng vật liệu từ ${source.name}.`, 'success');
      return;
    }
    const sourceChanged = String(source?.id || '') !== String(selectedSource?.id || '');
    if (sourceChanged && filledCount() && !window.confirm('Đổi mẫu vật liệu sẽ xóa các thành phần đang xếp. Bạn có muốn tiếp tục không?')) {
      sourceProductSelect.value = selectedSource ? String(selectedSource.id) : '';
      return;
    }

    selectedSource = source;
    slots = new Array(totalSlots).fill(null);
    currentDesignId = '';
    if (draftSelect) draftSelect.value = '';
    if (!source) {
      if (!productCustomizationMode) product = null;
      baseMaterial = null;
      refreshBaseChoices();
      basePreview.hidden = true;
      document.getElementById('czShopInfo').textContent = shopId
        ? `Gian hàng: ${shopName}. Chọn mẫu nền để tải thư viện charm.`
        : 'Chọn shop, sau đó chọn mẫu nền và vật liệu thiết kế.';
      if (!productCustomizationMode) document.getElementById('czHeading').textContent = cfg.heading;
      renderLibrary();
      refreshAll();
      return;
    }

    product = { id: source.id, name: source.name, shop: source.shopName };
    shopName = source.shopName;
    shopId = String(source.shopId);
    if (!productCustomizationMode && shopSelect) shopSelect.value = shopId;
    type = sourceType(source);
    cfg = TYPE_CONFIG[type];
    if (typeSelect) typeSelect.value = type;
    totalSlots = configureSizeSelect(cfg.slotCount) || cfg.slotCount;
    slots = new Array(totalSlots).fill(null);
    baseMaterial = readBaseMaterial();
    updateBaseMaterialPreview();
    refreshBaseChoices();
    (source.charms || []).forEach(charm => LIBRARY_ITEMS.push({
      id: charm.id,
      name: charm.name,
      price: Number(charm.price) || 0,
      image: charm.image || '',
      category: 'charm'
    }));

    const detailHref = `${context}/pages/product-detail?id=${encodeURIComponent(source.id)}`;
    document.getElementById('czBackLink').href = detailHref;
    document.getElementById('czBackLink').textContent = `← Quay lại sản phẩm của ${shopName}`;
    document.getElementById('czCancelLink').href = detailHref;
    document.getElementById('czCancelLink').textContent = `Quay lại sản phẩm của ${shopName}`;
    document.getElementById('czTag').textContent = productCustomizationMode
      ? `Tùy chỉnh charm · ${shopName}`
      : `Thiết kế theo mẫu của ${shopName}`;
    document.getElementById('czHeading').textContent = productCustomizationMode
      ? `Tùy chỉnh charm · ${source.name}`
      : `Thiết kế ${cfg.label}`;
    document.getElementById('czShopInfo').textContent = productCustomizationMode
      ? `Mẫu dây và giá nền giữ nguyên theo sản phẩm “${source.name}” của ${shopName}. Bạn chỉ thay đổi charm.`
      : `Vật liệu lấy từ sản phẩm “${source.name}” · Gian hàng: ${shopName}. Giá nền và charm lấy theo thiết lập của shop.`;
    if (sourceHelp) {
      sourceHelp.textContent = 'Dây nền và charm lấy từ mẫu đã chọn.';
      sourceHelp.title = `${source.name} · ${shopName}`;
    }
    renderLibrary();
    refreshAll();
    setFeedback(`Đã tải dây nền và vật liệu từ sản phẩm của ${shopName}.`, 'success');
  }

  async function loadCustomizerSources() {
    sourceProductSelect.innerHTML = '<option value="">Đang tải mẫu của shop...</option>';
    try {
      const sourceJson = document.getElementById('czInitialSources')?.textContent || '{"sources":[]}';
      const result = JSON.parse(sourceJson);
      customizerSources = Array.isArray(result.sources) ? result.sources : [];
      const shops = [...new Map(customizerSources.map(source => [String(source.shopId), source])).values()];
      shopSelect.innerHTML = '<option value="">Chọn shop</option>';
      shops.forEach(source => {
        const option = document.createElement('option');
        option.value = source.shopId;
        option.textContent = source.shopName;
        shopSelect.appendChild(option);
      });
      if (shopId && shops.some(source => String(source.shopId) === String(shopId))) shopSelect.value = String(shopId);
      refreshSourceOptions();

      if (!customizerSources.length) {
        sourceProductSelect.innerHTML = '<option value="">Chưa có mẫu thiết kế</option>';
        sourceHelp.textContent = 'Hiện chưa có sản phẩm đang bán để dùng làm mẫu thiết kế.';
        applySource(null);
        return;
      }

      const initialSource = productCustomizationMode
        ? customizerSources.find(source => String(source.id) === String(productId)) : null;
      if (initialSource) {
        shopId = String(initialSource.shopId);
        if (shopSelect) shopSelect.value = shopId;
        refreshSourceOptions();
        sourceProductSelect.value = String(initialSource.id);
        applySource(initialSource);
      } else if (productCustomizationMode) {
        LIBRARY_ITEMS = [];
        renderLibrary();
        setFeedback('Sản phẩm này không có cấu hình tùy chỉnh charm của shop.', 'error');
      } else {
        refreshBaseChoices();
        renderLibrary();
        sourceHelp.textContent = !hasDesignType
          ? 'Chọn danh mục thiết kế trước.'
          : !shopId ? 'Chọn shop để xem dây nền phù hợp.'
            : 'Chọn mẫu nền để tải danh sách charm của shop.';
      }
    } catch (error) {
      sourceProductSelect.innerHTML = '<option value="">Không tải được vật liệu</option>';
      sourceHelp.textContent = error.message || 'Không tải được vật liệu từ shop.';
      setFeedback(error.message || 'Không tải được vật liệu từ shop.', 'error');
    }
  }

  renderLibrary();
  document.querySelectorAll('[data-material-filter]').forEach(button => {
    button.addEventListener('click', () => {
      const category = button.dataset.materialFilter;
      document.querySelectorAll('[data-material-filter]').forEach(filter => {
        const active = filter === button;
        filter.classList.toggle('is-active', active);
        filter.setAttribute('aria-pressed', String(active));
      });
      libraryEl.querySelectorAll('.cz-lib-strip-item').forEach(item => {
        item.hidden = category !== 'all' && item.dataset.category !== category;
      });
    });
  });
  refreshAll();

  const sendShopBtn = document.getElementById('czSendShopBtn');
  const saveDesignBtn = document.getElementById('czSaveDesignBtn');
  const addToCartBtn = document.getElementById('czAddToCartBtn');
  function designPayload() {
    return { id: currentDesignId, name: product ? `Custom · ${cfg.label} · ${product.name}` : `Thiết kế ${cfg.label}`,
      orderType: productCustomizationMode ? "product_customization" : "shop_design",
      designType: type,
      productType: product ? `Custom · ${cfg.label} · ${product.name}` : `Custom · ${cfg.label}`,
      sellerName: shopName, shopId, baseMaterial: baseMaterial ? { ...baseMaterial } : null,
      slots: slots.map(item => item ? {...item} : null), size: totalSlots,
      price: (baseMaterial?.price || 0) + slots.reduce((sum, item) => sum + (item ? Number(item.price) || 0 : 0), 0) };
  }
  function persistDesignInCart() {
    const saved = window.AuraCraftCustom.saveCustomDesign(designPayload());
    currentDesignId = saved.id;
    refreshSavedDesigns(saved.id);
    let cart = [];
    try { cart = JSON.parse(localStorage.getItem('AuraCraftCart') || '[]'); } catch { cart = []; }
    if (!Array.isArray(cart)) cart = [];
    const id = `custom:${saved.id}`;
    const snapshot = JSON.stringify({
      baseMaterial: saved.baseMaterial,
      slots: saved.slots,
      designType: saved.designType,
      size: saved.size,
      shopId: saved.shopId
    });
    const previous = cart.find(item => String(item.id) === id);
    const cartItem = {
      id,
      customDesignId: saved.id,
      name: saved.baseMaterial?.sourceProductName
        ? `Thiết kế ${cfg.label} · ${saved.baseMaterial.sourceProductName}`
        : `Thiết kế ${cfg.label}`,
      price: saved.price,
      image: saved.baseMaterial?.image || saved.slots.find(Boolean)?.image || '',
      categoryName: `Thiết kế riêng · ${saved.size} vị trí`,
      shopName: saved.sellerName || shopName,
      selected: true,
      quantity: 1,
      stock: 1,
      designSnapshot: snapshot,
      pendingOrderId: previous?.designSnapshot === snapshot ? previous.pendingOrderId || '' : ''
    };
    cart = cart.filter(item => String(item.id) !== id);
    cart.push(cartItem);
    localStorage.setItem('AuraCraftCart', JSON.stringify(cart));
    window.dispatchEvent(new Event('auracraft:cart-updated'));
    return { saved, cartItem };
  }
  function refreshSavedDesigns(selectedId) {
    if (!draftSelect || !window.AuraCraftCustom) return;
    const designs = window.AuraCraftCustom.listCustomDesigns();
    draftSelect.innerHTML = '<option value="">Chọn thiết kế để tải</option>';
    designs.forEach(design => {
      const option = document.createElement('option'); option.value = design.id;
      const fullName = design.name || 'Thiết kế của tôi';
      const nameLimit = 30;
      const shortName = Array.from(fullName).length > nameLimit
        ? `${Array.from(fullName).slice(0, nameLimit - 1).join('')}…`
        : fullName;
      option.textContent = `${shortName} · ${formatVND(design.price)}`;
      option.title = `${fullName} · ${formatVND(design.price)}`;
      draftSelect.appendChild(option);
    });
    draftSelect.value = selectedId || '';
  }
  function loadDesign(saved) {
    if (!saved || productCustomizationMode) return;
    const savedSourceId = saved.baseMaterial?.sourceProductId
      || (/^\d+$/.test(String(saved.baseMaterial?.id || '')) ? saved.baseMaterial.id : '');
    const savedName = saved.baseMaterial?.sourceProductName
      || String(saved.name || '').split(' · ').pop()?.trim();
    const savedSource = customizerSources.find(source => String(source.id) === String(savedSourceId))
      || customizerSources.find(source => source.name === savedName
        && (!saved.shopId || String(source.shopId) === String(saved.shopId)));
    type = TYPE_CONFIG[saved.designType]
      ? saved.designType
      : (savedSource ? sourceType(savedSource) : type);
    hasDesignType = true;
    sizeSelect.disabled = false;
    cfg = TYPE_CONFIG[type];
    if (typeSelect) typeSelect.value = type;
    configureSizeSelect(Number(saved.size) || cfg.slotCount);
    selectedSource = null;
    sourceProductSelect.value = '';
    slots = new Array(totalSlots).fill(null);
    if (savedSource) {
      shopId = String(savedSource.shopId);
      shopName = savedSource.shopName;
      if (shopSelect) shopSelect.value = shopId;
      refreshSourceOptions();
      sourceProductSelect.value = String(savedSource.id);
      applySource(savedSource);
    } else if (saved.baseMaterial) {
      applySource(null);
    }
    currentDesignId = saved.id;
    if (typeSelect) typeSelect.value = type;
    baseMaterial = savedSource ? (readBaseMaterial() || { ...saved.baseMaterial })
      : (saved.baseMaterial ? { ...saved.baseMaterial } : null);
    if (!savedSource && saved.baseMaterial) {
      product = saved.baseMaterial.sourceProductName
        ? { id: saved.baseMaterial.sourceProductId, name: saved.baseMaterial.sourceProductName, shop: saved.baseMaterial.shopName }
        : null;
      shopName = saved.baseMaterial.shopName || saved.sellerName || shopName;
      shopId = String(saved.baseMaterial.shopId || saved.shopId || shopId);
    }
    updateBaseMaterialPreview();
    totalSlots = configureSizeSelect(Number(saved.size) || cfg.slotCount) || cfg.slotCount;
    slots = Array.from({length: totalSlots}, (_, i) => saved.slots?.[i] || null);
    document.getElementById('czTag').textContent = `Thiết kế Custom · ${cfg.label}`;
    document.getElementById('czHeading').textContent = product ? product.name : cfg.heading;
    document.getElementById('czShopInfo').textContent = baseMaterial
      ? `Mẫu nền: ${baseMaterial.sourceProductName || baseMaterial.name} · Gian hàng: ${shopName}.`
      : `Gian hàng: ${shopName}.`;
    refreshBaseChoices();
    if (savedSource) (savedSource.charms || []).forEach(charm => {
      if (!LIBRARY_ITEMS.some(item => item.category === 'charm' && String(item.id) === String(charm.id))) {
        LIBRARY_ITEMS.push({ id: charm.id, name: charm.name, price: Number(charm.price) || 0, image: charm.image || '', category: 'charm' });
      }
    });
    renderLibrary();
    refreshAll();
    setFeedback(`Đã tải thiết kế “${saved.name}”.`, 'success');
  }
  if (draftSelect) draftSelect.addEventListener('change', () => {
    const saved = window.AuraCraftCustom.listCustomDesigns().find(item => item.id === draftSelect.value);
    loadDesign(saved);
  });
  if (saveDesignBtn) saveDesignBtn.addEventListener('click', () => {
    if (!filledCount() && !baseMaterial) {
      const message = 'Hãy chọn dây nền, hạt hoặc charm trước khi lưu thiết kế.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
      return;
    }
    try {
      const saved = window.AuraCraftCustom.saveCustomDesign(designPayload());
      currentDesignId = saved.id; refreshSavedDesigns(saved.id);
      const message = 'Đã lưu thiết kế thành công. Bạn có thể tải lại trong mục “Thiết kế đã lưu”.';
      setFeedback(message, 'success');
      showNotice(message, 'success');
    } catch (error) {
      const message = error.message || 'Không thể lưu thiết kế.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
    }
  });

  if (addToCartBtn) addToCartBtn.addEventListener('click', () => {
    if (!filledCount() && !baseMaterial) {
      const message = 'Hãy chọn dây nền, hạt hoặc charm trước khi thêm vào giỏ.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
      return;
    }
    try {
      persistDesignInCart();
      const message = 'Đã thêm thiết kế vào giỏ hàng.';
      setFeedback(message, 'success');
      showNotice(message, 'success');
    } catch (error) {
      const message = error.message || 'Không thể thêm thiết kế vào giỏ hàng.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
    }
  });

  sendShopBtn.addEventListener('click', () => {
    if (filledCount() === 0 && !baseMaterial) {
      const message = 'Hãy chọn dây nền, hạt hoặc charm trước khi tiếp tục thanh toán.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
      return;
    }
    try {
      const { saved, cartItem } = persistDesignInCart();
      let order = cartItem.pendingOrderId && window.AuraCraftCustom.getOrder(cartItem.pendingOrderId);
      if (!order || order.paymentStatus === 'paid') {
        order = window.AuraCraftCustom.createDesignerOrder({ ...saved, customDesignId: saved.id });
        cartItem.pendingOrderId = order.id;
        let cart = [];
        try { cart = JSON.parse(localStorage.getItem('AuraCraftCart') || '[]'); } catch { cart = []; }
        localStorage.setItem('AuraCraftCart', JSON.stringify((Array.isArray(cart) ? cart : []).map(item =>
          String(item.id) === String(cartItem.id) ? cartItem : item)));
        window.dispatchEvent(new Event('auracraft:cart-updated'));
      }
      window.location.href = `${context}/pages/checkout?orderId=${encodeURIComponent(order.id)}`;
    } catch (error) {
      const message = error.message || 'Không thể tạo đơn thiết kế.';
      setFeedback(message, 'error');
      showNotice(message, 'error');
    }
  });

  loadCustomizerSources().then(() => {
    refreshSavedDesigns(currentDesignId);
    if (currentDesignId && window.AuraCraftCustom) {
      loadDesign(window.AuraCraftCustom.listCustomDesigns().find(item => item.id === currentDesignId));
    }
  });
});
