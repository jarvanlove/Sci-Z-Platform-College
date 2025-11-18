package com.sciz.server.infrastructure.external.mail.impl;

import com.sciz.server.infrastructure.external.mail.MailService;
import com.sciz.server.infrastructure.external.mail.provider.MailProviderRegistry;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 邮件发送服务实现
 *
 * @author JiaWen.Wu
 * @className MailServiceImpl
 * @date 2025-11-10 22:30
 */
@Slf4j
@Component
public class MailServiceImpl implements MailService {

    private final MailProviderRegistry mailProviderRegistry;

    public MailServiceImpl(MailProviderRegistry mailProviderRegistry) {
        this.mailProviderRegistry = mailProviderRegistry;
    }

    @Override
    public void sendEmailVerificationCode(MailProviderStatus provider, String toEmail, String code) {
        var subject = SystemConstant.EMAIL_SUBJECT_VERIFICATION;
        var content = """
                您正在申请接收 Sci-Z Platform 验证码。
                验证码：%s
                有效期：10 分钟，请勿泄露给任何人。
                如非本人操作，请忽略本邮件。
                """.formatted(code);
        mailProviderRegistry.getProvider(provider).send(toEmail, subject, content);
    }
}
