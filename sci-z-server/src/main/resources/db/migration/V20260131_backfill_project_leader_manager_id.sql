-- 历史数据回填：满足 project_leader_id / manager_id 字段要求，不影响已有数据
-- 执行顺序：在 V20260130_add_project_leader_id_and_manager_id.sql 之后执行
-- 仅更新当前为 NULL 的记录，已有人工/新流程写入的数据不覆盖

-- 1. 申报表：根据 project_leader（姓名）匹配 sys_user.real_name，回填 project_leader_id
--    仅处理 project_leader_id 为 NULL 且 project_leader 非空的申报；姓名匹配到多人时取 id 最小的一条
UPDATE declaration d
SET project_leader_id = (
    SELECT u.id
    FROM sys_user u
    WHERE u.real_name = TRIM(d.project_leader)
      AND u.is_deleted = 0
    ORDER BY u.id
    LIMIT 1
)
WHERE d.project_leader_id IS NULL
  AND d.project_leader IS NOT NULL
  AND TRIM(d.project_leader) != ''
  AND d.is_deleted = 0;

-- 2. 项目表：从关联申报同步 project_leader_id 到 manager_id
--    仅处理 manager_id 为 NULL 且申报已有 project_leader_id 的项目
UPDATE project p
SET manager_id = d.project_leader_id
FROM declaration d
WHERE d.id = p.declaration_id
  AND d.is_deleted = 0
  AND p.manager_id IS NULL
  AND p.declaration_id IS NOT NULL
  AND d.project_leader_id IS NOT NULL
  AND p.is_deleted = 0;
