package com.sciz.server.infrastructure.shared.event.user;

import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;
import com.sciz.server.infrastructure.shared.event.DomainEvent;
import java.util.Optional;

/**
 * 用户邮箱验证码事件
 *
 * @author JiaWen.Wu
 * @className UserEmailVerificationEvent
 * @date 2025-11-11 21:30
 */
public class UserEmailVerificationEvent extends DomainEvent {

    private final Long userId;
    private final String email;
    private final String verificationCode;
    private final MailProviderStatus mailProviderType;

    public UserEmailVerificationEvent(Long userId, String email, String verificationCode,
            MailProviderStatus mailProviderType) {
        super();
        this.userId = userId;
        this.email = email;
        this.verificationCode = verificationCode;
        this.mailProviderType = mailProviderType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getVerificationCode() {
        return verificationCode;
    }

    public MailProviderStatus getMailProviderType() {
        return mailProviderType;
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
