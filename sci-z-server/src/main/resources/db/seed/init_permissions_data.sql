-- 初始化权限与角色数据（与《前端定义权限码.md》《权限码.md》对齐）
-- 采用 INSERT ... SELECT ... WHERE NOT EXISTS 保证幂等
-- 行业类型统一为 'education'

BEGIN;

-- ===================== sys_permission =====================
-- 菜单（permission_type = 1）
INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '仪表板', 'menu:dashboard:view', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:dashboard:view' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '申报列表', 'menu:declaration:list', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:declaration:list' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '新建申报', 'menu:declaration:create', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:declaration:create' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '申报详情', 'menu:declaration:detail', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:declaration:detail' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '项目列表', 'menu:project:list', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:project:list' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '项目详情', 'menu:project:detail', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:project:detail' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '项目进度', 'menu:project:progress', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:project:progress' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '报告列表', 'menu:report:list', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:report:list' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '生成报告', 'menu:report:generate', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:report:generate' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '知识库列表', 'menu:knowledge:list', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:knowledge:list' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '知识库详情', 'menu:knowledge:detail', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:knowledge:detail' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT 'AI 对话', 'menu:ai:chat', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:ai:chat' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '个人信息', 'menu:user:profile', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:user:profile' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '安全设置', 'menu:user:security', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:user:security' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '用户管理', 'menu:system:user', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:system:user' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '角色权限', 'menu:system:role', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:system:role' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '系统配置', 'menu:system:config', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:system:config' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '日志管理', 'menu:system:logs', 1, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'menu:system:logs' AND industry_type = 'education');

-- 按钮（permission_type = 2）
INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '申报-新建', 'button:declaration:create', 2, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:declaration:create' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '项目-新建', 'button:project:create', 2, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:project:create' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '报告-生成', 'button:report:generate', 2, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:report:generate' AND industry_type = 'education');

INSERT INTO sys_permission (permission_name, permission_code, permission_type, industry_type, status)
SELECT '知识库-新建', 'button:knowledge:create', 2, 'education', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_permission WHERE permission_code = 'button:knowledge:create' AND industry_type = 'education');

-- ===================== sys_role =====================
INSERT INTO sys_role (role_name, role_code, description, industry_type, role_type, status)
SELECT '管理员', 'admin', '系统管理员，拥有全部权限', 'education', 'system', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'admin' AND industry_type = 'education');

INSERT INTO sys_role (role_name, role_code, description, industry_type, role_type, status)
SELECT '普通用户', 'user', '普通业务用户，拥有基础菜单权限', 'education', 'custom', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_code = 'user' AND industry_type = 'education');

-- ===================== sys_role_permission =====================
-- admin 角色授予全部已存在权限
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r, sys_permission p
WHERE r.role_code = 'admin' AND r.industry_type = 'education'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

-- user 角色授予常用菜单权限（dashboard、declaration、project、report、knowledge、ai、user.profile/security）
WITH perms AS (
  SELECT permission_code FROM (VALUES
    ('menu:dashboard:view'),
    ('menu:declaration:list'), ('menu:declaration:create'), ('menu:declaration:detail'),
    ('menu:project:list'), ('menu:project:detail'), ('menu:project:progress'),
    ('menu:report:list'), ('menu:knowledge:list'),
    ('menu:ai:chat'), ('menu:user:profile'), ('menu:user:security')
  ) AS v(permission_code)
)
INSERT INTO sys_role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM sys_role r
JOIN sys_permission p ON p.permission_code IN (SELECT permission_code FROM perms) AND p.industry_type = 'education'
WHERE r.role_code = 'user' AND r.industry_type = 'education'
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp WHERE rp.role_id = r.id AND rp.permission_id = p.id
  );

COMMIT;


