// Chatbot AuraBot - Trợ lý tư vấn AuraCraft
(function () {
    if (window.location.pathname.includes('/admin/')) return;

    // Tìm context path (ví dụ: "" hoặc "/E_Commerce_Final_exam")
    const ctx = (function () {
        if (window.CTX) return window.CTX;
        if (window.appContextPath) return window.appContextPath;
        const p = window.location.pathname.split('/').filter(Boolean);
        return (p.length > 0 && p[0] !== 'pages') ? '/' + p[0] : '';
    })();

    // Tạo HTML cho khung chat
    function initChatWidget() {
        if (document.getElementById('auraChatWidget')) return;

        // Tự nạp CSS nếu chưa có
        if (!document.getElementById('aura-chat-css')) {
            const link = document.createElement('link');
            link.id = 'aura-chat-css';
            link.rel = 'stylesheet';
            link.href = ctx + '/assets/css/chatbot.css';
            document.head.appendChild(link);
        }

        const div = document.createElement('div');
        div.id = 'auraChatWidget';
        div.innerHTML = `
            <div class="aura-chat-launcher" id="auraLauncher">
                <span class="aura-chat-badge" id="auraBadge">Tư vấn chọn quà? Nhắn mình nhé!</span>
                <i class="fa-solid fa-comments"></i>
            </div>
            <div class="aura-chat-window" id="auraWindow">
                <div class="aura-chat-header">
                    <div class="aura-chat-bot-info">
                        <div class="aura-chat-avatar"><i class="fa-solid fa-wand-magic-sparkles"></i></div>
                        <div class="aura-chat-title-group">
                            <h3>AuraBot</h3>
                            <span><span class="aura-chat-status-dot"></span> Trực tuyến</span>
                        </div>
                    </div>
                    <div class="aura-chat-controls">
                        <button class="aura-chat-btn-icon" id="auraCloseBtn" title="Đóng"><i class="fa-solid fa-xmark"></i></button>
                    </div>
                </div>
                <div class="aura-chat-messages" id="auraMessages">
                    <div class="aura-chat-msg bot">
                        <div class="aura-chat-bubble">
                            Chào bạn! Mình là <b>AuraBot</b>, trợ lý của AuraCraft. Mình có thể giúp bạn chọn quà handmade hoặc giải đáp thắc mắc nhé!
                            <div class="aura-chat-quick-replies">
                                <button class="aura-quick-chip" onclick="window.auraQuickSend('Gợi ý quà tặng bạn nữ')">Quà tặng bạn nữ</button>
                                <button class="aura-quick-chip" onclick="window.auraQuickSend('Chính sách freeship của shop')">Chính sách freeship</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="aura-chat-footer">
                    <div class="aura-chat-input-wrapper">
                        <input type="text" class="aura-chat-input" id="auraInput" placeholder="Nhập câu hỏi của bạn...">
                        <button class="aura-chat-send-btn" id="auraSendBtn"><i class="fa-solid fa-paper-plane"></i></button>
                    </div>
                </div>
            </div>
        `;
        document.body.appendChild(div);

        // Bắt sự kiện mở / đóng
        const launcher = document.getElementById('auraLauncher');
        const chatWindow = document.getElementById('auraWindow');
        const closeBtn = document.getElementById('auraCloseBtn');
        const badge = document.getElementById('auraBadge');

        launcher.onclick = function () {
            chatWindow.classList.toggle('active');
            if (badge) badge.style.display = 'none';
        };
        closeBtn.onclick = function () {
            chatWindow.classList.remove('active');
        };

        // Bắt sự kiện gửi tin nhắn
        const input = document.getElementById('auraInput');
        const sendBtn = document.getElementById('auraSendBtn');

        sendBtn.onclick = handleSend;
        input.onkeydown = function (e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                handleSend();
            }
        };
    }

    // Gửi tin nhắn từ câu hỏi nhanh
    window.auraQuickSend = function (text) {
        const input = document.getElementById('auraInput');
        if (input) {
            input.value = text;
            handleSend();
        }
    };

    // Hàm thêm tin nhắn vào màn hình
    function addMessage(sender, text) {
        const box = document.getElementById('auraMessages');
        if (!box) return;

        const msgDiv = document.createElement('div');
        msgDiv.className = 'aura-chat-msg ' + sender;

        // Định dạng xuống dòng và chữ đậm đơn giản
        let formatted = text
            .replace(/\*\*(.*?)\*\*/g, '<b>$1</b>')
            .replace(/\n/g, '<br>');

        msgDiv.innerHTML = `<div class="aura-chat-bubble">${formatted}</div>`;
        box.appendChild(msgDiv);
        box.scrollTop = box.scrollHeight;
    }

    // Xử lý gửi tin nhắn lên server
    function handleSend() {
        const input = document.getElementById('auraInput');
        const text = input.value.trim();
        if (!text) return;

        input.value = '';
        addMessage('user', text);

        // Hiển thị trạng thái đang trả lời
        const box = document.getElementById('auraMessages');
        const loadingDiv = document.createElement('div');
        loadingDiv.id = 'auraLoading';
        loadingDiv.className = 'aura-chat-msg bot';
        loadingDiv.innerHTML = '<div class="aura-chat-bubble"><i>AuraBot đang trả lời...</i></div>';
        box.appendChild(loadingDiv);
        box.scrollTop = box.scrollHeight;

        // Gọi Servlet backend
        fetch(ctx + '/api/chat', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json; charset=UTF-8' },
            body: JSON.stringify({ message: text })
        })
            .then(res => res.json())
            .then(data => {
                const loading = document.getElementById('auraLoading');
                if (loading) loading.remove();
                addMessage('bot', data.reply || 'Xin lỗi, mình chưa hiểu ý bạn.');
            })
            .catch(err => {
                const loading = document.getElementById('auraLoading');
                if (loading) loading.remove();
                addMessage('bot', 'Lỗi kết nối mạng, bạn thử lại sau nhé!');
            });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initChatWidget);
    } else {
        initChatWidget();
    }
})();
