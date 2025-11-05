package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysPermission;
import com.sciz.server.domain.pojo.mapper.user.SysPermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysPermissionRepo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 权限仓储实现
 *
 * @author JiaWen.Wu
 * @className SysPermissionRepoImpl
 * @date 2025-10-31 12:00
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

    @Override
    public List<SysPermission> findByIds(List<Long> ids) {
        return mapper.selectBatchIds(ids);
    }
}
