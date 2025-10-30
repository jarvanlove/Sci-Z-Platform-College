package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysUserProfile;
import com.sciz.server.domain.pojo.mapper.user.SysUserProfileMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserProfileRepo;
import org.springframework.stereotype.Repository;

/**
 * 用户档案仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysUserProfileRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysUserProfileRepoImpl implements SysUserProfileRepo {

    private final SysUserProfileMapper mapper;

    public SysUserProfileRepoImpl(SysUserProfileMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysUserProfile entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
