-- 添加 dify_knowledge_id 字段到 dify_api_keys 表
-- 用于存储工作流关联的知识库ID（Dify知识库ID）

ALTER TABLE dify_api_keys 
ADD COLUMN IF NOT EXISTS dify_knowledge_id VARCHAR(100);

COMMENT ON COLUMN dify_api_keys.dify_knowledge_id IS '工作流关联的知识库ID（Dify知识库ID）';


