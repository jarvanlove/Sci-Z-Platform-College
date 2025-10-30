-- 修复 dify_api_keys 表触发器
-- 删除现有触发器
DROP TRIGGER IF EXISTS update_dify_api_keys_updated_at ON dify_api_keys;

-- 重新创建触发器函数（修复字段名引用问题）
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW."updated_at" = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 重新创建触发器
CREATE TRIGGER update_dify_api_keys_updated_at 
    BEFORE UPDATE ON dify_api_keys 
    FOR EACH ROW 
    EXECUTE FUNCTION update_updated_at_column();

-- 验证触发器是否正常工作
-- 可以运行以下测试（可选）：
-- UPDATE dify_api_keys SET key_name = 'test' WHERE id = 1;
