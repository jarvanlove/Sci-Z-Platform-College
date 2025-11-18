-- 1. 新增：用户名 + 时间范围查询
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_username_time
ON sys_operation_log(username, created_time DESC);

-- 2. 新增：状态 + 时间范围查询
CREATE INDEX IF NOT EXISTS idx_sys_operation_log_status_time
ON sys_operation_log(status, created_time DESC);