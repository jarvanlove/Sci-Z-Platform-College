package com.sciz.server.infrastructure.external.sms.impl;

import com.sciz.server.infrastructure.config.sms.SmsProviderProperties;
import com.sciz.server.infrastructure.external.sms.SmsProvider;
import com.sciz.server.infrastructure.external.sms.SmsService;
import com.sciz.server.infrastructure.external.sms.AliyunSmsProvider;
import com.sciz.server.infrastructure.external.sms.TwilioSmsProvider;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 短信服务实现
 *
 * @author JiaWen.Wu
 * @className SmsServiceImpl
 * @date 2025-11-11 21:45
 */
@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final SmsProvider smsProvider;

    public SmsServiceImpl(SmsProviderProperties smsProviderProperties, RestTemplate restTemplate) {
        if (smsProviderProperties.aliyun() != null && smsProviderProperties.aliyun().isValid()) {
            this.smsProvider = new AliyunSmsProvider(smsProviderProperties.aliyun());
        } else if (smsProviderProperties.twilio() != null && smsProviderProperties.twilio().isValid()) {
            this.smsProvider = new TwilioSmsProvider(smsProviderProperties.twilio(), restTemplate);
        } else {
            this.smsProvider = null;
        }
    }

    @Override
    public void sendVerificationCode(String phone, String code) {
        if (smsProvider == null) {
            throw new BusinessException(ResultCode.SMS_PROVIDER_NOT_CONFIGURED, "短信服务商未配置");
        }
        smsProvider.send(phone, code);
    }
}
