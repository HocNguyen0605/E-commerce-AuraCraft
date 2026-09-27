# 🎨 AuraCraft - Sàn Thương Mại Điện Tử Đồ Handmade

Chào mừng team đã đến với repository của **AuraCraft**! Đây là dự án xây dựng bản mẫu giao diện (MVP Frontend) cho nền tảng thương mại điện tử kết nối thợ thủ công và người yêu nghệ thuật.

Để đảm bảo source code của cả nhóm luôn đồng bộ, giao diện không bị lệch tone và không xảy ra xung đột khi ghép code (conflict), **tất cả thành viên vui lòng đọc kỹ và tuân thủ các quy tắc dưới đây trước khi code.**

---

## 1. 🌈 Quy Chuẩn Thiết Kế (UI Guidelines)

### Bảng Màu (Color Palette)
Tất cả các màu sắc đã được định nghĩa sẵn thành biến trong file `assets/css/style.css`. **Tuyệt đối không gõ mã màu HEX thủ công** vào các file CSS khác hoặc viết inline, hãy sử dụng các biến sau:

| Tên biến CSS | Mã Hex | Ý nghĩa & Vị trí sử dụng |
| :--- | :--- | :--- |
| `var(--primary-blue)` | `#425B9A` | Xanh dương trầm - Dùng cho Tiêu đề (H1-H6), nút bấm chính, viền. |
| `var(--text-dark)` | `#432F2E` | Nâu sẫm - Dùng cho Header, Footer, và Text nội dung chung. |
| `var(--bg-cream)` | `#FAEFC6` | Vàng kem - Dùng làm màu nền (Background) cho toàn trang web. |
| `var(--accent-gold)` | `#D4AF37` | Vàng nhấn - Dùng cho nút phụ, màu khi hover menu, thẻ highlight. |
| `var(--highlight)` | `#D96C4A` | Cam gạch - Dùng cho các badge giảm giá, thông báo nhỏ. |

### Phông Chữ (Typography)
Dự án sử dụng 2 phông chữ từ Google Fonts (đã được nhúng sẵn ở file `style.css`):
*   **Tiêu đề (Headings, Menu, Buttons):** `Baloo 2` - Tròn trịa, đáng yêu, mang hơi hướng thủ công.
    *   *Cách dùng:* Thêm class `.font-heading` hoặc dùng thẻ `h1` đến `h6`.
*   **Nội dung (Body Text, Paragraphs):** `Mulish` - Thanh lịch, dễ đọc.
    *   *Cách dùng:* Mặc định toàn bộ thẻ `p`, `span`, `div` sẽ nhận font này.

### Đặc điểm nhận diện Logo
*   Logo AuraCraft sử dụng chữ "A" đặc biệt: Nằm trong khối tròn màu hồng (`#F48FB1`), nghiêng `-12deg`.
*   *Lưu ý:* Cấu trúc Logo đã được chuẩn hóa trong file `components/header.html` và `components/footer.html`. Các bạn không cần tự code lại logo ở các trang khác.

---

## 2. 💻 Quy Tắc Viết Code (Coding Conventions)

1.  **Quy tắc đặt tên Class CSS:**
    *   Bắt buộc dùng tiếng Anh, viết thường, ngăn cách bằng dấu gạch ngang (kebab-case).
    *   ✅ Đúng: `.product-card`, `.btn-primary`, `.cart-container`
    *   ❌ Sai: `.ProductCard`, `.san-pham`, `.btn_mua_hang`
2.  **Cấu trúc Layout chung:**
    *   Mọi trang web đều phải gọi `<div id="header-placeholder"></div>` ở đầu và `<div id="footer-placeholder"></div>` ở cuối.
    *   Nội dung riêng của trang đó phải được bọc trong thẻ `<main></main>`.
3.  **Tái sử dụng code:**
    *   Nếu bạn cần một nút bấm, hãy gọi class `.btn .btn-primary` đã có sẵn trong `style.css`. Không tự viết CSS lại cho nút bấm để tránh mỗi trang một kiểu.

---

## 3. 🚀 Hướng Dẫn Chạy Dự Án (Local Setup)

Dự án dùng Fetch API để tự động nối Header và Footer. Vì vậy, **TUYỆT ĐỐI KHÔNG** click đúp mở file `.html` trực tiếp bằng trình duyệt (giao diện sẽ bị lỗi trắng Header).

**Cách chạy đúng:**
1. Mở VS Code.
2. Cài đặt Extension: **Live Server** (của Ritwick Dey).
3. Click chuột phải vào file `index.html` (hoặc file bạn đang code) -> Chọn **"Open with Live Server"**.

---

## 4. 🔄 Quy Trình Sử Dụng Git (Git Workflow)

Để không lặp lại lỗi "Entirely different commit histories" và mất code, toàn team (Phong, Yến, Minh, Tuyến, Tú, Phước) làm theo đúng luồng sau:

**Bước 1: Clone repo gốc về máy (Chỉ làm 1 lần đầu tiên)**
```bash
git clone https://github.com/Thuong23130327/handmade_TMDT.git
cd handmade_TMDT
```

**Bước 2: Tạo nhánh riêng để làm việc (KHÔNG code thẳng trên main)**
```bash
# Đổi tên nhánh theo format: ten-chucnang (vd: yen-cart, phong-checkout)
git checkout -b ten-cua-ban-chuc-nang
```

**Bước 3: Lưu và Đẩy code lên nhánh của mình**
```bash
git add .
git commit -m "Mô tả ngắn gọn chức năng vừa làm (VD: Hoàn thiện UI Giỏ hàng)"
git push origin ten-cua-ban-chuc-nang
```

**Bước 4: Tạo Pull Request (PR)**
Lên trang GitHub, bấm nút **"Compare & pull request"**. Assign (Giao việc) cho Trưởng nhóm hoặc người phụ trách QA để họ review code và Merge vào `main`.

*Lưu ý: Trước khi tạo nhánh mới để làm task tiếp theo, hãy nhớ chạy `git checkout main` và `git pull origin main` để cập nhật code mới nhất của cả nhóm về máy nhé!*

---
**Chúc team hoàn thành đồ án thật xuất sắc! 🔥**