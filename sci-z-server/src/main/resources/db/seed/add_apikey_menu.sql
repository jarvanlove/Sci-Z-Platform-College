-- ============================================================
-- 添加 API Key 配置菜单 SQL
-- 功能：在系统管理下添加 API Key 配置子菜单
-- 作者：JiaWen.Wu
-- 日期：2025-01-28
-- ============================================================

BEGIN;

-- 1) 添加 API Key 配置菜单（系统管理下的子菜单）
-- 为所有行业（education, medical, power）创建菜单
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, path, icon, sort_order, status, is_deleted, created_time)
SELECT 
  (SELECT id FROM sys_permission WHERE permission_code='menu:system:user' AND industry_type=i.industry_type AND is_deleted=0),
  'API Key 配置',
  'menu:system:apikey',
  1,
  i.industry_type,
  '/system/apikey',
  'Key',
  5,
  1,
  0,
  CURRENT_TIMESTAMP
FROM industries i;

-- 2) 添加 API Key 配置相关的按钮权限
-- 为所有行业创建按钮权限
WITH industries(industry_type) AS (
  VALUES ('education'), ('medical'), ('power')
)
INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, sort_order, status, is_deleted, created_time)
SELECT 
  (SELECT id FROM sys_permission WHERE permission_code='menu:system:apikey' AND industry_type=i.industry_type AND is_deleted=0),
  'API Key-新建按钮',
  'button:apikey:create',
  2,
  i.industry_type,
  1,
  1,
  0,
  CURRENT_TIMESTAMP
FROM industries i
UNION ALL
SELECT 
  (SELECT id FROM sys_permission WHERE permission_code='menu:system:apikey' AND industry_type=i.industry_type AND is_deleted=0),
  'API Key-编辑按钮',
  'button:apikey:edit',
  2,
  i.industry_type,
  2,
  1,
  0,
  CURRENT_TIMESTAMP
FROM industries i
UNION ALL
SELECT 
  (SELECT id FROM sys_permission WHERE permission_code='menu:system:apikey' AND industry_type=i.industry_type AND is_deleted=0),
  'API Key-删除按钮',
  'button:apikey:delete',
  2,
  i.industry_type,
  3,
  1,
  0,
  CURRENT_TIMESTAMP
FROM industries i
UNION ALL
SELECT 
  (SELECT id FROM sys_permission WHERE permission_code='menu:system:apikey' AND industry_type=i.industry_type AND is_deleted=0),
  'API Key-启用/禁用按钮',
  'button:apikey:toggle',
  2,
  i.industry_type,
  4,
  1,
  0,
  CURRENT_TIMESTAMP
FROM industries i;

-- 3) 为管理员角色绑定新菜单权限（自动绑定所有新创建的权限）
-- 注意：如果管理员角色已经绑定了所有权限（通过通配符），此步骤可以省略
-- 这里仅为 education 行业的管理员绑定，其他行业可根据需要添加
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='education' AND is_deleted=0),
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_permission p
WHERE p.permission_code IN ('menu:system:apikey', 'button:apikey:create', 'button:apikey:edit', 'button:apikey:delete', 'button:apikey:toggle')
  AND p.industry_type='education'
  AND p.is_deleted=0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp 
    WHERE rp.role_id = (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='education' AND is_deleted=0)
      AND rp.permission_id = p.id
      AND rp.is_deleted=0
  );

-- 为 medical 行业的管理员绑定
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='medical' AND is_deleted=0),
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_permission p
WHERE p.permission_code IN ('menu:system:apikey', 'button:apikey:create', 'button:apikey:edit', 'button:apikey:delete', 'button:apikey:toggle')
  AND p.industry_type='medical'
  AND p.is_deleted=0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp 
    WHERE rp.role_id = (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='medical' AND is_deleted=0)
      AND rp.permission_id = p.id
      AND rp.is_deleted=0
  );

-- 为 power 行业的管理员绑定
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='power' AND is_deleted=0),
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_permission p
WHERE p.permission_code IN ('menu:system:apikey', 'button:apikey:create', 'button:apikey:edit', 'button:apikey:delete', 'button:apikey:toggle')
  AND p.industry_type='power'
  AND p.is_deleted=0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp 
    WHERE rp.role_id = (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='power' AND is_deleted=0)
      AND rp.permission_id = p.id
      AND rp.is_deleted=0
  );

COMMIT;

-- ============================================================
-- 说明：
-- 1. 菜单路径：/system/apikey
-- 2. 菜单权限码：menu:system:apikey
-- 3. 按钮权限码：
--    - button:apikey:create（新建）
--    - button:apikey:edit（编辑）
--    - button:apikey:delete（删除）
--    - button:apikey:toggle（启用/禁用）
-- 4. 已为所有行业（education, medical, power）创建菜单和按钮权限
-- 5. 已为所有行业的管理员角色绑定新权限
-- ============================================================

