-- ============================================================
-- PostgreSQL 序列诊断脚本
-- ============================================================
-- 功能：检查所有表的序列是否需要修复
-- 使用方法：在 PostgreSQL 客户端执行此脚本，查看哪些表存在问题
-- ============================================================
-- 注意：由于需要动态查询每个表的 MAX(id)，所以使用 DO 块执行
-- 执行后查看消息窗口（Messages）查看诊断结果
-- ============================================================

DO $$
DECLARE
    rec RECORD;
    max_id BIGINT;
    current_val BIGINT;
    needs_fix_count INT := 0;
BEGIN
    RAISE NOTICE '========================================';
    RAISE NOTICE '序列诊断报告';
    RAISE NOTICE '========================================';
    
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
            RAISE NOTICE '表: % | 状态: ⚠️  未找到序列', rec.table_name;
            CONTINUE;
        END IF;

        BEGIN
            -- 获取表中最大的 id 值
            EXECUTE format('SELECT COALESCE(MAX(%I), 0) FROM %I', rec.column_name, rec.table_name) INTO max_id;
            
            -- 获取序列的当前值
            EXECUTE format('SELECT last_value FROM %s', rec.sequence_name) INTO current_val;

            IF current_val < max_id THEN
                RAISE NOTICE '表: % | 序列: % | 最大ID: % | 序列值: % | 状态: ❌ 需要修复', 
                    rec.table_name, rec.sequence_name, max_id, current_val;
                needs_fix_count := needs_fix_count + 1;
            ELSE
                RAISE NOTICE '表: % | 序列: % | 最大ID: % | 序列值: % | 状态: ✅ 正常', 
                    rec.table_name, rec.sequence_name, max_id, current_val;
            END IF;

        EXCEPTION WHEN OTHERS THEN
            RAISE WARNING '诊断表 % 时出错: %', rec.table_name, SQLERRM;
        END;
    END LOOP;
    
    RAISE NOTICE '========================================';
    RAISE NOTICE '诊断完成：共 % 个表需要修复', needs_fix_count;
    RAISE NOTICE '========================================';
END $$;

