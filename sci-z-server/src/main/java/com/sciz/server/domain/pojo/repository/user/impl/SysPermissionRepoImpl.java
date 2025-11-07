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

    /**
     * 保存权限
     *
     * @param entity SysPermission 权限实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysPermission entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据ID列表批量查询权限
     *
     * @param ids List<Long> 权限ID列表
     * @return List<SysPermission> 权限列表
     */
    @Override
    public List<SysPermission> findByIds(List<Long> ids) {
        return mapper.selectBatchIds(ids);
    }
}
