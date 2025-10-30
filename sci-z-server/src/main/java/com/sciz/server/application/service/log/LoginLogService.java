package com.sciz.server.application.service.log;

import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;

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
}
