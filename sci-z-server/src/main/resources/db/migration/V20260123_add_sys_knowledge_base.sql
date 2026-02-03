-- 1. 增加两列（封面）
ALTER TABLE sys_knowledge_base
    ADD COLUMN IF NOT EXISTS cover_file_id BIGINT,
    ADD COLUMN IF NOT EXISTS cover_url     VARCHAR(500);

-- 2. 添加封面列注释
COMMENT ON COLUMN sys_knowledge_base.cover_file_id IS '封面附件ID';
COMMENT ON COLUMN sys_knowledge_base.cover_url     IS '封面图片URL';

-- 3. 增加 kb_type 列（知识库类型：personal=个人知识库，project=项目知识库）
ALTER TABLE sys_knowledge_base
    ADD COLUMN IF NOT EXISTS kb_type VARCHAR(20) NOT NULL DEFAULT 'personal';

-- 4. 添加 kb_type 注释
COMMENT ON COLUMN sys_knowledge_base.kb_type IS '知识库类型：personal=个人知识库，project=项目知识库';

-- 5. 历史数据回填：有 project_id 的为项目知识库
UPDATE sys_knowledge_base
SET kb_type = 'project'
WHERE project_id IS NOT NULL
  AND (kb_type IS NULL OR kb_type = 'personal');

-- 6. 创建 kb_type 索引
CREATE INDEX IF NOT EXISTS idx_sys_knowledge_base_kb_type ON sys_knowledge_base(kb_type);