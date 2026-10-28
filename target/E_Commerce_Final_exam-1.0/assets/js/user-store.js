(function () {
  const storageKey = "auracraft_users";
  const validRoles = new Set(["buyer", "artisan"]);
  const validStatuses = new Set(["active", "pending", "suspended"]);

  const defaultUsers = [
    {
      id: "usr-001",
      fullName: "Anna Nguyễn",
      email: "anna@auracraft.vn",
      role: "artisan",
      status: "active",
      introduction: "Chuyên chế tác trang sức hoa khô Resin, charm bạc thủ công 925 và phụ kiện Macrame cá nhân hóa.",
      portfolio: "https://instagram.com/annacraft_studio",
      createdAt: "2026-08-15T08:30:00.000Z"
    },
    {
      id: "usr-002",
      fullName: "Trần Bảo",
      email: "bao@auracraft.vn",
      role: "artisan",
      status: "active",
      introduction: "Nghệ nhân Resin Art Đà Nẵng, chuyên mặt dây chuyền phong cảnh đại dương và đèn ngủ epoxy handmade.",
      portfolio: "https://facebook.com/resinartdanang",
      createdAt: "2026-08-20T10:15:00.000Z"
    },
    {
      id: "usr-003",
      fullName: "Lê Vy",
      email: "vy@auracraft.vn",
      role: "artisan",
      status: "active",
      introduction: "Chủ xưởng Candle Lab & Trang sức bạc đính đá quý phong thủy cao cấp.",
      portfolio: "https://instagram.com/candlelab_handmade",
      createdAt: "2026-08-25T14:20:00.000Z"
    },
    {
      id: "usr-004",
      fullName: "Phạm Quang Huy",
      email: "huy@craft.vn",
      role: "artisan",
      status: "pending",
      introduction: "Thợ chạm khắc gỗ nghệ thuật và phụ kiện để bàn phong cách Vintage mộc mạc.",
      portfolio: "https://instagram.com/huywoodart",
      createdAt: "2026-09-25T09:00:00.000Z"
    },
    {
      id: "usr-005",
      fullName: "Đỗ Thảo Trang",
      email: "trang@craft.vn",
      role: "artisan",
      status: "pending",
      introduction: "Nghệ nhân gốm thủ công vuốt tay Bát Tràng, nhận nặn cốc và lọ gốm vẽ họa tiết custom.",
      portfolio: "https://behance.net/trangpottery",
      createdAt: "2026-09-28T11:45:00.000Z"
    },
    {
      id: "usr-006",
      fullName: "Vũ Đức Minh",
      email: "minh@artisan.vn",
      role: "artisan",
      status: "suspended",
      introduction: "Thợ đan len và móc thú bông thủ công.",
      portfolio: "",
      createdAt: "2026-07-10T16:00:00.000Z"
    },
    {
      id: "usr-007",
      fullName: "Hoàng Khang",
      email: "khang@gmail.com",
      role: "buyer",
      status: "active",
      introduction: "Khách hàng thân thiết, thường xuyên đặt quà tặng custom handmade.",
      portfolio: "",
      createdAt: "2026-08-10T09:12:00.000Z"
    },
    {
      id: "usr-008",
      fullName: "Minh Hằng",
      email: "hang@gmail.com",
      role: "buyer",
      status: "active",
      introduction: "Người mua tại Cầu Giấy, Hà Nội.",
      portfolio: "",
      createdAt: "2026-08-28T15:30:00.000Z"
    },
    {
      id: "usr-009",
      fullName: "Bảo Thy",
      email: "thy@gmail.com",
      role: "buyer",
      status: "active",
      introduction: "Khách hàng đặt trang sức và phụ kiện thời trang.",
      portfolio: "",
      createdAt: "2026-09-02T13:40:00.000Z"
    }
  ];

  function getAll() {
    try {
      const raw = localStorage.getItem(storageKey);
      if (!raw) {
        save(defaultUsers);
        return defaultUsers;
      }
      const users = JSON.parse(raw);
      return Array.isArray(users) && users.length > 0
        ? users.filter((user) => user && typeof user === "object")
        : defaultUsers;
    } catch (error) {
      return defaultUsers;
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