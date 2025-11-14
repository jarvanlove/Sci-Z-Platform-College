package com.sciz.server.domain.pojo.repository.log.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.domain.pojo.mapper.log.SysOperationLogMapper;
import com.sciz.server.domain.pojo.repository.log.SysOperationLogRepo;
import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import java.time.LocalDateTime;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

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

    @Override
    public SysOperationLog findById(Long id) {
        return mapper.selectById(id);
    }

    @Override
    public IPage<SysOperationLog> page(IPage<SysOperationLog> page,
            String username,
            String operation,
            String method,
            Integer status,
            LocalDateTime startTime,
            LocalDateTime endTime,
            LogLevelStatus level,
            String sortBy,
            boolean asc) {
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysOperationLog::getUsername, username);
        }
        if (StringUtils.hasText(operation)) {
            wrapper.like(SysOperationLog::getOperation, operation);
        }
        if (StringUtils.hasText(method)) {
            wrapper.eq(SysOperationLog::getMethod, method.toUpperCase());
        }
        if (status != null) {
            wrapper.eq(SysOperationLog::getStatus, status);
        }
        if (level != null) {
            wrapper.eq(SysOperationLog::getLogLevel, level.getCode());
        }
        if (startTime != null) {
            wrapper.ge(SysOperationLog::getCreatedTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperationLog::getCreatedTime, endTime);
        }
        SFunction<SysOperationLog, ?> sortColumn = resolveSortColumn(sortBy);
        wrapper.orderBy(true, asc, sortColumn);
        return mapper.selectPage(page, wrapper);
    }

    private SFunction<SysOperationLog, ?> resolveSortColumn(String sortBy) {
        if (StringUtils.hasText(sortBy)) {
            var normalized = sortBy.trim().toLowerCase();
            return switch (normalized) {
                case "executiontime" -> SysOperationLog::getExecutionTime;
                case "status" -> SysOperationLog::getStatus;
                case "username" -> SysOperationLog::getUsername;
                default -> SysOperationLog::getCreatedTime;
            };
        }
        return SysOperationLog::getCreatedTime;
    }
}
