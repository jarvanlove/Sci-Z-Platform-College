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
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
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
        logEntity.setExecutionTime(event.getExecutionTime());

        // 如果提供了 level，则根据 level 解析 status 和 logLevel
        if (event.getLevel() != null) {
            var logLevel = LogLevelStatus.fromCode(event.getLevel());
            logEntity.setLogLevel(logLevel.getCode());
            // 优先使用 level 解析的 status，但如果 event 中提供了 status，则使用 event 的 status（更准确）
            if (event.getStatus() != null) {
                logEntity.setStatus(event.getStatus());
            } else {
                logEntity.setStatus(resolveStatus(logLevel));
            }
            logEntity.setDetailInfo(event.getDetail());
            // 保留错误信息（如果有）
            logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
        } else {
            // 兼容旧逻辑：如果没有 level，使用 status 和固定 INFO 级别
            logEntity.setLogLevel(LogLevelStatus.INFO.getCode());
            logEntity.setStatus(event.getStatus());
            logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
            logEntity.setDetailInfo(event.getDetail());
        }

        // 设置创建人和更新人
        if (event.getUserId() != null) {
            logEntity.setCreatedBy(event.getUserId());
            logEntity.setUpdatedBy(event.getUserId());
        }

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
                null,
                null,
                null,
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
                        log.getBrowser(),
                        log.getOs(),
                        LogLevelStatus.fromCode(log.getLogLevel()),
                        log.getStatus(),
                        log.getExecutionTime(),
                        log.getDetailInfo(),
                        truncate(log.getErrorMessage(), 200)))
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
                logEntity.getBrowser(),
                logEntity.getOs(),
                LogLevelStatus.fromCode(logEntity.getLogLevel()),
                logEntity.getStatus(),
                logEntity.getExecutionTime(),
                logEntity.getDetailInfo(),
                logEntity.getErrorMessage());
    }

    /**
     * 验证时间范围
     *
     * @param startTime LocalDateTime 开始时间
     * @param endTime   LocalDateTime 结束时间
     */
    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && startTime.isAfter(endTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "开始时间不能晚于结束时间");
        }
    }

    /**
     * 根据日志级别解析状态
     *
     * @param level LogLevelStatus 日志级别
     * @return Integer 状态
     */
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

    /**
     * 批量保存操作日志
     * 优化：使用批量插入，提升性能
     *
     * @param events List<OperationLoggedEvent> 事件列表
     * @return boolean 是否全部保存成功
     */
    @Override
    public boolean saveBatch(List<OperationLoggedEvent> events) {
        if (events == null || events.isEmpty()) {
            return true;
        }

        try {
            var entities = events.stream()
                    .map(this::buildLogEntity)
                    .toList();

            boolean success = repository.saveBatch(entities);
            if (success) {
                log.info(String.format("[OperationLogService] 批量保存成功: size=%d", entities.size()));
            } else {
                log.warn(String.format("[OperationLogService] 批量保存失败: size=%d", entities.size()));
            }
            return success;
        } catch (Exception e) {
            log.error(String.format("[OperationLogService] 批量保存异常: size=%d, err=%s", events.size(), e.getMessage()), e);
            return false;
        }
    }

    /**
     * 构建日志实体（从事件）
     *
     * @param event OperationLoggedEvent 事件
     * @return SysOperationLog 日志实体
     */
    private SysOperationLog buildLogEntity(OperationLoggedEvent event) {
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
        logEntity.setExecutionTime(event.getExecutionTime());

        // 如果提供了 level，则根据 level 解析 status 和 logLevel
        if (event.getLevel() != null) {
            var logLevel = LogLevelStatus.fromCode(event.getLevel());
            logEntity.setLogLevel(logLevel.getCode());
            if (event.getStatus() != null) {
                logEntity.setStatus(event.getStatus());
            } else {
                logEntity.setStatus(resolveStatus(logLevel));
            }
            logEntity.setDetailInfo(event.getDetail());
            logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
        } else {
            logEntity.setLogLevel(LogLevelStatus.INFO.getCode());
            logEntity.setStatus(event.getStatus());
            logEntity.setErrorMessage(truncate(event.getErrorMessage(), 1000));
            logEntity.setDetailInfo(event.getDetail());
        }

        // 设置创建人和更新人
        if (event.getUserId() != null) {
            logEntity.setCreatedBy(event.getUserId());
            logEntity.setUpdatedBy(event.getUserId());
        }

        return logEntity;
    }

    /**
     * 截取字符串
     *
     * @param s   String 字符串
     * @param max int 最大长度
     * @return String 截取后的字符串
     */
    private String truncate(String s, int max) {
        return Optional.ofNullable(s)
                .map(value -> value.length() <= max ? value : value.substring(0, max))
                .orElse(null);
    }
}
