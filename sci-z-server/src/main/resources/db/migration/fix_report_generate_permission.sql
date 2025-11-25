-- ============================================================
-- 修复报告生成权限 SQL（强制插入版本）
-- 功能：强制添加 menu:report:generate 权限并绑定到 admin 角色
-- 使用场景：当 add_report_generate_menu.sql 执行后权限仍未生效时使用
-- 作者：JiaWen.Wu
-- 日期：2025-01-24
-- ============================================================

BEGIN;

-- 1. 确保权限存在（如果不存在则插入，如果存在则更新）
DO $$
DECLARE
  v_parent_id BIGINT;
  v_permission_id BIGINT;
BEGIN
  -- 获取父菜单ID
  SELECT id INTO v_parent_id
  FROM sys_permission
  WHERE permission_code = 'menu:report:list'
    AND industry_type = 'education'
    AND is_deleted = 0
  LIMIT 1;
  
  -- 检查权限是否已存在
  SELECT id INTO v_permission_id
  FROM sys_permission
  WHERE permission_code = 'menu:report:generate'
    AND industry_type = 'education'
    AND is_deleted = 0
  LIMIT 1;
  
  -- 如果不存在则插入
  IF v_permission_id IS NULL AND v_parent_id IS NOT NULL THEN
    INSERT INTO sys_permission (parent_id, permission_name, permission_code, permission_type, industry_type, path, icon, sort_order, status, is_deleted, created_time)
    VALUES (v_parent_id, '报告生成', 'menu:report:generate', 1, 'education', '/report/generate', 'Edit', 2, 1, 0, CURRENT_TIMESTAMP)
    RETURNING id INTO v_permission_id;
    
    RAISE NOTICE '权限创建成功，ID: %', v_permission_id;
  ELSIF v_permission_id IS NOT NULL THEN
    RAISE NOTICE '权限已存在，ID: %', v_permission_id;
  ELSE
    RAISE NOTICE '父菜单不存在，无法创建权限';
  END IF;
END $$;

-- 2. 确保权限状态为启用
UPDATE sys_permission
SET status = 1,
    is_deleted = 0
WHERE permission_code = 'menu:report:generate'
  AND industry_type = 'education';

-- 3. 强制绑定权限到 admin 角色（删除旧绑定后重新绑定）
-- 先删除可能存在的旧绑定（软删除）
UPDATE sys_role_permission
SET is_deleted = 1
WHERE role_id = (SELECT id FROM sys_role WHERE role_code='admin' AND industry_type='education' AND is_deleted=0 LIMIT 1)
  AND permission_id = (SELECT id FROM sys_permission WHERE permission_code='menu:report:generate' AND industry_type='education' AND is_deleted=0 LIMIT 1);

-- 4. 重新绑定权限
INSERT INTO sys_role_permission (role_id, permission_id, is_deleted, created_time)
SELECT
  r.id,
  p.id,
  0,
  CURRENT_TIMESTAMP
FROM sys_role r
CROSS JOIN sys_permission p
WHERE r.role_code = 'admin'
  AND r.industry_type = 'education'
  AND r.is_deleted = 0
  AND p.permission_code = 'menu:report:generate'
  AND p.industry_type = 'education'
  AND p.is_deleted = 0
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_permission rp 
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
      AND rp.is_deleted = 0
  );

-- 5. 验证绑定结果
SELECT 
  '权限验证' AS check_type,
  p.id AS permission_id,
  p.permission_code,
  p.permission_name,
  r.id AS role_id,
  r.role_code,
  rp.id AS binding_id,
  rp.is_deleted AS binding_deleted
FROM sys_permission p
JOIN sys_role_permission rp ON p.id = rp.permission_id
JOIN sys_role r ON rp.role_id = r.id
WHERE p.permission_code = 'menu:report:generate'
  AND p.industry_type = 'education'
  AND r.role_code = 'admin'
  AND r.industry_type = 'education'
  AND p.is_deleted = 0
  AND r.is_deleted = 0
  AND rp.is_deleted = 0;

COMMIT;

-- ============================================================
-- 说明：
-- 1. 此 SQL 会强制确保权限存在且状态为启用
-- 2. 会删除旧的权限绑定（软删除）后重新绑定
-- 3. 执行后需要清除 Redis 缓存或重新登录才能生效
-- 4. 如果执行后仍无权限，请检查：
--    - 用户是否绑定了 admin 角色（sys_user_role 表）
--    - 权限缓存是否已清除（Redis）
--    - 用户是否需要重新登录
-- ============================================================

