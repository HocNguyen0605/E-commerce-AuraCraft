document.addEventListener('DOMContentLoaded', () => {
  const formatVND = n => n.toLocaleString('vi-VN') + ' ₫';
  const params = new URLSearchParams(window.location.search);
  const id = parseInt(params.get('id'), 10) || AURA_PRODUCTS[0].id;
  const product = AURA_PRODUCTS.find(p => p.id === id) || AURA_PRODUCTS[0];

  function shade(hex, percent) {
    const n = parseInt(hex.slice(1), 16);
    let r = (n >> 16) + percent, g = ((n >> 8) & 0xff) + percent, b = (n & 0xff) + percent;
    r = Math.max(0, Math.min(255, r)); g = Math.max(0, Math.min(255, g)); b = Math.max(0, Math.min(255, b));
    return `#${(1 << 24 | r << 16 | g << 8 | b).toString(16).slice(1)}`;
  }
  const thumbShades = [0, -25, 25, -45];

  function boxBackground(index) {
    return product.image
      ? `url('${product.image}') center/cover no-repeat`
      : `linear-gradient(160deg, ${shade(product.color, thumbShades[index])}, #fff)`;
  }

  // Breadcrumb + main gallery
  document.getElementById('pdBreadType').textContent = product.typeLabel;
  document.getElementById('pdBreadName').textContent = product.name;
  const gallery = document.getElementById('pdGalleryMain');
  const thumbsEl = document.getElementById('pdThumbs');

  const realPhotos = (Array.isArray(product.images) && product.images.length) ? product.images : null;
  const thumbCount = realPhotos ? realPhotos.length : 4;

  function galleryBackground(index) {
    return realPhotos ? `url('${realPhotos[index]}') center/cover no-repeat` : boxBackground(index);
  }
  function thumbBackground(index) {
    return realPhotos ? `url('${realPhotos[index]}') center/cover no-repeat` : (product.image ? `url('${product.image}') center/cover no-repeat` : shade(product.color, thumbShades[index]));
  }

  function setGallery(index) {
    gallery.style.background = galleryBackground(index);
    if (realPhotos || product.image) {
      gallery.textContent = '';
      gallery.style.fontSize = '';
    } else {
      gallery.textContent = product.emoji;
      gallery.style.fontSize = '3.2rem';
    }
  }
  setGallery(0);
  thumbsEl.innerHTML = '';
  for (let index = 0; index < thumbCount; index++) {
    const thumb = document.createElement('button');
    thumb.type = 'button';
    thumb.className = 'pd-thumb' + (index === 0 ? ' is-active' : ''); 
    thumb.dataset.index = index;
    thumb.style.background = thumbBackground(index);
    thumb.addEventListener('click', () => {
      thumbsEl.querySelectorAll('.pd-thumb').forEach(t => t.classList.remove('is-active'));
      thumb.classList.add('is-active');
      setGallery(index);
    });
    thumbsEl.appendChild(thumb);
  }

  document.getElementById('pdTag').textContent = product.typeLabel + ' handmade';
  document.getElementById('pdTitle').textContent = product.name;
  document.getElementById('pdRating').innerHTML =
    `★★★★★ <span>${product.rating} (${product.reviews} đánh giá) · Đã bán ${product.sold}</span>`;
  document.getElementById('pdPrice').textContent = formatVND(product.price);
  document.title = product.name + ' — AuraCraft';
  document.getElementById('pdDesc').textContent = product.desc;
  document.getElementById('pdDescLong').textContent = product.desc;

  const colorSwatches = document.querySelectorAll('#pdColorList .pd-color-swatch');
  const colorSelectedEl = document.getElementById('pdColorSelected');
  colorSwatches.forEach(swatch => {
    swatch.addEventListener('click', () => {
      colorSwatches.forEach(s => s.classList.remove('is-active'));
      swatch.classList.add('is-active');
      colorSelectedEl.textContent = `Đã chọn: ${swatch.dataset.color}`;
    });
  });

  const sizeButtons = document.querySelectorAll('#pdSizeList .pd-size-btn');
  sizeButtons.forEach(btn => {
    btn.addEventListener('click', () => {
      sizeButtons.forEach(b => b.classList.remove('is-active'));
      btn.classList.add('is-active');
    });
  });

  document.getElementById('pdCustomCta').href = `customizer.html?type=${product.type}&id=${product.id}`;
  document.getElementById('pdCustomCtaTitle').textContent = `Custom ${product.typeLabel.toLowerCase()} này`;

  document.getElementById('pdShopAvatar').textContent = product.shop.charAt(0);
  document.getElementById('pdShopName').textContent = product.shop;
  document.getElementById('pdShopMeta').textContent = `★ ${product.rating} · Đã bán ${product.sold}`;

  function thumbBox(p) {
    return p.image
      ? { background: `url('${p.image}') center/cover no-repeat`, content: '' }
      : { background: p.color, content: p.emoji };
  }

  if (product.type === 'charm') {
    const grid = document.getElementById('pdCharmGalleryGrid');
    const charmGallery = document.getElementById('pdCharmGallery');
    const others = AURA_PRODUCTS.filter(p => p.type === 'charm' && p.shop === product.shop && p.id !== product.id);
    others.forEach(p => {
      const box = thumbBox(p);
      const item = document.createElement('article');
      item.className = 'charm-gallery-item';
      item.innerHTML = `
        <div class="charm-gallery-img" style="background:${box.background}">${box.content}</div>
        <div class="charm-gallery-body">
          <strong>${p.name}</strong>
          <span>${formatVND(p.price)}</span>
        </div>`;
      item.addEventListener('click', () => { window.location.href = `product-detail.html?id=${p.id}`; });
      grid.appendChild(item);
    });
    charmGallery.hidden = others.length === 0;
  }

  const MIN_SIMILAR = 4;
  const suggestSection = document.getElementById('pdShopSuggestions');
  const suggestGrid = document.getElementById('pdShopSuggestGrid');
  if (suggestSection && suggestGrid) {
    const sameType = AURA_PRODUCTS.filter(p => p.type === product.type && p.id !== product.id);
    let similarProducts = sameType.slice(0, 8);
    if (similarProducts.length < MIN_SIMILAR) {
      const usedIds = new Set(similarProducts.map(p => p.id).concat([product.id]));
      const fillers = AURA_PRODUCTS.filter(p => !usedIds.has(p.id));
      similarProducts = similarProducts.concat(fillers.slice(0, MIN_SIMILAR - similarProducts.length));
    }
    similarProducts.forEach(p => {
      const box = thumbBox(p);
      const item = document.createElement('article');
      item.className = 'shop-suggest-item';
      item.innerHTML = `
        <div class="shop-suggest-img" style="background:${box.background}">${box.content}</div>
        <div class="shop-suggest-body">
          <strong>${p.name}</strong>
          <span class="shop-suggest-price">${formatVND(p.price)}</span>
          <span class="shop-suggest-rating">★ ${p.rating} · Đã bán ${p.sold}</span>
        </div>`;
      item.addEventListener('click', () => { window.location.href = `product-detail.html?id=${p.id}`; });
      suggestGrid.appendChild(item);
    });
    suggestSection.hidden = similarProducts.length === 0;
  }

  const qty = document.getElementById('qty');
  document.getElementById('qtyMinus')?.addEventListener('click', () => {
    qty.value = Math.max(1, parseInt(qty.value || '1', 10) - 1);
  });
  document.getElementById('qtyPlus')?.addEventListener('click', () => {
    qty.value = parseInt(qty.value || '1', 10) + 1;
  });

  document.getElementById('pdAddToCartBtn')?.addEventListener('click', () => {
    alert(`Đã thêm ${qty.value} x ${product.name} vào giỏ hàng`);
  });

  document.getElementById('pdBuyNowBtn')?.addEventListener('click', () => {
    window.location.href = 'checkout.html';
  });
});