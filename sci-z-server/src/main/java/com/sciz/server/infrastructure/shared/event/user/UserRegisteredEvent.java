package com.sciz.server.infrastructure.shared.event.user;

import com.sciz.server.infrastructure.shared.event.DomainEvent;

import lombok.Getter;
import lombok.Setter;

/**
 * 用户注册事件
 * 
 * @author JiaWen.Wu
 * @className UserRegisteredEvent
 * @date 2025-10-29 10:00
 */
@Getter
@Setter
public class UserRegisteredEvent extends DomainEvent {

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
     * 邮箱地址
     * 
     */
    private String email;

    /**
     * 默认构造函数
     */
    public UserRegisteredEvent() {
        super();
    }

    /**
     * 带参数构造函数
     *
     * @param userId   用户ID
     * @param username 用户名
     * @param email    邮箱地址
     */
    public UserRegisteredEvent(Long userId, String username, String email) {
        super();
        this.userId = userId;
        this.username = username;
        this.email = email;
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
