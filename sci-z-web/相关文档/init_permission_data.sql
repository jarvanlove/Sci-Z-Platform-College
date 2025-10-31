BEGIN;

-- 行业集合（一次性初始化 3 个行业）
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
-- 1) 角色（每个行业各一套）
INSERT INTO sys_role (role_name, role_code, description, industry_type, role_type, sort_order, status)
SELECT '系统管理员', 'admin', '拥有系统全部权限', i.industry_type, 'system', 1, 1 FROM industries i
UNION ALL
SELECT '普通用户',   'user',  '基础功能权限',     i.industry_type, 'system', 2, 1 FROM industries i;

-- 2) 顶级菜单（先插入父级，随后插入子级）
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, path, icon, sort_order, status)
SELECT 0, '仪表板',   'menu:dashboard:view',   1, i.industry_type, '/dashboard', 'Odometer', 10, 1 FROM industries i
UNION ALL
SELECT 0, '申报管理', 'menu:declaration:list', 1, i.industry_type, 'declaration', 'Document', 20, 1 FROM industries i
UNION ALL
SELECT 0, '项目管理', 'menu:project:list',     1, i.industry_type, 'project', 'Folder', 30, 1 FROM industries i
UNION ALL
SELECT 0, '验收管理', 'menu:report:list',      1, i.industry_type, 'acceptance', 'Check', 40, 1 FROM industries i
UNION ALL
SELECT 0, 'AI助手',   'menu:ai:chat',          1, i.industry_type, 'ai', 'Monitor', 50, 1 FROM industries i
UNION ALL
SELECT 0, '用户中心', 'menu:user:profile',     1, i.industry_type, 'user', 'User', 60, 1 FROM industries i
UNION ALL
SELECT 0, '系统管理', 'menu:system:user',      1, i.industry_type, 'system', 'Setting', 70, 1 FROM industries i;

-- 3) 二级菜单
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, path, icon, sort_order, status)
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:declaration:list' AND industry_type=i.industry_type), '申报列表', 'menu:declaration:list:sub', 1, i.industry_type, '/declaration/list', 'List', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:declaration:list' AND industry_type=i.industry_type), '新建申报', 'menu:declaration:create', 1, i.industry_type, '/declaration/create', 'CirclePlus', 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:project:list' AND industry_type=i.industry_type), '项目列表', 'menu:project:list:sub', 1, i.industry_type, '/project/list', 'List', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:report:list' AND industry_type=i.industry_type), '报告管理', 'menu:report:list:sub', 1, i.industry_type, '/report/list', 'List', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:ai:chat' AND industry_type=i.industry_type), '知识库', 'menu:knowledge:list', 1, i.industry_type, '/knowledge/list', 'Reading', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:ai:chat' AND industry_type=i.industry_type), 'AI对话', 'menu:ai:chat:sub', 1, i.industry_type, '/ai/chat', 'Avatar', 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:user:profile' AND industry_type=i.industry_type), '个人信息', 'menu:user:profile:sub', 1, i.industry_type, '/user/profile', 'House', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:user:profile' AND industry_type=i.industry_type), '安全设置', 'menu:user:security', 1, i.industry_type, '/user/security', 'Lock', 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user' AND industry_type=i.industry_type), '用户管理', 'menu:system:user:sub', 1, i.industry_type, '/system/user', 'User', 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user' AND industry_type=i.industry_type), '角色权限', 'menu:system:role', 1, i.industry_type, '/system/role', 'Key', 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user' AND industry_type=i.industry_type), '系统配置', 'menu:system:config', 1, i.industry_type, '/system/config', 'Setting', 3, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user' AND industry_type=i.industry_type), '日志管理', 'menu:system:logs', 1, i.industry_type, '/system/logs', 'Document', 4, 1 FROM industries i;

