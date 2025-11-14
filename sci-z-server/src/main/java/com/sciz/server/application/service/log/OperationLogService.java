package com.sciz.server.application.service.log;

import com.sciz.server.domain.pojo.dto.request.log.OperationLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.OperationLogResp;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import com.sciz.server.infrastructure.shared.event.log.SystemOperationLoggedEvent;
import com.sciz.server.infrastructure.shared.result.PageResult;

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

    /**
     * 分页查询操作日志
     *
     * @param req OperationLogQueryReq 查询请求
     * @return PageResult<OperationLogResp> 分页结果
     */
    PageResult<OperationLogResp> page(OperationLogQueryReq req);

    /**
     * 查询操作日志详情
     *
     * @param logId Long 日志ID
     * @return OperationLogResp 日志详情
     */
    OperationLogResp findDetail(Long logId);

    /**
     * 从系统操作事件保存日志
     *
     * @param event SystemOperationLoggedEvent 事件
     * @return 日志ID
     */
    Long saveFromSystemEvent(SystemOperationLoggedEvent event);
}
