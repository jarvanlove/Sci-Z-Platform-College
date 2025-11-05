package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysUserRole;

/**
 * 用户角色关系仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysUserRoleRepo
 * @date 2025-10-30 11:00
 */
public interface SysUserRoleRepo {

    /**
     * 保存用户角色关系
     *
     * @param entity SysUserRole 实体
     * @return 生成的主键ID
     */
    Long save(SysUserRole entity);
}
