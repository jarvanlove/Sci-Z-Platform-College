package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysRole;
import com.sciz.server.domain.pojo.mapper.user.SysRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysRoleRepo;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 角色仓储实现
 *
 * @author JiaWen.Wu
 * @className SysRoleRepoImpl
 * @date 2025-10-31 12:00
 */
@Repository
public class SysRoleRepoImpl implements SysRoleRepo {

    private final SysRoleMapper mapper;

    public SysRoleRepoImpl(SysRoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysRole entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public List<SysRole> findByIds(List<Long> ids) {
        return mapper.selectBatchIds(ids);
    }
}
