package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 部门标签响应
 *
 * @param label String 部门显示名称
 * @param value String 部门编码
 * @author JiaWen.Wu
 * @className DepartmentLabelResp
 * @date 2025-11-08 23:30
 */
public record DepartmentLabelResp(
        String label,
        String value) {
}
