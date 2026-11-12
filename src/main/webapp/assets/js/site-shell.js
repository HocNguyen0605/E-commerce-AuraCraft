(function () {
    const scriptUrl = document.currentScript && document.currentScript.src;
    const scriptPath = scriptUrl ? new URL(scriptUrl, window.location.href).pathname : "";
    const context = scriptPath.endsWith("/assets/js/site-shell.js")
        ? scriptPath.slice(0, -"/assets/js/site-shell.js".length)
        : "";
    const shellRoutes = {
        home: context + "/pages/home",
        products: context + "/pages/products",
        custom: context + "/pages/customizer",
        login: context + "/pages/login",
        profile: context + "/pages/profile",
        cart: context + "/pages/cart"
    };

    function bindHeader(header) {
        if (header.dataset.shellBound) return;
        header.dataset.shellBound = "true";
        const menu = header.querySelector(".nav-menu");
        const toggle = header.querySelector(".menu-toggle");
        const close = header.querySelector(".close-menu");
        const overlay = header.querySelector(".menu-overlay");
        if (!menu || !toggle || !close || !overlay) return;

        const setOpen = open => {
            menu.classList.toggle("active", open);
            overlay.classList.toggle("active", open);
            menu.setAttribute("aria-hidden", String(!open));
            overlay.setAttribute("aria-hidden", String(!open));
            toggle.setAttribute("aria-expanded", String(open));
            document.body.classList.toggle("menu-open", open);
            if (open) close.focus();
            else toggle.focus();
        };
        toggle.addEventListener("click", () => setOpen(true));
        close.addEventListener("click", () => setOpen(false));
        overlay.addEventListener("click", () => setOpen(false));
        menu.addEventListener("click", event => {
            if (event.target.closest("a")) setOpen(false);
        });
        document.addEventListener("keydown", event => {
            if (!menu.classList.contains("active")) return;
            if (event.key === "Escape") {
                setOpen(false);
                return;
            }
            if (event.key === "Tab") {
                const focusable = [...menu.querySelectorAll('a[href], button:not([disabled]), input:not([disabled]), [tabindex]:not([tabindex="-1"])')]
                    .filter(element => !element.hidden && element.getClientRects().length);
                if (!focusable.length) return;
                const first = focusable[0];
                const last = focusable[focusable.length - 1];
                if (event.shiftKey && (document.activeElement === first || !menu.contains(document.activeElement))) {
                    event.preventDefault();
                    last.focus();
                } else if (!event.shiftKey && (document.activeElement === last || !menu.contains(document.activeElement))) {
                    event.preventDefault();
                    first.focus();
                }
            }
        });
        window.addEventListener("resize", () => {
            if (window.innerWidth > 992 && menu.classList.contains("active")) {
                menu.classList.remove("active");
                overlay.classList.remove("active");
                menu.setAttribute("aria-hidden", "true");
                overlay.setAttribute("aria-hidden", "true");
                toggle.setAttribute("aria-expanded", "false");
                document.body.classList.remove("menu-open");
            }
        });
    }

    function repairShell(root = document) {
        const routeLinks = [];
        if (root.matches?.("[data-shell-route]")) routeLinks.push(root);
        root.querySelectorAll?.("[data-shell-route]").forEach(link => routeLinks.push(link));
        routeLinks.forEach(link => {
            const route = link.dataset.shellRoute;
            if (shellRoutes[route]) link.href = shellRoutes[route];
        });
        const header = root.matches?.(".site-header") ? root : root.querySelector?.(".site-header");
        if (header) {
            const searchForm = header.querySelector(".search-box");
            if (searchForm) searchForm.action = context + "/pages/products";
            bindHeader(header);
        }
        const searchBox = header?.querySelector(".search-box");
        if (searchBox && !searchBox.dataset.productSearchBound) {
            searchBox.dataset.productSearchBound = "true";
            const input = searchBox.querySelector("input");
            const currentQuery = new URLSearchParams(window.location.search).get("q");
            if (input && currentQuery && !input.value) input.value = currentQuery;
            const submit = event => {
                event.preventDefault();
                if (!input) return;
                const query = input.value.trim();
                if (!query) {
                    input.focus();
                    return;
                }
                const destination = new URL(shellRoutes.products, window.location.origin);
                destination.searchParams.set("q", query);
                window.location.assign(destination.toString());
            };
            searchBox.addEventListener("submit", submit);
        }
        const cartLink = header?.querySelector(".cart-icon-btn");
        if (cartLink && !cartLink.dataset.cartCountBound) {
            cartLink.dataset.cartCountBound = "true";
            const badge = cartLink.querySelector(".cart-count-badge");
            let currentUser = null;
            const localItems = () => {
                try {
                    const items = JSON.parse(localStorage.getItem("AuraCraftCart") || "[]");
                    return Array.isArray(items) ? items : [];
                } catch { return []; }
            };
            const setCount = count => {
                if (!badge) return;
                badge.hidden = count < 1;
                badge.textContent = count > 99 ? "99+" : String(count);
                cartLink.setAttribute("aria-label", count > 0 ? `Giỏ hàng, ${count} sản phẩm` : "Giỏ hàng");
                cartLink.title = count > 0 ? `Giỏ hàng (${count})` : "Giỏ hàng";
            };
            const localCount = () => localItems().reduce((sum, item) => sum + Math.max(1, Number(item.quantity) || 1), 0);
            const refreshCount = async () => {
                const user = currentUser;
                if (user?.loggedIn && user.role === "buyer") {
                    try {
                        const response = await fetch(context + "/api/cart", { credentials: "same-origin", cache: "no-store" });
                        if (!response.ok) throw new Error("Cart unavailable");
                        const result = await response.json();
                        const productCount = (result.items || []).reduce((sum, item) => sum + Math.max(1, Number(item.quantity) || 1), 0);
                        const customCount = localItems().filter(item => item.customDesignId)
                            .reduce((sum, item) => sum + Math.max(1, Number(item.quantity) || 1), 0);
                        setCount(productCount + customCount);
                        return;
                    } catch { /* Use the browser cart if the account cart cannot be reached. */ }
                }
                setCount(localCount());
            };
            window.addEventListener("auracraft:cart-updated", refreshCount);
            window.addEventListener("storage", event => {
                if (event.key === "AuraCraftCart") refreshCount();
            });
            currentUser = window.__auraCraftUser || null;
            refreshCount();
            header.refreshCartCount = me => {
                if (me) currentUser = me;
                refreshCount();
            };
        }
        const login = document.getElementById("nav-login");
        if (header && login && !login.dataset.shellAuthLoaded) {
            login.dataset.shellAuthLoaded = "true";
            fetch(context + "/api/me", { credentials: "same-origin", cache: "no-store" })
                .then(response => response.ok ? response.json() : { loggedIn: false })
                .then(me => {
                    window.__auraCraftUser = me;
                    const current = document.getElementById("nav-login");
                    if (!current) return;
                    if (me.loggedIn) {
                        current.hidden = true;
                        const profileGroup = document.getElementById("user-logged-in");
                        const greeting = document.getElementById("user-greeting");
                        if (profileGroup) profileGroup.hidden = false;
                        if (greeting) {
                            greeting.textContent = "Xin chào, " + (me.name || "bạn");
                            greeting.href = shellRoutes.profile;
                        }
                    } else {
                        current.hidden = false;
                        current.textContent = "Đăng nhập";
                        const profileGroup = document.getElementById("user-logged-in");
                        if (profileGroup) profileGroup.hidden = true;
                    }
                    header.refreshCartCount?.(me);
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