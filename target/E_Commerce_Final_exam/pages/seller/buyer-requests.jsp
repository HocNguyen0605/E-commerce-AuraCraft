<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="curPage" value="${empty page ? 1 : page}"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kênh Thợ - Chợ Custom &amp; Báo giá</title>
    <link href="https://fonts.googleapis.com/css2?family=Baloo+2:wght@400;600;700&family=Mulish:wght@400;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleSeller.css">
    <link rel="stylesheet" href="${ctx}/assets/css/pages/styleRequests.css">
</head>
<body>
<jsp:include page="/components/header.html"/>

<main class="seller-layout container">
    <c:set var="sellerTab" value="custom" scope="request"/>
    <%@ include file="/components/seller-sidebar.jsp" %>

    <section class="seller-content">
        <c:if test="${not empty success}"><div class="alert alert-success"><c:out value="${success}"/></div></c:if>
        <c:if test="${not empty error}"><div class="alert alert-danger"><c:out value="${error}"/></div></c:if>

        <div class="content-header">
            <h1 class="page-title">Chợ Yêu Cầu Custom</h1>
        </div>

        <div class="tabs">
            <a href="${ctx}/seller/custom" class="${tab eq 'open' ? 'active' : ''}">Yêu cầu đang mở</a>
            <a href="${ctx}/seller/custom?tab=mine" class="${tab eq 'mine' ? 'active' : ''}">
                Báo giá của tôi<c:if test="${pendingQuotes > 0}"> (${pendingQuotes} đang chờ)</c:if>
            </a>
        </div>

        <div class="dashboard-section table-section">
        <c:choose>

            <%-- ============ TAB: BÁO GIÁ CỦA TÔI ============ --%>
            <c:when test="${tab eq 'mine'}">
                <c:choose>
                    <c:when test="${empty myQuotes}">
                        <p class="empty-note">Bạn chưa gửi báo giá nào. Vào "Yêu cầu đang mở" để chọn đơn muốn nhận.</p>
                    </c:when>
                    <c:otherwise>
                        <div style="overflow-x:auto;">
                        <table class="data-table quote-table">
                            <thead>
                            <tr><th>Yêu cầu</th><th>Người đặt</th><th>Giá dự kiến</th><th>Giá bạn báo</th><th>Số ngày</th><th>Trạng thái</th><th></th></tr>
                            </thead>
                            <tbody>
                            <c:forEach var="q" items="${myQuotes}">
                                <tr>
                                    <td><c:out value="${q.requestTitle}"/></td>
                                    <td><c:out value="${q.buyerName}"/></td>
                                    <td><fmt:formatNumber value="${q.budget}" pattern="#,##0"/>đ</td>
                                    <td><strong><fmt:formatNumber value="${q.price}" pattern="#,##0"/>đ</strong></td>
                                    <td>${q.days} ngày</td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${q.status eq 'pending'}"><span class="status-badge warning">Đang chờ</span></c:when>
                                            <c:when test="${q.status eq 'accepted'}"><span class="status-badge success">Được chọn</span></c:when>
                                            <c:when test="${q.status eq 'rejected'}"><span class="status-badge muted">Không được chọn</span></c:when>
                                            <c:otherwise><span class="status-badge muted">Đã rút</span></c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${q.status eq 'pending'}">
                                            <form method="post" action="${ctx}/seller/custom"
                                                  onsubmit="return confirm('Rút báo giá này?');">
                                                <input type="hidden" name="action" value="withdraw">
                                                <input type="hidden" name="quoteId" value="${q.id}">
                                                <button type="submit" class="btn-withdraw">
                                                    <i class="fa-solid fa-rotate-left"></i> Rút báo giá
                                                </button>
                                            </form>
                                        </c:if>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                        </div>
                    </c:otherwise>
                </c:choose>
            </c:when>

            <%-- ============ TAB: YÊU CẦU ĐANG MỞ ============ --%>
            <c:otherwise>
                <form method="get" action="${ctx}/seller/custom" class="search-row">
                    <input type="text" name="q" class="form-control" placeholder="Tìm theo loại sản phẩm, vật liệu, mô tả..."
                           value="<c:out value='${q}'/>">
                    <button type="submit" class="btn btn-primary">Tìm</button>
                </form>

                <c:if test="${empty requests}">
                    <p class="empty-note">Hiện chưa có yêu cầu custom nào đang mở.</p>
                </c:if>

                <div class="request-grid">
                    <c:forEach var="r" items="${requests}">
                        <c:set var="firstImg" value="${empty r.allImages ? '' : r.allImages[0]}"/>
                        <c:choose>
                            <c:when test="${empty firstImg}"><c:set var="firstSrc" value=""/></c:when>
                            <c:when test="${fn:startsWith(firstImg, 'http')}"><c:set var="firstSrc" value="${firstImg}"/></c:when>
                            <c:otherwise><c:set var="firstSrc" value="${ctx}/${firstImg}"/></c:otherwise>
                        </c:choose>
                        <div class="request-card"
                             data-req-id="${r.id}"
                             data-buyer="<c:out value='${r.buyerName}'/>"
                             data-date="${r.timeAgo}"
                             data-title="<c:out value='${r.productType}'/>"
                             data-desc="<c:out value='${r.charmDescription}'/>"
                             data-tags="<c:out value='${r.materials}'/>"
                             data-price="<fmt:formatNumber value='${r.budget}' pattern='#,##0'/>đ"
                             data-img="<c:out value='${firstSrc}'/>"
                             data-my-price="${r.myQuote.price}"
                             data-my-days="${r.myQuote.days}"
                             data-my-note="<c:out value='${r.myQuote.note}'/>"
                             data-my-portfolio="<c:out value='${r.myQuote.portfolio}'/>">
                            <div class="req-header">
                                <span class="req-buyer"><i class="fa-solid fa-user-circle"></i> <c:out value="${r.buyerName}"/></span>
                                <span class="req-date">${r.timeAgo}</span>
                            </div>
                            <c:if test="${not empty firstSrc}">
                                <div class="req-image" onclick="openDetail(this.parentElement)" title="Nhấn để xem chi tiết">
                                    <img src="<c:out value='${firstSrc}'/>" alt="Ảnh thiết kế" loading="lazy">
                                    <div class="overlay-text"><i class="fa-solid fa-eye"></i> Xem chi tiết</div>
                                </div>
                            </c:if>
                            <div class="req-body">
                                <h3 onclick="openDetail(this.parentElement.parentElement)" style="cursor:pointer;"><c:out value="${r.productType}"/></h3>
                                <p class="req-desc"><c:out value="${r.charmDescription}"/></p>
                                <div class="req-tags">
                                    <c:forEach var="t" items="${fn:split(r.materials, ',')}"><span><c:out value="${fn:trim(t)}"/></span></c:forEach>
                                </div>
                                <c:if test="${r.quoteCount > 0}"><small style="color:#888;">${r.quoteCount} báo giá</small></c:if>
                                <c:if test="${not empty r.myQuote and r.myQuote.status eq 'pending'}">
                                    <div><span class="my-quote-tag">Bạn đã báo <fmt:formatNumber value="${r.myQuote.price}" pattern="#,##0"/>đ</span></div>
                                </c:if>
                            </div>
                            <div class="req-footer">
                                <div class="req-price">Dự kiến: <fmt:formatNumber value="${r.budget}" pattern="#,##0"/>đ</div>
                                <button type="button" class="btn btn-primary" onclick="openQuote(this.closest('.request-card'))">
                                    ${empty r.myQuote ? 'Nhận làm' : 'Sửa báo giá'}
                                </button>
                            </div>
                        </div>
                    </c:forEach>
                </div>

                <c:if test="${totalPages > 1}">
                    <div class="pagination">
                        <c:if test="${curPage > 1}">
                            <a class="page-btn btn-page" href="${ctx}/seller/custom?page=${curPage - 1}&q=<c:out value='${q}'/>"><i class="fa-solid fa-chevron-left"></i></a>
                        </c:if>
                        <c:forEach var="i" begin="1" end="${totalPages}">
                            <a class="page-btn btn-page ${i == curPage ? 'active' : ''}" href="${ctx}/seller/custom?page=${i}&q=<c:out value='${q}'/>">${i}</a>
                        </c:forEach>
                        <c:if test="${curPage < totalPages}">
                            <a class="page-btn btn-page" href="${ctx}/seller/custom?page=${curPage + 1}&q=<c:out value='${q}'/>"><i class="fa-solid fa-chevron-right"></i></a>
                        </c:if>
                    </div>
                </c:if>
            </c:otherwise>
        </c:choose>
        </div>
    </section>
