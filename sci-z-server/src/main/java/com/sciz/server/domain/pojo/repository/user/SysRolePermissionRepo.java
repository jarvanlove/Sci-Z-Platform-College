package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysRolePermission;

/**
 * 角色权限关系仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysRolePermissionRepo
 * @date 2025-10-30 11:00
 */
public interface SysRolePermissionRepo {

    /**
     * 保存角色权限关系
     *
     * @param entity SysRolePermission 实体
     * @return 生成的主键ID
     */
    Long save(SysRolePermission entity);
}
