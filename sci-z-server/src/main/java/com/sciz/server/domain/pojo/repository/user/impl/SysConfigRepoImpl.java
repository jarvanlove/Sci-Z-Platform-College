package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
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

    /**
     * 保存系统配置
     *
     * @param entity SysConfig 配置实体
     * @return Long 主键ID
     */
    @Override
    public Long save(SysConfig entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    /**
     * 根据配置键查询配置
     *
     * @param configKey String 配置键
     * @return SysConfig 配置实体
     */
    @Override
    public SysConfig findByKey(String configKey) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysConfig::getConfigKey, configKey)
                .last("limit 1")
                .one();
    }
}
