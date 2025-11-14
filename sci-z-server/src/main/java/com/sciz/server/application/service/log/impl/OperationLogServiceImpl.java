package com.sciz.server.application.service.log.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.log.OperationLogService;
import com.sciz.server.domain.pojo.dto.request.log.OperationLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.OperationLogResp;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.domain.pojo.repository.log.SysOperationLogRepo;
import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import com.sciz.server.infrastructure.shared.event.log.SystemOperationLoggedEvent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 操作日志应用服务实现
 *
 * @author JiaWen.Wu
 * @className OperationLogServiceImpl
 * @date 2025-10-29 16:35
 */
@Slf4j
@Service
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogRepo repository;

    public OperationLogServiceImpl(SysOperationLogRepo repository) {
        this.repository = repository;
    }

    @Override
    public Long saveFromEvent(OperationLoggedEvent event) {
        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setUserId(event.getUserId());
        logEntity.setUsername(event.getUsername());
        logEntity.setOperation(event.getOperation());
        logEntity.setMethod(event.getMethod());
        logEntity.setRequestUrl(event.getRequestUrl());
        logEntity.setRequestParams(truncate(event.getRequestParams(), 2000));
        logEntity.setResponseResult(truncate(event.getResponseResult(), 2000));
        logEntity.setIpAddress(event.getIpAddress());
        logEntity.setLocation(event.getLocation());
        logEntity.setBrowser(event.getBrowser());
        logEntity.setOs(event.getOs());
        logEntity.setLogLevel(LogLevelStatus.INFO.getCode());
        logEntity.setStatus(event.getStatus());
        logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
        logEntity.setExecutionTime(event.getExecutionTime());
        logEntity.setDetailInfo(null);

        Long id = save(logEntity);
        log.info(String.format("[OperationLogService] saved operationLog id=%s op=%s user=%s",
                String.valueOf(id), event.getOperation(), event.getUsername()));
        return id;
    }

    @Override
    public Long save(SysOperationLog logEntity) {
        return repository.save(logEntity);
    }

    @Override
    public PageResult<OperationLogResp> page(OperationLogQueryReq req) {
        validateTimeRange(req.startTime(), req.endTime());

        var baseQuery = req.toBaseQuery();
        var page = new Page<SysOperationLog>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("createdTime");

        IPage<SysOperationLog> logPage = repository.page(page,
                req.username(),
                req.operation(),
                normalizeMethod(req.method()),
                req.status(),
                req.startTime(),
                req.endTime(),
                req.level(),
                sortBy,
                asc);

        var records = logPage.getRecords().stream()
                .map(log -> new OperationLogResp(
                        log.getId(),
                        log.getUserId(),
                        log.getUsername(),
                        log.getOperation(),
                        log.getMethod(),
                        log.getRequestUrl(),
                        log.getRequestParams(),
                        log.getResponseResult(),
                        log.getIpAddress(),
                        log.getLocation(),
                        LogLevelStatus.fromCode(log.getLogLevel()),
                        log.getStatus(),
                        log.getExecutionTime(),
                        log.getDetailInfo(),
                        truncate(log.getErrorMessage(), 200),
                        log.getCreatedTime()))
                .toList();

        Page<OperationLogResp> resultPage = new Page<>(logPage.getCurrent(), logPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(logPage.getTotal());
        return PageResult.of(resultPage);
    }

    @Override
    public OperationLogResp findDetail(Long logId) {
        var logEntity = Optional.ofNullable(repository.findById(logId))
                .orElseThrow(() -> new BusinessException(ResultCode.DATA_NOT_FOUND, "操作日志不存在"));
        return new OperationLogResp(
                logEntity.getId(),
                logEntity.getUserId(),
                logEntity.getUsername(),
                logEntity.getOperation(),
                logEntity.getMethod(),
                logEntity.getRequestUrl(),
                logEntity.getRequestParams(),
                logEntity.getResponseResult(),
                logEntity.getIpAddress(),
                logEntity.getLocation(),
                LogLevelStatus.fromCode(logEntity.getLogLevel()),
                logEntity.getStatus(),
                logEntity.getExecutionTime(),
                logEntity.getDetailInfo(),
                truncate(logEntity.getErrorMessage(), 200),
                logEntity.getCreatedTime());
    }

    @Override
    public Long saveFromSystemEvent(SystemOperationLoggedEvent event) {
        SysOperationLog entity = new SysOperationLog();
        entity.setUserId(event.getUserId());
        entity.setUsername(event.getUsername());
        entity.setOperation(event.getOperation());
        entity.setMethod(event.getMethod());
        entity.setRequestUrl(event.getRequestUrl());
        entity.setRequestParams(truncate(event.getRequestParams(), 2000));
        entity.setResponseResult(truncate(event.getResponseResult(), 2000));
        entity.setIpAddress(event.getIpAddress());
        entity.setLocation(event.getLocation());
        entity.setBrowser(event.getBrowser());
        entity.setOs(event.getOs());
        var logLevel = LogLevelStatus.fromCode(event.getLevel());
        entity.setStatus(resolveStatus(logLevel));
        entity.setLogLevel(logLevel.getCode());
        entity.setDetailInfo(event.getDetail());
        entity.setErrorMessage(null);
        entity.setExecutionTime(event.getExecutionTime());
        return save(entity);
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始时间不能晚于结束时间");
        }
    }

    private String normalizeMethod(String method) {
        if (!StringUtils.hasText(method)) {
            return null;
        }
        return method.trim().toUpperCase();
    }

    private Integer resolveStatus(LogLevelStatus level) {
        if (level == null) {
            return 1;
        }
        return switch (level) {
            case WARN -> 2;
            case ERROR -> 0;
            default -> 1;
        };
    }

    private String truncate(String s, int max) {
        return Optional.ofNullable(s)
                .map(value -> value.length() <= max ? value : value.substring(0, max))
                .orElse(null);
    }
}
