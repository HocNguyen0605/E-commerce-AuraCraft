
document.addEventListener('DOMContentLoaded', () => {
  const cards = document.querySelectorAll('.seller-card');
  const infoOverlay = document.getElementById('csInfoOverlay');
  const infoClose = document.getElementById('csInfoClose');
  const infoNameEl = document.getElementById('csInfoName');
  const infoExperienceEl = document.getElementById('csInfoExperience');
  const infoYearsEl = document.getElementById('csInfoYears');
  const infoSpecialtyEl = document.getElementById('csInfoSpecialty');

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

  cards.forEach(card => {
    const chooseBtn = card.querySelector('.quote-choose-btn');
    const chatBtn = card.querySelector('.quote-chat-btn');
    const nameBtn = card.querySelector('.seller-name-btn');
    const sellerName = card.querySelector('.seller-info h3').textContent;

    nameBtn.addEventListener('click', () => openSellerInfo(card, sellerName));

    chooseBtn.addEventListener('click', () => {
      const alreadyChosen = document.querySelector('.seller-card.selected');
      if (alreadyChosen) return; // yêu cầu này đã có thợ được chọn

      cards.forEach(c => {
        const cChooseBtn = c.querySelector('.quote-choose-btn');
        if (c === card) {
          c.classList.add('selected');
          c.classList.remove('rejected');
          cChooseBtn.textContent = 'Đã chọn';
          cChooseBtn.disabled = true;
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
});