document.addEventListener("DOMContentLoaded", () => {
  const tableBody = document.getElementById("userTableBody");
  const searchInput = document.getElementById("userSearch");
  const roleFilter = document.getElementById("roleFilter");
  const statusFilter = document.getElementById("statusFilter");
  const emptyState = document.getElementById("emptyUsers");
  const feedback = document.getElementById("userFeedback");
  const roleLabels = { buyer: "Người mua", artisan: "Thợ thủ công" };
  const statusLabels = {
    active: "Đang hoạt động",
    pending: "Chờ duyệt",
    suspended: "Tạm khóa"
  };

  function createCell(content, className) {
    const cell = document.createElement("td");
    if (className) cell.className = className;
    if (content instanceof Node) cell.append(content);
    else cell.textContent = content;
    return cell;
  }

  function createAccountCell(user) {
    const content = document.createElement("div");
    const name = document.createElement("span");
    const email = document.createElement("span");
    name.className = "user-name";
    name.textContent = user.fullName || "Chưa có tên";
    email.className = "user-email";
    email.textContent = user.email || "Chưa có email";
    content.append(name, email);
    return content;
  }

  function createProfileCell(user) {
    const content = document.createElement("div");
    content.className = "user-profile-summary";
    content.textContent = user.introduction || "Chưa có thông tin giới thiệu.";

    if (user.portfolio) {
      try {
        const portfolioUrl = new URL(user.portfolio);
        if (portfolioUrl.protocol === "https:" || portfolioUrl.protocol === "http:") {
          const link = document.createElement("a");
          link.className = "user-portfolio";
          link.href = portfolioUrl.href;
          link.target = "_blank";
          link.rel = "noopener noreferrer";
          link.textContent = "Mở portfolio";
          content.append(document.createElement("br"), link);
        }
      } catch (error) {
        // Ignore malformed portfolio links saved by older browser data.
      }
    }

    return content;
  }

  function createStatusSelect(user) {
    const select = document.createElement("select");
    select.className = "filter-select";
    select.style.padding = "6px 12px";
    select.style.fontSize = "13px";
    select.style.borderRadius = "6px";
    select.setAttribute("aria-label", `Trạng thái tài khoản ${user.email}`);

    Object.entries(statusLabels).forEach(([value, label]) => {
      const option = document.createElement("option");
      option.value = value;
      option.textContent = label;
      option.selected = user.status === value;
      select.append(option);
    });

    select.addEventListener("change", () => {
      try {
        if (!window.AuraCraftUsers.updateStatus(user.id, select.value)) {
          throw new Error("Không tìm thấy tài khoản cần cập nhật.");
        }
        feedback.textContent = `Đã cập nhật trạng thái cho ${user.email}.`;
        renderUsers();
      } catch (error) {
        feedback.textContent = error.message;
        renderUsers();
      }
    });

    return select;
  }

  function updateStats(users) {
    document.getElementById("totalUsers").textContent = users.length;
    document.getElementById("buyerUsers").textContent = users.filter((user) => user.role === "buyer").length;
    document.getElementById("pendingArtisans").textContent = users.filter(
      (user) => user.role === "artisan" && user.status === "pending"
    ).length;
    document.getElementById("suspendedUsers").textContent = users.filter((user) => user.status === "suspended").length;
  }

  function renderUsers() {
    const users = window.AuraCraftUsers.getAll();
    const keyword = searchInput.value.trim().toLowerCase();
    const visibleUsers = users
      .filter((user) => {
        const searchableText = `${user.fullName || ""} ${user.email || ""}`.toLowerCase();
        return searchableText.includes(keyword)
          && (roleFilter.value === "all" || user.role === roleFilter.value)
          && (statusFilter.value === "all" || user.status === statusFilter.value);
      })
      .sort((first, second) => (second.createdAt || "").localeCompare(first.createdAt || ""));

    updateStats(users);
    tableBody.replaceChildren();
    visibleUsers.forEach((user) => {
      const row = document.createElement("tr");
      const role = document.createElement("span");
      role.className = user.role === "artisan" ? "badge crafting" : "badge shipping";
      role.textContent = roleLabels[user.role] || "Không xác định";
      const createdAt = user.createdAt
        ? new Intl.DateTimeFormat("vi-VN", { dateStyle: "medium" }).format(new Date(user.createdAt))
        : "Không rõ";

      row.append(
        createCell(createAccountCell(user)),
        createCell(role),
        createCell(createProfileCell(user)),
        createCell(createdAt),
        createCell(createStatusSelect(user))
      );
      tableBody.append(row);
    });

    emptyState.hidden = visibleUsers.length > 0;
  }

  searchInput.addEventListener("input", renderUsers);
  roleFilter.addEventListener("change", renderUsers);
  statusFilter.addEventListener("change", renderUsers);
  window.addEventListener("storage", renderUsers);
  renderUsers();
});