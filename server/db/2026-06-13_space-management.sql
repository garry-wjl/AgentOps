-- ============================================================
-- AgentOps - Space module schema
-- 配套：feature-20260613-space-management
-- 说明：本脚本仅创建空间主表，并迁移 user 表 create_no/update_no 类型
-- ============================================================

-- ------------------------------------------------------------
-- 1. 空间主表（唯一表，成员关系内联在 admin_user_codes / member_user_codes JSON 列中）
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `spaces` (
  `id`                BIGINT       NOT NULL AUTO_INCREMENT,
  `num`               VARCHAR(32)  NOT NULL                          COMMENT '业务编码 SP+yyyyMMddHHmmssSSS+4 位随机数',
  `name`              VARCHAR(50)  NOT NULL                          COMMENT '空间名称',
  `description`       VARCHAR(500) DEFAULT NULL                      COMMENT '空间描述',
  `icon_url`          VARCHAR(500) DEFAULT NULL                      COMMENT 'Logo URL',
  `owner_user_code`   VARCHAR(32)  NOT NULL                          COMMENT '空间所有者用户业务编码（user.num）',
  `status`            TINYINT(1)   NOT NULL DEFAULT 1                COMMENT '1=ENABLED',
  `admin_user_codes`  JSON         NOT NULL                          COMMENT '管理员用户业务编码列表（含 owner），如 ["US...","US..."]',
  `member_user_codes` JSON         NOT NULL                          COMMENT '普通成员用户业务编码列表',
  `create_no`         VARCHAR(32)  NOT NULL                          COMMENT '创建人用户业务编码',
  `update_no`         VARCHAR(32)  NOT NULL                          COMMENT '最近更新人用户业务编码',
  `create_time`       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time`       DATETIME(3)  NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted`        TINYINT(1)   NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_name_deleted` (`name`, `is_deleted`),
  KEY `idx_owner` (`owner_user_code`),
  /* MySQL 8.0.17+ 多值索引，加速 JSON_CONTAINS(admin_user_codes, JSON_QUOTE(?)) 查询；
     部署的 MySQL 版本不支持时可注释掉这两行（不影响功能） */
  KEY `idx_admin_user_codes` ((CAST(`admin_user_codes` AS CHAR(32) ARRAY))),
  KEY `idx_member_user_codes` ((CAST(`member_user_codes` AS CHAR(32) ARRAY)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间主表';


-- ------------------------------------------------------------
-- 2. 修改 users 表：create_no / update_no 类型从 BIGINT 改为 VARCHAR(32)
--    （V1.5 跨领域引用统一使用业务编码，user 模块中的审计字段从 user.id 改为 user.num）
-- ------------------------------------------------------------
-- 步骤 2.1：把现有 BIGINT 值映射成对应 user.num 字符串。
--           对于已有数据：先把 user.id (Long) 用对应的 user.num (String) 回填。
UPDATE `users` u1
JOIN   `users` u2 ON u1.create_no = u2.id
SET    u1.create_no = u2.num;

UPDATE `users` u1
JOIN   `users` u2 ON u1.update_no = u2.id
SET    u1.update_no = u2.num;

-- 步骤 2.2：修改列类型。
ALTER TABLE `users`
    MODIFY COLUMN `create_no` VARCHAR(32) NOT NULL COMMENT '创建人用户业务编码',
    MODIFY COLUMN `update_no` VARCHAR(32) NOT NULL COMMENT '最近更新人用户业务编码';
