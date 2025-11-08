package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 权限校验响应
 *
 * @param permissionCode String 权限编码
 * @param industryType   String 行业类型
 * @param hasPermission  boolean 是否拥有权限
 * @author JiaWen.Wu
 * @className CheckPermResp
 * @date 2025-11-09 03:40
 */
public record CheckPermResp(
        String permissionCode,
        String industryType,
        boolean hasPermission) {
}
