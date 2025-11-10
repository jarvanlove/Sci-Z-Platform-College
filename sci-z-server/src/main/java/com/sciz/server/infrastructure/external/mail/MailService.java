package com.sciz.server.infrastructure.external.mail;

/**
 * 邮件发送服务
 *
 * @author JiaWen.Wu
 * @className MailService
 * @date 2025-11-10 15:30
 */
public interface MailService {

    /**
     * 发送重置密码验证码邮件
     *
     * @param toEmail String 收件人邮箱
     * @param code    String 验证码
     */
    void sendResetPasswordCodeMail(String toEmail, String code);
}
