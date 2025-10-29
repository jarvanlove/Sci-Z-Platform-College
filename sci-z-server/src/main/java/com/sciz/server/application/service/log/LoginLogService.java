package com.sciz.server.application.service.log;

import com.sciz.server.domain.pojo.entity.log.SysLoginLog;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;

/**
 * 登录日志应用服务
 */
public interface LoginLogService {

    Long saveFromEvent(LoginLoggedEvent event);

    Long save(SysLoginLog entity);
}