</main>

<jsp:include page="/components/footer.html"/>

<!-- Popup Form Báo Giá -->
<div class="modal-overlay" id="quoteModal">
    <div class="modal-content">
        <div class="modal-header">
            <h2>Gửi Báo Giá</h2>
            <button type="button" class="close-btn" onclick="closeQuote()"><i class="fa-solid fa-times"></i></button>
        </div>
        <form id="quoteForm" method="post" action="${ctx}/seller/custom">
            <input type="hidden" name="action" value="quote">
            <input type="hidden" name="requestId" id="qReqId">
            <p style="margin-top:0; margin-bottom:12px; color:var(--text-muted); font-size:13px;">
                Đang báo giá cho: <strong id="modalReqName" style="color:var(--primary-brown);"></strong><br>
                Tên thợ, đánh giá và số đơn hoàn thành lấy từ hồ sơ gian hàng
                (<strong><c:out value="${shop.name}"/></strong>).
            </p>

            <div class="form-row">
                <div class="form-group">
                    <label for="quotePrice">Giá nhận làm (VNĐ)</label>
                    <input type="number" id="quotePrice" name="price" min="1000" step="1000" placeholder="VD: 110000" required>
                </div>
                <div class="form-group">
                    <label for="quoteDays">Số ngày hoàn thành</label>
                    <input type="number" id="quoteDays" name="days" min="1" max="15" placeholder="Tối đa 15 ngày" required>
                </div>
            </div>

            <div class="form-group">
                <label for="quotePortfolio">Portfolio (Tùy chọn)</label>
                <input type="url" id="quotePortfolio" name="portfolio" maxlength="255" placeholder="https://example.com/portfolio">
            </div>

            <div class="form-group" style="margin-bottom:8px;">
                <label for="quoteNote">Ghi chú cho khách (Tùy chọn)</label>
                <textarea id="quoteNote" name="note" maxlength="1000" placeholder="Mô tả chất liệu bạn dùng, cam kết..."></textarea>
            </div>
            <div class="modal-actions">
                <button type="button" class="btn btn-outline" onclick="closeQuote()">Hủy</button>
                <button type="submit" class="btn btn-primary" id="quoteSubmit">Gửi báo giá</button>
            </div>
        </form>
    </div>
