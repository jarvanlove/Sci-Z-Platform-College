package com.sciz.server.infrastructure.shared.handler.log;

import com.sciz.server.application.service.user.LoginLogAndStatsService;
import com.sciz.server.infrastructure.shared.event.log.LoginLoggedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 登录日志事件处理器：异步落库
 * 在同一事务中：
 * 1. 保存登录日志到 sys_login_log
 * 2. 更新用户登录统计到 sys_user（login_count, last_login_time, last_login_ip）
 *
 * @author JiaWen.Wu
 * @className LoginLogHandler
 * @date 2025-11-07 10:32
 */
@Slf4j
@Component
public class LoginLogHandler {

    private final LoginLogAndStatsService loginLogAndStatsService;

    public LoginLogHandler(LoginLogAndStatsService loginLogAndStatsService) {
        this.loginLogAndStatsService = loginLogAndStatsService;
    }

    /**
     * 处理登录日志事件
     * 注意：虽然方法标记为 @Async（异步执行），但内部的事务处理仍然有效
     * 事务会在异步线程中执行
     * 
     * 在同一事务中：
     * 1. 保存登录日志到 sys_login_log
     * 2. 更新用户登录统计到 sys_user（login_count, last_login_time, last_login_ip）
     *
     * @param event 登录日志事件
     */
    @Async
    @EventListener
    public void onLoginLogged(LoginLoggedEvent event) {
        try {
            // 在同一事务中保存登录日志并更新用户统计
            loginLogAndStatsService.saveLoginLogAndUpdateStats(event);
            log.info(String.format("[LoginLogHandler] handled loginLog successfully: userId=%s, username=%s, status=%s",
                    event.getUserId(), event.getUsername(), event.getStatus()));

        } catch (Exception e) {
            log.error(String.format("[LoginLogHandler] handle loginLog failed: userId=%s, username=%s, error=%s",
                    event.getUserId(), event.getUsername(), e.getMessage()), e);
        }
    }
}
