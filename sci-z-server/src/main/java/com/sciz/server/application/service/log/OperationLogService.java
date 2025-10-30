package com.sciz.server.application.service.log;

import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;

/**
 * 操作日志应用服务
 *
 * @author JiaWen.Wu
 * @className OperationLogService
 * @date 2025-10-29 16:35
 */
public interface OperationLogService {

    /**
     * 从事件保存操作日志
     *
     * @param event OperationLoggedEvent 事件
     * @return 日志ID
     */
    Long saveFromEvent(OperationLoggedEvent event);

    /**
     * 直接保存操作日志实体
     *
     * @param log SysOperationLog 实体
     * @return 日志ID
     */
    Long save(SysOperationLog log);
}
