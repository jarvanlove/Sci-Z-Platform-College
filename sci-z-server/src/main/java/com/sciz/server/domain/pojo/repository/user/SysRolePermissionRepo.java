package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysRolePermission;

import java.util.List;

/**
 * 角色权限仓储
 *
 * @author JiaWen.Wu
 * @className SysRolePermissionRepo
 * @date 2025-10-31 12:00
 */
public interface SysRolePermissionRepo {

    /**
     * 保存角色权限关系
     *
     * @param entity SysRolePermission 实体
     * @return 生成的主键ID
     */
    Long save(SysRolePermission entity);

    /**
     * 按角色ID查询未删除的角色权限关系
     *
     * @param roleIds List<Long> 角色ID集合
     * @return List<SysRolePermission> 关系集合
     */
    List<SysRolePermission> findNotDeletedByRoleIds(List<Long> roleIds);
}
