window.CTX = window.CTX || location.pathname.replace(/\/(pages\/[^/]*|index\.html)?$/, '');
function whenReady(selector, cb) {
  const el = document.querySelector(selector);
  if (el) return cb(el);
  const obs = new MutationObserver(() => {
    const found = document.querySelector(selector);
    if (found) { obs.disconnect(); cb(found); }
  });
  obs.observe(document.documentElement, { childList: true, subtree: true });
}

(async function initAuth() {
  let me = { loggedIn: false };
  try {
    me = await (await fetch(window.CTX + '/api/me')).json();
  } catch (e) {}

  whenReady('#nav-login', link => {
    const profile = document.getElementById('nav-profile');

    if (!me.loggedIn) {
      console.log("ĐÃ TÌM THẤY NAV-LOGIN:", link);
      link.textContent = 'Đăng nhập';
      link.href = window.CTX + '/pages/login.html';
      return;
    }

    link.textContent = 'Xin chào, ' + me.name;
    link.href = window.CTX + '/profile';

    if (profile) {
      profile.style.display = '';
      profile.href = window.CTX + '/profile';
    }
  });
})();

window.CTX = window.CTX || ('/' + location.pathname.split('/')[1]);
document.addEventListener('click', e => {
  const home = e.target.closest('[data-home]');
  if (home) {
    e.preventDefault();
    location.href = window.CTX + '/index.html';
    return;
  }
  const out = e.target.closest('[data-logout]');
  if (out) {
    e.preventDefault();
    fetch(window.CTX + '/logout', { method: 'POST' })
        .finally(() => location.href = window.CTX + '/index.html');
  }
});