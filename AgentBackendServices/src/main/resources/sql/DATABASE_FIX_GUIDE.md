# 数据库字段映射修复指南

## 问题描述
PostgreSQL 触发器报错：`ERROR: record "new" has no field "updated at"`

## 问题原因
1. PostgreSQL 触发器函数中字段名引用不正确
2. Java 实体类字段名与数据库字段名不匹配

## 解决方案

### 1. 修复 PostgreSQL 触发器

执行以下 SQL 脚本：

```sql
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
```

### 2. 修复 Java 实体类字段映射

已修复 `BaseEntity` 类中的字段映射：

```java
@TableField(value = "created_at", fill = FieldFill.INSERT)
private LocalDateTime createTime;

@TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
private LocalDateTime updateTime;

@TableField(value = "created_by", fill = FieldFill.INSERT)
private String createBy;

@TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
private String updateBy;
```

### 3. 验证修复

#### 测试触发器
```sql
-- 更新一条记录测试触发器
UPDATE dify_api_keys 
SET key_name = '测试更新' 
WHERE id = 1;

-- 检查 updated_at 字段是否自动更新
SELECT id, key_name, updated_at 
FROM dify_api_keys 
WHERE id = 1;
```

#### 测试 Java 应用
```java
// 在 Java 中更新记录
DifyApiKey apiKey = difyApiKeyService.getById(1L);
apiKey.setKeyName("Java 测试更新");
difyApiKeyService.updateById(apiKey);
```

## 字段映射对照表

| Java 字段名 | 数据库字段名 | 说明 |
|------------|-------------|------|
| createTime | created_at | 创建时间 |
| updateTime | updated_at | 更新时间 |
| createBy | created_by | 创建人 |
| updateBy | updated_by | 更新人 |
| id | id | 主键ID |
| deleted | deleted | 逻辑删除标识 |

## 注意事项

1. **字段名大小写敏感**: PostgreSQL 中带下划线的字段名需要用双引号引用
2. **MyBatis Plus 映射**: 使用 `@TableField(value = "字段名")` 指定数据库字段名
3. **自动填充**: 使用 `fill = FieldFill.INSERT_UPDATE` 实现自动填充
4. **触发器优先级**: 数据库触发器在 MyBatis Plus 自动填充之后执行

## 相关文件

- `BaseEntity.java` - 基础实体类，包含通用字段映射
- `DifyApiKey.java` - Dify API 密钥实体类
- `dify_api_keys.sql` - 数据库表结构和触发器
- `fix_trigger.sql` - 触发器修复脚本
