package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import com.sciz.server.domain.pojo.mapper.user.SysRolePermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysRolePermissionRepo;
import org.springframework.stereotype.Repository;

/**
 * 角色权限关系仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysRolePermissionRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysRolePermissionRepoImpl implements SysRolePermissionRepo {

    private final SysRolePermissionMapper mapper;

    public SysRolePermissionRepoImpl(SysRolePermissionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysRolePermission entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
