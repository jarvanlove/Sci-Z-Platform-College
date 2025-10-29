package com.sciz.server.domain.pojo.repository.log.impl;

import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.domain.pojo.mapper.log.SysLoginLogMapper;
import com.sciz.server.domain.pojo.repository.log.LoginLogRepo;
import org.springframework.stereotype.Repository;

/**
 * 登录日志仓储实现
 */
@Repository
public class LoginLogRepoImpl implements LoginLogRepo {

    private final SysLoginLogMapper mapper;

    public LoginLogRepoImpl(SysLoginLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysLoginLog entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
