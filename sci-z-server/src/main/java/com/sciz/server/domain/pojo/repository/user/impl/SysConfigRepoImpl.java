package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysConfig;
import com.sciz.server.domain.pojo.mapper.user.SysConfigMapper;
import com.sciz.server.domain.pojo.repository.user.SysConfigRepo;
import org.springframework.stereotype.Repository;

/**
 * 系统配置仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysConfigRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysConfigRepoImpl implements SysConfigRepo {

    private final SysConfigMapper mapper;

    public SysConfigRepoImpl(SysConfigMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysConfig entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
