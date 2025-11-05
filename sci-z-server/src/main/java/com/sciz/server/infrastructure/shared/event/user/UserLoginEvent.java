package com.sciz.server.infrastructure.shared.event.user;

import com.sciz.server.infrastructure.shared.event.DomainEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户登录事件
 * 
 * @author JiaWen.Wu
 * @className UserLoginEvent
 * @date 2025-10-29 10:00
 */
@Getter
@Setter
public class UserLoginEvent extends DomainEvent {

    /**
     * 用户ID
     * 
     */
    private Long userId;

    /**
     * 用户名
     *
     */
    private String username;

    /**
     * 登录IP地址
     * 
     */
    private String loginIp;

    /**
     * 登录时间
     * 
     */
    private String loginTime;

    /**
     * 默认构造函数
     */
    public UserLoginEvent() {
        super();
    }

    /**
     * 带参数构造函数
     *
     * @param userId    用户ID
     * @param username  用户名
     * @param loginIp   登录IP地址
     * @param loginTime 登录时间
     */
    public UserLoginEvent(Long userId, String username, String loginIp, String loginTime) {
        super();
        this.userId = userId;
        this.username = username;
        this.loginIp = loginIp;
        this.loginTime = loginTime;
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(userId);
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
