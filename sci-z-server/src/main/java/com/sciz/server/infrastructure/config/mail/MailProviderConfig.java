package com.sciz.server.infrastructure.config.mail;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件服务商配置
 *
 * @author JiaWen.Wu
 * @className MailProviderConfig
 * @date 2025-11-11 20:30
 */
@Configuration
@EnableConfigurationProperties(MailProviderProperties.class)
public class MailProviderConfig {
}
