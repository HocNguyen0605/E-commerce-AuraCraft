-- Thêm cột status vào bảng account
ALTER TABLE `account` ADD COLUMN `status` ENUM('active', 'pending', 'locked') NOT NULL DEFAULT 'active';

-- Cập nhật đồng bộ status cho thợ (seller) nếu cần thiết
UPDATE `account` a
JOIN `shops` s ON a.id_user = s.id_owner
SET a.status = s.status
WHERE a.role = 'seller';
