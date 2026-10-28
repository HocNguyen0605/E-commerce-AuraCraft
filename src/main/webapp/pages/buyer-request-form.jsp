<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Gửi yêu cầu Custom — AuraCraft</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pages/styleRequest-flow.css?v=request-form-v4">
</head>
<body>
<div id="header-placeholder"></div>
<main class="container rf-wrap" id="requestPage" data-context="${pageContext.request.contextPath}">
    <div class="rf-hero">
        <span class="tag">Yêu cầu thiết kế riêng</span>
        <h1>Gửi yêu cầu Custom</h1>
        <p class="rf-sub">Dành cho thiết kế hoàn toàn mới do bạn tự đề xuất (không dựa trên sản phẩm có sẵn của shop).
            Yêu cầu sẽ được gửi tới nhiều thợ thủ công phù hợp — bạn so sánh và chọn thợ ưng ý ở bước tiếp theo.</p>
    </div>

    <section id="requestAuthNotice" class="rf-auth-notice card" role="status" aria-live="polite">
        <div class="rf-auth-icon" aria-hidden="true"><i class="fa-solid fa-user-lock"></i></div>
        <div class="rf-auth-copy">
            <span class="rf-auth-kicker">Một bước trước khi bắt đầu</span>
            <h2>Đăng nhập để gửi yêu cầu thiết kế</h2>
            <p>Đăng nhập bằng tài khoản người mua để gửi yêu cầu vào chợ báo giá và theo dõi phản hồi từ các shop.</p>
            <a id="requestLoginButton" class="btn btn-primary" href="${pageContext.request.contextPath}/pages/login">Đăng nhập ngay <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
        </div>
    </section>

    <div class="rf-layout">
        <form class="rf-form card" id="requestForm" hidden>

            <div class="rf-field">
                <label for="buyerName">Tên người mua <em class="rf-required">*</em></label>
                <input type="text" id="buyerName" required>
            </div>
            <div class="rf-field">
                <label for="buyerEmail">Email người mua <em class="rf-required">*</em></label>
                <input type="email" id="buyerEmail" required>
            </div>

            <div class="rf-field">
                <label for="productType"><span class="rf-step">1</span>Loại sản phẩm <em
                        class="rf-required">*</em></label>
                <input type="text" id="productType" list="productTypeList"
                       placeholder="Ví dụ: Vòng tay, Nhẫn, Dây chuyền..." required>
                <datalist id="productTypeList">
                    <option value="Vòng tay">
                    <option value="Dây chuyền">
                    <option value="Nhẫn">
                    <option value="Móc khóa điện thoại">
                </datalist>
            </div>

            <div class="rf-field">
                <label for="materials"><span class="rf-step">2</span>Vật liệu làm sản phẩm <em
                        class="rf-required">*</em></label>
                <input type="text" id="materials" placeholder="Ví dụ: dây da, hạt gỗ, bạc 925..." required>
            </div>

            <div class="rf-field">
                <label for="charmDesc"><span class="rf-step">3</span>Yêu cầu charm — mô tả <em
                        class="rf-required">*</em></label>
                <textarea id="charmDesc" rows="6" placeholder="Mô tả charm bạn muốn: hình dáng, chất liệu, số lượng..."
                          required></textarea>
                <label class="rf-image-link-label" for="charmImageUrls">Hoặc dán link ảnh charm tham khảo (mỗi link một dòng)</label>
                <textarea id="charmImageUrls" class="rf-image-link-input" rows="2" placeholder="https://example.com/charm-mau.jpg"></textarea>
                <div class="rf-upload rf-upload--multi" data-target="charmImages">
                    <input type="file" id="charmImages" accept="image/*" multiple>
                    <span class="rf-upload-icon" aria-hidden="true">🖼️</span>
                    <span class="rf-upload-text">Không bắt buộc — kéo thả hoặc bấm để tải ảnh charm tham khảo (có thể chọn nhiều ảnh)</span>
                </div>
                <div class="rf-preview-grid" id="charmImagesPreview"></div>
            </div>

            <div class="rf-field">
                <label for="designImages"><span class="rf-step">4</span>Ảnh tự thiết kế</label>
                <div class="rf-upload rf-upload--multi" data-target="designImages">
                    <input type="file" id="designImages" accept="image/*" multiple>
                    <span class="rf-upload-icon" aria-hidden="true">📎</span>
                    <span class="rf-upload-text">Không bắt buộc — tải lên bản phác thảo hoặc ảnh mẫu bạn tự chuẩn bị</span>
                </div>
                <label class="rf-image-link-label" for="designImageUrls">Hoặc dán link ảnh thiết kế (mỗi link một dòng)</label>
                <textarea id="designImageUrls" class="rf-image-link-input" rows="2" placeholder="https://example.com/phac-thao.jpg"></textarea>
                <div class="rf-preview-grid" id="designImagesPreview"></div>
            </div>

            <div class="rf-field">
                <label for="budget"><span class="rf-step">5</span>Giá tiền mong muốn thợ làm sản phẩm <em
                        class="rf-required">*</em></label>
                <input type="text" id="budget" placeholder="Ví dụ: 120.000đ" required>
            </div>

            <button type="submit" class="btn btn-primary rf-submit">Gửi yêu cầu &amp; xem báo giá</button>
        </form>

        <aside class="rf-side card">
            <h3>Lưu ý khi gửi yêu cầu</h3>
            <ul class="rf-checklist">
                <li>Mô tả charm là bắt buộc, ảnh charm chỉ là tham khảo thêm</li>
                <li>Ảnh tự thiết kế không bắt buộc, giúp thợ hình dung rõ hơn</li>
                <li>Giá mong muốn là mức tham khảo, thợ có thể gửi báo giá khác</li>
            </ul>
            <p class="rf-note">Sau khi gửi, các thợ phù hợp có thể gửi báo giá. Bạn sẽ so sánh và chọn thợ ở bước tiếp
                theo.</p>
        </aside>
    </div>
    <div id="requestSuccess" class="rf-side card" role="status" hidden style="margin-top:18px"></div>
</main>
<div id="footer-placeholder"></div>
<script src="${pageContext.request.contextPath}/assets/js/products-data.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/custom-workflow.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/buyer-request-form.js?v=request-form-v3"></script>
<script>
    const requestContext = '${pageContext.request.contextPath}';
    fetch(requestContext + '/components/header.html').then(response => response.text()).then(data => {
        document.getElementById('header-placeholder').innerHTML = data;
    }).catch(error => console.error('Error loading header:', error));

    fetch(requestContext + '/components/footer.html').then(response => response.text()).then(data => {
        document.getElementById('footer-placeholder').innerHTML = data;
    }).catch(error => console.error('Error loading footer:', error));
</script>
<script src="${pageContext.request.contextPath}/assets/js/popup.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js"></script>
<script src="${pageContext.request.contextPath}/assets/js/site-shell.js"></script>

</body>
</html>
