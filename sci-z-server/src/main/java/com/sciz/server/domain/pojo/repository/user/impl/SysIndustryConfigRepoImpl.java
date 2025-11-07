package com.sciz.server.domain.pojo.repository.user.impl;

import com.sciz.server.domain.pojo.entity.user.SysIndustryConfig;
import com.sciz.server.domain.pojo.mapper.user.SysIndustryConfigMapper;
import com.sciz.server.domain.pojo.repository.user.SysIndustryConfigRepo;
import org.springframework.stereotype.Repository;

/**
 * 行业配置仓储实现
 * 
 * @author JiaWen.Wu
 * @className SysIndustryConfigRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysIndustryConfigRepoImpl implements SysIndustryConfigRepo {

    private final SysIndustryConfigMapper mapper;

    public SysIndustryConfigRepoImpl(SysIndustryConfigMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 保存行业配置
     *
     * @param entity SysIndustryConfig 行业配置实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysIndustryConfig entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
