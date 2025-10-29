package com.sciz.server.application.service.log.impl;

import com.sciz.server.application.service.log.OperationLogService;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.domain.pojo.repository.log.SysOperationLogRepo;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
        logEntity.setStatus(event.getStatus());
        logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
        logEntity.setExecutionTime(event.getExecutionTime());

        Long id = save(logEntity);
        log.info(String.format("[OperationLogService] saved operationLog id=%s op=%s user=%s",
                String.valueOf(id), event.getOperation(), event.getUsername()));
        return id;
    }

    @Override
    public Long save(SysOperationLog logEntity) {
        return repository.save(logEntity);
    }

    private String truncate(String s, int max) {
        if (s == null)
            return null;
        if (s.length() <= max)
            return s;
        return s.substring(0, max);
    }
}
