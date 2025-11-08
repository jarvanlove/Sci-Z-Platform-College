package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 角色校验响应
 *
 * @param roleCode     String 角色编码
 * @param industryType String 行业类型
 * @param hasRole      boolean 是否拥有角色
 * @author JiaWen.Wu
 * @className CheckRoleResp
 * @date 2025-11-09 03:40
 */
public record CheckRoleResp(
        String roleCode,
        String industryType,
        boolean hasRole) {
}
