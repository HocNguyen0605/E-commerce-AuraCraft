
(function (global) {
    "use strict";
    const DATA_URL = "https://raw.githubusercontent.com/mantisvn/Vietnamese-Administrative-Units-Dataset/main/Vietnamese-Administrative-Units-Dataset.json";
    const CACHE_KEY = "AuraCraftAdministrativeWards2025"; // trùng key với checkout.js để dùng chung cache
    const EXPECTED_COUNT = 3321;

    let cache = null;
    let pending = null;

    function loadAllWards() {
        if (cache) return Promise.resolve(cache);
        if (pending) return pending;
        pending = (async () => {
            let list = [];
            try { list = JSON.parse(sessionStorage.getItem(CACHE_KEY) || "[]"); } catch (_) { list = []; }
            if (!Array.isArray(list) || list.length !== EXPECTED_COUNT) {
                const res = await fetch(DATA_URL, { cache: "force-cache" });
                if (!res.ok) throw new Error("Không tải được danh sách xã / phường.");
                const dataset = await res.json();
                list = Array.isArray(dataset.wards) ? dataset.wards : [];
                if (list.length !== EXPECTED_COUNT) throw new Error("Danh sách đơn vị hành chính không đầy đủ.");
                try { sessionStorage.setItem(CACHE_KEY, JSON.stringify(list)); } catch (_) { }
            }
            cache = list;
            return list;
        })();
        pending.catch(() => { pending = null; }); // cho phép thử lại lần sau nếu lỗi mạng
        return pending;
    }

    function init(o) {
        const { province, ward, fallback, provinceName, wardName } = o;
        let token = 0; // chống race-condition khi đổi tỉnh liên tục

        const usingFallback = () => ward.hidden;

        function syncNames() {
            provinceName.value = province.value ? province.selectedOptions[0].textContent.trim() : "";
            wardName.value = usingFallback()
                ? fallback.value.trim()
                : (ward.value ? ward.selectedOptions[0].textContent.trim() : "");
        }

        function resetWard() {
            ward.hidden = false;
            ward.required = true;
            ward.replaceChildren(new Option("Vui lòng chọn tỉnh / thành phố trước", ""));
            ward.disabled = true;
            fallback.hidden = true;
            fallback.required = false;
            fallback.value = "";
            syncNames();
        }

        async function loadWards(provinceCode, wardCode, savedWardName) {
            const my = ++token;
            if (!provinceCode) { resetWard(); return; }
            ward.hidden = false;
            ward.required = true;
            ward.disabled = true;
            fallback.hidden = true;
            fallback.required = false;
            ward.replaceChildren(new Option("Đang tải danh sách xã / phường...", ""));
            try {
                const all = await loadAllWards();
                if (my !== token) return;
                const options = all
                    .filter(w => String(w.province_id) === String(Number(provinceCode)))
                    .sort((a, b) => a.name.localeCompare(b.name, "vi"));
                if (!options.length) throw new Error("Không có dữ liệu xã / phường cho tỉnh đã chọn.");
                ward.replaceChildren(new Option("Chọn xã / phường", ""));
                options.forEach(w => ward.add(new Option(w.name, w.code)));
                ward.disabled = false;
                // Khôi phục xã đã lưu: ưu tiên theo mã, không có thì khớp theo tên
                let match = wardCode ? options.find(w => String(w.code) === String(wardCode)) : null;
                if (!match && savedWardName) {
                    match = options.find(w => w.name.toLocaleLowerCase("vi") === savedWardName.toLocaleLowerCase("vi"));
                }
                if (match) ward.value = match.code;
            } catch (err) {
                if (my !== token) return;
                ward.replaceChildren(new Option("Không tải được danh sách", ""));
                ward.hidden = true;
                ward.disabled = true;
                ward.required = false;
                fallback.hidden = false;
                fallback.required = true;
                fallback.value = savedWardName || "";
            }
            syncNames();
        }

        province.addEventListener("change", () => { loadWards(province.value); syncNames(); });
        ward.addEventListener("change", syncNames);
        fallback.addEventListener("input", syncNames);

        resetWard();
        return {
            setValues(provinceCode, wardCode, savedWardName) {
                province.value = provinceCode || "";
                if (province.value !== (provinceCode || "")) province.value = ""; // mã không có trong danh sách
                return loadWards(province.value, wardCode, savedWardName).then(syncNames);
            },
            reset() { province.value = ""; resetWard(); },
            syncNames
        };
    }

    global.VnAddressPicker = { init };
})(window);