
document.addEventListener('DOMContentLoaded', () => {

  const params = new URLSearchParams(window.location.search);
  const typeKey = params.get('type');
  if (typeKey && AURA_TYPE_LABELS && AURA_TYPE_LABELS[typeKey]) {
    document.getElementById('productType').value = AURA_TYPE_LABELS[typeKey];
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
      preview.innerHTML = '';
      files.forEach((file, index) => {
        const url = URL.createObjectURL(file);
        const item = document.createElement('div');
        item.className = 'rf-preview-item';
        const image = document.createElement('img');
        const removeButton = document.createElement('button');
        image.src = url;
        image.alt = file.name;
        removeButton.type = 'button';
        removeButton.className = 'rf-preview-remove';
        removeButton.setAttribute('aria-label', 'Xóa ảnh');
        removeButton.textContent = '×';
        removeButton.addEventListener('click', () => {
          files.splice(index, 1);
          renderPreview();
        });
        item.append(image, removeButton);
        preview.appendChild(item);
      });
    }

    return async () => Promise.all(files.map((file) => new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.onload = () => resolve(reader.result);
      reader.onerror = () => reject(new Error(`Không đọc được ảnh ${file.name}.`));
      reader.readAsDataURL(file);
    })));
  }

  const getCharmImages = setupImageUpload('charmImages', 'charmImagesPreview');
  const getDesignImages = setupImageUpload('designImages', 'designImagesPreview');

  document.getElementById('requestForm').addEventListener('submit', async e => {
    e.preventDefault();
    const submitButton = e.currentTarget.querySelector('[type="submit"]');
    submitButton.disabled = true;
    try {
      const request = window.AuraCraftCustom.createRequest({
        buyerName: document.getElementById('buyerName').value,
        buyerEmail: document.getElementById('buyerEmail').value,
        productType: document.getElementById('productType').value,
        materials: document.getElementById('materials').value,
        charmDescription: document.getElementById('charmDesc').value,
        charmImages: await getCharmImages(),
        designImages: await getDesignImages(),
        budget: document.getElementById('budget').value.replace(/[^\d.]/g, '')
      });
      window.location.href = `buyer-requests.html?mine=${encodeURIComponent(request.id)}`;
    } catch (error) {
      alert(error.message || 'Không thể lưu yêu cầu.');
      submitButton.disabled = false;
    }
  });
});