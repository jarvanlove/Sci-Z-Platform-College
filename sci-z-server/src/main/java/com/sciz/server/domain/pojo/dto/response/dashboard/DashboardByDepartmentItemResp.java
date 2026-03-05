package com.sciz.server.domain.pojo.dto.response.dashboard;

/**
 * 按学院 / 团队数量分布条目
 *
 * @param departmentName 学院 / 团队名称
 * @param count          项目数量
 */
public record DashboardByDepartmentItemResp(
        String departmentName,
        Long count) {
}

