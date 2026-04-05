-- Phase 3 schema additions

-- Add view_count to biz_showcase for visit tracking
ALTER TABLE `biz_showcase` ADD COLUMN IF NOT EXISTS `view_count` BIGINT NOT NULL DEFAULT 0 COMMENT 'View count' AFTER `status`;

-- Add visibility to biz_showcase (1=public, 2=internal)
ALTER TABLE `biz_showcase` ADD COLUMN IF NOT EXISTS `visibility` TINYINT NOT NULL DEFAULT 1 COMMENT '1=public, 2=internal' AFTER `view_count`;

-- Add video_url and panorama_url to biz_showcase
ALTER TABLE `biz_showcase` ADD COLUMN IF NOT EXISTS `video_url` VARCHAR(500) NULL COMMENT 'Video link' AFTER `cover_url`;
ALTER TABLE `biz_showcase` ADD COLUMN IF NOT EXISTS `panorama_url` VARCHAR(500) NULL COMMENT 'Panoramic photo link' AFTER `video_url`;

-- Add status to sys_announcement (1=draft, 2=published, 3=offline)
ALTER TABLE `sys_announcement` ADD COLUMN IF NOT EXISTS `status` TINYINT NOT NULL DEFAULT 1 COMMENT '1=draft, 2=published, 3=offline' AFTER `publish_at`;

-- Add scope for department-scoped announcements
ALTER TABLE `sys_announcement` ADD COLUMN IF NOT EXISTS `dept_scope` VARCHAR(500) NULL COMMENT 'Comma-separated dept IDs, null=all' AFTER `status`;
