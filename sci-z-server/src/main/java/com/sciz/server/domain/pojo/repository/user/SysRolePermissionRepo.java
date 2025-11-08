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

    /**
     * 查询指定角色未删除的权限关系
     *
     * @param roleId Long 角色ID
     * @return List<SysRolePermission> 角色权限列表
     */
    List<SysRolePermission> findNotDeletedByRoleId(Long roleId);

    /**
     * 根据ID集合标记删除
     *
     * @param ids List<Long> 主键ID集合
     */
    void markDeletedByIds(List<Long> ids);
}
