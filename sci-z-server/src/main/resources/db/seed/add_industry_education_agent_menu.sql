-- ============================================================
-- 产教研智能体 - 菜单与权限初始化
-- 功能：新增「产教研智能体」顶级菜单及 3 个按钮权限，并绑定至各行业 admin
-- 编写时间: 2026-02-09
-- 维护人: 研发
-- 说明：与 init_permission_data.sql 写法一致；若库中无 permission_code 列则先补列再插入
-- ============================================================

BEGIN;

-- 0) 若当前库 sys_permission 表无 permission_code 列则先添加（按项目标准表结构）
ALTER TABLE sys_permission ADD COLUMN IF NOT EXISTS permission_code VARCHAR(100);

-- 1) 顶级菜单：产教研智能体（三行业，若已存在则跳过）
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, path, icon, sort_order, status)
SELECT 0, '产教研智能体', 'menu:practice:industry-education', 1, i.industry_type, '/practice/industry-education', 'School', 52, 1
FROM industries i
WHERE NOT EXISTS (
  SELECT 1 FROM sys_permission
  WHERE permission_code = 'menu:practice:industry-education'
    AND industry_type = i.industry_type
    AND (is_deleted = 0 OR is_deleted IS NULL)
);

-- 2) 按钮权限：产教研-匹配团队、查看团队明细、分发项目（三行业，若已存在则跳过）
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, sort_order, status)
SELECT (SELECT id FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL) LIMIT 1),
       '产教研-匹配团队', 'button:practice:industry-education:match', 2, i.industry_type, 1, 1
FROM industries i
WHERE EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL))
  AND NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:practice:industry-education:match' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL))
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL) LIMIT 1),
       '产教研-查看团队明细', 'button:practice:industry-education:detail', 2, i.industry_type, 2, 1
FROM industries i
WHERE EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL))
  AND NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:practice:industry-education:detail' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL))
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL) LIMIT 1),
       '产教研-分发项目', 'button:practice:industry-education:assign', 2, i.industry_type, 3, 1
FROM industries i
WHERE EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:practice:industry-education' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL))
  AND NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:practice:industry-education:assign' AND industry_type = i.industry_type AND (is_deleted = 0 OR is_deleted IS NULL));

-- 3) 为各行业 admin 角色绑定上述 4 个权限（1 菜单 + 3 按钮）
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT r.id, p.id, 0, CURRENT_TIMESTAMP
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.role_code = 'admin'
  AND r.is_deleted = 0
  AND p.permission_code IN (
    'menu:practice:industry-education',
    'button:practice:industry-education:match',
    'button:practice:industry-education:detail',
    'button:practice:industry-education:assign'
  )
  AND r.industry_type = p.industry_type
  AND p.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp
    WHERE rp.role_id = r.id AND rp.permission_id = p.id AND rp.is_deleted = 0
  );

COMMIT;

-- ============================================================
-- 说明：
-- 1. 菜单路径：/practice/industry-education，权限码：menu:practice:industry-education
-- 2. 按钮权限码：button:practice:industry-education:match | detail | assign
-- 3. 已为 education / medical / power 三行业插入菜单与按钮，并绑定至 admin
-- 4. 若表中原无 permission_code 列，脚本开头已自动补列；若重复执行会重复插入菜单/按钮，建议仅在未存在时执行，或先删再执行
-- ============================================================
