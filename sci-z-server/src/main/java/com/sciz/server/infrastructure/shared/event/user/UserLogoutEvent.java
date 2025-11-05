package com.sciz.server.infrastructure.shared.event.user;

import com.sciz.server.infrastructure.shared.event.DomainEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户退出事件
 * 
 * @author JiaWen.Wu
 * @className UserLogoutEvent
 * @date 2025-10-29 10:00
 */
@Getter
@Setter
public class UserLogoutEvent extends DomainEvent {

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
     * 退出时间
     * 
     */
    private String logoutTime;

    /**
     * 默认构造函数
     */
    public UserLogoutEvent() {
        super();
    }

    /**
     * 带参数构造函数
     *
     * @param userId     用户ID
     * @param username   用户名
     * @param logoutTime 退出时间
     */
    public UserLogoutEvent(Long userId, String username, String logoutTime) {
        super();
        this.userId = userId;
        this.username = username;
        this.logoutTime = logoutTime;
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
