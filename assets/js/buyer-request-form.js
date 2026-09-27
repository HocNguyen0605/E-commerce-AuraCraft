
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
        item.innerHTML = `<img src="${url}" alt="${file.name}"><button type="button" class="rf-preview-remove" aria-label="Xóa ảnh">×</button>`;
        item.querySelector('button').addEventListener('click', () => {
          files.splice(index, 1);
          renderPreview();
        });
        preview.appendChild(item);
      });
    }
  }

  setupImageUpload('charmImages', 'charmImagesPreview');
  setupImageUpload('designImages', 'designImagesPreview');

  document.getElementById('requestForm').addEventListener('submit', e => {
    e.preventDefault();
    window.location.href = 'choose-seller.html';
  });
});