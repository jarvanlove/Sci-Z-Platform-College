package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.response.user.RolePermissionIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.UserRoleIdsResp;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;

/**
 * 用户模块转换器
 *
 * <p>
 * 负责用户管理相关接口的入参/出参转换，减少 Controller 中的重复代码。
 * </p>
 *
 * @author JiaWen.Wu
 * @className UserConverter
 * @date 2025-11-09 03:05
 */
@Mapper(componentModel = "spring")
public interface UserConverter {

    /**
     * 角色ID列表 → UserRoleIdsResp
     *
     * @param roleIds List<Long> 角色ID列表
     * @return UserRoleIdsResp 响应
     */
    default UserRoleIdsResp toUserRoleIdsResp(List<Long> roleIds) {
        return new UserRoleIdsResp(Optional.ofNullable(roleIds).map(List::copyOf).orElseGet(List::of));
    }

    /**
     * 权限ID列表 → RolePermissionIdsResp
     *
     * @param permissionIds List<Long> 权限ID列表
     * @return RolePermissionIdsResp 响应
     */
    default RolePermissionIdsResp toRolePermissionIdsResp(List<Long> permissionIds) {
        return new RolePermissionIdsResp(
                Optional.ofNullable(permissionIds).map(List::copyOf).orElseGet(List::of));
    }
}