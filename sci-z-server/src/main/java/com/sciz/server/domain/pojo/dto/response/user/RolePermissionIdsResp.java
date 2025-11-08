package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 角色权限ID列表响应
 *
 * @param permissionIdList List<Long> 权限ID集合
 * @author JiaWen.Wu
 * @className RolePermissionIdsResp
 * @date 2025-11-09 03:05
 */
public record RolePermissionIdsResp(List<Long> permissionIdList) {
}
