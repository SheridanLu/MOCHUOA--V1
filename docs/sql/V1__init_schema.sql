-- ============================================================
-- MOCHU-OA Construction Management System
-- Flyway Migration: V1__init_schema.sql
-- Database: MySQL 8.0  |  Charset: utf8mb4  |  Collation: utf8mb4_general_ci
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================
-- 1. SYSTEM TABLES (sys_*)
-- ============================================================

-- -----------------------------------------------------------
-- 1.1 sys_user - System users
-- -----------------------------------------------------------
CREATE TABLE `sys_user` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
  `username`      VARCHAR(50)   NOT NULL                           COMMENT 'Login username',
  `password_hash` VARCHAR(200)  NOT NULL                           COMMENT 'BCrypt password hash',
  `real_name`     VARCHAR(50)   NOT NULL DEFAULT ''                COMMENT 'Real name',
  `phone`         VARCHAR(20)   NULL                               COMMENT 'Mobile phone',
  `email`         VARCHAR(100)  NULL                               COMMENT 'Email address',
  `avatar`        VARCHAR(500)  NULL                               COMMENT 'Avatar URL',
  `dept_id`       BIGINT        NULL                               COMMENT 'Department FK',
  `status`        TINYINT       NOT NULL DEFAULT 1                 COMMENT '1=active, 0=disabled',
  `last_login_at` DATETIME      NULL                               COMMENT 'Last login time',
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `deleted`       TINYINT       NOT NULL DEFAULT 0                 COMMENT 'Soft delete flag',
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_phone`    (`phone`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System user table';

-- -----------------------------------------------------------
-- 1.2 sys_role - Roles
-- -----------------------------------------------------------
CREATE TABLE `sys_role` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT 'Primary key',
  `role_code`   VARCHAR(50)  NOT NULL                           COMMENT 'Role code',
  `role_name`   VARCHAR(100) NOT NULL                           COMMENT 'Role display name',
  `description` VARCHAR(500) NULL                               COMMENT 'Description',
  `data_scope`  TINYINT      NOT NULL DEFAULT 3                 COMMENT '1=all, 2=dept, 3=self, 4=custom',
  `status`      TINYINT      NOT NULL DEFAULT 1                 COMMENT '1=active, 0=disabled',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Role table';

-- -----------------------------------------------------------
-- 1.3 sys_permission - Permissions
-- -----------------------------------------------------------
CREATE TABLE `sys_permission` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `perm_code`   VARCHAR(100) NOT NULL COMMENT 'Permission code, e.g. project:create',
  `perm_name`   VARCHAR(100) NOT NULL COMMENT 'Permission display name',
  `module`      VARCHAR(50)  NOT NULL COMMENT 'Functional module',
  `description` VARCHAR(500) NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_perm_code` (`perm_code`),
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Permission table';

-- -----------------------------------------------------------
-- 1.4 sys_user_role - User-Role mapping
-- -----------------------------------------------------------
CREATE TABLE `sys_user_role` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`    BIGINT   NOT NULL,
  `role_id`    BIGINT   NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User-role mapping table';

-- -----------------------------------------------------------
-- 1.5 sys_role_permission - Role-Permission mapping
-- -----------------------------------------------------------
CREATE TABLE `sys_role_permission` (
  `id`            BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_id`       BIGINT   NOT NULL,
  `permission_id` BIGINT   NOT NULL,
  `created_at`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_role_perm` (`role_id`, `permission_id`),
  KEY `idx_permission_id` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Role-permission mapping table';

-- -----------------------------------------------------------
-- 1.6 sys_department - Department tree
-- -----------------------------------------------------------
CREATE TABLE `sys_department` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dept_name`  VARCHAR(100) NOT NULL COMMENT 'Department name',
  `parent_id`  BIGINT       NOT NULL DEFAULT 0 COMMENT '0 for root',
  `path`       VARCHAR(500) NOT NULL DEFAULT '' COMMENT 'Materialized path, e.g. /1/2/3',
  `sort_order` INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active, 0=disabled',
  `leader_id`  BIGINT       NULL COMMENT 'Department leader user_id',
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_leader_id` (`leader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Department table';

