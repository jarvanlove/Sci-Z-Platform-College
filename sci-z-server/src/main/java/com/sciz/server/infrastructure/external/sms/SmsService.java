package com.sciz.server.infrastructure.external.sms;

/**
 * 短信服务
 *
 * @author JiaWen.Wu
 * @className SmsService
 * @date 2025-11-11 21:45
 */
public interface SmsService {

    /**
     * 发送短信验证码
     *
     * @param phone String 手机号
     * @param code  String 验证码
     */
    void sendVerificationCode(String phone, String code);
}
