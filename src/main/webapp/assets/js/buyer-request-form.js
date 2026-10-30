
document.addEventListener('DOMContentLoaded', () => {
  const page = document.getElementById('requestPage');
  const context = page.dataset.context || '';
  const params = new URLSearchParams(window.location.search);
  const savedDesignId = params.get('designId');
  const typeKey = params.get('type');
  const form = document.getElementById('requestForm');
  const authNotice = document.getElementById('requestAuthNotice');
  const loginButton = document.getElementById('requestLoginButton');
  const submitButton = form.querySelector('[type="submit"]');
  const success = document.getElementById('requestSuccess');
  let selectedDesign = null;

  if (typeKey && window.AURA_TYPE_LABELS && window.AURA_TYPE_LABELS[typeKey]) {
    document.getElementById('productType').value = window.AURA_TYPE_LABELS[typeKey];
  }

  function setupImageUpload(inputId, previewId) {
    const input = document.getElementById(inputId);
    const preview = document.getElementById(previewId);
    let files = [];
    input.addEventListener('change', () => {
      files = files.concat(Array.from(input.files));
      renderPreview();
      input.value = '';
    });
    function renderPreview() {
      preview.replaceChildren();
      files.forEach((file, index) => {
        const item = document.createElement('div');
        item.className = 'rf-preview-item';
        const image = document.createElement('img');
        const remove = document.createElement('button');
        image.src = URL.createObjectURL(file);
        image.alt = file.name;
        remove.type = 'button';
        remove.className = 'rf-preview-remove';
        remove.setAttribute('aria-label', 'Xóa ảnh');
        remove.textContent = '×';
        remove.addEventListener('click', () => { files.splice(index, 1); renderPreview(); });
        item.append(image, remove);
        preview.appendChild(item);
      });
    }
    return async () => Promise.all(files.map(file => new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(new Error(`Không đọc được ảnh ${file.name}.`));
      reader.readAsDataURL(file);
    })));
  }

  const getCharmImages = setupImageUpload('charmImages', 'charmImagesPreview');
  const getDesignImages = setupImageUpload('designImages', 'designImagesPreview');

  function setValue(id, value) {
    const input = document.getElementById(id);
    if (value && !input.value) input.value = value;
  }

  function readImageLinks(id) {
    return document.getElementById(id).value.split(/[\n,]+/).map(value => value.trim()).filter(Boolean);
  }

  const returnToForm = window.location.pathname + window.location.search;
  loginButton.addEventListener('click', () => {
    try { sessionStorage.setItem('AuraCraftAfterLogin', returnToForm); } catch (_) { }
  });

  fetch(context + '/api/buyer/custom-requests', { credentials: 'same-origin', cache: 'no-store' })
    .then(async response => {
      const text = await response.text();
      let profile = null;
      try { profile = JSON.parse(text); } catch (_) { }
      if (response.status === 401) {
        authNotice.hidden = false;
        form.hidden = true;
        return;
      }
      if (!response.ok || !profile?.ok) {
        authNotice.querySelector('h2').textContent = 'Chưa thể kiểm tra đăng nhập';
        authNotice.querySelector('p').textContent = 'Máy chủ chưa nhận route xác thực yêu cầu thiết kế. Hãy build và deploy phiên bản mới của ứng dụng rồi tải lại trang.';
        loginButton.hidden = true;
        authNotice.hidden = false;
        form.hidden = true;
        return;
      }
      setValue('buyerName', profile.name);
      setValue('buyerEmail', profile.email);
      authNotice.hidden = true;
      form.hidden = false;
    })
    .catch(() => {
      authNotice.querySelector('h2').textContent = 'Chưa thể kiểm tra đăng nhập';
      authNotice.querySelector('p').textContent = 'Không kết nối được với máy chủ. Vui lòng tải lại trang sau khi máy chủ hoạt động.';
      loginButton.hidden = true;
      authNotice.hidden = false;
      form.closest('.rf-layout').hidden = true;
    });

  if (savedDesignId && window.AuraCraftCustom) {
    selectedDesign = window.AuraCraftCustom.listCustomDesigns()
      .find(item => String(item.id) === String(savedDesignId)) || null;
    if (selectedDesign) {
      const pieces = (selectedDesign.slots || []).filter(Boolean);
      const base = selectedDesign.baseMaterial;
      const materials = [base && (base.sourceProductName || base.name), ...pieces.map(item => item.name)].filter(Boolean);
      const description = pieces.map((item, index) => `Vị trí ${index + 1}: ${item.name}`).join('\n')
        || (base ? `Mẫu nền: ${base.sourceProductName || base.name}` : selectedDesign.name);
      document.querySelector('.rf-hero h1').textContent = 'Gửi thiết kế vào chợ báo giá';
      document.querySelector('.rf-hero .rf-sub').textContent = 'Thông tin thiết kế đã lưu được điền sẵn. Bạn có thể chỉnh vật liệu, mô tả và giá mong muốn trước khi gửi cho các thợ thủ công.';
      document.getElementById('productType').value = window.AURA_TYPE_LABELS?.[selectedDesign.designType]
        || selectedDesign.designType || selectedDesign.productType || selectedDesign.name || '';
      document.getElementById('materials').value = materials.join(', ') || 'Theo bản thiết kế đính kèm';
      document.getElementById('charmDesc').value = `Thiết kế gửi từ AuraCraft: ${selectedDesign.name || selectedDesign.productType || 'Thiết kế của tôi'}\n${description}`;
      document.getElementById('budget').value = String(Number(selectedDesign.price) || 0);
      const summary = document.createElement('div');
      summary.className = 'rf-side card';
      summary.style.marginBottom = '18px';
      const heading = document.createElement('strong');
      heading.textContent = 'Thiết kế được gửi';
      const detail = document.createElement('p');
      detail.textContent = `${selectedDesign.name || selectedDesign.productType} · ${materials.length} vật liệu`;
      summary.append(heading);
      const previewImage = base && (base.image || base.imageUrl);
      if (previewImage) {
        const image = document.createElement('img');
        image.alt = 'Ảnh mẫu nền trong thiết kế';
        image.src = previewImage.startsWith('/') ? context + previewImage : previewImage;
        image.style.cssText = 'display:block;width:100%;max-width:220px;height:140px;object-fit:contain;margin:10px 0;border-radius:10px;background:#fff';
        summary.append(image);
      }
      summary.append(detail);
      form.prepend(summary);
    } else {
      const notice = document.createElement('p');
      notice.className = 'rf-note';
      notice.textContent = 'Không tìm thấy bản thiết kế đã lưu này. Bạn vẫn có thể điền yêu cầu mới bên dưới.';
      form.prepend(notice);
    }
  }

  form.addEventListener('submit', async event => {
    event.preventDefault();
    submitButton.disabled = true;
    submitButton.textContent = 'Đang gửi yêu cầu…';
    try {
      const payload = {
        productType: document.getElementById('productType').value,
        materials: document.getElementById('materials').value,
        charmDescription: document.getElementById('charmDesc').value,
        budget: Number(document.getElementById('budget').value.replace(/[^\d]/g, '')),
        design: selectedDesign,
        previewImage: selectedDesign?.baseMaterial?.image || '',
        charmImages: await getCharmImages(),
        designImages: await getDesignImages(),
        charmImageUrls: readImageLinks('charmImageUrls'),
        designImageUrls: readImageLinks('designImageUrls')
      };
      const response = await fetch(context + '/api/buyer/custom-requests', {
        method: 'POST', credentials: 'same-origin',
        headers: { 'Content-Type': 'application/json;charset=UTF-8', Accept: 'application/json' },
        body: JSON.stringify(payload)
      });
      const responseText = await response.text();
      let result;
      try {
        result = JSON.parse(responseText);
      } catch (_) {
        const contentType = response.headers.get('content-type') || '';
        if (contentType.includes('text/html') || /^\s*<!doctype|^\s*<html/i.test(responseText)) {
          throw new Error(`Máy chủ trả về trang HTML thay vì JSON (HTTP ${response.status}). Hãy kiểm tra ứng dụng đã được build/deploy phiên bản mới và route ${context}/api/buyer/custom-requests đã hoạt động.`);
        }
        throw new Error('Máy chủ trả về dữ liệu không hợp lệ. Vui lòng thử lại.');
      }
      if (!response.ok || !result.ok) {
        if (response.status === 401) {
          throw new Error('Vui lòng đăng nhập bằng tài khoản người mua rồi gửi lại yêu cầu.');
        }
        throw new Error(result.message || 'Không thể gửi yêu cầu.');
      }
      form.closest('.rf-layout').hidden = true;
      success.hidden = false;
      page.classList.add('rf-request-success');
      success.innerHTML = '';
      const heading = document.createElement('h2');
      heading.textContent = 'Đã gửi yêu cầu vào chợ báo giá';
      const message = document.createElement('p');
      message.textContent = `Mã yêu cầu #${result.requestId}. Các thợ phù hợp có thể gửi báo giá cho thiết kế này.`;
      const back = document.createElement('a');
      back.className = 'btn btn-primary';
      back.href = context + '/index.jsp';
      back.textContent = 'Về trang chủ';
      const quotes = document.createElement('a');
      quotes.className = 'btn btn-outline';
      quotes.href = context + '/pages/profile-quotes?requestId=' + encodeURIComponent(result.requestId)
        + '#quote-request-' + encodeURIComponent(result.requestId);
      quotes.textContent = 'Xem báo giá của tôi';
      const actions = document.createElement('div');
      actions.className = 'rf-success-actions';
      actions.append(quotes, back);
      success.append(heading, message, actions);
    } catch (error) {
      alert(error.message || 'Không thể gửi yêu cầu.');
    } finally {
      if (!form.hidden) {
        submitButton.disabled = false;
        submitButton.textContent = 'Gửi yêu cầu & xem báo giá';
      }
    }
  });
});
