const CTX = location.pathname.replace(/\/(pages\/[^/]*|index\.html)?$/, '');

function whenReady(selector, cb) {
  const el = document.querySelector(selector);
  if (el) return cb(el);
  const obs = new MutationObserver(() => {
    const found = document.querySelector(selector);
    if (found) { obs.disconnect(); cb(found); }
  });
  obs.observe(document.documentElement, { childList: true, subtree: true });
}

async function logout() {
  await fetch(CTX + '/logout', { method: 'POST' });
  location.href = CTX + '/index.html';
}

(async function initAuth() {
  let me = { loggedIn: false };
  try { me = await (await fetch(CTX + '/api/me')).json(); } catch (e) {}

  whenReady('#nav-login', link => {
    const profile = document.getElementById('nav-profile');

    if (!me.loggedIn) {
      link.href = CTX + '/pages/login.html';
      return;
    }

    link.textContent = 'Xin chào, ' + me.name;
    link.removeAttribute('href');

    if (profile) {
      profile.style.display = '';
      profile.href = CTX + '/pages/profile.html';
    }

    const out = document.createElement('a');
    out.href = '#';
    out.className = 'nav-text-btn';
    out.textContent = 'Đăng xuất';
    out.addEventListener('click', e => { e.preventDefault(); logout(); });
    link.insertAdjacentElement('afterend', out);
  });
})();