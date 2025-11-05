package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import com.sciz.server.domain.pojo.mapper.user.SysRolePermissionMapper;
import com.sciz.server.domain.pojo.repository.user.SysRolePermissionRepo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色权限仓储实现
 *
 * @author JiaWen.Wu
 * @className SysRolePermissionRepoImpl
 * @date 2025-10-31 12:00
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

    @Override
    public List<SysRolePermission> findNotDeletedByRoleIds(List<Long> roleIds) {
        return mapper.selectList(new QueryWrapper<SysRolePermission>()
                .in("role_id", roleIds)
                .eq("is_deleted", 0));
    }
}
