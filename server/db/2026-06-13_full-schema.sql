-- ============================================================
-- AgentOps 平台完整 DDL 汇总（2026-06-13）
-- 所有 8 个模块 + 基础设施表
-- 建表顺序：platform → space → votes → resolution → user-modeled outer joins
-- ============================================================

-- ------------------------------------------------------------
-- 0. 基础设施公共列变化：users 表 create_no/update_no 类型迁移
-- ------------------------------------------------------------
UPDATE `users` u1
JOIN   `users` u2 ON u1.create_no = u2.id
SET    u1.create_no = u2.num;

UPDATE `users` u1
JOIN   `users` u2 ON u1.update_no = u2.id
SET    u1.update_no = u2.num;

ALTER TABLE `users`
    MODIFY COLUMN `create_no` VARCHAR(32) NOT NULL COMMENT '创建人用户业务编码',
    MODIFY COLUMN `update_no` VARCHAR(32) NOT NULL COMMENT '最近更新人用户业务编码';


-- ------------------------------------------------------------
-- 1. system_settings — 系统设置
--    phase-1
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `system_settings` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `category` VARCHAR(50) NOT NULL COMMENT 'platform_basic/smtp/space_policy/sandbox_default',
  `setting_json` JSON NOT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_category_deleted` (`category`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统设置';

-- audit_logs — 审计日志
CREATE TABLE IF NOT EXISTS `audit_logs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `module` VARCHAR(50) NOT NULL,
  `action` VARCHAR(100) NOT NULL,
  `operator_code` VARCHAR(32) NOT NULL,
  `target_num` VARCHAR(32) DEFAULT NULL,
  `detail_json` TEXT,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  KEY `idx_module_time` (`module`, `create_time`),
  KEY `idx_operator` (`operator_code`),
  KEY `idx_target` (`target_num`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='审计日志';

-- 系统设置初始化种子
INSERT IGNORE INTO `system_settings`(`num`, `category`, `setting_json`, `create_no`, `update_no`)
VALUES
    ('SS00000000000000000001', 'platform_basic', '{"platformName":"AgentOps","logoUrl":"","encryptionKey":""}', 'SYSTEM', 'SYSTEM'),
    ('SS00000000000000000002', 'space_policy', '{"quotaPerUser":10,"namingRegex":"^[A-Za-z0-9_-]+$"}', 'SYSTEM', 'SYSTEM'),
    ('SS00000000000000000003', 'sandbox_default', '{"baseUrl":"","heartbeatIntervalSec":30}', 'SYSTEM', 'SYSTEM'),
    ('SS00000000000000000004', 'smtp', '{"host":"","port":465,"username":"","passwordCipher":"","from":"","ssl":true}', 'SYSTEM', 'SYSTEM')
ON DUPLICATE KEY UPDATE `num` = `num`;


-- ------------------------------------------------------------
-- 2. spaces — 空间（成员内联 JSON 列）
--    phase-1
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `spaces` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL COMMENT '业务编码 SP+ts+rand',
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `icon_url` VARCHAR(500) DEFAULT NULL,
  `owner_user_code` VARCHAR(32) NOT NULL COMMENT '空间所有者用户业务编码',
  `status` TINYINT(1) NOT NULL DEFAULT 1,
  `admin_user_codes` JSON NOT NULL COMMENT '管理员业务编码列表（含 owner），如 ["US...","US..."]',
  `member_user_codes` JSON NOT NULL COMMENT '普通成员业务编码列表',
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_name_deleted` (`name`, `is_deleted`),
  KEY `idx_owner` (`owner_user_code`),
  KEY `idx_admin_user_codes` ((CAST(`admin_user_codes` AS CHAR(32) ARRAY))),
  KEY `idx_member_user_codes` ((CAST(`member_user_codes` AS CHAR(32) ARRAY)))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='空间主表';


-- ------------------------------------------------------------
-- 3. models — 模型
--    phase-2a
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `models` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `model_id` VARCHAR(100) NOT NULL,
  `base_url` VARCHAR(500) NOT NULL,
  `api_key_cipher` VARCHAR(2048) NOT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  UNIQUE KEY `uk_space_model_id_deleted` (`space_code`, `model_id`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='模型';


-- ------------------------------------------------------------
-- 4. prompts — Prompt
--    phase-2b
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `prompts` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `key` VARCHAR(64) NOT NULL,
  `content` TEXT NOT NULL,
  `variables_json` JSON DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  UNIQUE KEY `uk_space_key_deleted` (`space_code`, `key`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Prompt';


-- ------------------------------------------------------------
-- 5. sandboxes — 沙箱
--    phase-2c
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `sandboxes` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `image` VARCHAR(200) NOT NULL,
  `base_url_override` VARCHAR(500) DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `last_status_reason` VARCHAR(200) DEFAULT NULL,
  `last_heartbeat_time` DATETIME(3) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  KEY `idx_status` (`status`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='沙箱';


-- ------------------------------------------------------------
-- 6. tools — 工具
--    phase-2d
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `tools` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `type` VARCHAR(20) NOT NULL COMMENT 'FUNCTION_CALL / MCP',
  `sub_type` VARCHAR(20) NOT NULL COMMENT 'OPENAPI / ENDPOINT / REMOTE / LOCAL',
  `description` VARCHAR(500) DEFAULT NULL,
  `tags_json` JSON DEFAULT NULL,
  `config_json` LONGTEXT NOT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0,
  `remark` VARCHAR(200) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`),
  KEY `idx_type` (`type`, `sub_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工具';


-- ------------------------------------------------------------
-- 7. skills + skill_versions + skill_resource_files
--    phase-3
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `skills` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(50) NOT NULL,
  `description` VARCHAR(500) NOT NULL,
  `current_version_no` VARCHAR(20) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=草稿 1=生效 2=下架',
  `tags_json` JSON DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Skill 主体';

CREATE TABLE IF NOT EXISTS `skill_versions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `skill_code` VARCHAR(32) NOT NULL,
  `version_no` VARCHAR(20) NOT NULL,
  `skill_md_content` MEDIUMTEXT NOT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=草稿 1=生效 2=下架',
  `publish_time` DATETIME(3) DEFAULT NULL,
  `withdraw_time` DATETIME(3) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_skill_version_deleted` (`skill_code`, `version_no`, `is_deleted`),
  KEY `idx_skill_status` (`skill_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Skill 版本';

CREATE TABLE IF NOT EXISTS `skill_resource_files` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `skill_version_code` VARCHAR(32) NOT NULL,
  `path` VARCHAR(512) NOT NULL,
  `type` TINYINT(1) NOT NULL COMMENT '1=FILE 2=FOLDER',
  `content` MEDIUMTEXT,
  `size_bytes` BIGINT DEFAULT 0,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_version_path_deleted` (`skill_version_code`, `path`, `is_deleted`),
  KEY `idx_version` (`skill_version_code`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Skill 资源文件';


-- ------------------------------------------------------------
-- 8. agents + agent_versions + ref 反查表
--    phase-4
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS `agents` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `space_code` VARCHAR(32) NOT NULL,
  `name` VARCHAR(64) NOT NULL,
  `display_name` VARCHAR(50) DEFAULT NULL,
  `description` VARCHAR(500) DEFAULT NULL,
  `current_version_no` VARCHAR(20) DEFAULT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=草稿 1=生效 2=下架',
  `tags_json` JSON DEFAULT NULL,
  `remark` VARCHAR(200) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_space_name_deleted` (`space_code`, `name`, `is_deleted`),
  KEY `idx_space_status` (`space_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 主体';

CREATE TABLE IF NOT EXISTS `agent_versions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `num` VARCHAR(32) NOT NULL,
  `agent_code` VARCHAR(32) NOT NULL,
  `version_no` VARCHAR(20) NOT NULL,
  `assembly_snapshot` LONGTEXT NOT NULL,
  `status` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '0=草稿 1=在线 2=离线',
  `online_time` DATETIME(3) DEFAULT NULL,
  `offline_time` DATETIME(3) DEFAULT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_num` (`num`),
  UNIQUE KEY `uk_agent_version_deleted` (`agent_code`, `version_no`, `is_deleted`),
  KEY `idx_agent_status` (`agent_code`, `status`, `is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 版本';

CREATE TABLE IF NOT EXISTS `agent_version_skill_refs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `agent_version_code` VARCHAR(32) NOT NULL,
  `skill_code` VARCHAR(32) NOT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_version` (`agent_version_code`),
  KEY `idx_skill_code` (`skill_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 版本→Skill 引用反查表';

CREATE TABLE IF NOT EXISTS `agent_version_tool_refs` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `agent_version_code` VARCHAR(32) NOT NULL,
  `tool_code` VARCHAR(32) NOT NULL,
  `create_no` VARCHAR(32) NOT NULL,
  `update_no` VARCHAR(32) NOT NULL,
  `create_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3),
  `update_time` DATETIME(3) NOT NULL DEFAULT CURRENT_TIMESTAMP(3) ON UPDATE CURRENT_TIMESTAMP(3),
  `is_deleted` TINYINT(1) NOT NULL DEFAULT 0,
  PRIMARY KEY (`id`),
  KEY `idx_version` (`agent_version_code`),
  KEY `idx_tool_code` (`tool_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 版本→Tool 引用反查表';