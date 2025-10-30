package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysRole;

/**
 * 角色仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysRoleRepo
 * @date 2025-10-30 11:00
 */
public interface SysRoleRepo {

    /**
     * 保存角色
     *
     * @param entity SysRole 实体
     * @return 生成的主键ID
     */
    Long save(SysRole entity);
}
