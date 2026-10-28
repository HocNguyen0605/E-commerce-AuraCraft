window.CTX = window.CTX || location.pathname.replace(/\/(pages\/[^/]*|index\.(html|jsp))?$/, '');
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
    if (!me.loggedIn) {
      link.textContent = 'Đăng nhập';
      link.href = window.CTX + '/pages/login';
      return;
    }

    link.hidden = true;
    const group = document.getElementById('user-logged-in');
    const greeting = document.getElementById('user-greeting');
    if (group) group.hidden = false;
    if (greeting) {
      greeting.textContent = 'Xin chào, ' + (me.name || 'bạn');
      greeting.href = window.CTX + '/pages/profile';
    }
  });
})();

window.CTX = window.CTX || ('/' + location.pathname.split('/')[1]);
document.addEventListener('click', e => {
  const home = e.target.closest('[data-home]');
  if (home) {
    e.preventDefault();
    location.href = window.CTX + '/pages/home';
    return;
  }
  const out = e.target.closest('[data-logout]');
  if (out) {
    e.preventDefault();
    fetch(window.CTX + '/logout', { method: 'POST' })
        .finally(() => location.href = window.CTX + '/pages/home');
  }
});
