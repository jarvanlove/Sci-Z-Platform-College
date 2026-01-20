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
public record SmsProviderProperties(AliyunConfig aliyun) {

    public record AliyunConfig(
            boolean enabled,
            String regionId,
            String accessKeyId,
            String accessKeySecret,
            String signName,
            String templateCode,
            String templateParamName) {

        /**
         * 获取处理后的签名名称（去除前后空格）
         */
        public String signNameTrimmed() {
            return signName != null ? signName.trim() : "";
        }

        /**
         * 获取处理后的模板代码（去除前后空格）
         */
        public String templateCodeTrimmed() {
            return templateCode != null ? templateCode.trim() : "";
        }

        /**
         * 获取处理后的 AccessKey Secret（去除前后空格）
         */
        public String accessKeySecretTrimmed() {
            return accessKeySecret != null ? accessKeySecret.trim() : "";
        }

        public boolean isValid() {
            return enabled
                    && StringUtils.hasText(regionId)
                    && StringUtils.hasText(accessKeyId)
                    && StringUtils.hasText(accessKeySecretTrimmed())
                    && StringUtils.hasText(signNameTrimmed())
                    && StringUtils.hasText(templateCodeTrimmed());
        }

        public String templateParamNameOrDefault() {
            return StringUtils.hasText(templateParamName) ? templateParamName.trim() : "code";
        }
    }
}
