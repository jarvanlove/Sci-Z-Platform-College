package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysUserRole;
import com.sciz.server.domain.pojo.mapper.user.SysUserRoleMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRoleRepo;
import org.springframework.stereotype.Repository;

/**
 * SysUserRoleRepoImpl
 * 
 * @author JiaWen.Wu
 * @className SysUserRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysUserRoleRepoImpl implements SysUserRoleRepo {

    private final SysUserRoleMapper mapper;

    public SysUserRoleRepoImpl(SysUserRoleMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysUserRole entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
