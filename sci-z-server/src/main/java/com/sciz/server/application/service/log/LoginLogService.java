package com.sciz.server.application.service.log;

import com.sciz.server.domain.pojo.dto.request.log.LoginLogQueryReq;
import com.sciz.server.domain.pojo.dto.response.log.LoginLogResp;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import com.sciz.server.domain.pojo.entity.log.SysLoginLog;

/**
 * 登录日志应用服务
 * 
 * @author JiaWen.Wu
 * @className LoginLogService
 * @date 2025-10-29 11:00
 */

public interface LoginLogService {

    /**
     * 从事件保存登录日志
     *
     * @param event LoginLoggedEvent 事件
     * @return 日志ID
     */
    Long saveFromEvent(LoginLoggedEvent event);

    /**
     * 直接保存登录日志实体
     *
     * @param entity SysLoginLog 实体
     * @return 日志ID
     */
    Long save(SysLoginLog entity);

    /**
     * 分页查询登录日志
     *
     * @param req LoginLogQueryReq 查询请求
     * @return PageResult<LoginLogResp> 分页结果
     */
    PageResult<LoginLogResp> page(LoginLogQueryReq req);
}
