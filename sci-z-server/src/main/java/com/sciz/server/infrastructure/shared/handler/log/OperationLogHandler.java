package com.sciz.server.infrastructure.shared.handler.log;

import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import com.sciz.server.application.service.log.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 操作日志事件处理器（占位实现：打印日志，后续接入 Mapper 持久化到 sys_operation_log）
 * 
 * @author JiaWen.Wu
 * @className OperationLogHandler
 * @date 2025-10-29 14:00
 */
@Slf4j
@Component
public class OperationLogHandler {

    private final OperationLogService operationLogService;

    public OperationLogHandler(OperationLogService operationLogService) {
        this.operationLogService = operationLogService;
    }

    @Async
    @EventListener
    public void handle(OperationLoggedEvent event) {
        try {
            Long id = operationLogService.saveFromEvent(event);
            log.info(String.format("[OperationLog] saved id=%s traceId=%s user=%s op=%s method=%s status=%s cost=%sms",
                    String.valueOf(id), event.getTraceId(), event.getUsername(), event.getOperation(),
                    event.getMethod(),
                    String.valueOf(event.getStatus()), String.valueOf(event.getExecutionTime())));
        } catch (Exception e) {
            log.error(String.format("[OperationLog] save failed traceId=%s op=%s err=%s",
                    event.getTraceId(), event.getOperation(), e.getMessage()), e);
        }
    }
}
