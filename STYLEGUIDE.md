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

## 5. Cấu trúc thư mục (Directory Structure)
Để tránh code bị phân tán và khó quản lý, tất cả các thành viên trong nhóm phải tuân thủ nghiêm ngặt cấu trúc thư mục sau đây. Tuyệt đối **KHÔNG** vứt file CSS, JS hay HTML lung tung ở thư mục gốc.

```text
/
├── index.html                 # Trang chủ DUY NHẤT nằm ở thư mục gốc
├── STYLEGUIDE.md              # File tài liệu hướng dẫn (bạn đang đọc)
├── /pages/                    # CHỈ CHỨA các file giao diện HTML (trừ index)
│   ├── cart.html              # Trang giỏ hàng
│   ├── products.html          # Trang danh sách sản phẩm
│   ├── customizer.html        # v.v...
│   └── ...
├── /components/               # CHỈ CHỨA các HTML component dùng chung (dùng fetch load vào)
│   ├── header.html            
│   └── footer.html
└── /assets/                   # CHỨA TÀI NGUYÊN TĨNH (CSS, JS, Images, Fonts)
    ├── /css/                  # CHỈ CHỨA file .css
    │   ├── style.css          # CSS toàn cục (Global) chứa biến và style chung nhất
    │   ├── header-footer.css  # CSS riêng cho component Header và Footer
    │   └── /pages/            # CSS cụ thể riêng cho TỪNG trang (tránh phình to style.css)
    │       ├── styleCart.css
    │       └── styleProducts.css
    ├── /img/                  # CHỈ CHỨA hình ảnh
    └── /js/                   # CHỈ CHỨA mã Javascript (nếu có)
```

**Quy tắc bắt buộc:**
- Khi code 1 trang mới (ví dụ: `checkout.html`), hãy bỏ nó vào thư mục `/pages/`.
- Nếu trang đó cần style riêng biệt phức tạp, tạo 1 file CSS tương ứng (ví dụ: `styleCheckout.css`) và lưu vào `/assets/css/pages/`.
- File html con nằm trong thư mục `/pages/` phải gọi CSS/JS bằng đường dẫn lùi 1 cấp: `../assets/css/...`
- Tất cả hình ảnh tải về phải gom gọn vào thư mục `/assets/img/`. KHÔNG để lẫn vào thư mục `/pages/` hay `/css/`.

## 6. Cơ chế tự động chèn Header & Footer
Để tránh việc phải copy-paste code của Header và Footer ra mọi trang HTML (khiến việc sửa chữa cực kỳ vất vả), hệ thống đã dùng Javascript để tự động lấy (fetch) giao diện chung từ thư mục `/components/`.

Khi tạo một trang mới, bạn **bắt buộc** phải tuân thủ sườn HTML sau để JS có thể hoạt động đúng:

1. **Chuẩn bị 2 div trống (placeholder):** Phải có `<div id="header-placeholder"></div>` đặt trên thẻ `<main>` và `<div id="footer-placeholder"></div>` đặt dưới thẻ `<main>`.
2. **Nhúng đoạn Script:** Phải chèn đoạn JS để gọi Header/Footer ở cuối thẻ `<body>`.
3. **Đường dẫn CSS:** Nhớ link file `header-footer.css`.

**Template chuẩn cho một trang trắng (nằm trong thư mục `/pages/`):**

```html
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>Tiêu đề trang</title>
    <!-- Thư viện Font Awesome -->
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <!-- CSS hệ thống -->
    <link rel="stylesheet" href="../assets/css/style.css">
    <link rel="stylesheet" href="../assets/css/header-footer.css">
</head>
<body>
    <!-- 1. Đây là nơi Header sẽ tự động hiện ra -->
    <div id="header-placeholder"></div>

    <!-- 2. Code phần ruột (body) của trang vào đây -->
    <main>
        <div class="container">
            <!-- Nội dung của bạn -->
        </div>
    </main>

    <!-- 3. Đây là nơi Footer sẽ tự động hiện ra -->
    <div id="footer-placeholder"></div>

    <!-- 4. Script tự động chèn Header/Footer -->
    <script>
        fetch('../components/header.html')
            .then(r => r.text())
            .then(html => document.getElementById('header-placeholder').innerHTML = html);

        fetch('../components/footer.html')
            .then(r => r.text())
            .then(html => document.getElementById('footer-placeholder').innerHTML = html);
    </script>
</body>
</html>
```
Tuyệt đối **không code lại** Header hay Footer bằng tay trong các file HTML nữa nhé!
