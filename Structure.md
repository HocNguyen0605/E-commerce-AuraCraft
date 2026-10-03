## Cấu trúc thư mục (Directory Structure)
Dự án sử dụng **Maven** theo mô hình **Servlet/JSP + DAO**. Tất cả thành viên phải tuân thủ nghiêm ngặt cấu trúc dưới đây. Tuyệt đối **KHÔNG** để file Java, CSS, JS hay HTML lung tung sai chỗ.

```text
E_Commerce_Final_exam/
├── pom.xml                          # Cấu hình Maven (dependency, build)
├── STYLEGUIDE.md                    # File tài liệu hướng dẫn (bạn đang đọc)
├── .gitignore                       # Danh sách file không đưa lên Git
│
├── /src/
│   └── /main/
│       ├── /java/                   # CHỈ CHỨA mã nguồn Java (.java)
│       │   ├── /controller/         # Servlet: nhận request, điều hướng tới JSP
│       │   ├── /dao/                # Data Access Object: truy vấn database
│       │   └── /util/               # Lớp tiện ích dùng chung
│       │       └── DBConnection.java   # Tạo kết nối database
│       │
│       ├── /resources/              # CHỈ CHỨA file cấu hình (không phải code)
│       │   └── db.properties        # Thông tin kết nối database (xem ghi chú bên dưới)
│       │
│       └── /webapp/                 # Phần giao diện (web root của Tomcat)
│           ├── index.html           # Trang chủ DUY NHẤT nằm ở thư mục gốc webapp
│           ├── /pages/              # CHỈ CHỨA các file giao diện (HTML/JSP), trừ index
│           │   ├── cart.html        # Trang giỏ hàng
│           │   ├── products.html    # Trang danh sách sản phẩm
│           │   └── ...
│           ├── /components/         # CHỈ CHỨA component dùng chung (load bằng fetch)
│           │   ├── header.html
│           │   └── footer.html
│           ├── /assets/             # TÀI NGUYÊN TĨNH
│           │   ├── /css/            # CHỈ CHỨA file .css
│           │   │   ├── style.css          # CSS toàn cục (biến và style chung nhất)
│           │   │   ├── header-footer.css  # CSS riêng cho Header và Footer
│           │   │   └── /pages/            # CSS riêng cho TỪNG trang
│           │   │       ├── styleCart.css
│           │   │       └── styleProducts.css
│           │   ├── /img/            # CHỈ CHỨA hình ảnh
│           │   └── /js/             # CHỈ CHỨA mã JavaScript
│           └── /WEB-INF/            # (Nếu có) web.xml, JSP không cho truy cập trực tiếp
│
└── /target/                         # Thư mục do Maven tự sinh khi build. KHÔNG sửa, KHÔNG commit
```

### Quy tắc phân chia theo tầng

| Tầng | Vị trí | Nhiệm vụ |
|------|--------|----------|
| Controller | `src/main/java/controller` | Nhận request, gọi DAO, chuyển dữ liệu cho giao diện |
| DAO | `src/main/java/dao` | Chỉ làm việc với database (SQL), không xử lý giao diện |
| Util | `src/main/java/util` | Hàm dùng chung (kết nối DB, mã hóa, v.v.) |
| View | `src/main/webapp` | HTML/JSP/CSS/JS, không viết SQL ở đây |

### Ghi chú về file `.properties`

`db.properties` là **file cấu hình dạng `key=value`** dùng để lưu thông tin kết nối database, giúp không phải viết cứng (hard-code) vào code Java.

**Vị trí:** bắt buộc đặt trong `src/main/resources/`. Maven sẽ tự copy file này vào `WEB-INF/classes` khi build, nhờ đó code đọc được qua classpath.

**Nội dung mẫu:**
```properties
db.url=jdbc:mysql://127.0.0.1:3306/handmade_marketplace
db.user=root
db.password=your_password_here
db.driver=com.mysql.cj.jdbc.Driver
```

**Cách đọc trong code** (đường dẫn KHÔNG có `src/` hay `resources/`):
```java
DBConnection.class.getClassLoader().getResourceAsStream("db.properties");
```

**Quy tắc cho cả nhóm:**
- **KHÔNG** commit mật khẩu thật lên Git. Thêm `src/main/resources/db.properties` vào `.gitignore`.
- Tạo file mẫu `db.properties.example` (để trống mật khẩu) và commit file này. Thành viên mới copy ra thành `db.properties` rồi điền thông tin máy mình.
- Mỗi người dùng database local khác nhau nên phải tự sửa `db.user` và `db.password` cho phù hợp.
- Sửa `db.properties` xong phải **build lại và restart Tomcat** thì thay đổi mới có hiệu lực.
- Chỉ đặt file cấu hình (`.properties`, `.xml`) trong `resources`. File `.java` luôn nằm trong `java`.