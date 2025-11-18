package com.sciz.server.infrastructure.external.mail.provider;

import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;

/**
 * 邮件服务商适配器
 *
 * @author JiaWen.Wu
 * @className MailProvider
 * @date 2025-11-11 21:30
 */
public interface MailProvider {

    /**
     * 支持的邮箱服务商类型
     *
     * @return MailProviderStatus 服务商类型
     */
    MailProviderStatus type();

    /**
     * 发送邮件
     *
     * @param to      String 收件人
     * @param subject String 邮件主题
     * @param content String 邮件内容
     */
    void send(String to, String subject, String content);
}
