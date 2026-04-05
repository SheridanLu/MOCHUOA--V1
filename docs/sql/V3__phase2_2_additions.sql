-- Phase 2.2 schema additions
-- NOTE: Tables biz_progress_plan, biz_progress_task, biz_progress_deviation,
--       biz_change_site_visa, biz_change_owner, biz_change_labor_visa,
--       biz_finance_income_split, biz_finance_reconciliation, biz_finance_payment,
--       biz_finance_invoice, biz_finance_cost are already created in V1__init_schema.sql.

-- Add received_amount to biz_contract for receipt tracking
ALTER TABLE `biz_contract` ADD COLUMN IF NOT EXISTS `received_amount` DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT 'Total received amount' AFTER `amount_with_tax`;
