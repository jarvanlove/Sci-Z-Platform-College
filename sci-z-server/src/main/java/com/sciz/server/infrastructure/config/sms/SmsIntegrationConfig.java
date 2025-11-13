package com.sciz.server.infrastructure.config.sms;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信服务商集成配置
 *
 * @author JiaWen.Wu
 * @className SmsIntegrationConfig
 * @date 2025-11-11 21:45
 */
@Configuration
@EnableConfigurationProperties(SmsProviderProperties.class)
public class SmsIntegrationConfig {
}
