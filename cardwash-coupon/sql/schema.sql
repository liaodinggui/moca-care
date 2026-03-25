-- 洗车优惠券系统数据库初始化脚本
-- MySQL 8.0+

CREATE DATABASE IF NOT EXISTS cardwash_coupon DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cardwash_coupon;

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `openid` VARCHAR(64) NOT NULL COMMENT '微信 openid',
    `unionid` VARCHAR(64) DEFAULT NULL COMMENT '微信 unionid',
    `nickname` VARCHAR(64) DEFAULT NULL COMMENT '昵称',
    `avatar` VARCHAR(512) DEFAULT NULL COMMENT '头像 URL',
    `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `role` TINYINT DEFAULT 1 COMMENT '角色：1-客户，2-商家',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_openid` (`openid`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 商家表
DROP TABLE IF EXISTS `merchant`;
CREATE TABLE `merchant` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '关联用户 ID',
    `name` VARCHAR(128) NOT NULL COMMENT '商家名称',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商家表';

-- 洗车券类型表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `merchant_id` BIGINT NOT NULL COMMENT '商家 ID',
    `name` VARCHAR(64) NOT NULL COMMENT '券名称',
    `description` TEXT COMMENT '描述',
    `images` JSON DEFAULT NULL COMMENT '图片 URL 数组',
    `price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价',
    `buy_amount` INT DEFAULT 0 COMMENT '买 X 张',
    `send_amount` INT DEFAULT 0 COMMENT '送 Y 张',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架，1-上架',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_merchant_id` (`merchant_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='洗车券类型表';

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_no` VARCHAR(32) NOT NULL COMMENT '订单号（唯一）',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `coupon_id` BIGINT NOT NULL COMMENT '洗车券 ID',
    `coupon_name` VARCHAR(64) NOT NULL COMMENT '洗车券名称（冗余）',
    `coupon_price` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '单价（冗余）',
    `total_quantity` INT NOT NULL DEFAULT 0 COMMENT '总数量（含赠送）',
    `paid_quantity` INT NOT NULL DEFAULT 0 COMMENT '实际支付数量',
    `send_quantity` INT NOT NULL DEFAULT 0 COMMENT '赠送数量',
    `used_quantity` INT NOT NULL DEFAULT 0 COMMENT '已核销数量',
    `total_amount` DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '订单总额',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0-待付款，1-使用中，2-已完成，3-已取消',
    `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- 洗车券核销记录表
DROP TABLE IF EXISTS `coupon_write_off`;
CREATE TABLE `coupon_write_off` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `merchant_id` BIGINT NOT NULL COMMENT '核销商家 ID',
    `quantity` INT NOT NULL DEFAULT 0 COMMENT '核销数量',
    `write_off_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '核销时间',
    `operator_id` BIGINT NOT NULL COMMENT '操作人 ID',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_write_off_time` (`write_off_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='洗车券核销记录表';

-- 二维码令牌表
DROP TABLE IF EXISTS `qrcode_token`;
CREATE TABLE `qrcode_token` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键 ID',
    `order_id` BIGINT NOT NULL COMMENT '订单 ID',
    `token` VARCHAR(128) NOT NULL COMMENT '加密令牌',
    `expire_time` DATETIME NOT NULL COMMENT '过期时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-无效，1-有效',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_order_id` (`order_id`),
    KEY `idx_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='二维码令牌表';

-- 删除唯一索引
ALTER TABLE `merchant` DROP INDEX `uk_user_id`;

-- 添加普通索引
ALTER TABLE `merchant` ADD INDEX `idx_user_id` (`user_id`);
