package com.sciz.server.infrastructure.external.mail;

import com.sciz.server.infrastructure.shared.enums.MailProviderType;

/**
 * 邮件发送服务
 *
 * @author JiaWen.Wu
 * @className MailService
 * @date 2025-11-10 15:30
 */
public interface MailService {

    /**
     * 发送邮箱验证码邮件
     *
     * @param provider MailProviderType 邮箱服务商
     * @param toEmail  String 收件人邮箱
     * @param code     String 验证码
     */
    void sendEmailVerificationCode(MailProviderType provider, String toEmail, String code);
}