-- 4) 按钮权限（按实际页面按钮需求初始化）
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, sort_order, status)
-- 仪表板：新建申报
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:dashboard:view' AND industry_type=i.industry_type), '仪表板-新建申报', 'button:dashboard:create_declaration', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
-- 申报管理：新建申报、删除
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:declaration:list' AND industry_type=i.industry_type), '申报-新建按钮', 'button:declaration:create', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:declaration:list' AND industry_type=i.industry_type), '申报-删除按钮', 'button:declaration:delete', 2, i.industry_type, 2, 1 FROM industries i
UNION ALL
-- 项目管理：编辑、删除
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:project:list' AND industry_type=i.industry_type), '项目-编辑按钮', 'button:project:edit', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:project:list' AND industry_type=i.industry_type), '项目-删除按钮', 'button:project:delete', 2, i.industry_type, 2, 1 FROM industries i
UNION ALL
-- 验收管理：删除、验收
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:report:list' AND industry_type=i.industry_type), '报告-删除按钮', 'button:report:delete', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:report:list' AND industry_type=i.industry_type), '报告-验收按钮', 'button:report:accept', 2, i.industry_type, 2, 1 FROM industries i
UNION ALL
-- 系统管理-用户管理：新建用户、禁用、删除
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user:sub' AND industry_type=i.industry_type), '用户-新建按钮', 'button:user:create', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user:sub' AND industry_type=i.industry_type), '用户-禁用按钮', 'button:user:disable', 2, i.industry_type, 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:user:sub' AND industry_type=i.industry_type), '用户-删除按钮', 'button:user:delete', 2, i.industry_type, 3, 1 FROM industries i
UNION ALL
-- 系统管理-角色权限：新建角色、编辑、配置权限、删除
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:role' AND industry_type=i.industry_type), '角色-新建按钮', 'button:role:create', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:role' AND industry_type=i.industry_type), '角色-编辑按钮', 'button:role:edit', 2, i.industry_type, 2, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:role' AND industry_type=i.industry_type), '角色-配置权限按钮', 'button:role:config', 2, i.industry_type, 3, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:role' AND industry_type=i.industry_type), '角色-删除按钮', 'button:role:delete', 2, i.industry_type, 4, 1 FROM industries i
UNION ALL
-- 系统管理-日志管理：清空日志、导出日志
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:logs' AND industry_type=i.industry_type), '日志-清空按钮', 'button:log:clear', 2, i.industry_type, 1, 1 FROM industries i
UNION ALL
SELECT (SELECT id FROM sys_permission WHERE permission_code='menu:system:logs' AND industry_type=i.industry_type), '日志-导出按钮', 'button:log:export', 2, i.industry_type, 2, 1 FROM industries i;

-- 5) 用户（仅默认 education 行业创建示例账户，避免 username 唯一索引冲突）
INSERT INTO sys_user (username, password, real_name, email, industry_type, status, is_deleted, created_time)
VALUES
  ('admin', 'admin', '系统管理员', 'admin@example.com', 'education', 1, 0, CURRENT_TIMESTAMP),
  ('user',  'user',  '普通用户',   'user@example.com',  'education', 1, 0, CURRENT_TIMESTAMP);

-- 6) 用户-角色 绑定（education 行业）
INSERT INTO sys_user_role (user_id, role_id, is_deleted, created_time)
VALUES
  ((SELECT id FROM sys_user WHERE username='admin' AND is_deleted=0), (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='education'), 0, CURRENT_TIMESTAMP),
  ((SELECT id FROM sys_user WHERE username='user'  AND is_deleted=0), (SELECT id FROM sys_role WHERE role_code='user'  AND industry_type='education'), 0, CURRENT_TIMESTAMP);

-- 7) 角色-权限 绑定
-- 管理员：绑定 education 行业全部权限（如需其他行业管理员绑定，可复制下述语句并替换行业）
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='education'),
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_permission p
WHERE p.industry_type='education';

-- 普通用户：绑定基础菜单（按钮权限当前前端已全部放开，无需绑定）
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  (SELECT id FROM sys_role WHERE role_code='user' AND industry_type='education'),
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_permission p
WHERE p.permission_code IN (
  'menu:dashboard:view',
  'menu:declaration:list', 'menu:declaration:list:sub', 'menu:declaration:create',
  'menu:project:list',     'menu:project:list:sub',
  'menu:report:list',      'menu:report:list:sub',
  'menu:ai:chat',          'menu:ai:chat:sub', 'menu:knowledge:list',
  'menu:user:profile',     'menu:user:profile:sub', 'menu:user:security'
)
AND p.industry_type='education';

COMMIT;

-- End of init_permission_data.sql


