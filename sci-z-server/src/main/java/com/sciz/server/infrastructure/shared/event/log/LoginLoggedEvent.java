package com.sciz.server.infrastructure.shared.event.log;

import com.sciz.server.infrastructure.shared.event.DomainEvent;

/**
 * 用户登录日志事件
 * 对应表：sys_login_log
 */
public class LoginLoggedEvent extends DomainEvent {

    private Long userId;
    private String username;
    private String loginIp;
    private String loginLocation;
    private String browser;
    private String os;
    private Integer status;
    private String message;
    private String loginTime;

    public LoginLoggedEvent() {
        super();
    }

    public LoginLoggedEvent(Long userId, String username, String loginIp, String loginLocation,
            String browser, String os, Integer status, String message, String loginTime) {
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
        return userId == null ? "0" : String.valueOf(userId);
    }

    @Override
    public String getAggregateType() {
        return "User";
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getLoginIp() {
        return loginIp;
    }

    public String getLoginLocation() {
        return loginLocation;
    }

    public String getBrowser() {
        return browser;
    }

    public String getOs() {
        return os;
    }

    public Integer getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getLoginTime() {
        return loginTime;
    }
}
