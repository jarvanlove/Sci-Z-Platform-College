-- 为 report_management 表添加 dify_api_keys_id 字段
-- 用于存储 Dify API Keys 表的 ID

-- 添加 dify_api_keys_id 字段
ALTER TABLE report_management 
ADD COLUMN IF NOT EXISTS dify_api_keys_id VARCHAR(100);

-- 添加字段注释
COMMENT ON COLUMN report_management.dify_api_keys_id IS 'Dify API Keys 表 ID（从 dify_api_keys 表的 id 获取）';

-- 创建索引（可选，如果需要根据 API Keys ID 查询）
CREATE INDEX IF NOT EXISTS idx_report_management_dify_api_keys_id ON report_management(dify_api_keys_id);

