(function () {
  const scriptUrl = document.currentScript && document.currentScript.src;
  const scriptPath = scriptUrl ? new URL(scriptUrl, window.location.href).pathname : "";
  const context = scriptPath.endsWith("/assets/js/site-shell.js")
    ? scriptPath.slice(0, -"/assets/js/site-shell.js".length)
    : "";
  const routeByHref = new Map([
    ["index.html", context + "/index.html"], ["../index.html", context + "/index.html"],
    ["products", context + "/pages/products"], ["pages/products", context + "/pages/products"],
    ["../products", context + "/pages/products"], ["customizer.html", context + "/pages/customizer.html"],
    ["login.html", context + "/pages/login.html"], ["profile.html", context + "/profile"],
    ["cart.jsp", context + "/pages/cart.jsp"], ["pages/cart.jsp", context + "/pages/cart.jsp"]
  ]);

  function repairShell(root = document) {
    const links = [];
    if (root.matches?.("a[href]")) links.push(root);
    root.querySelectorAll?.("a[href]").forEach(link => links.push(link));
    links.forEach(link => {
      const href = link.getAttribute("href");
      if (routeByHref.has(href)) link.href = routeByHref.get(href);
    });
    const searchBox = root.matches?.(".search-box") ? root : root.querySelector?.(".search-box");
    if (searchBox && !searchBox.dataset.productSearchBound) {
      searchBox.dataset.productSearchBound = "true";
      const input = searchBox.querySelector("input");
      const submit = event => {
        event?.preventDefault();
        if (!input) return;
        const query = input.value.trim();
        if (!query) {
          input.focus();
          return;
        }
        const url = new URL(context + "/pages/products", window.location.origin);
        url.searchParams.set("q", query);
        window.location.assign(url.href);
      };
      searchBox.querySelector(".btn-search")?.addEventListener("click", submit);
      input?.addEventListener("keydown", event => {
        if (event.key === "Enter") submit(event);
      });
    }
    const login = document.getElementById("nav-login");
    if (login && !login.dataset.shellAuthLoaded) {
      login.dataset.shellAuthLoaded = "true";
      fetch(context + "/api/me", { credentials: "same-origin", cache: "no-store" })
        .then(response => response.ok ? response.json() : { loggedIn: false })
        .then(me => {
          const current = document.getElementById("nav-login");
          if (!current) return;
          if (me.loggedIn) {
            current.textContent = "Xin chào, " + (me.name || "bạn");
            current.href = context + "/profile";
            current.title = "Trang cá nhân";
            const profileIcon = document.getElementById("nav-profile");
            if (profileIcon) {
              profileIcon.style.display = "";
              profileIcon.href = context + "/profile";
            }
          } else {
            current.textContent = "Đăng nhập";
            current.href = context + "/pages/login.html";
          }
        })
        .catch(() => {});
    }
  }

  function loadMissingShell() {
    [
      ["header-placeholder", "header.html"],
      ["footer-placeholder", "footer.html"]
    ].forEach(([id, file]) => {
      const placeholder = document.getElementById(id);
      if (!placeholder || placeholder.innerHTML.trim()) return;
      fetch(context + "/components/" + file, { credentials: "same-origin" })
        .then(response => { if (!response.ok) throw new Error("HTTP " + response.status); return response.text(); })
        .then(html => { placeholder.innerHTML = html; repairShell(placeholder); })
        .catch(error => console.error("Không thể tải " + file + ":", error));
    });
  }

  const observer = new MutationObserver(records => {
    records.forEach(record => record.addedNodes.forEach(node => {
      if (node.nodeType === Node.ELEMENT_NODE) repairShell(node);
    }));
  });
  observer.observe(document.documentElement, { childList: true, subtree: true });
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", () => { loadMissingShell(); repairShell(); }, { once: true });
  } else {
    loadMissingShell();
    repairShell();
  }
})();
