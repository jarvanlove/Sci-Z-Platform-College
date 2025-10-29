package com.sciz.server.domain.pojo.repository.log.impl;

import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.domain.pojo.mapper.log.SysOperationLogMapper;
import com.sciz.server.domain.pojo.repository.log.SysOperationLogRepo;
import org.springframework.stereotype.Repository;

/**
 * 操作日志仓储实现
 *
 * @author JiaWen.Wu
 * @className SysOperationLogRepoImpl
 * @date 2025-10-29 16:40
 */
@Repository
public class SysOperationLogRepoImpl implements SysOperationLogRepo {

    private final SysOperationLogMapper mapper;

    public SysOperationLogRepoImpl(SysOperationLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysOperationLog entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }
}
