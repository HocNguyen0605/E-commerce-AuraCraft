(function () {
  const storageKey = "auracraft_users";
  const validRoles = new Set(["buyer", "artisan"]);
  const validStatuses = new Set(["active", "pending", "suspended"]);

  function getAll() {
    try {
      const users = JSON.parse(localStorage.getItem(storageKey) || "[]");
      return Array.isArray(users) ? users.filter((user) => user && typeof user === "object") : [];
    } catch (error) {
      return [];
    }
  }

  function save(users) {
    localStorage.setItem(storageKey, JSON.stringify(users));
  }

  function create(profile) {
    const users = getAll();
    const email = profile.email.trim().toLowerCase();

    if (users.some((user) => String(user.email || "").toLowerCase() === email)) {
      throw new Error("Email này đã được đăng ký.");
    }
    if (!validRoles.has(profile.role)) {
      throw new Error("Vai trò tài khoản không hợp lệ.");
    }

    const user = {
      id: window.crypto && window.crypto.randomUUID
        ? window.crypto.randomUUID()
        : `user-${Date.now()}-${Math.random().toString(16).slice(2)}`,
      fullName: profile.fullName.trim(),
      email,
      role: profile.role,
      status: profile.role === "artisan" ? "pending" : "active",
      introduction: profile.introduction.trim(),
      portfolio: profile.portfolio.trim(),
      createdAt: new Date().toISOString()
    };

    users.push(user);
    save(users);
    return user;
  }

  function updateStatus(userId, status) {
    if (!validStatuses.has(status)) {
      throw new Error("Trạng thái tài khoản không hợp lệ.");
    }

    const users = getAll();
    const user = users.find((item) => item.id === userId);
    if (!user) return false;

    user.status = status;
    save(users);
    return true;
  }

  window.AuraCraftUsers = { getAll, create, updateStatus };
})();