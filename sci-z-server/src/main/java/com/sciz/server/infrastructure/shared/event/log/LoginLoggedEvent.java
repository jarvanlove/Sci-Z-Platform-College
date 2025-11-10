package com.sciz.server.infrastructure.shared.event.log;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import java.util.Optional;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录日志事件（用于异步落库 sys_login_log）
 * 
 * @author JiaWen.Wu
 * @className LoginLoggedEvent
 * @date 2025-10-29 14:00
 */
@Getter
@Setter
public class LoginLoggedEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 用户名
     */
    private String username;
    /**
     * 登录IP
     */
    private String loginIp;
    /**
     * 登录地点
     */
    private String loginLocation;
    /**
     * 浏览器
     */
    private String browser;
    /**
     * 操作系统
     */
    private String os;
    /**
     * 登录状态（0失败 1成功）
     */
    private Integer status;
    /**
     * 登录消息
     */
    private String message;
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    public LoginLoggedEvent() {
        super();
    }

    public LoginLoggedEvent(Long userId, String username, String loginIp, String loginLocation,
            String browser, String os, Integer status, String message, LocalDateTime loginTime) {
        super();
        this.userId = userId;
        this.username = username;
        this.loginIp = loginIp;
        this.loginLocation = loginLocation;
        this.browser = browser;
        this.os = os;
        this.status = status;
        this.message = message;
        this.loginTime = loginTime;
    }

    @Override
    public String getAggregateId() {
        return Optional.ofNullable(userId)
                .map(String::valueOf)
                .orElse("0");
    }

    @Override
    public String getAggregateType() {
        return "LoginLog";
    }
}
