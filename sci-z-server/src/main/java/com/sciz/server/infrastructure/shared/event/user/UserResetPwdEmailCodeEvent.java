package com.sciz.server.infrastructure.shared.event.user;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import java.util.Optional;

/**
 * 用户重置密码邮箱验证码事件
 *
 * @author JiaWen.Wu
 * @className UserResetPwdEmailCodeEvent
 * @date 2025-11-10 21:15
 */
public class UserResetPwdEmailCodeEvent extends DomainEvent {

    /**
     * 用户ID
     */
    private final Long userId;

    /**
     * 登录邮箱
     */
    private final String email;

    /**
     * 邮箱验证码
     */
    private final String emailCode;

    public UserResetPwdEmailCodeEvent(Long userId, String email, String emailCode) {
        super();
        this.userId = userId;
        this.email = email;
        this.emailCode = emailCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getEmailCode() {
        return emailCode;
    }

    @Override
    public String getAggregateId() {
        return Optional.ofNullable(userId)
                .map(String::valueOf)
                .orElse(email);
    }

    @Override
    public String getAggregateType() {
        return "User";
    }
}
