package com.sciz.server.infrastructure.external.mail.impl;

import com.sciz.server.infrastructure.external.mail.MailService;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
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
    private final JavaMailSender javaMailSender;
    private final String defaultFromAddress;

    public MailServiceImpl(JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") @Nullable String defaultFromAddress) {
        this.javaMailSender = javaMailSender;
        this.defaultFromAddress = Optional.ofNullable(defaultFromAddress).orElse("no-reply@sci-z.local");
    }

    @Override
    public void sendResetPasswordCodeMail(String toEmail, String code) {
        var subject = SystemConstant.EMAIL_SUBJECT_RESET_PASSWORD;
        var content = """
                您正在申请重置 Sci-Z Platform 账号密码。
                验证码：%s
                有效期：10 分钟，请勿泄露给任何人。
                如果非本人操作，请忽略本邮件。
                """.formatted(code);
        sendPlainTextMail(subject, content, toEmail);
    }

    private void sendPlainTextMail(@NonNull String subject, @NonNull String content, @NonNull String toEmail) {
        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(defaultFromAddress);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, false);
            javaMailSender.send(mimeMessage);
            log.info(String.format("邮件发送成功: to=%s, subject=%s", toEmail, subject));
        } catch (Exception exception) {
            log.error(String.format("邮件发送失败: to=%s, subject=%s, err=%s", toEmail, subject, exception.getMessage()),
                    exception);
            throw new IllegalStateException("邮件发送失败", exception);
        }
    }
}
