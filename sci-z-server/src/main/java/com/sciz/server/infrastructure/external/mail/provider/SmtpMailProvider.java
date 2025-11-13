package com.sciz.server.infrastructure.external.mail.provider;

import com.sciz.server.infrastructure.config.mail.MailProviderProperties.SmtpConfig;
import com.sciz.server.infrastructure.shared.enums.MailProviderType;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * SMTP 邮件服务商实现
 *
 * @author JiaWen.Wu
 * @className SmtpMailProvider
 * @date 2025-11-11 21:35
 */
@Slf4j
public class SmtpMailProvider implements MailProvider {

    private final MailProviderType type;
    private final SmtpConfig smtpConfig;
    private final JavaMailSenderImpl javaMailSender;

    public SmtpMailProvider(MailProviderType type, SmtpConfig smtpConfig) {
        this.type = type;
        this.smtpConfig = smtpConfig;
        this.javaMailSender = buildSender(smtpConfig);
    }

    @Override
    public MailProviderType type() {
        return type;
    }

    @Override
    public void send(String to, String subject, String content) {
        try {
            var mimeMessage = javaMailSender.createMimeMessage();
            var helper = new MimeMessageHelper(mimeMessage, false, StandardCharsets.UTF_8.name());
            helper.setFrom(smtpConfig.fromAddress());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, false);
            javaMailSender.send(mimeMessage);
            log.info(String.format("SMTP 邮件发送成功: provider=%s, to=%s", type, to));
        } catch (MailException exception) {
            log.error(String.format("SMTP 邮件发送失败: provider=%s, to=%s, err=%s", type, to, exception.getMessage()),
                    exception);
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, exception.getMessage());
        } catch (Exception exception) {
            log.error(String.format("SMTP 邮件发送异常: provider=%s, to=%s, err=%s", type, to, exception.getMessage()),
                    exception);
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, exception.getMessage());
        }
    }

    /**
     * 构建 JavaMailSenderImpl
     *
     * @param config SmtpConfig 配置
     * @return JavaMailSenderImpl 邮件发送器
     */
    private JavaMailSenderImpl buildSender(SmtpConfig config) {
        var sender = new JavaMailSenderImpl();
        sender.setHost(config.host());
        sender.setPort(config.port());
        sender.setUsername(config.username());
        sender.setPassword(config.password());
        sender.setDefaultEncoding(StandardCharsets.UTF_8.name());

        Properties javaMailProperties = sender.getJavaMailProperties();
        javaMailProperties.put("mail.smtp.auth", "true");
        javaMailProperties.put("mail.smtp.starttls.enable", String.valueOf(config.starttls()));
        javaMailProperties.put("mail.smtp.starttls.required", String.valueOf(config.starttls()));
        if (config.ssl()) {
            javaMailProperties.put("mail.smtp.ssl.enable", "true");
            javaMailProperties.put("mail.smtp.ssl.trust", config.host());
        }
        return sender;
    }
}
