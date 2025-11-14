package com.sciz.server.domain.pojo.dto.request.system;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色权限更新请求
 *
 * @param roleId           Long 角色ID
 * @param permissionIdList List<Long> 权限ID集合
 * @author JiaWen.Wu
 * @className RolePermissionUpdateReq
 * @date 2025-11-14 15:10
 */
public record RolePermissionUpdateReq(
        @NotNull(message = "角色ID不能为空") Long roleId,
        @NotEmpty(message = "权限列表不能为空") List<Long> permissionIdList) {
}
