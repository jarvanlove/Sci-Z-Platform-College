-- 为 report_management 表添加 attachment_id 字段
-- 用于存储 MinIO 返回的附件 ID

-- 添加 attachment_id 字段
ALTER TABLE report_management
ADD COLUMN IF NOT EXISTS attachment_id BIGINT;

-- 添加字段注释
COMMENT ON COLUMN report_management.attachment_id IS 'MinIO 附件 ID（从 sys_attachment 表的 id 获取）';

-- 创建索引（可选，如果需要根据附件 ID 查询）
CREATE INDEX IF NOT EXISTS idx_report_management_attachment_id ON report_management(attachment_id);

