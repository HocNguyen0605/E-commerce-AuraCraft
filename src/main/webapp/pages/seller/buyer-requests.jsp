<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="curPage" value="${empty page ? 1 : page}"/>
<c:set var="pagePath" value="${ctx}/pages/seller/seller-custom"/>
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
    <style>
        /* Toàn bộ style của trang dùng tiền tố cm-* để không bị CSS chung của site ghi đè.
           Màu lấy từ biến của site, có giá trị dự phòng. */
        .cm-scope { --cm-blue: var(--primary-blue, #44599a); --cm-brown: var(--primary-brown, #5b3a29);
            --cm-gold: var(--accent-gold, #c9923a); --cm-muted: var(--text-muted, #6b7280);
            --cm-line: #e5e8ee; --cm-soft: #f6f7fb; }

        /* ---- Tiêu đề trang ---- */
        .cm-head { margin-bottom:18px; }
        .cm-head h1 { margin:0 0 4px; font-family:'Baloo 2', sans-serif; font-size:28px; color:var(--cm-blue); }
        .cm-head p { margin:0; color:var(--cm-muted); font-size:14px; }

        /* ---- Thông báo ---- */
        .cm-alert { display:flex; gap:10px; align-items:flex-start; padding:12px 16px; margin-bottom:16px;
            border-radius:10px; font-size:14px; border:1px solid transparent; }
        .cm-alert.ok  { background:#e8f6ee; color:#146c43; border-color:#bfe3cd; }
        .cm-alert.bad { background:#fdecee; color:#a52834; border-color:#f4c7cc; }

        /* ---- Tab ---- */
        .cm-tabs { display:flex; gap:4px; border-bottom:1px solid var(--cm-line); margin-bottom:20px; overflow-x:auto; }
        .cm-tab { display:inline-flex; align-items:center; gap:8px; padding:10px 16px; margin-bottom:-1px;
            color:var(--cm-muted); text-decoration:none; font-weight:600; font-size:14px; white-space:nowrap;
            border-bottom:3px solid transparent; transition:color .15s, border-color .15s; }
        .cm-tab:hover { color:var(--cm-blue); }
        .cm-tab.is-active { color:var(--cm-blue); border-bottom-color:var(--cm-blue); }
        .cm-count { min-width:20px; height:20px; padding:0 6px; border-radius:999px; background:var(--cm-gold);
            color:#fff; font-size:12px; display:inline-flex; align-items:center; justify-content:center; }

        /* ---- Nút ---- */
        .cm-btn { all:unset; box-sizing:border-box; cursor:pointer; height:38px; padding:0 16px; display:inline-flex;
            align-items:center; justify-content:center; gap:7px; border-radius:8px; border:1px solid transparent;
            font-size:14px; font-weight:700; line-height:1; white-space:nowrap; transition:background .15s, border-color .15s; }
        .cm-btn.primary { background:var(--cm-blue); color:#fff; }
        .cm-btn.primary:hover { filter:brightness(.92); }
        .cm-btn.ghost { background:#fff; color:#333; border-color:#cfd4dc; }
        .cm-btn.ghost:hover { background:var(--cm-soft); }
        .cm-btn.danger { background:#fff; color:#b42335; border-color:#f0c2c8; height:32px; padding:0 12px; font-size:13px; }
        .cm-btn.danger:hover { background:#fdecee; }
        .cm-btn.block { width:100%; }
        .cm-btn:focus-visible, .cm-link:focus-visible, .cm-page:focus-visible, .cm-tab:focus-visible,
        .cm-media:focus-visible { outline:2px solid var(--cm-blue); outline-offset:2px; }

        /* ---- Tìm kiếm ---- */
        .cm-search { display:flex; gap:10px; margin:0 0 6px; max-width:620px; }
        .cm-search-box { position:relative; flex:1; }
        .cm-search-box i { position:absolute; left:13px; top:50%; transform:translateY(-50%); color:var(--cm-muted); pointer-events:none; }
        .cm-search-box input { box-sizing:border-box; width:100%; height:40px; padding:0 12px 0 38px; margin:0;
            border:1px solid #cfd4dc; border-radius:10px; background:#fff; font:inherit; font-size:14px; }
        .cm-search-box input:focus { outline:2px solid #b3c0e6; border-color:var(--cm-blue); }
        .cm-search-note { margin:0 0 16px; font-size:13px; color:var(--cm-muted); }
        .cm-search-note a { color:var(--cm-blue); font-weight:600; margin-left:6px; }
        .cm-search-gap { margin-bottom:18px; }

        /* ---- Lưới thẻ yêu cầu ---- */
        .cm-grid { display:grid; grid-template-columns:repeat(auto-fill, minmax(270px, 1fr)); gap:18px; }
        .cm-card { display:flex; flex-direction:column; background:#fff; border:1px solid var(--cm-line);
            border-radius:14px; overflow:hidden; transition:box-shadow .2s, transform .2s; }
        .cm-card:hover { box-shadow:0 8px 24px rgba(30,40,80,.10); transform:translateY(-2px); }
        .cm-media { all:unset; box-sizing:border-box; position:relative; display:block; width:100%; aspect-ratio:4 / 3;
            background:var(--cm-soft); cursor:pointer; overflow:hidden; }
        .cm-media img { width:100%; height:100%; object-fit:cover; display:block; transition:transform .35s; }
        .cm-card:hover .cm-media img { transform:scale(1.04); }
        .cm-media-empty { display:flex; align-items:center; justify-content:center; height:100%; color:#b5bccb; font-size:38px; }
        .cm-media-hint { position:absolute; inset:auto 0 0 0; padding:26px 12px 10px; color:#fff; font-size:13px; font-weight:700;
            background:linear-gradient(to top, rgba(20,25,45,.65), transparent); opacity:0; transition:opacity .2s; }
        .cm-card:hover .cm-media-hint, .cm-media:focus-visible .cm-media-hint { opacity:1; }
        .cm-card-body { display:flex; flex-direction:column; gap:10px; padding:14px 16px 6px; flex:1; }
        .cm-meta { display:flex; align-items:center; justify-content:space-between; gap:8px; font-size:12px; color:var(--cm-muted); }
        .cm-buyer { display:inline-flex; align-items:center; gap:8px; min-width:0; font-weight:600; color:#3b4252; }
        .cm-buyer span:last-child { overflow:hidden; text-overflow:ellipsis; white-space:nowrap; }
        .cm-avatar { flex:0 0 auto; width:26px; height:26px; border-radius:50%; background:#e8edf9; color:var(--cm-blue);
            display:inline-flex; align-items:center; justify-content:center; font-weight:700; font-size:12px; }
        .cm-time { white-space:nowrap; }
        .cm-card-title { margin:0; font-family:'Baloo 2', sans-serif; font-size:18px; line-height:1.3; }
        .cm-link { all:unset; cursor:pointer; color:var(--cm-blue); }
        .cm-link:hover { text-decoration:underline; }
        .cm-desc { margin:0; font-size:14px; line-height:1.55; color:#4b5260; display:-webkit-box;
            -webkit-line-clamp:3; -webkit-box-orient:vertical; overflow:hidden; }
        .cm-chips { display:flex; flex-wrap:wrap; gap:6px; }
        .cm-chips span { padding:3px 10px; border-radius:999px; background:var(--cm-soft); border:1px solid var(--cm-line);
            font-size:12px; color:#3b4252; }
        .cm-notes { display:flex; flex-wrap:wrap; gap:8px; align-items:center; font-size:12px; color:var(--cm-muted); }
        .cm-mine-tag { display:inline-flex; align-items:center; gap:5px; padding:3px 10px; border-radius:999px;
            background:#e8f6ee; color:#146c43; font-weight:700; }
        .cm-card-foot { display:flex; align-items:center; justify-content:space-between; gap:10px; padding:12px 16px 16px;
            border-top:1px dashed var(--cm-line); margin-top:8px; }
        .cm-budget-label { display:block; font-size:12px; color:var(--cm-muted); }
        .cm-budget { font-size:17px; color:var(--cm-gold); font-weight:800; }

        /* ---- Trạng thái rỗng ---- */
        .cm-empty { text-align:center; padding:48px 16px; color:var(--cm-muted); }
        .cm-empty i { display:block; font-size:42px; color:#c4cadb; margin-bottom:12px; }
        .cm-empty p { margin:0 0 16px; font-size:15px; }

        /* ---- Bảng báo giá của tôi ---- */
        .cm-table-wrap { overflow-x:auto; border:1px solid var(--cm-line); border-radius:12px; }
        .cm-table { width:100%; border-collapse:collapse; font-size:14px; min-width:760px; }
        .cm-table th { background:#cdd2e7; text-align:left; padding:12px 14px; font-size:13px; color:#4b5260; white-space:nowrap; }
        .cm-table td { padding:14px; border-top:1px solid var(--cm-line); vertical-align:middle; }
        .cm-table tr:hover td { background:#fafbfe; }
        .cm-table .num { white-space:nowrap; }
        .cm-table form { margin:0; }
        .cm-badge { display:inline-flex; align-items:center; padding:4px 10px; border-radius:999px; font-size:12px; font-weight:700; white-space:nowrap; }
        .cm-badge.warn  { background:#fff3d6; color:#8a5a00; }
        .cm-badge.ok    { background:#e3f4ea; color:#146c43; }
        .cm-badge.muted { background:#eceef3; color:#5b6472; }

        /* ---- Phân trang ---- */
        .cm-pager { display:flex; flex-wrap:wrap; gap:6px; justify-content:center; margin-top:24px; }
        .cm-page { min-width:36px; height:36px; padding:0 10px; display:inline-flex; align-items:center; justify-content:center;
            border:1px solid #cfd4dc; border-radius:8px; background:#fff; color:#333; text-decoration:none; font-size:14px; }
        a.cm-page:hover { background:var(--cm-soft); }
        .cm-page.is-current { background:var(--cm-blue); border-color:var(--cm-blue); color:#fff; font-weight:700; }
        .cm-page.is-gap { border-color:transparent; background:transparent; }

        /* ---- Hàng thống kê ---- */
        .cm-stats { display:grid; grid-template-columns:repeat(auto-fit, minmax(210px, 1fr)); gap:14px; margin-bottom:22px; }
        .cm-stat { display:flex; gap:14px; align-items:center; padding:14px 16px; background:#fff;
            border:1px solid var(--cm-line); border-radius:14px; }
        .cm-stat-icon { flex:0 0 auto; width:44px; height:44px; border-radius:12px; display:flex; align-items:center;
            justify-content:center; font-size:20px; background:#e8edf9; color:var(--cm-blue); }
        .cm-stat-icon.amber { background:#fff3d6; color:#b7791f; }
        .cm-stat-icon.green { background:#e3f4ea; color:#198754; }
        .cm-stat-icon.rose  { background:#fde8ec; color:#c2415a; }
        .cm-stat p { margin:0; font-size:13px; color:var(--cm-muted); }
        .cm-stat strong { display:block; font-size:22px; line-height:1.25; color:#2b3a67; }
        .cm-stat small { display:block; margin-top:2px; font-size:12px; color:var(--cm-muted); }

        /* ---- Bố cục 2 cột + cột phụ ---- */
        .cm-layout { display:grid; grid-template-columns:minmax(0, 1fr) 290px; gap:24px; align-items:start; }
        .cm-main { min-width:0; }
        .cm-aside { display:flex; flex-direction:column; gap:16px; position:sticky; top:16px; }
        .cm-box { padding:16px 18px; background:#fff; border:1px solid var(--cm-line); border-radius:14px; }
        .cm-box h4 { display:flex; align-items:center; gap:8px; margin:0 0 12px; font-family:'Baloo 2', sans-serif;
            font-size:17px; color:var(--cm-blue); }
        .cm-box-link { display:inline-block; margin-top:10px; font-size:13px; font-weight:700; color:var(--cm-blue); text-decoration:none; }
        .cm-box-link:hover { text-decoration:underline; }
        .cm-recent { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:12px; }
        .cm-recent li { display:flex; justify-content:space-between; gap:10px; align-items:flex-start; padding-bottom:12px;
            border-bottom:1px dashed var(--cm-line); }
        .cm-recent li:last-child { border-bottom:0; padding-bottom:0; }
        .cm-recent-title { font-size:14px; font-weight:700; color:#3b4252; display:-webkit-box; -webkit-line-clamp:2;
            -webkit-box-orient:vertical; overflow:hidden; }
        .cm-recent-price { display:block; margin-top:2px; font-size:13px; color:var(--cm-gold); font-weight:800; }
        .cm-tips { list-style:none; margin:0; padding:0; display:flex; flex-direction:column; gap:10px; font-size:13px;
            line-height:1.5; color:#4b5260; }
        .cm-tips li { display:flex; gap:10px; }
        .cm-tips i { flex:0 0 auto; width:18px; margin-top:2px; color:var(--cm-gold); text-align:center; }
        .cm-rules { background:#fff8e8; border-color:#f1deb0; }
        .cm-rules h4 { color:#8a5a00; }
        .cm-small-note { margin:0; font-size:13px; color:var(--cm-muted); }

        /* ---- Thanh lọc / sắp xếp ---- */
        .cm-toolbar { display:flex; flex-wrap:wrap; align-items:center; gap:10px 16px; margin:0 0 16px; padding:10px 12px;
            background:var(--cm-soft); border:1px solid var(--cm-line); border-radius:12px; }
        .cm-seg { display:inline-flex; padding:3px; background:#fff; border:1px solid var(--cm-line); border-radius:10px; }
        .cm-seg-btn { all:unset; box-sizing:border-box; cursor:pointer; height:30px; padding:0 14px; display:inline-flex;
            align-items:center; border-radius:8px; font-size:13px; font-weight:700; color:var(--cm-muted); }
        .cm-seg-btn:hover { color:var(--cm-blue); }
        .cm-seg-btn.is-active { background:var(--cm-blue); color:#fff; }
        .cm-seg-btn:focus-visible { outline:2px solid var(--cm-blue); outline-offset:2px; }
        .cm-sort { display:inline-flex; align-items:center; gap:8px; font-size:13px; color:var(--cm-muted); margin:0; }
        .cm-sort select { box-sizing:border-box; height:34px; padding:0 10px; margin:0; border:1px solid #cfd4dc;
            border-radius:8px; background:#fff; font:inherit; font-size:13px; }
        .cm-view-count { margin-left:auto; font-size:12px; color:var(--cm-muted); }
        .cm-card[hidden], .cm-empty[hidden] { display:none; }

        @media (max-width:1180px) {
            .cm-layout { grid-template-columns:1fr; }
            .cm-aside { position:static; display:grid; grid-template-columns:repeat(auto-fit, minmax(260px, 1fr)); }
        }

        /* ---- Modal ---- */
        .cm-modal { position:fixed; inset:0; z-index:1000; display:none; align-items:center; justify-content:center;
            padding:16px; background:rgba(20,25,45,.55); }
        .cm-modal.is-open { display:flex; }
        .cm-dialog { width:100%; max-width:520px; max-height:calc(100vh - 32px); overflow-y:auto; background:#fff;
            border-radius:16px; box-shadow:0 20px 60px rgba(0,0,0,.25); }
        .cm-dialog.wide { max-width:860px; }
        .cm-dialog-head { display:flex; align-items:center; justify-content:space-between; gap:12px; padding:18px 22px;
            border-bottom:1px solid var(--cm-line); }
        .cm-dialog-head h2 { margin:0; font-family:'Baloo 2', sans-serif; font-size:21px; color:var(--cm-blue); }
        .cm-x { all:unset; cursor:pointer; width:34px; height:34px; border-radius:50%; display:inline-flex; align-items:center;
            justify-content:center; color:var(--cm-muted); }
        .cm-x:hover { background:var(--cm-soft); }
        .cm-x:focus-visible { outline:2px solid var(--cm-blue); }
        .cm-dialog-body { padding:20px 22px 22px; }
        .cm-forwho { margin:0 0 16px; padding:12px 14px; border-radius:10px; background:var(--cm-soft); font-size:13px;
            line-height:1.6; color:var(--cm-muted); }
        .cm-forwho strong { color:var(--cm-brown); }
        .cm-form-row { display:grid; grid-template-columns:1fr 1fr; gap:14px; }
        .cm-field { display:flex; flex-direction:column; gap:6px; margin-bottom:14px; }
        .cm-field label { font-size:13px; font-weight:700; color:#3b4252; }
        .cm-field input, .cm-field textarea { box-sizing:border-box; width:100%; margin:0; padding:10px 12px;
            border:1px solid #cfd4dc; border-radius:8px; background:#fff; font:inherit; font-size:14px; }
        .cm-field textarea { min-height:96px; resize:vertical; }
        .cm-field input:focus, .cm-field textarea:focus { outline:2px solid #b3c0e6; border-color:var(--cm-blue); }
        .cm-hint { font-size:12px; color:var(--cm-muted); }
        .cm-actions { display:flex; justify-content:flex-end; gap:10px; margin-top:6px; }

        .cm-detail { display:grid; grid-template-columns:minmax(0, 1fr) minmax(0, 1fr); }
        .cm-detail-img { background:var(--cm-soft); min-height:260px; display:flex; align-items:center; justify-content:center; }
        .cm-detail-img img { width:100%; height:100%; max-height:520px; object-fit:cover; display:block; }
        .cm-detail-info { padding:22px; display:flex; flex-direction:column; gap:16px; }
        .cm-detail-info h3 { margin:0; font-family:'Baloo 2', sans-serif; font-size:22px; color:var(--cm-blue); }
        .cm-detail-meta { display:flex; flex-wrap:wrap; gap:6px 18px; font-size:13px; color:var(--cm-muted); }
        .cm-detail-meta strong { color:var(--cm-brown); }
        .cm-detail-label { display:block; margin-bottom:6px; font-size:13px; font-weight:700; color:#3b4252; }
        .cm-detail-desc { margin:0; line-height:1.65; font-size:15px; color:#3b4252; white-space:pre-line; }
        .cm-detail-price { padding:12px 14px; border-radius:10px; background:#fff8e8; border:1px solid #f1deb0; }
        .cm-detail-price span:first-child { display:block; font-size:12px; color:#8a5a00; }
        .cm-detail-price span:last-child { font-size:22px; font-weight:800; color:var(--cm-gold); }
        body.cm-lock { overflow:hidden; }

        @media (max-width:720px) {
            .cm-detail { grid-template-columns:1fr; }
            .cm-detail-img { min-height:0; }
            .cm-form-row { grid-template-columns:1fr; gap:0; }
            .cm-head h1 { font-size:24px; }
        }
        @media (prefers-reduced-motion:reduce) {
            .cm-card, .cm-media img { transition:none; }
            .cm-card:hover { transform:none; }
        }
    </style>
</head>
<body>

<main class="seller-layout container">
    <c:set var="sellerTab" value="custom" scope="request"/>
    <%@ include file="/components/seller-sidebar.jsp" %>

    <section class="seller-content cm-scope">
        <c:if test="${not empty success}"><div class="cm-alert ok"><i class="fa-solid fa-circle-check"></i><span><c:out value="${success}"/></span></div></c:if>
        <c:if test="${not empty error}"><div class="cm-alert bad"><i class="fa-solid fa-circle-exclamation"></i><span><c:out value="${error}"/></span></div></c:if>

        <header class="cm-head">
            <h1>Chợ Yêu Cầu Custom</h1>
            <p>Xin chào <strong><c:out value="${shop.name}"/></strong>, chọn yêu cầu phù hợp với tay nghề của bạn và gửi báo giá cho khách.</p>
        </header>

        <%-- Tính thống kê từ danh sách báo giá của shop --%>
        <c:set var="stPending" value="0"/><c:set var="stAccepted" value="0"/><c:set var="stRejected" value="0"/>
        <c:set var="stPendingValue" value="0"/><c:set var="stAcceptedValue" value="0"/>
        <c:forEach var="mq" items="${myQuotes}">
            <c:choose>
                <c:when test="${mq.status eq 'pending'}">
                    <c:set var="stPending" value="${stPending + 1}"/>
                    <c:set var="stPendingValue" value="${stPendingValue + mq.price}"/>
                </c:when>
                <c:when test="${mq.status eq 'accepted'}">
                    <c:set var="stAccepted" value="${stAccepted + 1}"/>
                    <c:set var="stAcceptedValue" value="${stAcceptedValue + mq.price}"/>
                </c:when>
                <c:when test="${mq.status eq 'rejected'}"><c:set var="stRejected" value="${stRejected + 1}"/></c:when>
            </c:choose>
        </c:forEach>
        <c:set var="stDecided" value="${stAccepted + stRejected}"/>

        <div class="cm-stats">
            <div class="cm-stat">
                <div class="cm-stat-icon"><i class="fa-solid fa-store"></i></div>
                <div>
                    <p>Yêu cầu đang mở</p>
                    <strong>${openTotal}</strong>
                    <small>Đang chờ thợ nhận làm</small>
                </div>
            </div>
            <div class="cm-stat">
                <div class="cm-stat-icon amber"><i class="fa-solid fa-hourglass-half"></i></div>
                <div>
                    <p>Báo giá đang chờ</p>
                    <strong>${stPending}</strong>
                    <small>Tổng <fmt:formatNumber value="${stPendingValue}" pattern="#,##0"/>đ</small>
                </div>
            </div>
            <div class="cm-stat">
                <div class="cm-stat-icon green"><i class="fa-solid fa-circle-check"></i></div>
                <div>
                    <p>Được khách chọn</p>
                    <strong>${stAccepted}</strong>
                    <small>Tổng <fmt:formatNumber value="${stAcceptedValue}" pattern="#,##0"/>đ</small>
                </div>
            </div>
            <div class="cm-stat">
                <div class="cm-stat-icon rose"><i class="fa-solid fa-bullseye"></i></div>
                <div>
                    <p>Tỷ lệ được chọn</p>
                    <c:choose>
                        <c:when test="${stDecided > 0}">
                            <strong><fmt:formatNumber value="${stAccepted * 100.0 / stDecided}" pattern="0"/>%</strong>
                            <small>${stAccepted}/${stDecided} báo giá đã có kết quả</small>
                        </c:when>
                        <c:otherwise>
                            <strong>—</strong>
                            <small>Chưa có báo giá nào có kết quả</small>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>

        <nav class="cm-tabs" aria-label="Chuyển tab">
            <a href="${pagePath}" class="cm-tab ${tab eq 'open' ? 'is-active' : ''}">
                <i class="fa-solid fa-store"></i> Yêu cầu đang mở
            </a>
            <a href="${pagePath}?tab=mine" class="cm-tab ${tab eq 'mine' ? 'is-active' : ''}">
                <i class="fa-solid fa-file-invoice-dollar"></i> Báo giá của tôi
                <c:if test="${pendingQuotes > 0}"><span class="cm-count" title="${pendingQuotes} báo giá đang chờ">${pendingQuotes}</span></c:if>
            </a>
        </nav>

        <c:choose>

            <%-- ============ TAB: BÁO GIÁ CỦA TÔI ============ --%>
            <c:when test="${tab eq 'mine'}">
                <c:choose>
                    <c:when test="${empty myQuotes}">
                        <div class="cm-empty">
                            <i class="fa-regular fa-folder-open"></i>
                            <p>Bạn chưa gửi báo giá nào. Vào "Yêu cầu đang mở" để chọn đơn muốn nhận.</p>
                            <a class="cm-btn primary" href="${pagePath}">Xem yêu cầu đang mở</a>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="cm-table-wrap">
                            <table class="cm-table">
                                <thead>
                                <tr><th>Yêu cầu</th><th>Người đặt</th><th>Giá dự kiến</th><th>Giá bạn báo</th><th>Số ngày</th><th>Trạng thái</th><th></th></tr>
                                </thead>
                                <tbody>
                                <c:forEach var="q" items="${myQuotes}">
                                    <tr>
                                        <td><strong><c:out value="${q.requestTitle}"/></strong></td>
                                        <td><c:out value="${q.buyerName}"/></td>
                                        <td class="num"><fmt:formatNumber value="${q.budget}" pattern="#,##0"/>đ</td>
                                        <td class="num"><strong><fmt:formatNumber value="${q.price}" pattern="#,##0"/>đ</strong></td>
                                        <td class="num">${q.days} ngày</td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${q.status eq 'pending'}"><span class="cm-badge warn">Đang chờ</span></c:when>
                                                <c:when test="${q.status eq 'accepted'}"><span class="cm-badge ok">Được chọn</span></c:when>
                                                <c:when test="${q.status eq 'rejected'}"><span class="cm-badge muted">Không được chọn</span></c:when>
                                                <c:otherwise><span class="cm-badge muted">Đã rút</span></c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:if test="${q.status eq 'pending'}">
                                                <form method="post" action="${pagePath}" onsubmit="return confirm('Rút báo giá này?');">
                                                    <input type="hidden" name="action" value="withdraw">
                                                    <input type="hidden" name="quoteId" value="${q.id}">
                                                    <button type="submit" class="cm-btn danger"><i class="fa-solid fa-rotate-left"></i> Rút báo giá</button>
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
                <div class="cm-layout">
                    <div class="cm-main">
                        <form method="get" action="${pagePath}" class="cm-search" role="search">
                            <div class="cm-search-box">
                                <i class="fa-solid fa-magnifying-glass"></i>
                                <input type="search" name="q" placeholder="Tìm theo loại sản phẩm, vật liệu, mô tả..."
                                       value="<c:out value='${q}'/>" aria-label="Tìm yêu cầu">
                            </div>
                            <button type="submit" class="cm-btn primary">Tìm</button>
                        </form>
                        <c:choose>
                            <c:when test="${not empty q}">
                                <p class="cm-search-note">Kết quả cho “<c:out value="${q}"/>”<a href="${pagePath}">Xóa tìm kiếm</a></p>
                            </c:when>
                            <c:otherwise><div class="cm-search-gap"></div></c:otherwise>
                        </c:choose>

                        <c:if test="${not empty requests}">
                            <div class="cm-toolbar" id="cmToolbar">
                                <div class="cm-seg" role="group" aria-label="Lọc theo tình trạng báo giá">
                                    <button type="button" class="cm-seg-btn is-active" data-filter="all">Tất cả</button>
                                    <button type="button" class="cm-seg-btn" data-filter="new">Chưa báo giá</button>
                                    <button type="button" class="cm-seg-btn" data-filter="quoted">Đã báo giá</button>
                                </div>
                                <label class="cm-sort">Sắp xếp
                                    <select id="cmSort">
                                        <option value="default">Mặc định</option>
                                        <option value="high">Giá dự kiến cao → thấp</option>
                                        <option value="low">Giá dự kiến thấp → cao</option>
                                    </select>
                                </label>
                                <span class="cm-view-count" id="cmViewCount"></span>
                            </div>
                        </c:if>

                        <c:if test="${empty requests}">
                            <div class="cm-empty">
                                <i class="fa-regular fa-face-smile"></i>
                                <p>${empty q ? 'Hiện chưa có yêu cầu custom nào đang mở.' : 'Không có yêu cầu nào khớp từ khóa. Thử từ khóa khác nhé.'}</p>
                                <c:if test="${not empty q}"><a class="cm-btn ghost" href="${pagePath}">Xem tất cả yêu cầu</a></c:if>
                            </div>
                        </c:if>

                        <div class="cm-grid" id="cmGrid">
                            <c:forEach var="r" items="${requests}" varStatus="st">
                                <c:set var="firstImg" value="${empty r.allImages ? '' : r.allImages[0]}"/>
                                <c:choose>
                                    <c:when test="${empty firstImg}"><c:set var="firstSrc" value=""/></c:when>
                                    <c:when test="${fn:startsWith(firstImg, 'http')}"><c:set var="firstSrc" value="${firstImg}"/></c:when>
                                    <c:otherwise><c:set var="firstSrc" value="${ctx}/${firstImg}"/></c:otherwise>
                                </c:choose>
                                <article class="cm-card"
                                         data-req-id="${r.id}"
                                         data-idx="${st.index}"
                                         data-budget="${r.budget}"
                                         data-quoted="${empty r.myQuote ? 'no' : 'yes'}"
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

                                    <button type="button" class="cm-media" onclick="openDetail(this.closest('.cm-card'))"
                                            aria-label="Xem chi tiết yêu cầu">
                                        <c:choose>
                                            <c:when test="${not empty firstSrc}">
                                                <img src="<c:out value='${firstSrc}'/>" alt="Ảnh thiết kế" loading="lazy">
                                            </c:when>
                                            <c:otherwise><span class="cm-media-empty"><i class="fa-regular fa-image"></i></span></c:otherwise>
                                        </c:choose>
                                        <span class="cm-media-hint"><i class="fa-solid fa-eye"></i> Xem chi tiết</span>
                                    </button>

                                    <div class="cm-card-body">
                                        <div class="cm-meta">
                                    <span class="cm-buyer">
                                        <span class="cm-avatar"><c:out value="${fn:toUpperCase(fn:substring(r.buyerName, 0, 1))}"/></span>
                                        <span><c:out value="${r.buyerName}"/></span>
                                    </span>
                                            <span class="cm-time"><i class="fa-regular fa-clock"></i> ${r.timeAgo}</span>
                                        </div>

                                        <h3 class="cm-card-title">
                                            <button type="button" class="cm-link" onclick="openDetail(this.closest('.cm-card'))"><c:out value="${r.productType}"/></button>
                                        </h3>
                                        <p class="cm-desc"><c:out value="${r.charmDescription}"/></p>

                                        <div class="cm-chips">
                                            <c:forEach var="t" items="${fn:split(r.materials, ',')}"><span><c:out value="${fn:trim(t)}"/></span></c:forEach>
                                        </div>

                                        <c:if test="${r.quoteCount > 0 or (not empty r.myQuote and r.myQuote.status eq 'pending')}">
                                            <div class="cm-notes">
                                                <c:if test="${r.quoteCount > 0}"><span><i class="fa-regular fa-comments"></i> ${r.quoteCount} báo giá</span></c:if>
                                                <c:if test="${not empty r.myQuote and r.myQuote.status eq 'pending'}">
                                                    <span class="cm-mine-tag"><i class="fa-solid fa-check"></i> Bạn đã báo <fmt:formatNumber value="${r.myQuote.price}" pattern="#,##0"/>đ</span>
                                                </c:if>
                                            </div>
                                        </c:if>
                                    </div>

                                    <footer class="cm-card-foot">
                                        <div>
                                            <span class="cm-budget-label">Giá dự kiến</span>
                                            <strong class="cm-budget"><fmt:formatNumber value="${r.budget}" pattern="#,##0"/>đ</strong>
                                        </div>
                                        <button type="button" class="cm-btn primary" onclick="openQuote(this.closest('.cm-card'))">
                                                ${empty r.myQuote ? 'Nhận làm' : 'Sửa báo giá'}
                                        </button>
                                    </footer>
                                </article>
                            </c:forEach>
                        </div>

                        <div class="cm-empty" id="cmFilterEmpty" hidden>
                            <i class="fa-regular fa-folder-open"></i>
                            <p>Không có yêu cầu nào khớp bộ lọc trên trang này.</p>
                        </div>

                            <%-- Phân trang: c:param tự mã hóa từ khóa tìm kiếm --%>
                        <c:if test="${totalPages > 1}">
                            <c:set var="lo" value="${curPage - 2 < 1 ? 1 : curPage - 2}"/>
                            <c:set var="hi" value="${curPage + 2 > totalPages ? totalPages : curPage + 2}"/>
                            <nav class="cm-pager" aria-label="Phân trang">
                                <c:if test="${curPage > 1}">
                                    <c:url var="uPrev" value="/pages/seller/seller-custom"><c:param name="q" value="${q}"/><c:param name="page" value="${curPage - 1}"/></c:url>
                                    <a class="cm-page" href="${uPrev}" aria-label="Trang trước"><i class="fa-solid fa-chevron-left"></i></a>
                                </c:if>
                                <c:if test="${lo > 1}">
                                    <c:url var="uFirst" value="/pages/seller/seller-custom"><c:param name="q" value="${q}"/><c:param name="page" value="1"/></c:url>
                                    <a class="cm-page" href="${uFirst}">1</a>
                                    <c:if test="${lo > 2}"><span class="cm-page is-gap">…</span></c:if>
                                </c:if>
                                <c:forEach var="i" begin="${lo}" end="${hi}">
                                    <c:choose>
                                        <c:when test="${i == curPage}"><span class="cm-page is-current" aria-current="page">${i}</span></c:when>
                                        <c:otherwise>
                                            <c:url var="uPage" value="/pages/seller/seller-custom"><c:param name="q" value="${q}"/><c:param name="page" value="${i}"/></c:url>
                                            <a class="cm-page" href="${uPage}">${i}</a>
                                        </c:otherwise>
                                    </c:choose>
                                </c:forEach>
                                <c:if test="${hi < totalPages}">
                                    <c:if test="${hi < totalPages - 1}"><span class="cm-page is-gap">…</span></c:if>
                                    <c:url var="uLast" value="/pages/seller/seller-custom"><c:param name="q" value="${q}"/><c:param name="page" value="${totalPages}"/></c:url>
                                    <a class="cm-page" href="${uLast}">${totalPages}</a>
                                </c:if>
                                <c:if test="${curPage < totalPages}">
                                    <c:url var="uNext" value="/pages/seller/seller-custom"><c:param name="q" value="${q}"/><c:param name="page" value="${curPage + 1}"/></c:url>
                                    <a class="cm-page" href="${uNext}" aria-label="Trang sau"><i class="fa-solid fa-chevron-right"></i></a>
                                </c:if>
                            </nav>
                        </c:if>
                    </div>

                    <aside class="cm-aside" aria-label="Thông tin hữu ích">
                        <div class="cm-box">
                            <h4><i class="fa-solid fa-clock-rotate-left"></i> Báo giá gần đây</h4>
                            <c:choose>
                                <c:when test="${empty myQuotes}">
                                    <p class="cm-small-note">Bạn chưa gửi báo giá nào. Chọn một yêu cầu bên trái và bấm "Nhận làm" để bắt đầu.</p>
                                </c:when>
                                <c:otherwise>
                                    <ul class="cm-recent">
                                        <c:forEach var="rq" items="${myQuotes}" end="2">
                                            <li>
                                                <div>
                                                    <span class="cm-recent-title"><c:out value="${rq.requestTitle}"/></span>
                                                    <span class="cm-recent-price"><fmt:formatNumber value="${rq.price}" pattern="#,##0"/>đ · ${rq.days} ngày</span>
                                                </div>
                                                <c:choose>
                                                    <c:when test="${rq.status eq 'pending'}"><span class="cm-badge warn">Đang chờ</span></c:when>
                                                    <c:when test="${rq.status eq 'accepted'}"><span class="cm-badge ok">Được chọn</span></c:when>
                                                    <c:otherwise><span class="cm-badge muted">${rq.status eq 'rejected' ? 'Không chọn' : 'Đã rút'}</span></c:otherwise>
                                                </c:choose>
                                            </li>
                                        </c:forEach>
                                    </ul>
                                    <a class="cm-box-link" href="${pagePath}?tab=mine">Xem tất cả báo giá <i class="fa-solid fa-arrow-right"></i></a>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="cm-box">
                            <h4><i class="fa-regular fa-lightbulb"></i> Mẹo để được khách chọn</h4>
                            <ul class="cm-tips">
                                <li><i class="fa-solid fa-bolt"></i><span>Báo giá sớm: khách thường xem những báo giá đầu tiên.</span></li>
                                <li><i class="fa-solid fa-image"></i><span>Gắn link portfolio để khách thấy sản phẩm thật của bạn.</span></li>
                                <li><i class="fa-solid fa-pen"></i><span>Ghi rõ chất liệu và cam kết trong phần ghi chú.</span></li>
                                <li><i class="fa-solid fa-scale-balanced"></i><span>Báo giá sát với giá dự kiến của khách sẽ dễ được chọn hơn.</span></li>
                            </ul>
                        </div>

                        <div class="cm-box cm-rules">
                            <h4><i class="fa-solid fa-circle-info"></i> Quy định báo giá</h4>
                            <ul class="cm-tips">
                                <li><i class="fa-solid fa-coins"></i><span>Giá nhận làm từ 1,000đ đến 1,000,000,000đ.</span></li>
                                <li><i class="fa-solid fa-calendar-day"></i><span>Hoàn thành tối đa 15 ngày.</span></li>
                                <li><i class="fa-solid fa-rotate-left"></i><span>Chỉ rút được báo giá khi đang ở trạng thái chờ.</span></li>
                            </ul>
                        </div>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </section>
</main>

<jsp:include page="/components/footer.html"/>

<!-- Popup Form Báo Giá -->
<div class="cm-modal cm-scope" id="quoteModal" role="dialog" aria-modal="true" aria-labelledby="quoteTitle">
    <div class="cm-dialog">
        <div class="cm-dialog-head">
            <h2 id="quoteTitle">Gửi báo giá</h2>
            <button type="button" class="cm-x" onclick="closeQuote()" aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <form id="quoteForm" method="post" action="${pagePath}" class="cm-dialog-body">
            <input type="hidden" name="action" value="quote">
            <input type="hidden" name="requestId" id="qReqId">

            <p class="cm-forwho">
                Đang báo giá cho: <strong id="modalReqName"></strong><br>
                Tên thợ, đánh giá và số đơn hoàn thành lấy từ hồ sơ gian hàng
                (<strong><c:out value="${shop.name}"/></strong>).
            </p>

            <div class="cm-form-row">
                <div class="cm-field">
                    <label for="quotePrice">Giá nhận làm (VNĐ)</label>
                    <input type="number" id="quotePrice" name="price" min="1000" step="1000" placeholder="VD: 110000" required>
                </div>
                <div class="cm-field">
                    <label for="quoteDays">Số ngày hoàn thành</label>
                    <input type="number" id="quoteDays" name="days" min="1" max="15" placeholder="Tối đa 15 ngày" required>
                </div>
            </div>

            <div class="cm-field">
                <label for="quotePortfolio">Portfolio <span class="cm-hint">(không bắt buộc)</span></label>
                <input type="url" id="quotePortfolio" name="portfolio" maxlength="255" placeholder="https://example.com/portfolio">
            </div>

            <div class="cm-field">
                <label for="quoteNote">Ghi chú cho khách <span class="cm-hint">(không bắt buộc)</span></label>
                <textarea id="quoteNote" name="note" maxlength="1000" placeholder="Mô tả chất liệu bạn dùng, cam kết..."></textarea>
            </div>

            <div class="cm-actions">
                <button type="button" class="cm-btn ghost" onclick="closeQuote()">Hủy</button>
                <button type="submit" class="cm-btn primary" id="quoteSubmit">Gửi báo giá</button>
            </div>
        </form>
    </div>
</div>

<!-- Popup Chi tiết Yêu cầu -->
<div class="cm-modal cm-scope" id="detailModal" role="dialog" aria-modal="true" aria-labelledby="detailHeading">
    <div class="cm-dialog wide">
        <div class="cm-dialog-head">
            <h2 id="detailHeading">Chi tiết thiết kế custom</h2>
            <button type="button" class="cm-x" onclick="closeDetail()" aria-label="Đóng"><i class="fa-solid fa-xmark"></i></button>
        </div>
        <div class="cm-detail">
            <div class="cm-detail-img"><img id="detailImg" src="" alt="Sản phẩm custom"></div>
            <div class="cm-detail-info">
                <h3 id="detailTitle"></h3>
                <div class="cm-detail-meta">
                    <span><i class="fa-solid fa-user"></i> Người đặt: <strong id="detailBuyer"></strong></span>
                    <span><i class="fa-regular fa-clock"></i> <span id="detailDate"></span></span>
                </div>
                <div>
                    <span class="cm-detail-label">Mô tả chi tiết</span>
                    <p id="detailDesc" class="cm-detail-desc"></p>
                </div>
                <div>
                    <span class="cm-detail-label">Thành phần yêu cầu</span>
                    <div id="detailTags" class="cm-chips"></div>
                </div>
                <div class="cm-detail-price">
                    <span>Giá dự kiến</span>
                    <span id="detailPrice"></span>
                </div>
                <button type="button" class="cm-btn primary block" id="detailQuoteBtn" style="height:44px;">Báo giá ngay cho đơn này</button>
            </div>
        </div>
    </div>
</div>

<script>
    var quoteModal = document.getElementById('quoteModal');
    var detailModal = document.getElementById('detailModal');

    function lockScroll() { document.body.classList.toggle('cm-lock', quoteModal.classList.contains('is-open') || detailModal.classList.contains('is-open')); }

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
        document.getElementById('quoteTitle').textContent = d.myPrice ? 'Sửa báo giá' : 'Gửi báo giá';
        quoteModal.classList.add('is-open');
        lockScroll();
        document.getElementById('quotePrice').focus();
    }
    function closeQuote() { quoteModal.classList.remove('is-open'); lockScroll(); }

    function openDetail(card) {
        var d = card.dataset;
        var img = document.getElementById('detailImg');
        img.src = d.img || '';
        img.parentElement.style.display = d.img ? '' : 'none';
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
        detailModal.classList.add('is-open');
        lockScroll();
    }
    function closeDetail() { detailModal.classList.remove('is-open'); lockScroll(); }

    // Lọc / sắp xếp các thẻ đang hiển thị trên trang hiện tại
    (function () {
        var grid = document.getElementById('cmGrid');
        var count = document.getElementById('cmViewCount');
        var emptyEl = document.getElementById('cmFilterEmpty');
        if (!grid || !count) return;
        var cards = Array.prototype.slice.call(grid.children);
        var state = { filter: 'all', sort: 'default' };

        function render() {
            var list = cards.slice();
            if (state.sort === 'default') {
                list.sort(function (a, b) { return a.dataset.idx - b.dataset.idx; });
            } else {
                list.sort(function (a, b) {
                    var x = parseFloat(a.dataset.budget) || 0, y = parseFloat(b.dataset.budget) || 0;
                    return state.sort === 'high' ? y - x : x - y;
                });
            }
            var shown = 0;
            list.forEach(function (c) {
                var quoted = c.dataset.quoted === 'yes';
                var ok = state.filter === 'all' || (state.filter === 'quoted' && quoted) || (state.filter === 'new' && !quoted);
                c.hidden = !ok;
                if (ok) shown++;
                grid.appendChild(c);
            });
            count.textContent = 'Hiển thị ' + shown + '/' + cards.length + ' yêu cầu trên trang này';
            if (emptyEl) emptyEl.hidden = shown !== 0;
        }

        document.querySelectorAll('.cm-seg-btn').forEach(function (btn) {
            btn.addEventListener('click', function () {
                document.querySelectorAll('.cm-seg-btn').forEach(function (b) { b.classList.remove('is-active'); });
                btn.classList.add('is-active');
                state.filter = btn.dataset.filter;
                render();
            });
        });
        document.getElementById('cmSort').addEventListener('change', function (e) { state.sort = e.target.value; render(); });
        render();
    })();

    // Bấm ra ngoài hộp hoặc nhấn Esc để đóng
    quoteModal.addEventListener('click', function (e) { if (e.target === quoteModal) closeQuote(); });
    detailModal.addEventListener('click', function (e) { if (e.target === detailModal) closeDetail(); });
    document.addEventListener('keydown', function (e) { if (e.key === 'Escape') { closeQuote(); closeDetail(); } });
</script>
<script src="${ctx}/assets/js/popup.js"></script>
<script src="${ctx}/assets/js/auth.js"></script>
</body>
</html>
