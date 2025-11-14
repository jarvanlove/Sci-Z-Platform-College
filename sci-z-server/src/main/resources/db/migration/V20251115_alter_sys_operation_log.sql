-- =============================================
-- 添加 sys_operation_log 表字段：日志级别、详细信息
-- @date 2025-11-15
-- =============================================

ALTER TABLE sys_operation_log
    ADD COLUMN IF NOT EXISTS log_level VARCHAR(16) DEFAULT 'INFO';

COMMENT ON COLUMN sys_operation_log.log_level IS '日志级别(INFO/WARN/ERROR)';

ALTER TABLE sys_operation_log
    ADD COLUMN IF NOT EXISTS detail_info TEXT;

COMMENT ON COLUMN sys_operation_log.detail_info IS '操作详细信息';

-- 索引：按日志级别和创建时间组合查询
CREATE INDEX IF NOT EXISTS idx_operation_log_level_time
    ON sys_operation_log (log_level, created_time DESC);

