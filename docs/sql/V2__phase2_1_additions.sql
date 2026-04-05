-- Phase 2.1 schema additions

-- Benchmark price table for material price management
CREATE TABLE `biz_benchmark_price` (
  `id`                 BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
  `material_name`      VARCHAR(200)  NOT NULL,
  `spec`               VARCHAR(200)  NULL,
  `unit`               VARCHAR(50)   NULL,
  `benchmark_price`    DECIMAL(14,4) NOT NULL,
  `update_type`        TINYINT       NOT NULL DEFAULT 2 COMMENT '1=auto(contract), 2=manual',
  `source_contract_id` BIGINT        NULL,
  `source_supplier_id` BIGINT        NULL,
  `status`             TINYINT       NOT NULL DEFAULT 1 COMMENT '1=active, 2=pending approval',
  `creator_id`         BIGINT        NULL,
  `created_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`         DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `deleted`            TINYINT       NOT NULL DEFAULT 0,
  KEY `idx_material_name` (`material_name`),
  KEY `idx_source_contract` (`source_contract_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='Material benchmark price';

-- Add return_type and target_project_id to material_return table
ALTER TABLE `biz_material_return` ADD COLUMN `return_type` TINYINT NOT NULL DEFAULT 1 COMMENT '1=on-site, 2=manufacturer, 3=company warehouse, 4=inter-project' AFTER `reason`;
ALTER TABLE `biz_material_return` ADD COLUMN `target_project_id` BIGINT NULL COMMENT 'Target project for inter-project transfer' AFTER `return_type`;
-- NOTE: creator_id already exists in V1__init_schema.sql, no need to add again
