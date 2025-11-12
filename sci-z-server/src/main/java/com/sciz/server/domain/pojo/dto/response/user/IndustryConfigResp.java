package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 行业配置响应
 *
 * @param industryCode     String 行业编码
 * @param industryName     String 行业名称
 * @param departmentLabel  String 部门标签
 * @param roleLabel        String 角色标签
 * @param employeeIdLabel  String 员工编号标签
 * @param profileFieldList List<IndustryProfileFieldResp> 扩展字段列表
 * @author JiaWen.Wu
 * @className IndustryConfigResp
 * @date 2025-11-12 11:20
 */
public record IndustryConfigResp(
                String industryCode,
                String industryName,
                String departmentLabel,
                String roleLabel,
                String employeeIdLabel,
                List<IndustryProfileFieldResp> profileFieldList) {
}
