package com.sciz.server.infrastructure.shared.handler.log;

import com.sciz.server.application.service.log.LoginLogService;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件处理器：异步落库
 */
@Slf4j
@Component
public class LoginLogHandler {

    private final LoginLogService loginLogService;

    public LoginLogHandler(LoginLogService loginLogService) {
        this.loginLogService = loginLogService;
    }

    @Async
    @EventListener
    public void onLoginLogged(LoginLoggedEvent event) {
        try {
            Long id = loginLogService.saveFromEvent(event);
            log.info(String.format("[LoginLogHandler] saved loginLog id=%s user=%s status=%s",
                    String.valueOf(id), event.getUsername(), String.valueOf(event.getStatus())));
        } catch (Exception e) {
            log.error(String.format("[LoginLogHandler] save loginLog failed user=%s err=%s",
                    event.getUsername(), e.getMessage()), e);
        }
    }
}
