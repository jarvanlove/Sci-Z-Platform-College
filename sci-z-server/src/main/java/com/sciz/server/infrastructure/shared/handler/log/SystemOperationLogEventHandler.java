package com.sciz.server.infrastructure.shared.handler.log;

import com.sciz.server.application.service.log.OperationLogService;
import com.sciz.server.infrastructure.shared.event.log.SystemOperationLoggedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 系统操作日志事件处理器
 *
 * @author JiaWen.Wu
 * @className SystemOperationLogEventHandler
 * @date 2025-11-14 21:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemOperationLogEventHandler {

    private final OperationLogService operationLogService;

    @Async
    @EventListener
    public void handle(SystemOperationLoggedEvent event) {
        try {
            var id = operationLogService.saveFromSystemEvent(event);
            log.info(String.format("[SystemOperationLog] saved id=%s operation=%s level=%s",
                    id, event.getOperation(), event.getLevel()));
        } catch (Exception e) {
            log.error(String.format("[SystemOperationLog] save failed operation=%s err=%s",
                    event.getOperation(), e.getMessage()), e);
        }
    }
}