-- -----------------------------------------------------------
-- 1.7 sys_audit_log - Audit / operation log
-- -----------------------------------------------------------
CREATE TABLE `sys_audit_log` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`     BIGINT       NULL,
  `username`    VARCHAR(50)  NULL,
  `module`      VARCHAR(50)  NULL,
  `action`      VARCHAR(50)  NULL COMMENT 'Action verb',
  `target_type` VARCHAR(50)  NULL COMMENT 'Entity type',
  `target_id`   BIGINT       NULL,
  `detail`      TEXT         NULL,
  `ip`          VARCHAR(50)  NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id`    (`user_id`),
  KEY `idx_module`     (`module`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_target`     (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Audit log table';

-- -----------------------------------------------------------
-- 1.8 sys_announcement - System announcements
-- -----------------------------------------------------------
CREATE TABLE `sys_announcement` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `title`      VARCHAR(200) NOT NULL,
  `content`    TEXT         NULL,
  `type`       TINYINT      NOT NULL DEFAULT 1 COMMENT 'Announcement type',
  `pinned`     TINYINT      NOT NULL DEFAULT 0,
  `publish_at` DATETIME     NULL,
  `creator_id` BIGINT       NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_creator_id` (`creator_id`),
  KEY `idx_publish_at` (`publish_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Announcement table';

-- -----------------------------------------------------------
-- 1.9 sys_user_shortcut - User quick-access shortcuts
-- -----------------------------------------------------------
CREATE TABLE `sys_user_shortcut` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`       BIGINT       NOT NULL,
  `shortcut_code` VARCHAR(100) NOT NULL,
  `sort_order`    INT          NOT NULL DEFAULT 0,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User shortcut table';

-- -----------------------------------------------------------
-- 1.10 sys_role_mutex - Mutually exclusive role pairs
-- -----------------------------------------------------------
CREATE TABLE `sys_role_mutex` (
  `id`         BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_id_a`  BIGINT   NOT NULL,
  `role_id_b`  BIGINT   NOT NULL,
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_role_a` (`role_id_a`),
  KEY `idx_role_b` (`role_id_b`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Mutually exclusive role pairs';

-- -----------------------------------------------------------
-- 1.11 sys_permission_delegation - Permission delegation
-- -----------------------------------------------------------
CREATE TABLE `sys_permission_delegation` (
  `id`           BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `from_user_id` BIGINT   NOT NULL,
  `to_user_id`   BIGINT   NOT NULL,
  `start_time`   DATETIME NOT NULL,
  `end_time`     DATETIME NOT NULL,
  `status`       TINYINT  NOT NULL DEFAULT 1,
  `created_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_from_user` (`from_user_id`),
  KEY `idx_to_user`   (`to_user_id`),
  KEY `idx_status`    (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Permission delegation table';

-- -----------------------------------------------------------
-- 1.12 sys_config - System configuration KV
-- -----------------------------------------------------------
CREATE TABLE `sys_config` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `config_key`   VARCHAR(100) NOT NULL,
  `config_value` TEXT         NULL,
  `description`  VARCHAR(200) NULL,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='System configuration table';

-- -----------------------------------------------------------
-- 1.13 sys_login_log - Login log
-- -----------------------------------------------------------
CREATE TABLE `sys_login_log` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`        BIGINT       NULL,
  `username`       VARCHAR(50)  NULL,
  `login_ip`       VARCHAR(50)  NULL,
  `login_location` VARCHAR(200) NULL,
  `browser`        VARCHAR(200) NULL,
  `os`             VARCHAR(200) NULL,
  `login_time`     DATETIME     NULL,
  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '1=success, 0=fail',
  `message`        VARCHAR(200) NULL,
  KEY `idx_user_id`    (`user_id`),
  KEY `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Login log table';

-- -----------------------------------------------------------
-- 1.14 sys_dict_type - Dictionary type
-- -----------------------------------------------------------
CREATE TABLE `sys_dict_type` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dict_type`  VARCHAR(100) NOT NULL,
  `dict_name`  VARCHAR(200) NOT NULL,
  `status`     TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Dictionary type table';

-- -----------------------------------------------------------
-- 1.15 sys_dict_data - Dictionary data
-- -----------------------------------------------------------
CREATE TABLE `sys_dict_data` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `dict_type`  VARCHAR(100) NOT NULL,
  `dict_label` VARCHAR(200) NOT NULL,
  `dict_value` VARCHAR(200) NOT NULL,
  `sort_order` INT          NOT NULL DEFAULT 0,
  `status`     TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Dictionary data table';

-- -----------------------------------------------------------
-- 1.16 sys_data_scope_custom - Custom data scope per role
-- -----------------------------------------------------------
CREATE TABLE `sys_data_scope_custom` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `role_id`    BIGINT      NOT NULL,
  `module`     VARCHAR(50) NOT NULL,
  `scope_ids`  TEXT        NULL COMMENT 'Comma-separated dept/user IDs',
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Custom data scope per role/module';


-- ============================================================
-- 2. BUSINESS TABLES (biz_*)
-- ============================================================

-- -----------------------------------------------------------
-- 2.1 biz_project - Project master
-- -----------------------------------------------------------
CREATE TABLE `biz_project` (
  `id`                     BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_no`             VARCHAR(30)  NOT NULL COMMENT 'Project number',
  `project_name`           VARCHAR(200) NOT NULL,
  `project_type`           TINYINT      NOT NULL DEFAULT 1 COMMENT '1=entity, 2=virtual',
  `status`                 TINYINT      NOT NULL DEFAULT 1 COMMENT '1=draft,2=pending,3=approved,4=in_progress,5=paused,6=closed,7=terminated,8=tracking,9=converted',
  `description`            TEXT         NULL,
  `owner_id`               BIGINT       NULL COMMENT 'Project owner user_id',
  `dept_id`                BIGINT       NULL,
  `bid_amount`             DECIMAL(14,2) NULL COMMENT 'Bid amount (tax included)',
  `tax_rate`               DECIMAL(5,2)  NULL,
  `tax_amount`             DECIMAL(14,2) NULL,
  `amount_without_tax`     DECIMAL(14,2) NULL,
  `invest_limit`           DECIMAL(14,2) NULL COMMENT 'Investment limit',
  `bid_notice_url`         VARCHAR(500)  NULL,
  `termination_reason`     TEXT          NULL,
  `cost_target_project_id` BIGINT        NULL COMMENT 'Cost-tracking target project',
  `approver_id`            BIGINT        NULL,
  `approved_at`            DATETIME      NULL,
  `paused_at`              DATETIME      NULL,
  `closed_at`              DATETIME      NULL,
  `created_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`             DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`                TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_project_no` (`project_no`),
  KEY `idx_owner_id`  (`owner_id`),
  KEY `idx_dept_id`   (`dept_id`),
  KEY `idx_status`    (`status`),
  KEY `idx_approver`  (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Project master table';

-- -----------------------------------------------------------
-- 2.2 biz_project_payment_batch - Project payment batches
-- -----------------------------------------------------------
CREATE TABLE `biz_project_payment_batch` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT        NOT NULL,
  `batch_no`     INT           NOT NULL,
  `description`  VARCHAR(200)  NULL,
  `ratio`        DECIMAL(5,2)  NULL COMMENT 'Percentage of total',
  `amount`       DECIMAL(14,2) NULL,
  `planned_date` DATE          NULL,
  `status`       TINYINT       NOT NULL DEFAULT 1,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Project payment batch table';

-- -----------------------------------------------------------
-- 2.3 biz_contract - Contracts
-- -----------------------------------------------------------
CREATE TABLE `biz_contract` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_no`        VARCHAR(30)   NOT NULL,
  `contract_name`      VARCHAR(200)  NOT NULL,
  `contract_type`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=income, 2=expenditure',
  `project_id`         BIGINT        NULL,
  `parent_contract_id` BIGINT        NULL COMMENT 'Parent contract for sub-contracts',
  `supplier_id`        BIGINT        NULL,
  `amount_with_tax`    DECIMAL(14,2) NULL,
  `tax_rate`           DECIMAL(5,2)  NULL,
  `tax_amount`         DECIMAL(14,2) NULL,
  `amount_without_tax` DECIMAL(14,2) NULL,
  `sign_date`          DATE          NULL,
  `start_date`         DATE          NULL,
  `end_date`           DATE          NULL,
  `status`             TINYINT       NOT NULL DEFAULT 1 COMMENT '1=draft,2=pending,3=fin_approved,4=legal_approved,5=approved,6=executing,7=completed,8=terminated',
  `template_id`        BIGINT        NULL,
  `file_url`           VARCHAR(500)  NULL,
  `remark`             TEXT          NULL,
  `creator_id`         BIGINT        NULL,
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_contract_no` (`contract_no`),
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_status`      (`status`),
  KEY `idx_creator_id`  (`creator_id`),
  KEY `idx_parent_contract` (`parent_contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Contract table';

-- -----------------------------------------------------------
-- 2.4 biz_contract_supplement - Contract supplements / amendments
-- -----------------------------------------------------------
CREATE TABLE `biz_contract_supplement` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id`   BIGINT        NOT NULL,
  `supplement_no` VARCHAR(30)   NOT NULL,
  `reason`        TEXT          NULL,
  `amount_change` DECIMAL(14,2) NULL,
  `new_total`     DECIMAL(14,2) NULL,
  `file_url`      VARCHAR(500)  NULL,
  `status`        TINYINT       NOT NULL DEFAULT 1,
  `approver_id`   BIGINT        NULL,
  `approved_at`   DATETIME      NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_supplement_no` (`supplement_no`),
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Contract supplement table';

-- -----------------------------------------------------------
-- 2.5 biz_contract_template - Contract templates
-- -----------------------------------------------------------
CREATE TABLE `biz_contract_template` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `template_name` VARCHAR(200) NOT NULL,
  `template_type` TINYINT      NOT NULL DEFAULT 1,
  `content`       LONGTEXT     NULL,
  `file_url`      VARCHAR(500) NULL,
  `version`       INT          NOT NULL DEFAULT 1,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `creator_id`    BIGINT       NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_creator_id` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Contract template table';

-- -----------------------------------------------------------
-- 2.6 biz_contract_item - Contract line items
-- -----------------------------------------------------------
CREATE TABLE `biz_contract_item` (
  `id`            BIGINT         NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `contract_id`   BIGINT         NOT NULL,
  `material_name` VARCHAR(200)   NOT NULL,
  `spec`          VARCHAR(200)   NULL COMMENT 'Specification',
  `unit`          VARCHAR(50)    NULL,
  `quantity`      DECIMAL(12,4)  NULL,
  `unit_price`    DECIMAL(14,4)  NULL,
  `amount`        DECIMAL(14,2)  NULL,
  `remark`        VARCHAR(500)   NULL,
  `created_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT        NOT NULL DEFAULT 0,
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Contract line items';

-- -----------------------------------------------------------
-- 2.7 biz_supplier - Suppliers
-- -----------------------------------------------------------
CREATE TABLE `biz_supplier` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `supplier_name`  VARCHAR(200) NOT NULL,
  `supplier_code`  VARCHAR(50)  NOT NULL,
  `contact_person` VARCHAR(50)  NULL,
  `contact_phone`  VARCHAR(20)  NULL,
  `address`        VARCHAR(500) NULL,
  `bank_name`      VARCHAR(200) NULL,
  `bank_account`   VARCHAR(50)  NULL,
  `tax_no`         VARCHAR(50)  NULL,
  `category`       TINYINT      NULL,
  `rating`         TINYINT      NULL,
  `status`         TINYINT      NOT NULL DEFAULT 1,
  `creator_id`     BIGINT       NULL,
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_supplier_code` (`supplier_code`),
  KEY `idx_category` (`category`),
  KEY `idx_status`   (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Supplier table';

-- -----------------------------------------------------------
-- 2.8 biz_purchase_list - Purchase requisitions
-- -----------------------------------------------------------
CREATE TABLE `biz_purchase_list` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT        NULL,
  `contract_id`  BIGINT        NULL,
  `list_no`      VARCHAR(30)   NOT NULL,
  `status`       TINYINT       NOT NULL DEFAULT 1,
  `total_amount` DECIMAL(14,2) NULL,
  `creator_id`   BIGINT        NULL,
  `approved_at`  DATETIME      NULL,
  `approver_id`  BIGINT        NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_list_no` (`list_no`),
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_creator_id`  (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Purchase requisition list';

-- -----------------------------------------------------------
-- 2.9 biz_purchase_item - Purchase line items
-- -----------------------------------------------------------
CREATE TABLE `biz_purchase_item` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `list_id`         BIGINT        NOT NULL,
  `material_name`   VARCHAR(200)  NOT NULL,
  `spec`            VARCHAR(200)  NULL,
  `unit`            VARCHAR(50)   NULL,
  `quantity`        DECIMAL(12,4) NULL,
  `estimated_price` DECIMAL(14,4) NULL,
  `remark`          VARCHAR(500)  NULL,
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_list_id` (`list_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Purchase line items';

-- -----------------------------------------------------------
-- 2.10 biz_material_inbound - Material inbound records
-- -----------------------------------------------------------
CREATE TABLE `biz_material_inbound` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `inbound_no`   VARCHAR(30)   NOT NULL,
  `contract_id`  BIGINT        NULL,
  `project_id`   BIGINT        NULL,
  `supplier_id`  BIGINT        NULL,
  `warehouse`    VARCHAR(200)  NULL,
  `receiver`     VARCHAR(50)   NULL,
  `inbound_date` DATE          NULL,
  `status`       TINYINT       NOT NULL DEFAULT 1,
  `total_amount` DECIMAL(14,2) NULL,
  `creator_id`   BIGINT        NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_inbound_no` (`inbound_no`),
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material inbound header';

-- -----------------------------------------------------------
-- 2.11 biz_material_inbound_item - Inbound line items
-- -----------------------------------------------------------
CREATE TABLE `biz_material_inbound_item` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `inbound_id`       BIGINT        NOT NULL,
  `material_name`    VARCHAR(200)  NOT NULL,
  `spec`             VARCHAR(200)  NULL,
  `unit`             VARCHAR(50)   NULL,
  `quantity`         DECIMAL(12,4) NULL,
  `unit_price`       DECIMAL(14,4) NULL,
  `amount`           DECIMAL(14,2) NULL,
  `contract_item_id` BIGINT        NULL COMMENT 'FK to biz_contract_item',
  `remark`           VARCHAR(500)  NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_inbound_id`       (`inbound_id`),
  KEY `idx_contract_item_id` (`contract_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material inbound line items';

-- -----------------------------------------------------------
-- 2.12 biz_material_outbound - Material outbound records
-- -----------------------------------------------------------
CREATE TABLE `biz_material_outbound` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `outbound_no`   VARCHAR(30)  NOT NULL,
  `project_id`    BIGINT       NULL,
  `warehouse`     VARCHAR(200) NULL,
  `recipient`     VARCHAR(50)  NULL,
  `purpose`       VARCHAR(500) NULL,
  `outbound_date` DATE         NULL,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `creator_id`    BIGINT       NULL,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_outbound_no` (`outbound_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material outbound header';

-- -----------------------------------------------------------
-- 2.13 biz_material_outbound_item - Outbound line items
-- -----------------------------------------------------------
CREATE TABLE `biz_material_outbound_item` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `outbound_id`   BIGINT        NOT NULL,
  `material_name` VARCHAR(200)  NOT NULL,
  `spec`          VARCHAR(200)  NULL,
  `unit`          VARCHAR(50)   NULL,
  `quantity`      DECIMAL(12,4) NULL,
  `unit_price`    DECIMAL(14,4) NULL,
  `amount`        DECIMAL(14,2) NULL,
  `remark`        VARCHAR(500)  NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_outbound_id` (`outbound_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material outbound line items';

-- -----------------------------------------------------------
-- 2.14 biz_material_return - Material returns
-- -----------------------------------------------------------
CREATE TABLE `biz_material_return` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `return_no`   VARCHAR(30) NOT NULL,
  `inbound_id`  BIGINT      NULL COMMENT 'Original inbound',
  `project_id`  BIGINT      NULL,
  `return_date` DATE        NULL,
  `reason`      TEXT        NULL,
  `status`      TINYINT     NOT NULL DEFAULT 1,
  `creator_id`  BIGINT      NULL,
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT     NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_return_no` (`return_no`),
  KEY `idx_inbound_id` (`inbound_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material return header';

-- -----------------------------------------------------------
-- 2.15 biz_material_return_item - Return line items
-- -----------------------------------------------------------
CREATE TABLE `biz_material_return_item` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `return_id`       BIGINT        NOT NULL,
  `inbound_item_id` BIGINT        NULL,
  `material_name`   VARCHAR(200)  NOT NULL,
  `spec`            VARCHAR(200)  NULL,
  `unit`            VARCHAR(50)   NULL,
  `quantity`        DECIMAL(12,4) NULL,
  `unit_price`      DECIMAL(14,4) NULL,
  `amount`          DECIMAL(14,2) NULL,
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_return_id`       (`return_id`),
  KEY `idx_inbound_item_id` (`inbound_item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material return line items';

-- -----------------------------------------------------------
-- 2.16 biz_inventory - Inventory snapshot (weighted avg)
-- -----------------------------------------------------------
CREATE TABLE `biz_inventory` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`         BIGINT        NOT NULL,
  `material_name`      VARCHAR(200)  NOT NULL,
  `spec`               VARCHAR(200)  NOT NULL DEFAULT '',
  `unit`               VARCHAR(50)   NULL,
  `quantity`           DECIMAL(12,4) NOT NULL DEFAULT 0,
  `weighted_avg_price` DECIMAL(14,4) NULL,
  `total_amount`       DECIMAL(14,2) NULL,
  `warehouse`          VARCHAR(200)  NOT NULL DEFAULT '',
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_inventory` (`project_id`, `material_name`, `spec`, `warehouse`),
  KEY `idx_warehouse` (`warehouse`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Inventory table (weighted average price)';

-- -----------------------------------------------------------
-- 2.17 biz_progress_plan - Project progress plans
-- -----------------------------------------------------------
CREATE TABLE `biz_progress_plan` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id` BIGINT       NOT NULL,
  `plan_name`  VARCHAR(200) NOT NULL,
  `start_date` DATE         NULL,
  `end_date`   DATE         NULL,
  `status`     TINYINT      NOT NULL DEFAULT 1,
  `creator_id` BIGINT       NULL,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress plan table';

-- -----------------------------------------------------------
-- 2.18 biz_progress_task - Tasks within a plan (WBS)
-- -----------------------------------------------------------
CREATE TABLE `biz_progress_task` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `plan_id`        BIGINT       NOT NULL,
  `project_id`     BIGINT       NOT NULL,
  `task_name`      VARCHAR(200) NOT NULL,
  `parent_task_id` BIGINT       NULL,
  `start_date`     DATE         NULL,
  `end_date`       DATE         NULL,
  `actual_start`   DATE         NULL,
  `actual_end`     DATE         NULL,
  `progress`       DECIMAL(5,2) NOT NULL DEFAULT 0,
  `status`         TINYINT      NOT NULL DEFAULT 1,
  `assignee_id`    BIGINT       NULL,
  `milestone`      TINYINT      NOT NULL DEFAULT 0,
  `sort_order`     INT          NOT NULL DEFAULT 0,
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_plan_id`        (`plan_id`),
  KEY `idx_project_id`     (`project_id`),
  KEY `idx_parent_task_id` (`parent_task_id`),
  KEY `idx_assignee_id`    (`assignee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress task (WBS node)';

-- -----------------------------------------------------------
-- 2.19 biz_progress_deviation - Schedule deviations
-- -----------------------------------------------------------
CREATE TABLE `biz_progress_deviation` (
  `id`             BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `task_id`        BIGINT      NOT NULL,
  `project_id`     BIGINT      NOT NULL,
  `deviation_days` INT         NOT NULL DEFAULT 0,
  `deviation_type` TINYINT     NOT NULL DEFAULT 1 COMMENT 'Deviation type',
  `description`    TEXT        NULL,
  `created_at`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_task_id`    (`task_id`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress deviation table';

-- -----------------------------------------------------------
-- 2.20 biz_change_site_visa - Site visa (design change)
-- -----------------------------------------------------------
CREATE TABLE `biz_change_site_visa` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `visa_no`     VARCHAR(50)   NOT NULL,
  `description` TEXT          NULL,
  `amount`      DECIMAL(14,2) NULL,
  `status`      TINYINT       NOT NULL DEFAULT 1,
  `file_url`    VARCHAR(500)  NULL,
  `creator_id`  BIGINT        NULL,
  `approver_id` BIGINT        NULL,
  `approved_at` DATETIME      NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_visa_no`   (`visa_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Site visa table';

-- -----------------------------------------------------------
-- 2.21 biz_change_owner - Owner-initiated changes
-- -----------------------------------------------------------
CREATE TABLE `biz_change_owner` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`    BIGINT        NOT NULL,
  `change_no`     VARCHAR(50)   NOT NULL,
  `description`   TEXT          NULL,
  `amount_change` DECIMAL(14,2) NULL,
  `status`        TINYINT       NOT NULL DEFAULT 1,
  `file_url`      VARCHAR(500)  NULL,
  `creator_id`    BIGINT        NULL,
  `approver_id`   BIGINT        NULL,
  `approved_at`   DATETIME      NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_change_no`  (`change_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Owner change order table';

-- -----------------------------------------------------------
-- 2.22 biz_change_labor_visa - Labor visa
-- -----------------------------------------------------------
CREATE TABLE `biz_change_labor_visa` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `visa_no`     VARCHAR(50)   NOT NULL,
  `description` TEXT          NULL,
  `labor_count` INT           NULL,
  `amount`      DECIMAL(14,2) NULL,
  `status`      TINYINT       NOT NULL DEFAULT 1,
  `creator_id`  BIGINT        NULL,
  `approver_id` BIGINT        NULL,
  `approved_at` DATETIME      NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_labor_visa_no` (`visa_no`),
  KEY `idx_project_id`    (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Labor visa table';

-- -----------------------------------------------------------
-- 2.23 biz_finance_income_split - Income splitting
-- -----------------------------------------------------------
CREATE TABLE `biz_finance_income_split` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `contract_id` BIGINT        NULL,
  `split_no`    VARCHAR(30)   NULL,
  `period`      VARCHAR(20)   NULL COMMENT 'e.g. 2026-03',
  `amount`      DECIMAL(14,2) NULL,
  `status`      TINYINT       NOT NULL DEFAULT 1,
  `creator_id`  BIGINT        NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_period`      (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Income split table';

-- -----------------------------------------------------------
-- 2.24 biz_finance_reconciliation - Financial reconciliation
-- -----------------------------------------------------------
CREATE TABLE `biz_finance_reconciliation` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `reconciliation_no` VARCHAR(30)   NOT NULL,
  `project_id`        BIGINT        NULL,
  `supplier_id`       BIGINT        NULL,
  `contract_id`       BIGINT        NULL,
  `period`            VARCHAR(20)   NULL,
  `total_amount`      DECIMAL(14,2) NULL,
  `confirmed_amount`  DECIMAL(14,2) NULL,
  `difference`        DECIMAL(14,2) NULL,
  `status`            TINYINT       NOT NULL DEFAULT 1,
  `confirmed_at`      DATETIME      NULL,
  `creator_id`        BIGINT        NULL,
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_reconciliation_no` (`reconciliation_no`),
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`),
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Finance reconciliation table';

-- -----------------------------------------------------------
-- 2.25 biz_finance_payment - Payments
-- -----------------------------------------------------------
CREATE TABLE `biz_finance_payment` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `payment_no`        VARCHAR(30)   NOT NULL,
  `payment_type`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=material, 2=labor',
  `project_id`        BIGINT        NULL,
  `contract_id`       BIGINT        NULL,
  `supplier_id`       BIGINT        NULL,
  `reconciliation_id` BIGINT        NULL,
  `amount`            DECIMAL(14,2) NULL COMMENT 'Requested amount',
  `paid_amount`       DECIMAL(14,2) NULL COMMENT 'Actually paid',
  `status`            TINYINT       NOT NULL DEFAULT 1,
  `bank_name`         VARCHAR(200)  NULL,
  `bank_account`      VARCHAR(50)   NULL,
  `remark`            TEXT          NULL,
  `creator_id`        BIGINT        NULL,
  `approver_id`       BIGINT        NULL,
  `approved_at`       DATETIME      NULL,
  `paid_at`           DATETIME      NULL,
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_payment_no`   (`payment_no`),
  KEY `idx_project_id`     (`project_id`),
  KEY `idx_supplier_id`    (`supplier_id`),
  KEY `idx_contract_id`    (`contract_id`),
  KEY `idx_reconciliation` (`reconciliation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Payment table';

-- -----------------------------------------------------------
-- 2.26 biz_finance_invoice - Invoices
-- -----------------------------------------------------------
CREATE TABLE `biz_finance_invoice` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT        NULL,
  `contract_id`  BIGINT        NULL,
  `invoice_no`   VARCHAR(100)  NULL,
  `invoice_type` TINYINT       NULL,
  `amount`       DECIMAL(14,2) NULL,
  `tax_amount`   DECIMAL(14,2) NULL,
  `invoice_date` DATE          NULL,
  `file_url`     VARCHAR(500)  NULL,
  `creator_id`   BIGINT        NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id`  (`project_id`),
  KEY `idx_contract_id` (`contract_id`),
  KEY `idx_invoice_no`  (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Invoice table';

-- -----------------------------------------------------------
-- 2.27 biz_finance_cost - Cost ledger
-- -----------------------------------------------------------
CREATE TABLE `biz_finance_cost` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `cost_type`   TINYINT       NOT NULL DEFAULT 1,
  `category`    VARCHAR(100)  NULL,
  `amount`      DECIMAL(14,2) NULL,
  `description` VARCHAR(500)  NULL,
  `period`      VARCHAR(20)   NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_period`     (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cost ledger table';

-- -----------------------------------------------------------
-- 2.28 biz_completion_report - Completion reports
-- -----------------------------------------------------------
CREATE TABLE `biz_completion_report` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`      BIGINT       NOT NULL,
  `report_no`       VARCHAR(50)  NOT NULL,
  `completion_date` DATE         NULL,
  `quality_rating`  TINYINT      NULL,
  `summary`         TEXT         NULL,
  `file_url`        VARCHAR(500) NULL,
  `status`          TINYINT      NOT NULL DEFAULT 1,
  `creator_id`      BIGINT       NULL,
  `approver_id`     BIGINT       NULL,
  `approved_at`     DATETIME     NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_report_no`  (`report_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Completion report table';

-- -----------------------------------------------------------
-- 2.29 biz_completion_drawing - As-built drawings
-- -----------------------------------------------------------
CREATE TABLE `biz_completion_drawing` (
  `id`           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT       NOT NULL,
  `drawing_name` VARCHAR(200) NOT NULL,
  `drawing_type` VARCHAR(100) NULL,
  `file_url`     VARCHAR(500) NULL,
  `version`      INT          NOT NULL DEFAULT 1,
  `uploader_id`  BIGINT       NULL,
  `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Completion drawing table';

-- -----------------------------------------------------------
-- 2.30 biz_completion_settlement - Engineering settlement
-- -----------------------------------------------------------
CREATE TABLE `biz_completion_settlement` (
  `id`              BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`      BIGINT        NOT NULL,
  `settlement_no`   VARCHAR(50)   NOT NULL,
  `contract_amount` DECIMAL(14,2) NULL,
  `change_amount`   DECIMAL(14,2) NULL,
  `final_amount`    DECIMAL(14,2) NULL,
  `status`          TINYINT       NOT NULL DEFAULT 1,
  `creator_id`      BIGINT        NULL,
  `approver_id`     BIGINT        NULL,
  `approved_at`     DATETIME      NULL,
  `created_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`         TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_settlement_no` (`settlement_no`),
  KEY `idx_project_id`    (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Completion settlement table';

-- -----------------------------------------------------------
-- 2.31 biz_completion_labor_settlement - Labor settlement
-- -----------------------------------------------------------
CREATE TABLE `biz_completion_labor_settlement` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`    BIGINT        NOT NULL,
  `settlement_no` VARCHAR(50)   NOT NULL,
  `team_name`     VARCHAR(100)  NULL,
  `labor_type`    VARCHAR(100)  NULL,
  `total_days`    INT           NULL,
  `daily_rate`    DECIMAL(14,2) NULL,
  `total_amount`  DECIMAL(14,2) NULL,
  `deduction`     DECIMAL(14,2) NULL,
  `final_amount`  DECIMAL(14,2) NULL,
  `status`        TINYINT       NOT NULL DEFAULT 1,
  `creator_id`    BIGINT        NULL,
  `approved_at`   DATETIME      NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_labor_settlement_no` (`settlement_no`),
  KEY `idx_project_id`          (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Labor settlement table';

-- -----------------------------------------------------------
-- 2.32 biz_hr_employee - Employee records
-- -----------------------------------------------------------
CREATE TABLE `biz_hr_employee` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`     BIGINT       NULL COMMENT 'Linked sys_user',
  `employee_no` VARCHAR(30)  NOT NULL,
  `real_name`   VARCHAR(50)  NOT NULL,
  `gender`      TINYINT      NULL COMMENT '1=male, 2=female',
  `birth_date`  DATE         NULL,
  `id_card`     VARCHAR(20)  NULL,
  `phone`       VARCHAR(20)  NULL,
  `address`     VARCHAR(500) NULL,
  `entry_date`  DATE         NULL,
  `leave_date`  DATE         NULL,
  `status`      TINYINT      NOT NULL DEFAULT 1 COMMENT '1=active, 2=probation, 3=resigned',
  `dept_id`     BIGINT       NULL,
  `position`    VARCHAR(100) NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`),
  KEY `idx_status`  (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Employee table';

-- -----------------------------------------------------------
-- 2.33 biz_hr_payroll - Payroll
-- -----------------------------------------------------------
CREATE TABLE `biz_hr_payroll` (
  `id`               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `employee_id`      BIGINT        NOT NULL,
  `period`           VARCHAR(20)   NOT NULL COMMENT 'e.g. 2026-03',
  `base_salary`      DECIMAL(14,2) NULL,
  `overtime`         DECIMAL(14,2) NULL,
  `bonus`            DECIMAL(14,2) NULL,
  `deduction`        DECIMAL(14,2) NULL,
  `social_insurance` DECIMAL(14,2) NULL,
  `tax`              DECIMAL(14,2) NULL,
  `net_salary`       DECIMAL(14,2) NULL,
  `status`           TINYINT       NOT NULL DEFAULT 1,
  `paid_at`          DATETIME      NULL,
  `created_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`          TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_period`      (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Payroll table';

-- -----------------------------------------------------------
-- 2.34 biz_hr_reimbursement - Expense reimbursement
-- -----------------------------------------------------------
CREATE TABLE `biz_hr_reimbursement` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `employee_id`  BIGINT        NOT NULL,
  `project_id`   BIGINT        NULL,
  `reimburse_no` VARCHAR(30)   NOT NULL,
  `category`     VARCHAR(100)  NULL,
  `amount`       DECIMAL(14,2) NULL,
  `description`  TEXT          NULL,
  `status`       TINYINT       NOT NULL DEFAULT 1,
  `file_url`     VARCHAR(500)  NULL,
  `approver_id`  BIGINT        NULL,
  `approved_at`  DATETIME      NULL,
  `creator_id`   BIGINT        NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_reimburse_no` (`reimburse_no`),
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_project_id`  (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Reimbursement table';

-- -----------------------------------------------------------
-- 2.35 biz_hr_contract - Employment contracts
-- -----------------------------------------------------------
CREATE TABLE `biz_hr_contract` (
  `id`            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `employee_id`   BIGINT       NOT NULL,
  `contract_type` TINYINT      NOT NULL DEFAULT 1,
  `start_date`    DATE         NULL,
  `end_date`      DATE         NULL,
  `file_url`      VARCHAR(500) NULL,
  `status`        TINYINT      NOT NULL DEFAULT 1,
  `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_employee_id` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Employment contract table';

-- -----------------------------------------------------------
-- 2.36 biz_hr_qualification - Employee qualifications / certs
-- -----------------------------------------------------------
CREATE TABLE `biz_hr_qualification` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `employee_id` BIGINT       NOT NULL,
  `qual_name`   VARCHAR(200) NOT NULL,
  `qual_no`     VARCHAR(100) NULL,
  `issue_date`  DATE         NULL,
  `expire_date` DATE         NULL,
  `file_url`    VARCHAR(500) NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_employee_id` (`employee_id`),
  KEY `idx_expire_date` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Employee qualification table';

-- -----------------------------------------------------------
-- 2.37 biz_contact - Contact book
-- -----------------------------------------------------------
CREATE TABLE `biz_contact` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`    BIGINT       NULL,
  `dept_id`    BIGINT       NULL,
  `real_name`  VARCHAR(50)  NOT NULL,
  `phone`      VARCHAR(20)  NULL,
  `email`      VARCHAR(100) NULL,
  `position`   VARCHAR(100) NULL,
  `visible`    TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_dept_id` (`dept_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Contact book';

-- -----------------------------------------------------------
-- 2.38 biz_showcase - Project showcase
-- -----------------------------------------------------------
CREATE TABLE `biz_showcase` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT       NULL,
  `title`       VARCHAR(200) NOT NULL,
  `description` TEXT         NULL,
  `cover_url`   VARCHAR(500) NULL,
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `status`      TINYINT      NOT NULL DEFAULT 1,
  `creator_id`  BIGINT       NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Project showcase table';

-- -----------------------------------------------------------
-- 2.39 biz_showcase_image - Showcase images
-- -----------------------------------------------------------
CREATE TABLE `biz_showcase_image` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `showcase_id` BIGINT       NOT NULL,
  `image_url`   VARCHAR(500) NOT NULL,
  `caption`     VARCHAR(200) NULL,
  `sort_order`  INT          NOT NULL DEFAULT 0,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_showcase_id` (`showcase_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Showcase images';

-- -----------------------------------------------------------
-- 2.40 biz_approval_flow - Approval flow definitions
-- -----------------------------------------------------------
CREATE TABLE `biz_approval_flow` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `module`     VARCHAR(50)  NOT NULL,
  `flow_name`  VARCHAR(200) NOT NULL,
  `nodes_json` TEXT         NULL COMMENT 'JSON array of approval nodes',
  `status`     TINYINT      NOT NULL DEFAULT 1,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_module` (`module`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Approval flow definition';

-- -----------------------------------------------------------
-- 2.41 biz_approval_instance - Approval instances
-- -----------------------------------------------------------
CREATE TABLE `biz_approval_instance` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `flow_id`      BIGINT      NOT NULL,
  `module`       VARCHAR(50) NOT NULL,
  `target_id`    BIGINT      NOT NULL,
  `target_type`  VARCHAR(50) NOT NULL,
  `current_node` INT         NOT NULL DEFAULT 0,
  `status`       TINYINT     NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=rejected',
  `applicant_id` BIGINT      NOT NULL,
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_flow_id`      (`flow_id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_target`       (`target_type`, `target_id`),
  KEY `idx_status`       (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Approval instance table';

-- -----------------------------------------------------------
-- 2.42 biz_approval_record - Approval action records
-- -----------------------------------------------------------
CREATE TABLE `biz_approval_record` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `instance_id` BIGINT      NOT NULL,
  `node_index`  INT         NOT NULL,
  `approver_id` BIGINT      NOT NULL,
  `action`      TINYINT     NOT NULL COMMENT '1=approve, 2=reject, 3=transfer',
  `opinion`     TEXT        NULL,
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Approval record table';

-- -----------------------------------------------------------
-- 2.43 biz_approval_reminder - Approval reminders
-- -----------------------------------------------------------
CREATE TABLE `biz_approval_reminder` (
  `id`              BIGINT   NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `instance_id`     BIGINT   NOT NULL,
  `approver_id`     BIGINT   NOT NULL,
  `reminder_count`  INT      NOT NULL DEFAULT 0,
  `last_reminded_at` DATETIME NULL,
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_instance_id` (`instance_id`),
  KEY `idx_approver_id` (`approver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Approval reminder table';

-- -----------------------------------------------------------
-- 2.44 biz_no_seed - Business number generator seed (Redis fallback)
-- -----------------------------------------------------------
CREATE TABLE `biz_no_seed` (
  `id`          BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `prefix`      VARCHAR(20) NOT NULL,
  `date_part`   VARCHAR(20) NOT NULL,
  `current_seq` INT         NOT NULL DEFAULT 0,
  `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY `uk_prefix_date` (`prefix`, `date_part`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Biz number sequence seed';

-- -----------------------------------------------------------
-- 2.45 biz_file_attachment - File attachments
-- -----------------------------------------------------------
CREATE TABLE `biz_file_attachment` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `module`      VARCHAR(50)  NOT NULL,
  `target_id`   BIGINT       NOT NULL,
  `file_name`   VARCHAR(200) NOT NULL,
  `file_url`    VARCHAR(500) NOT NULL,
  `file_size`   BIGINT       NULL,
  `file_type`   VARCHAR(100) NULL,
  `uploader_id` BIGINT       NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_module_target` (`module`, `target_id`),
  KEY `idx_uploader_id`   (`uploader_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='File attachment table';

-- -----------------------------------------------------------
-- 2.46 biz_todo - User to-do items
-- -----------------------------------------------------------
CREATE TABLE `biz_todo` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`     BIGINT       NOT NULL,
  `title`       VARCHAR(200) NOT NULL,
  `module`      VARCHAR(50)  NULL,
  `target_id`   BIGINT       NULL,
  `target_type` VARCHAR(50)  NULL,
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=pending, 1=done',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status`  (`status`),
  KEY `idx_target`  (`target_type`, `target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='User to-do table';

-- -----------------------------------------------------------
-- 2.47 biz_report_snapshot - Report data snapshots
-- -----------------------------------------------------------
CREATE TABLE `biz_report_snapshot` (
  `id`           BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `report_type`  VARCHAR(50) NOT NULL,
  `period`       VARCHAR(20) NULL,
  `data_json`    LONGTEXT    NULL,
  `generated_at` DATETIME    NULL,
  `created_at`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_report_type` (`report_type`),
  KEY `idx_period`      (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Report snapshot table';

-- -----------------------------------------------------------
-- 2.48 biz_project_member - Project team members
-- -----------------------------------------------------------
CREATE TABLE `biz_project_member` (
  `id`         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id` BIGINT      NOT NULL,
  `user_id`    BIGINT      NOT NULL,
  `role`       VARCHAR(50) NULL COMMENT 'Role within project',
  `joined_at`  DATETIME    NULL,
  `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`    TINYINT     NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_user_id`    (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Project member table';

-- -----------------------------------------------------------
-- 2.49 biz_project_document - Project documents
-- -----------------------------------------------------------
CREATE TABLE `biz_project_document` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT       NOT NULL,
  `doc_name`    VARCHAR(200) NOT NULL,
  `doc_type`    VARCHAR(50)  NULL,
  `file_url`    VARCHAR(500) NULL,
  `version`     INT          NOT NULL DEFAULT 1,
  `uploader_id` BIGINT       NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Project document table';

-- -----------------------------------------------------------
-- 2.50 biz_message_notification - In-app notifications
-- -----------------------------------------------------------
CREATE TABLE `biz_message_notification` (
  `id`         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `user_id`    BIGINT       NOT NULL,
  `title`      VARCHAR(200) NOT NULL,
  `content`    TEXT         NULL,
  `module`     VARCHAR(50)  NULL,
  `target_id`  BIGINT       NULL,
  `read_flag`  TINYINT      NOT NULL DEFAULT 0,
  `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_user_id`   (`user_id`),
  KEY `idx_read_flag` (`read_flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Message notification table';


-- ============================================================
-- 3. SEED DATA
-- ============================================================

-- -----------------------------------------------------------
-- 3.1 Departments
-- -----------------------------------------------------------
INSERT INTO `sys_department` (`id`, `dept_name`, `parent_id`, `path`, `sort_order`, `status`) VALUES
(1,  '总公司',       0, '/1',       1,  1),
(2,  '工程部',       1, '/1/2',     1,  1),
(3,  '预算部',       1, '/1/3',     2,  1),
(4,  '采购部',       1, '/1/4',     3,  1),
(5,  '财务部',       1, '/1/5',     4,  1),
(6,  '法务部',       1, '/1/6',     5,  1),
(7,  '资料部',       1, '/1/7',     6,  1),
(8,  '人力资源部',   1, '/1/8',     7,  1),
(9,  '基地管理部',   1, '/1/9',     8,  1),
(10, '软件部',       1, '/1/10',    9,  1),
(11, '施工一队',     2, '/1/2/11',  1,  1),
(12, '施工二队',     2, '/1/2/12',  2,  1);

-- -----------------------------------------------------------
-- 3.2 Admin user  (BCrypt hash of 'Admin@2026')
-- -----------------------------------------------------------
INSERT INTO `sys_user` (`id`, `username`, `password_hash`, `real_name`, `phone`, `dept_id`, `status`) VALUES
(1, 'admin', '$2b$10$B17IelFvQW28KAx1nmkF3uJpGCOf4WTvqfa9RFEAWJ.Ml26VLmqVW', '系统管理员', '13800000000', 1, 1);

-- -----------------------------------------------------------
-- 3.3 Roles
-- -----------------------------------------------------------
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `data_scope`, `status`) VALUES
(1,  'GM',          '总经理',       '拥有全部权限',           1, 1),
(2,  'PROJ_MGR',    '项目经理',     '项目全流程管理',         2, 1),
(3,  'BUDGET',      '预算员',       '预算与成本管理',         2, 1),
(4,  'PURCHASE',    '采购员',       '采购与材料管理',         2, 1),
(5,  'FINANCE',     '财务人员',     '财务收支与对账',         2, 1),
(6,  'LEGAL',       '法务人员',     '合同法务审核',           2, 1),
(7,  'DATA',        '资料员',       '资料与文档管理',         2, 1),
(8,  'HR',          '人力资源',     '人力资源管理',           2, 1),
(9,  'BASE',        '基地管理',     '基地设备与库存管理',     2, 1),
(10, 'SOFT',        '软件管理',     '系统配置与维护',         1, 1),
(11, 'TEAM_MEMBER', '施工班组',     '施工现场执行',           3, 1);

-- Assign admin user the GM role
INSERT INTO `sys_user_role` (`user_id`, `role_id`) VALUES (1, 1);

-- -----------------------------------------------------------
-- 3.4 Permissions (47 total)
-- -----------------------------------------------------------
INSERT INTO `sys_permission` (`id`, `perm_code`, `perm_name`, `module`) VALUES
-- Project (8)
(1,  'project:create',   '创建项目',     'project'),
(2,  'project:view',     '查看项目',     'project'),
(3,  'project:edit',     '编辑项目',     'project'),
(4,  'project:delete',   '删除项目',     'project'),
(5,  'project:approve',  '审批项目',     'project'),
(6,  'project:pause',    '暂停项目',     'project'),
(7,  'project:resume',   '恢复项目',     'project'),
(8,  'project:close',    '关闭项目',     'project'),
-- Contract (5+2)
(9,  'contract:create',     '创建合同',     'contract'),
(10, 'contract:view',       '查看合同',     'contract'),
(11, 'contract:edit',       '编辑合同',     'contract'),
(12, 'contract:delete',     '删除合同',     'contract'),
(13, 'contract:approve',    '审批合同',     'contract'),
(14, 'contract:supplement', '合同补充',     'contract'),
(15, 'contract:template',   '合同模板管理', 'contract'),
-- Purchase (3)
(16, 'purchase:create',  '创建采购单',   'purchase'),
(17, 'purchase:view',    '查看采购单',   'purchase'),
(18, 'purchase:approve', '审批采购单',   'purchase'),
-- Material (4)
(19, 'material:inbound',   '材料入库',   'material'),
(20, 'material:outbound',  '材料出库',   'material'),
(21, 'material:return',    '材料退货',   'material'),
(22, 'material:inventory', '库存查看',   'material'),
-- Progress (3)
(23, 'progress:create', '创建进度计划', 'progress'),
(24, 'progress:view',   '查看进度',     'progress'),
(25, 'progress:edit',   '编辑进度',     'progress'),
-- Change (3)
(26, 'change:create',  '创建变更',     'change'),
(27, 'change:view',    '查看变更',     'change'),
(28, 'change:approve', '审批变更',     'change'),
-- Finance (5)
(29, 'finance:view',           '财务查看',   'finance'),
(30, 'finance:payment',        '付款管理',   'finance'),
(31, 'finance:reconciliation', '财务对账',   'finance'),
(32, 'finance:invoice',        '发票管理',   'finance'),
(33, 'finance:cost',           '成本管理',   'finance'),
-- Completion (3)
(34, 'completion:create',  '创建竣工资料', 'completion'),
(35, 'completion:view',    '查看竣工资料', 'completion'),
(36, 'completion:approve', '审批竣工',     'completion'),
-- HR (3)
(37, 'hr:view',          '人事查看',   'hr'),
(38, 'hr:payroll',       '薪资管理',   'hr'),
(39, 'hr:reimbursement', '报销管理',   'hr'),
-- Announcement (2)
(40, 'announcement:create', '发布公告', 'announcement'),
(41, 'announcement:view',   '查看公告', 'announcement'),
-- Showcase (2)
(42, 'showcase:create', '创建展示', 'showcase'),
(43, 'showcase:view',   '查看展示', 'showcase'),
-- Report (2)
(44, 'report:view',   '查看报表', 'report'),
(45, 'report:export', '导出报表', 'report'),
-- Audit (1)
(46, 'audit:view', '审计日志查看', 'audit'),
-- User management (3)
(47, 'user:create',  '创建用户', 'user'),
(48, 'user:view',    '查看用户', 'user'),
(49, 'user:edit',    '编辑用户', 'user'),
(50, 'user:disable', '禁用用户', 'user'),
-- Role management (2)
(51, 'role:create', '创建角色', 'role'),
(52, 'role:edit',   '编辑角色', 'role'),
-- Dept management (2)
(53, 'dept:create', '创建部门', 'dept'),
(54, 'dept:edit',   '编辑部门', 'dept');

-- -----------------------------------------------------------
-- 3.5 Role-Permission mappings
-- -----------------------------------------------------------

-- GM (role_id=1) gets ALL 54 permissions (ids 1-54 to cover the extra user/role/dept management)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`)
SELECT 1, `id` FROM `sys_permission`;

-- PROJ_MGR (role_id=2): project, contract(view), purchase(view), material(view+inventory),
--   progress, change, finance(view), completion, report(view), announcement(view), showcase(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(2,1),(2,2),(2,3),(2,4),(2,5),(2,6),(2,7),(2,8),  -- project:*
(2,10),(2,11),                                      -- contract:view, edit
(2,17),                                             -- purchase:view
(2,22),                                             -- material:inventory
(2,23),(2,24),(2,25),                               -- progress:*
(2,26),(2,27),(2,28),                               -- change:*
(2,29),                                             -- finance:view
(2,34),(2,35),(2,36),                               -- completion:*
(2,41),                                             -- announcement:view
(2,43),                                             -- showcase:view
(2,44);                                             -- report:view

-- BUDGET (role_id=3): project(view), contract(view), finance(view,cost), completion(view), report
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(3,2),                                              -- project:view
(3,10),                                             -- contract:view
(3,29),(3,33),                                      -- finance:view, cost
(3,35),                                             -- completion:view
(3,44),(3,45);                                      -- report:view, export

-- PURCHASE (role_id=4): purchase:*, material:inbound/outbound/return/inventory, contract(view), project(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(4,2),                                              -- project:view
(4,10),                                             -- contract:view
(4,16),(4,17),(4,18),                               -- purchase:*
(4,19),(4,20),(4,21),(4,22);                        -- material:*

-- FINANCE (role_id=5): finance:*, project(view), contract(view), report, invoice
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(5,2),                                              -- project:view
(5,10),                                             -- contract:view
(5,29),(5,30),(5,31),(5,32),(5,33),                 -- finance:*
(5,37),(5,38),(5,39),                               -- hr:view, payroll, reimbursement
(5,44),(5,45);                                      -- report:view, export

-- LEGAL (role_id=6): contract:view/approve/supplement/template, project(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(6,2),                                              -- project:view
(6,10),(6,13),(6,14),(6,15);                        -- contract:view, approve, supplement, template

-- DATA (role_id=7): project(view), contract(view), completion:*, showcase:*, report(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(7,2),                                              -- project:view
(7,10),                                             -- contract:view
(7,34),(7,35),                                      -- completion:create, view
(7,42),(7,43),                                      -- showcase:create, view
(7,44);                                             -- report:view

-- HR (role_id=8): hr:*, announcement:*, user(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(8,37),(8,38),(8,39),                               -- hr:*
(8,40),(8,41),                                      -- announcement:*
(8,48);                                             -- user:view

-- BASE (role_id=9): material:*, project(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(9,2),                                              -- project:view
(9,19),(9,20),(9,21),(9,22);                        -- material:*

-- SOFT (role_id=10): user:*, role:*, dept:*, audit:view, report:*, announcement:*, config
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(10,40),(10,41),                                    -- announcement:*
(10,44),(10,45),                                    -- report:*
(10,46),                                            -- audit:view
(10,47),(10,48),(10,49),(10,50),                    -- user:*
(10,51),(10,52),                                    -- role:*
(10,53),(10,54);                                    -- dept:*

-- TEAM_MEMBER (role_id=11): project(view), progress(view), material(outbound), change(view), announcement(view)
INSERT INTO `sys_role_permission` (`role_id`, `permission_id`) VALUES
(11,2),                                             -- project:view
(11,20),                                            -- material:outbound
(11,24),                                            -- progress:view
(11,27),                                            -- change:view
(11,41),                                            -- announcement:view
(11,43);                                            -- showcase:view

-- -----------------------------------------------------------
-- 3.6 Role mutex: PURCHASE <-> FINANCE
-- -----------------------------------------------------------
INSERT INTO `sys_role_mutex` (`role_id_a`, `role_id_b`) VALUES (4, 5);

SET FOREIGN_KEY_CHECKS = 1;
