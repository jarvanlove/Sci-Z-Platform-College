package com.sciz.server.domain.pojo.repository.log;

import com.sciz.server.domain.pojo.entity.log.SysLoginLog;

/**
 * 登录日志仓储（领域层抽象）
 */
public interface LoginLogRepo {

    /**
     * 保存登录日志
     *
     * @param entity SysLoginLog 实体
     * @return 生成的主键ID
     */
    Long save(SysLoginLog entity);
}
