window.alert = function(message) {
    let popup = document.getElementById('global-popup');
    if (!popup) {
        popup = document.createElement('div');
        popup.id = 'global-popup';
        popup.innerHTML = `
            <div style="background: white; padding: 24px; border-radius: 12px; max-width: 400px; text-align: center; box-shadow: 0 10px 25px rgba(0,0,0,0.2); font-family: 'Mulish', sans-serif;">
                <div style="font-size: 32px; color: #425B9A; margin-bottom: 15px;">
                    <i class="fa-solid fa-bell"></i>
                </div>
                <p id="global-popup-message" style="margin-bottom: 24px; color: #333; font-size: 16px; line-height: 1.5;"></p>
                <button id="global-popup-close" style="padding: 8px 24px; background: #425B9A; color: white; border: none; border-radius: 6px; cursor: pointer; font-family: 'Baloo 2', cursive; font-size: 16px;">Đóng</button>
            </div>
        `;
        Object.assign(popup.style, {
            position: 'fixed',
            top: '0',
            left: '0',
            width: '100vw',
            height: '100vh',
            backgroundColor: 'rgba(0,0,0,0.6)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: '99999',
            opacity: '0',
            visibility: 'hidden',
            transition: 'all 0.3s ease',
            backdropFilter: 'blur(4px)'
        });
        document.body.appendChild(popup);
        
        document.getElementById('global-popup-close').addEventListener('click', () => {
            popup.style.opacity = '0';
            popup.style.visibility = 'hidden';
        });
    }
    document.getElementById('global-popup-message').innerText = message;
    popup.style.visibility = 'visible';
    popup.style.opacity = '1';
};

// Kiểm tra và cập nhật trạng thái đăng nhập cho Header
document.addEventListener("DOMContentLoaded", () => {
    setInterval(() => {
        const currentUser = localStorage.getItem("currentUser");
        const navLogin = document.getElementById("nav-login");
        const navProfile = document.getElementById("nav-profile");
        if (navLogin && navProfile) {
            if (currentUser) {
                navLogin.style.display = "none";
                navProfile.style.display = "flex";
            } else {
                navLogin.style.display = "inline-block";
                navProfile.style.display = "none";
            }
        }
    }, 500); // Kiểm tra mỗi 500ms vì header được load bất đồng bộ
});
