-- ******************************************************
-- Sci-Z Platform - Initial Seed Data (PostgreSQL)
-- Purpose: Insert minimal baseline data to make system usable
-- Notes:
--  - Safe to run multiple times with ON CONFLICT where applicable
--  - Adjust values per environment needs
-- ******************************************************

BEGIN;

-- 初始化行业配置数据
INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'education', '教育行业', '院系', '角色', '学工号', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'education');
INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'medical', '医疗行业', '科室', '职务', '工号', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'medical');
INSERT INTO sys_industry_config (industry_type, industry_name, department_label, role_label, employee_id_label, is_active)
SELECT 'power', '电力行业', '部门', '岗位', '员工号', 1
WHERE NOT EXISTS (SELECT 1 FROM sys_industry_config WHERE industry_type = 'power');

-- 初始化教育行业部门数据
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '计算机科学学院', 'CS', 'education', 1, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'CS' AND industry_type = 'education'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '人工智能学院', 'AI', 'education', 1, 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'AI' AND industry_type = 'education'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '软件学院', 'SOFTWARE', 'education', 1, 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'SOFTWARE' AND industry_type = 'education'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '信息工程学院', 'IE', 'education', 1, 4, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'IE' AND industry_type = 'education'
);

-- 初始化医疗行业部门数据
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '内科', 'INTERNAL', 'medical', 1, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'INTERNAL' AND industry_type = 'medical'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '外科', 'SURGERY', 'medical', 1, 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'SURGERY' AND industry_type = 'medical'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '儿科', 'PEDIATRICS', 'medical', 1, 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'PEDIATRICS' AND industry_type = 'medical'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '妇产科', 'OBSTETRICS', 'medical', 1, 4, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'OBSTETRICS' AND industry_type = 'medical'
);

-- 初始化电力行业部门数据
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '发电部', 'GENERATION', 'power', 1, 1, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'GENERATION' AND industry_type = 'power'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '输电部', 'TRANSMISSION', 'power', 1, 2, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'TRANSMISSION' AND industry_type = 'power'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '配电部', 'DISTRIBUTION', 'power', 1, 3, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'DISTRIBUTION' AND industry_type = 'power'
);
INSERT INTO sys_department (parent_id, department_name, department_code, industry_type, level, sort_order, status)
SELECT 0, '调度部', 'DISPATCH', 'power', 1, 4, 1
WHERE NOT EXISTS (
  SELECT 1 FROM sys_department WHERE department_code = 'DISPATCH' AND industry_type = 'power'
);

-- 系统配置初始化数据
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'current_industry_type', 'education', 'string', '当前系统使用的行业类型'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'current_industry_type');
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'current_industry_name', '教育行业', 'string', '当前系统使用的行业名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'current_industry_name');
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'system_name', '高校科研项目管理平台', 'string', '系统名称'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_name');
INSERT INTO sys_config (config_key, config_value, config_type, description)
SELECT 'system_version', 'v1.0.0', 'string', '系统版本'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'system_version');
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

