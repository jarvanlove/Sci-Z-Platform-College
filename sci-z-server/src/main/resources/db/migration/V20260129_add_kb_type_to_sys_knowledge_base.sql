-- 知识库分类与可见性：为 sys_knowledge_base 增加 kb_type 字段并回填历史数据
-- 执行前请备份数据库。新环境使用 init_all_tables.sql 已包含该字段，无需执行本脚本。

-- 1. 增加 kb_type 列（默认 personal，便于先插入后回填）
ALTER TABLE sys_knowledge_base
    ADD COLUMN IF NOT EXISTS kb_type VARCHAR(20) NOT NULL DEFAULT 'personal';

-- 2. 注释
COMMENT ON COLUMN sys_knowledge_base.kb_type IS '知识库类型：personal=个人知识库，project=项目知识库';

-- 3. 历史数据回填：有 project_id 的为项目知识库
UPDATE sys_knowledge_base
SET kb_type = 'project'
WHERE project_id IS NOT NULL
  AND (kb_type IS NULL OR kb_type = 'personal');

-- 4. 索引（若已存在则跳过，视数据库支持情况可改为 CREATE INDEX IF NOT EXISTS）
-- H2 / PostgreSQL 等支持 IF NOT EXISTS：
CREATE INDEX IF NOT EXISTS idx_sys_knowledge_base_kb_type ON sys_knowledge_base(kb_type);
