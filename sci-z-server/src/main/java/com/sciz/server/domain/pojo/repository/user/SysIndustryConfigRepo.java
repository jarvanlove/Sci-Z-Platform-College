package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysIndustryConfig;

/**
 * 行业配置仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysIndustryConfigRepo
 * @date 2025-10-30 11:00
 */
public interface SysIndustryConfigRepo {

    /**
     * 保存行业配置
     *
     * @param entity SysIndustryConfig 实体
     * @return 生成的主键ID
     */
    Long save(SysIndustryConfig entity);
}
