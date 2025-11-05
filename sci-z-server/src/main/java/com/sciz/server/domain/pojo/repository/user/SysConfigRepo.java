package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysConfig;

/**
 * 系统配置仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className SysConfigRepo
 * @date 2025-10-30 11:00
 */
public interface SysConfigRepo {

    /**
     * 保存系统配置
     *
     * @param entity SysConfig 实体
     * @return 生成的主键ID
     */
    Long save(SysConfig entity);

    /**
     * 根据配置键查询配置
     *
     * @param configKey String 配置键
     * @return SysConfig 配置实体（可能为 null）
     */
    SysConfig findByKey(String configKey);
}
