-- Phase 2.2 schema: progress, change, finance

-- ==================== PROGRESS MODULE ====================

CREATE TABLE `biz_progress_plan` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT       NOT NULL,
  `plan_name`   VARCHAR(200) NOT NULL,
  `start_date`  DATE         NULL,
  `end_date`    DATE         NULL,
  `status`      TINYINT      NOT NULL DEFAULT 0 COMMENT '0=draft, 1=in_progress, 2=completed',
  `creator_id`  BIGINT       NULL,
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT      NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress plan';

CREATE TABLE `biz_progress_task` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `plan_id`        BIGINT        NOT NULL,
  `project_id`     BIGINT        NOT NULL,
  `task_name`      VARCHAR(200)  NOT NULL,
  `parent_task_id` BIGINT        NULL,
  `start_date`     DATE          NULL,
  `end_date`       DATE          NULL,
  `actual_start`   DATE          NULL,
  `actual_end`     DATE          NULL,
  `progress`       DECIMAL(5,2)  NOT NULL DEFAULT 0,
  `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '0=not_started, 1=in_progress, 2=completed, 3=delayed',
  `assignee_id`    BIGINT        NULL,
  `milestone`      TINYINT       NOT NULL DEFAULT 0 COMMENT '0=no, 1=yes',
  `sort_order`     INT           NOT NULL DEFAULT 0,
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`        TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_plan_id` (`plan_id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_parent_task` (`parent_task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress task (Gantt)';

CREATE TABLE `biz_progress_deviation` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `task_id`         BIGINT       NOT NULL,
  `project_id`      BIGINT       NOT NULL,
  `deviation_days`  INT          NOT NULL DEFAULT 0,
  `deviation_type`  TINYINT      NOT NULL DEFAULT 1 COMMENT '1=delay, 2=ahead',
  `description`     VARCHAR(500) NULL,
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Progress deviation record';

-- ==================== CHANGE MODULE ====================

CREATE TABLE `biz_change_site_visa` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `visa_no`     VARCHAR(50)   NOT NULL,
  `description` TEXT          NULL,
  `amount`      DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=rejected',
  `file_url`    VARCHAR(500)  NULL,
  `creator_id`  BIGINT        NULL,
  `approver_id` BIGINT        NULL,
  `approved_at` DATETIME      NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_visa_no` (`visa_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Site visa';

CREATE TABLE `biz_change_owner` (
  `id`            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`    BIGINT        NOT NULL,
  `change_no`     VARCHAR(50)   NOT NULL,
  `description`   TEXT          NULL,
  `amount_change` DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`        TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=rejected',
  `file_url`      VARCHAR(500)  NULL,
  `creator_id`    BIGINT        NULL,
  `approver_id`   BIGINT        NULL,
  `approved_at`   DATETIME      NULL,
  `created_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`       TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_change_no` (`change_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Owner change order';

CREATE TABLE `biz_change_labor_visa` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT        NOT NULL,
  `visa_no`      VARCHAR(50)   NOT NULL,
  `description`  TEXT          NULL,
  `labor_count`  INT           NOT NULL DEFAULT 0,
  `amount`       DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`       TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=rejected',
  `creator_id`   BIGINT        NULL,
  `approver_id`  BIGINT        NULL,
  `approved_at`  DATETIME      NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_visa_no` (`visa_no`),
  KEY `idx_project_id` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Labor visa';

-- ==================== FINANCE MODULE ====================

CREATE TABLE `biz_finance_income_split` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `contract_id` BIGINT        NOT NULL,
  `split_no`    VARCHAR(50)   NOT NULL,
  `period`      VARCHAR(20)   NULL,
  `amount`      DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=rejected',
  `creator_id`  BIGINT        NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_split_no` (`split_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_contract_id` (`contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Income split';

CREATE TABLE `biz_finance_reconciliation` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `reconciliation_no`  VARCHAR(50)   NOT NULL,
  `project_id`         BIGINT        NOT NULL,
  `supplier_id`        BIGINT        NULL,
  `contract_id`        BIGINT        NULL,
  `period`             VARCHAR(20)   NULL,
  `total_amount`       DECIMAL(14,2) NOT NULL DEFAULT 0,
  `confirmed_amount`   DECIMAL(14,2) NOT NULL DEFAULT 0,
  `difference`         DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`             TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=confirmed, 3=rejected',
  `confirmed_at`       DATETIME      NULL,
  `creator_id`         BIGINT        NULL,
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_reconciliation_no` (`reconciliation_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Reconciliation';

CREATE TABLE `biz_finance_payment` (
  `id`                BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `payment_no`        VARCHAR(50)   NOT NULL,
  `payment_type`      TINYINT       NOT NULL DEFAULT 1 COMMENT '1=material, 2=labor',
  `project_id`        BIGINT        NOT NULL,
  `contract_id`       BIGINT        NULL,
  `supplier_id`       BIGINT        NULL,
  `reconciliation_id` BIGINT        NULL,
  `amount`            DECIMAL(14,2) NOT NULL DEFAULT 0,
  `paid_amount`       DECIMAL(14,2) NOT NULL DEFAULT 0,
  `status`            TINYINT       NOT NULL DEFAULT 1 COMMENT '1=pending, 2=approved, 3=paid, 4=rejected',
  `bank_name`         VARCHAR(100)  NULL,
  `bank_account`      VARCHAR(50)   NULL,
  `remark`            VARCHAR(500)  NULL,
  `creator_id`        BIGINT        NULL,
  `approver_id`       BIGINT        NULL,
  `approved_at`       DATETIME      NULL,
  `paid_at`           DATETIME      NULL,
  `created_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`           TINYINT       NOT NULL DEFAULT 0,
  UNIQUE KEY `uk_payment_no` (`payment_no`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_supplier_id` (`supplier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Payment';

CREATE TABLE `biz_finance_invoice` (
  `id`           BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`   BIGINT        NOT NULL,
  `contract_id`  BIGINT        NULL,
  `invoice_no`   VARCHAR(50)   NOT NULL,
  `invoice_type` TINYINT       NOT NULL DEFAULT 1 COMMENT '1=special VAT, 2=general VAT, 3=other',
  `amount`       DECIMAL(14,2) NOT NULL DEFAULT 0,
  `tax_amount`   DECIMAL(14,2) NOT NULL DEFAULT 0,
  `invoice_date` DATE          NULL,
  `file_url`     VARCHAR(500)  NULL,
  `creator_id`   BIGINT        NULL,
  `created_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`      TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_invoice_no` (`invoice_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Invoice';

CREATE TABLE `biz_finance_cost` (
  `id`          BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `project_id`  BIGINT        NOT NULL,
  `cost_type`   TINYINT       NOT NULL DEFAULT 1,
  `category`    VARCHAR(100)  NOT NULL,
  `amount`      DECIMAL(14,2) NOT NULL DEFAULT 0,
  `description` VARCHAR(500)  NULL,
  `period`      VARCHAR(20)   NULL,
  `created_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`     TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_project_id` (`project_id`),
  KEY `idx_period` (`period`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Cost aggregation';

-- Add received_amount to biz_contract for receipt tracking
ALTER TABLE `biz_contract` ADD COLUMN `received_amount` DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT 'Total received amount' AFTER `amount_with_tax`;
