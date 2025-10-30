package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysPermission;
import com.sciz.server.domain.pojo.mapper.user.SysPermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysPermissionRepo;
import org.springframework.stereotype.Repository;

/**
 * 权限仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysPermissionRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysPermissionRepoImpl implements SysPermissionRepo {

    private final SysPermissionMapper mapper;

    public SysPermissionRepoImpl(SysPermissionMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysPermission entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