</div>

<!-- Popup Chi tiết Yêu cầu -->
<div class="modal-overlay" id="detailModal">
    <div class="modal-content detail-content">
        <div class="modal-header">
            <h2>Chi tiết thiết kế Custom</h2>
            <button type="button" class="close-btn" onclick="closeDetail()"><i class="fa-solid fa-times"></i></button>
        </div>
        <div class="detail-body">
            <div class="detail-img"><img id="detailImg" src="" alt="Sản phẩm custom"></div>
            <div class="detail-info">
                <h3 id="detailTitle" style="color:var(--primary-blue); margin-top:0;"></h3>
                <div style="margin-bottom:15px; font-size:14px; color:var(--text-muted);">
                    <span><i class="fa-solid fa-user"></i> Người đặt: <strong id="detailBuyer" style="color:var(--primary-brown);"></strong></span>
                    <span style="margin-left:15px;"><i class="fa-regular fa-clock"></i> <span id="detailDate"></span></span>
                </div>
                <div style="margin-bottom:15px;">
                    <strong style="display:block; margin-bottom:5px;">Mô tả chi tiết:</strong>
                    <p id="detailDesc" style="margin:0; line-height:1.6; font-size:15px;"></p>
                </div>
                <div style="margin-bottom:20px;">
                    <strong style="display:block; margin-bottom:5px;">Thành phần yêu cầu:</strong>
                    <div id="detailTags" class="req-tags" style="margin-bottom:0;"></div>
                </div>
                <div style="font-size:20px; font-weight:bold; color:var(--accent-gold); margin-bottom:20px;">
                    Giá dự kiến: <span id="detailPrice"></span>
                </div>
                <button type="button" class="btn btn-primary" id="detailQuoteBtn" style="width:100%;">Báo giá ngay cho đơn này</button>
            </div>
        </div>
    </div>
</div>

<script>
    var quoteModal = document.getElementById('quoteModal');
    var detailModal = document.getElementById('detailModal');

    function openQuote(card) {
        closeDetail();
        var d = card.dataset;
        document.getElementById('qReqId').value = d.reqId;
        document.getElementById('modalReqName').textContent = d.title;
        document.getElementById('quotePrice').value = d.myPrice || '';
        document.getElementById('quoteDays').value = d.myDays || '';
        document.getElementById('quotePortfolio').value = d.myPortfolio || '';
        document.getElementById('quoteNote').value = d.myNote || '';
        document.getElementById('quoteSubmit').textContent = d.myPrice ? 'Cập nhật báo giá' : 'Gửi báo giá';
        quoteModal.classList.add('active');
    }
    function closeQuote() { quoteModal.classList.remove('active'); }

    function openDetail(card) {
        var d = card.dataset;
        var img = document.getElementById('detailImg');
        img.src = d.img || '';
        img.style.display = d.img ? '' : 'none';
        document.getElementById('detailTitle').textContent = d.title;
        document.getElementById('detailBuyer').textContent = d.buyer;
        document.getElementById('detailDate').textContent = d.date;
        document.getElementById('detailDesc').textContent = d.desc;
        document.getElementById('detailPrice').textContent = d.price;
        var tags = document.getElementById('detailTags');
        tags.replaceChildren.apply(tags, (d.tags || '').split(',').filter(function (t) { return t.trim(); }).map(function (t) {
            var s = document.createElement('span');
            s.textContent = t.trim();
            return s;
        }));
        document.getElementById('detailQuoteBtn').onclick = function () { openQuote(card); };
        detailModal.classList.add('active');
    }
    function closeDetail() { detailModal.classList.remove('active'); }

    quoteModal.addEventListener('click', function (e) { if (e.target === quoteModal) closeQuote(); });
    detailModal.addEventListener('click', function (e) { if (e.target === detailModal) closeDetail(); });
</script>
<script src="${ctx}/assets/js/popup.js"></script>
<script src="${ctx}/assets/js/auth.js"></script>
</body>
</html>
