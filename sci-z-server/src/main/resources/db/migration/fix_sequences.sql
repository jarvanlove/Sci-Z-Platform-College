-- ============================================================
-- PostgreSQL 序列修复脚本
-- ============================================================
-- 问题：数据迁移时手动指定了 id 值，导致序列的当前值小于表中最大的 id
-- 解决：自动将序列的当前值设置为表中最大的 id 值
-- 
-- 使用方法：
-- 1. 在 PostgreSQL 客户端执行此脚本
-- 2. 或者复制单个表的修复 SQL 单独执行
-- ============================================================

-- 方法一：自动修复所有表的序列（推荐）
DO $$
DECLARE
    rec RECORD;
    max_id BIGINT;
    current_val BIGINT;
    fixed_count INT := 0;
    skipped_count INT := 0;
BEGIN
    FOR rec IN
        SELECT 
            t.table_name,
            c.column_name,
            pg_get_serial_sequence(t.table_schema || '.' || t.table_name, c.column_name) AS sequence_name
        FROM information_schema.tables t
        INNER JOIN information_schema.columns c 
            ON t.table_schema = c.table_schema 
            AND t.table_name = c.table_name
        WHERE t.table_schema = 'public'
          AND t.table_type = 'BASE TABLE'
          AND c.is_identity = 'YES'
          AND c.column_name = 'id'
        ORDER BY t.table_name
    LOOP
        IF rec.sequence_name IS NULL THEN
            RAISE NOTICE '跳过表 %：未找到对应的序列', rec.table_name;
            skipped_count := skipped_count + 1;
            CONTINUE;
        END IF;

        BEGIN
            -- 获取表中最大的 id 值
            EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %I', rec.column_name, rec.table_name) INTO max_id;

            IF max_id IS NULL OR max_id = 0 THEN
                RAISE NOTICE '跳过序列 %：表 % 为空或最大 id 为 0', rec.sequence_name, rec.table_name;
                skipped_count := skipped_count + 1;
                CONTINUE;
            END IF;

            -- 获取序列的当前值
            EXECUTE format('SELECT last_value FROM %s', rec.sequence_name) INTO current_val;

            -- 如果序列值小于最大 id，则修复
            IF current_val < max_id THEN
                EXECUTE format('SELECT setval(%L, %s, false)', rec.sequence_name, max_id);
                RAISE NOTICE '已修复序列: % (表: %, 原值: %, 新值: %)', 
                    rec.sequence_name, rec.table_name, current_val, max_id;
                fixed_count := fixed_count + 1;
            ELSE
                RAISE NOTICE '序列正常: % (表: %, 当前值: %, 最大 id: %)', 
                    rec.sequence_name, rec.table_name, current_val, max_id;
                skipped_count := skipped_count + 1;
            END IF;

        EXCEPTION WHEN OTHERS THEN
            RAISE WARNING '修复序列失败: % (表: %), 错误: %', rec.sequence_name, rec.table_name, SQLERRM;
            skipped_count := skipped_count + 1;
        END;
    END LOOP;

    RAISE NOTICE '序列检查完成：修复 % 个序列，跳过 % 个序列', fixed_count, skipped_count;
END $$;

-- ============================================================
-- 方法二：手动修复单个表的序列（示例）
-- ============================================================
-- 如果需要手动修复某个表，可以使用以下 SQL：
--
-- -- 1. 查看序列的当前值
-- SELECT last_value FROM sys_role_permission_id_seq;
--
-- -- 2. 查看表中最大的 id 值
-- SELECT MAX(id) FROM sys_role_permission;
--
-- -- 3. 修复序列（将序列值设置为表中最大的 id）
-- SELECT setval('sys_role_permission_id_seq', (SELECT MAX(id) FROM sys_role_permission), false);
--
-- ============================================================
-- 常见表的序列修复 SQL（复制执行）
-- ============================================================

-- 修复 sys_role_permission 表的序列
SELECT setval('sys_role_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role_permission), false);

-- 修复 sys_user 表的序列
SELECT setval('sys_user_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user), false);

-- 修复 sys_role 表的序列
SELECT setval('sys_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_role), false);

-- 修复 sys_permission 表的序列
SELECT setval('sys_permission_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_permission), false);

-- 修复 sys_user_role 表的序列
SELECT setval('sys_user_role_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_user_role), false);

-- 修复 project 表的序列
SELECT setval('project_id_seq', (SELECT COALESCE(MAX(id), 1) FROM project), false);

-- 修复 declaration 表的序列
SELECT setval('declaration_id_seq', (SELECT COALESCE(MAX(id), 1) FROM declaration), false);

-- 修复 sys_knowledge_base 表的序列
SELECT setval('sys_knowledge_base_id_seq', (SELECT COALESCE(MAX(id), 1) FROM sys_knowledge_base), false);

-- ============================================================
-- 查询所有需要修复的序列（诊断用）
-- ============================================================
SELECT 
    t.table_name AS "表名",
    c.column_name AS "列名",
    pg_get_serial_sequence(t.table_schema || '.' || t.table_name, c.column_name) AS "序列名",
    (SELECT MAX(id) FROM information_schema.columns WHERE table_name = t.table_name AND column_name = 'id') AS "最大ID",
    (SELECT last_value FROM pg_sequences WHERE sequencename = pg_get_serial_sequence(t.table_schema || '.' || t.table_name, c.column_name)) AS "序列当前值"
FROM information_schema.tables t
INNER JOIN information_schema.columns c 
    ON t.table_schema = c.table_schema 
    AND t.table_name = c.table_name
WHERE t.table_schema = 'public'
  AND t.table_type = 'BASE TABLE'
  AND c.is_identity = 'YES'
  AND c.column_name = 'id'
ORDER BY t.table_name;

