-- ============================================================
-- 添加报告生成菜单权限 SQL
-- 功能：在报告管理下添加报告生成菜单权限
-- 作者：JiaWen.Wu
-- 日期：2025-01-24
-- ============================================================

BEGIN;

-- 1. 添加报告生成菜单（验收管理下的子菜单，与报告管理同级）
-- 为 education 行业创建菜单
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
    VALUES (v_parent_id, '报告生成', 'menu:report:generate', 1, 'education', '/report/generate', 'Edit', 2, 1, 0, CURRENT_TIMESTAMP);
    
    RAISE NOTICE '权限创建成功';
  ELSIF v_permission_id IS NOT NULL THEN
    RAISE NOTICE '权限已存在，ID: %', v_permission_id;
  ELSE
    RAISE NOTICE '父菜单不存在，无法创建权限';
  END IF;
END $$;

-- 2. 为 education 行业的管理员角色绑定新菜单权限
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

COMMIT;

-- ============================================================
-- 说明：
-- 1. 菜单路径：/report/generate
-- 2. 菜单权限码：menu:report:generate
-- 3. 父菜单：验收管理（menu:report:list），与"报告管理"同级
-- 4. 已为 education 行业创建菜单权限
-- 5. 已为 education 行业的管理员角色绑定新权限
-- ============================================================

