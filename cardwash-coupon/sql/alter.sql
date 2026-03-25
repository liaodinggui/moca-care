-- 修改商家表结构，支持一个用户拥有多个店铺
-- 删除唯一索引 uk_user_id，改为普通索引

USE cardwash_coupon;

-- 删除唯一索引
ALTER TABLE `merchant` DROP INDEX `uk_user_id`;

-- 添加普通索引
ALTER TABLE `merchant` ADD INDEX `idx_user_id` (`user_id`);

-- 验证修改
SHOW INDEX FROM `merchant`;
