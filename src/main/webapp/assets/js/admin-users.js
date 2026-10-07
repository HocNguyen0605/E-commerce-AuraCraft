document.addEventListener("DOMContentLoaded", () => {
  const tableBody = document.getElementById("userTableBody");
  const searchInput = document.getElementById("userSearch");
  const roleFilter = document.getElementById("roleFilter");
  const statusFilter = document.getElementById("statusFilter");
  const emptyState = document.getElementById("emptyUsers");
  const feedback = document.getElementById("userFeedback");
  
  const roleLabels = { buyer: "Người mua", seller: "Thợ thủ công", admin: "Admin" };
  const statusLabels = {
    active: "Đang hoạt động",
    pending: "Chờ duyệt",
    locked: "Tạm khóa"
  };

  let usersData = [];

  function loadUsers() {
    fetch('../api/admin/users?t=' + new Date().getTime())
      .then(response => {
        if (!response.ok) {
          throw new Error('Lỗi khi tải dữ liệu người dùng: ' + response.statusText);
        }
        return response.json();
      })
      .then(data => {
        console.log("Dữ liệu Users nhận được từ API:", data);
        usersData = data.users || [];
        updateStats(data);
        renderUsers();
      })
      .catch(error => {
        console.error("Lỗi:", error);
        feedback.textContent = "Lỗi khi tải dữ liệu. Vui lòng thử lại.";
      });
  }

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
    name.textContent = user.name || "Chưa có tên";
    email.className = "user-email";
    email.textContent = user.email || "Chưa có email";
    content.append(name, email);
    return content;
  }

  function createProfileCell(user) {
    const content = document.createElement("div");
    content.className = "user-profile-summary";
    
    if (user.role === 'seller') {
        const desc = document.createElement("span");
        desc.textContent = user.description ? user.description : (user.shopName ? `Cửa hàng: ${user.shopName}` : "Chưa cập nhật giới thiệu");
        content.append(desc);
        
        if (user.shopId) {
            content.append(document.createElement("br"));
            const link = document.createElement("a");
            link.className = "user-portfolio";
            link.href = "shop-profile.html?id=" + user.shopId;
            link.target = "_blank";
            link.textContent = "Mở portfolio";
            link.style.color = "var(--primary-blue)";
            link.style.textDecoration = "underline";
            link.style.fontSize = "13px";
            content.append(link);
        }
    } else {
        content.textContent = "Khách hàng";
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
      const newStatus = select.value;
      fetch('../api/admin/users', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          userId: user.userId,
          status: newStatus
        })
      })
      .then(response => response.json())
      .then(data => {
        if (data.success) {
          feedback.textContent = `Đã cập nhật trạng thái cho ${user.email} thành công.`;
          // Reload to update stats
          loadUsers();
        } else {
          throw new Error(data.message || "Lỗi cập nhật");
        }
      })
      .catch(err => {
        feedback.textContent = `Cập nhật thất bại: ${err.message}`;
        // Revert select back
        select.value = user.status;
      });
    });

    return select;
  }

  function updateStats(data) {
    document.getElementById("totalUsers").textContent = data.totalUsers || 0;
    document.getElementById("buyerUsers").textContent = data.buyerUsers || 0;
    document.getElementById("pendingArtisans").textContent = data.pendingArtisans || 0;
    document.getElementById("suspendedUsers").textContent = data.suspendedUsers || 0;
  }

  function renderUsers() {
    const keyword = searchInput.value.trim().toLowerCase();
    
    // Map artisan to seller for filtering to match DB roles
    let currentRoleFilter = roleFilter.value;
    if (currentRoleFilter === 'artisan') currentRoleFilter = 'seller';

    const visibleUsers = usersData
      .filter((user) => {
        const searchableText = `${user.name || ""} ${user.email || ""}`.toLowerCase();
        return searchableText.includes(keyword)
          && (currentRoleFilter === "all" || user.role === currentRoleFilter)
          && (statusFilter.value === "all" || user.status === statusFilter.value);
      });

    tableBody.replaceChildren();
    visibleUsers.forEach((user) => {
      const row = document.createElement("tr");
      const role = document.createElement("span");
      role.className = user.role === "seller" ? "badge crafting" : (user.role === "admin" ? "badge cancel" : "badge shipping");
      role.textContent = roleLabels[user.role] || "Không xác định";
      
      const createdAt = "N/A"; // Assuming we don't have creation date in this schema

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
  
  // Load initially
  loadUsers();
});