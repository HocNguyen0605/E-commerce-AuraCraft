# AuraCraft - UI/UX Style Guide & Coding Rules

Tài liệu này quy định các quy tắc chuẩn khi code giao diện (CSS/HTML) cho dự án AuraCraft để đảm bảo không bị xung đột, thống nhất "vibe" và dễ dàng bảo trì.

## 1. Typography (Phông chữ)
Chúng ta thống nhất sử dụng 2 phông chữ chính từ Google Fonts:
- **Title (Tiêu đề):** `Baloo 2`, cursive. Dành cho các thẻ `h1` - `h6`, logo, nút bấm (buttons), navigation menu.
- **Content (Nội dung):** `Mulish`, sans-serif. Dành cho thẻ `p`, `span`, `input`, nội dung thẻ card.
- **Base Font Size:** `16px` (Không set cứng `28px` vào `body` gây phá vỡ layout mặc định).

## 2. Color Palette (Bảng màu)
Tất cả các màu phải được sử dụng thông qua CSS Variables (`:root`) trong file `style.css`. KHÔNG dùng mã màu hard-code rải rác.
- `--primary-blue`: `#425B9A` (Màu xanh chủ đạo)
- `--primary-brown` (hoặc `--text-dark`): `#432F2E` (Màu chữ chính và màu nền header/footer)
- `--bg-cream`: `#FFFFFF` (Nền trang web)
- `--card-bg`: `#EBDABB` (Nền màu be nhạt cho card/phần nhấn)
- `--accent-gold`: `#D4AF37` (Màu vàng nhấn)
- `--logo-pink`: `#FF99B0` / `#F48FB1` (Màu hồng cho icon logo)
- `--white`: `#FFFFFF`
- `--gray-light`: `#E5E5E5`

## 3. CSS Architecture (Quy tắc viết CSS)
- **File chung:** Các biến, reset CSS, components chung (như `.btn`, `.container`) để ở đầu file `style.css`.
- **Naming Convention:** Dùng BEM (Block__Element--Modifier) hoặc cách đặt tên có tiền tố rõ ràng. Ví dụ: `.btn`, `.btn-primary`, `.product-card`, `.product-info`.
- **Tránh Override vô tội vạ:** KHÔNG set style trực tiếp vào tag name như `div {}` hoặc `section {}` mà phải dùng `class`.
- **Kích thước & Khoảng cách:** Sử dụng padding/margin với các hằng số chẵn (`8px`, `16px`, `24px`, `32px`...). Max-width của container chuẩn là `1200px`.
- **Hiệu ứng hover:** Giữ các hiệu ứng mượt mà với `transition: all 0.3s ease;`.

## 4. Components Chuẩn
### Buttons
Luôn dùng class `.btn` đi kèm với class màu:
- `<a href="#" class="btn btn-primary">Mua sắm</a>`
- `<a href="#" class="btn btn-outline">Thiết kế</a>`

### Container
Mọi nội dung căn giữa trang phải bọc trong class `.container`:
```html
<section class="some-section">
    <div class="container">...</div>
</section>
```

Vui lòng đọc kỹ style guide này và sử dụng các biến CSS có sẵn thay vì tự định nghĩa mới để tránh conflict!
