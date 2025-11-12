-- ******************************************************
-- Sci-Z Platform - Initial Seed Data (PostgreSQL)
-- Purpose: Insert minimal baseline data to make system usable
-- Notes:
--  - Safe to run multiple times with ON CONFLICT where applicable
--  - Adjust values per environment needs
-- ******************************************************

BEGIN;

-- =============== 行业配置（更完整、可跨行业） ===============
-- 约定：仅一个行业 is_active=1 作为默认“当前行业”；其余为 0
INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'education', '教育行业', '院系', '职称/职务', '学工号', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'education');

INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'medical', '医疗行业', '科室', '职称/职务', '工号', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'medical');

INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'power', '电力行业', '部门', '职称/职务', '员工号', 0
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'power');

-- =============== 行业 -> 部门种子（贴近真实行业，仅顶级层，幂等） ===============
-- 教育
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '计算机科学学院', 'CS', 'education', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'CS' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '人工智能学院', 'AI', 'education', 1, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'AI' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '软件工程学院', 'SE', 'education', 1, 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'SE' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '信息工程学院', 'IE', 'education', 1, 4, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'IE' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '数学学院', 'MATH', 'education', 1, 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'MATH' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '物理学院', 'PHYS', 'education', 1, 6, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'PHYS' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '人文社科学院', 'HSS', 'education', 1, 7, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'HSS' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '教务处', 'ACADEMIC', 'education', 1, 8, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'ACADEMIC' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '科研处', 'RESEARCH', 'education', 1, 9, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'RESEARCH' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '财务处', 'FINANCE', 'education', 1, 10, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'FINANCE' AND industry_type = 'education');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '系统管理部', 'SYSTEM_ADMIN', 'education', 1, 11, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'SYSTEM_ADMIN' AND industry_type = 'education');

-- 医疗
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '内科', 'INTERNAL', 'medical', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'INTERNAL' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '外科', 'SURGERY', 'medical', 1, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'SURGERY' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '儿科', 'PEDIATRICS', 'medical', 1, 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'PEDIATRICS' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '妇产科', 'OBSTETRICS', 'medical', 1, 4, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'OBSTETRICS' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '急诊医学科', 'EMERGENCY', 'medical', 1, 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'EMERGENCY' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '放射科', 'RADIOLOGY', 'medical', 1, 6, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'RADIOLOGY' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '检验科', 'LAB', 'medical', 1, 7, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'LAB' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '麻醉科', 'ANESTHESIA', 'medical', 1, 8, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'ANESTHESIA' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '药学部', 'PHARMACY', 'medical', 1, 9, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'PHARMACY' AND industry_type = 'medical');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '护理部', 'NURSING', 'medical', 1, 10, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'NURSING' AND industry_type = 'medical');

-- 电力
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '发电部', 'GENERATION', 'power', 1, 1, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'GENERATION' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '输电部', 'TRANSMISSION', 'power', 1, 2, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'TRANSMISSION' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '配电部', 'DISTRIBUTION', 'power', 1, 3, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'DISTRIBUTION' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '调度控制中心', 'DISPATCH', 'power', 1, 4, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'DISPATCH' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '运维中心', 'OMC', 'power', 1, 5, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'OMC' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '安全监督部', 'SAFETY', 'power', 1, 6, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'SAFETY' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '设备管理部', 'EQUIPMENT', 'power', 1, 7, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'EQUIPMENT' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '生产计划部', 'PLANNING', 'power', 1, 8, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'PLANNING' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '采购供应部', 'PROCUREMENT', 'power', 1, 9, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'PROCUREMENT' AND industry_type = 'power');
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '综合管理部', 'ADMIN', 'power', 1, 10, 1
WHERE NOT EXISTS (SELECT 1 FROM sys_department WHERE department_code = 'ADMIN' AND industry_type = 'power');

-- =============== 系统配置（面向前端动态渲染） ===============
-- 当前行业（默认教育）
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'current_industry_type', 'education', 'string', '当前系统使用的行业类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'current_industry_type');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'current_industry_name', '教育行业', 'string', '当前系统使用的行业名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'current_industry_name');

-- 行业通用标签（供前端展示，不依赖联表查询时也可快速渲染）
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'label.department', '院系', 'string', '部门标签（随当前行业变化）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'label.department');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'label.role', '角色', 'string', '角色标签（随当前行业变化）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'label.role');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'label.employee_id', '学工号', 'string', '员工ID标签（随当前行业变化）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'label.employee_id');

-- 系统信息
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'system_name', '高校科研项目管理平台', 'string', '系统名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_name');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'system_version', 'v1.0.0', 'string', '系统版本'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_version');

-- Dify 互联配置（占位）
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'dify_api_url', 'https://api.dify.ai/v1', 'string', 'Dify API地址'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'dify_api_url');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'dify_api_key', '', 'string', 'Dify API密钥'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'dify_api_key');

INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'dify_timeout', '60', 'number', 'Dify连接超时时间（秒）'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'dify_timeout');

COMMIT;

-- End of seed data

