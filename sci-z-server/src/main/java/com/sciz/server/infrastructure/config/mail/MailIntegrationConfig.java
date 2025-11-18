package com.sciz.server.infrastructure.config.mail;

import com.sciz.server.infrastructure.config.mail.MailProviderProperties.OAuthConfig;
import com.sciz.server.infrastructure.config.mail.MailProviderProperties.ProviderConfig;
import com.sciz.server.infrastructure.external.mail.provider.GmailOAuthMailProvider;
import com.sciz.server.infrastructure.external.mail.provider.MailProvider;
import com.sciz.server.infrastructure.external.mail.provider.MailProviderRegistry;
import com.sciz.server.infrastructure.external.mail.provider.OutlookGraphMailProvider;
import com.sciz.server.infrastructure.external.mail.provider.SmtpMailProvider;
import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 邮件服务商集成配置
 *
 * @author JiaWen.Wu
 * @className MailIntegrationConfig
 * @date 2025-11-11 21:35
 */
@Configuration
@EnableConfigurationProperties(MailProviderProperties.class)
public class MailIntegrationConfig {

    @Bean
    public MailProviderRegistry mailProviderRegistry(MailProviderProperties properties, RestTemplate restTemplate) {
        List<MailProvider> providers = new ArrayList<>();
        properties.providers().forEach((type, config) -> buildProvider(type, config, restTemplate, providers));
        return new MailProviderRegistry(providers);
    }

    /**
     * 构建邮件服务商
     *
     * @param type         MailProviderType 服务商类型
     * @param config       ProviderConfig 服务商配置
     * @param restTemplate RestTemplate HTTP 客户端
     * @param providers    List<MailProvider> 服务商列表
     */
    private void buildProvider(MailProviderStatus type, ProviderConfig config, RestTemplate restTemplate,
            List<MailProvider> providers) {
        if (config == null || !config.enabled()) {
            return;
        }

        if (config.smtpEnabled()) {
            providers.add(new SmtpMailProvider(type, config.smtp()));
            return;
        }

        OAuthConfig oauthConfig = config.oauth();
        if (config.oauthEnabled()) {
            switch (type) {
                case GMAIL -> providers.add(new GmailOAuthMailProvider(oauthConfig, restTemplate));
                case OUTLOOK -> providers.add(new OutlookGraphMailProvider(oauthConfig, restTemplate));
                default -> {
                    // 其他服务商暂未支持 OAuth
                }
            }
        }
    }
}
