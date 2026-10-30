-- Thêm cột status vào bảng account
ALTER TABLE `account` ADD COLUMN `status` ENUM('active', 'pending', 'locked') NOT NULL DEFAULT 'active';

-- Cập nhật đồng bộ status cho thợ (seller) nếu cần thiết
UPDATE `account` a
JOIN `shops` s ON a.id_user = s.id_owner
SET a.status = s.status
WHERE a.role = 'seller';

-- Bảng quản lý sản phẩm nổi bật
CREATE TABLE IF NOT EXISTS `featured_products` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `product_id` INT UNSIGNED NOT NULL,
    `display_order` INT NOT NULL DEFAULT 1,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    KEY `idx_fp_product` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
