<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<fmt:setLocale value="vi_VN"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sellerTab" value="chat" scope="request"/>
<c:set var="defaultAvatar" value="https://i.pravatar.cc/150?img=12"/>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Chat với Khách hàng - Kênh Thợ</title>
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.0/css/all.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/style.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/header-footer.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/pages/styleChat.css">
    <link rel="stylesheet" href="${ctx}/src/main/webapp/assets/css/pages/styleSeller.css">
    <style>
        .chat-item { cursor: pointer; }
        .chat-search form { display:flex; align-items:center; width:100%; }
        .chat-empty { padding: 30px; text-align: center; color: #888; }
    </style>
</head>
<body>
<jsp:include page="/src/main/webapp/components/header.html"/>

<main class="seller-layout container">
    <%@ include file="/src/main/webapp/components/seller-sidebar.jspf" %>

    <section class="seller-content">
        <div class="chat-layout" style="height: 75vh; min-height: 600px; border-radius: 20px; overflow: hidden; box-shadow: var(--shadow);">

            <!-- Danh sách hội thoại -->
            <aside class="chat-sidebar" style="border-right: 1px solid var(--gray-light);">
                <div class="chat-search">
                    <form action="${ctx}/seller/chat" method="GET">
                        <i class="bi bi-search"></i>
                        <input type="text" name="q" value="<c:out value='${param.q}'/>" placeholder="Tìm tên khách hàng...">
                    </form>
                </div>
                <ul class="chat-list">
                    <c:forEach var="cv" items="${conversations}">
                        <c:choose>
                            <c:when test="${empty cv.buyerAvatar}"><c:set var="cvImg" value="${defaultAvatar}"/></c:when>
                            <c:when test="${fn:startsWith(cv.buyerAvatar, 'http')}"><c:set var="cvImg" value="${cv.buyerAvatar}"/></c:when>
                            <c:otherwise><c:set var="cvImg" value="${ctx}/${cv.buyerAvatar}"/></c:otherwise>
                        </c:choose>
                        <li class="chat-item ${not empty active and active.id == cv.id ? 'active' : ''}"
                            onclick="location.href='${ctx}/seller/chat?c=${cv.id}'">
                            <img src="${cvImg}" alt="Avatar" class="avatar">
                            <div class="chat-info">
                                <h4><c:out value="${cv.buyerName}"/></h4>
                                <p><c:out value="${cv.lastMessage}"/></p>
                            </div>
                            <span class="chat-time"><fmt:formatDate value="${cv.lastMessageAt}" pattern="HH:mm"/></span>
                        </li>
                    </c:forEach>
                    <c:if test="${empty conversations}">
                        <li class="chat-empty">Chưa có hội thoại nào.</li>
                    </c:if>
                </ul>
            </aside>

            <!-- Khung chat chính -->
            <section class="chat-main">
                <c:choose>
                    <c:when test="${not empty active}">
                        <c:choose>
                            <c:when test="${empty active.buyerAvatar}"><c:set var="activeImg" value="${defaultAvatar}"/></c:when>
                            <c:when test="${fn:startsWith(active.buyerAvatar, 'http')}"><c:set var="activeImg" value="${active.buyerAvatar}"/></c:when>
                            <c:otherwise><c:set var="activeImg" value="${ctx}/${active.buyerAvatar}"/></c:otherwise>
                        </c:choose>

                        <div class="chat-header">
                            <div class="user-info">
                                <img src="${activeImg}" alt="Avatar" class="avatar">
                                <div>
                                    <h3><c:out value="${active.buyerName}"/></h3>
                                </div>
                            </div>
                            <div class="chat-actions">
                                <button type="button" class="icon-btn-ghost"><i class="bi bi-three-dots-vertical"></i></button>
                            </div>
                        </div>

                        <div class="chat-messages" id="customMessages">
                            <c:forEach var="m" items="${messages}">
                                <div class="message ${m.sent ? 'sent' : 'received'}">
                                    <c:if test="${not m.sent}">
                                        <img src="${activeImg}" alt="Avatar" class="avatar-sm">
                                    </c:if>
                                    <div class="msg-bubble">
                                        <c:out value="${m.content}"/>
                                        <span class="msg-time"><fmt:formatDate value="${m.createdAt}" pattern="HH:mm"/></span>
                                    </div>
                                </div>
                            </c:forEach>
                            <c:if test="${empty messages}">
                                <div class="chat-empty">Chưa có tin nhắn nào.</div>
                            </c:if>
                        </div>

                        <form action="${ctx}/seller/chat" method="POST" class="chat-input-area">
                            <input type="hidden" name="action" value="send">
                            <input type="hidden" name="conversationId" value="${active.id}">
                            <button type="button" class="icon-btn-ghost"><i class="bi bi-paperclip"></i></button>
                            <button type="button" class="icon-btn-ghost"><i class="bi bi-emoji-smile"></i></button>
                            <input type="text" name="content" placeholder="Nhập tin nhắn tư vấn..." class="chat-input"
                                   id="customMessageInput" required maxlength="2000" autocomplete="off">
                            <button type="submit" class="btn-send" aria-label="Gửi tin nhắn"><i class="bi bi-send-fill"></i></button>
                        </form>
                        <section class="custom-change-requests" id="customChangeRequests" aria-live="polite"></section>
                    </c:when>
                    <c:otherwise>
                        <div class="chat-empty" style="margin:auto;">Chọn một hội thoại để bắt đầu.</div>
                    </c:otherwise>
                </c:choose>
            </section>
        </div>
    </section>
</main>

<jsp:include page="/src/main/webapp/components/footer.html"/>
<script src="${ctx}/src/main/webapp/assets/js/popup.js"></script>
<script>
    var box = document.getElementById('customMessages');
    if (box) box.scrollTop = box.scrollHeight;
</script>
<script src="assets/js/auth.js"></script>
</body>
</html>
