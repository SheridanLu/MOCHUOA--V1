-- Phase 4: 报表统计 + 审计日志增强
-- biz_report_snapshot: 报表预计算快照
CREATE TABLE IF NOT EXISTS biz_report_snapshot (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    report_type  VARCHAR(50)  NOT NULL COMMENT '报表类型: project_cost/income_expense/procurement/inventory',
    period       VARCHAR(20)  NOT NULL COMMENT '统计期间, 如 2026-03',
    data_json    LONGTEXT     NOT NULL COMMENT '报表数据JSON',
    generated_at DATETIME     NOT NULL COMMENT '生成时间',
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_report_type_period (report_type, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报表快照';

-- sys_audit_log 如已存在则忽略 (Phase 0 已建)
-- 确保 target_type 字段存在
ALTER TABLE sys_audit_log ADD COLUMN IF NOT EXISTS target_type VARCHAR(50) DEFAULT NULL COMMENT '目标类型' AFTER action;
