package com.sciz.server.infrastructure.config.sms;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 短信服务商配置
 *
 * @author JiaWen.Wu
 * @className SmsProviderProperties
 * @date 2025-11-11 21:45
 */
@ConfigurationProperties(prefix = "sms")
public record SmsProviderProperties(AliyunConfig aliyun, TwilioConfig twilio) {

    public record AliyunConfig(
            boolean enabled,
            String regionId,
            String accessKeyId,
            String accessKeySecret,
            String signName,
            String templateCode,
            String templateParamName) {

        public boolean isValid() {
            return enabled
                    && StringUtils.hasText(regionId)
                    && StringUtils.hasText(accessKeyId)
                    && StringUtils.hasText(accessKeySecret)
                    && StringUtils.hasText(signName)
                    && StringUtils.hasText(templateCode);
        }

        public String templateParamNameOrDefault() {
            return StringUtils.hasText(templateParamName) ? templateParamName : "code";
        }
    }

    public record TwilioConfig(
            boolean enabled,
            String accountSid,
            String authToken,
            String fromNumber,
            String apiUrl) {

        public boolean isValid() {
            return enabled
                    && StringUtils.hasText(accountSid)
                    && StringUtils.hasText(authToken)
                    && StringUtils.hasText(fromNumber);
        }

        public String resolvedApiUrl() {
            return StringUtils.hasText(apiUrl) ? apiUrl : "https://api.twilio.com/2010-04-01";
        }
    }
}
