package com.sciz.server.infrastructure.external.sms;

/**
 * 短信服务商
 *
 * @author JiaWen.Wu
 * @className SmsProvider
 * @date 2025-11-11 21:45
 */
public interface SmsProvider {

    /**
     * 发送短信验证码
     *
     * @param phone String 手机号
     * @param code  String 验证码
     */
    void send(String phone, String code);
}
