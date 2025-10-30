package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysPermission;

/**
 * 权限仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysPermissionRepo
 * @date 2025-10-30 11:00
 */
public interface SysPermissionRepo {

    /**
     * 保存权限
     *
     * @param entity SysPermission 实体
     * @return 生成的主键ID
     */
    Long save(SysPermission entity);
}
