package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.user.UserCreateReq;
import com.sciz.server.domain.pojo.dto.response.user.RolePermissionIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.RoleUserIdsResp;
import com.sciz.server.domain.pojo.dto.response.user.UserCreateResp;
import com.sciz.server.domain.pojo.dto.response.user.UserRoleIdsResp;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import java.util.List;
import java.util.Optional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

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

    /**
     * 用户ID列表 → RoleUserIdsResp
     *
     * @param userIds List<Long> 用户ID列表
     * @return RoleUserIdsResp 响应
     */
    default RoleUserIdsResp toRoleUserIdsResp(List<Long> userIds) {
        return new RoleUserIdsResp(
                Optional.ofNullable(userIds).map(List::copyOf).orElseGet(List::of));
    }

    /**
     * 创建用户请求 → 用户实体（仅用于字段映射，不包含业务逻辑）
     *
     * @param req UserCreateReq 创建请求
     * @return SysUser 用户实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "avatarUrl", ignore = true)
    @Mapping(target = "employeeId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "loginCount", ignore = true)
    @Mapping(target = "lastLoginTime", ignore = true)
    @Mapping(target = "lastLoginIp", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    SysUser toEntity(UserCreateReq req);

    /**
     * 用户实体 → 创建用户响应
     *
     * @param entity SysUser 用户实体
     * @return UserCreateResp 创建响应
     */
    UserCreateResp toUserCreateResp(SysUser entity);
}