window.CTX = window.CTX || location.pathname.replace(/\/(pages\/[^/]*|index\.(html|jsp))?$/, '');
if (!document.getElementById('aura-message-css')) {
    const link = document.createElement('link');
    link.id = 'aura-message-css';
    link.rel = 'stylesheet';
    link.href = window.CTX + '/assets/css/message.css';
    document.head.appendChild(link);
}
const div = document.createElement('div');
div.id = 'auraMessage';
div.innerHTML = `
             <button class="aura-msg-launcher" id="auraMsgBtn" title="Tin nhắn">
        <i class="fa-solid fa-envelope"></i>
    </button>
        `;
document.body.appendChild(div);