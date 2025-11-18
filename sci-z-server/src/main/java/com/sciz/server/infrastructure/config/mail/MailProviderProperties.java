package com.sciz.server.infrastructure.config.mail;

import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * 邮件服务商配置属性
 *
 * @author JiaWen.Wu
 * @className MailProviderProperties
 * @date 2025-11-11 20:30
 */
@ConfigurationProperties(prefix = "mail")
public record MailProviderProperties(Map<MailProviderStatus, ProviderConfig> providers) {

    public MailProviderProperties(Map<MailProviderStatus, ProviderConfig> providers) {
        this.providers = CollectionUtils.isEmpty(providers)
                ? Map.of()
                : new EnumMap<>(providers);
    }

    /**
     * 获取服务商配置
     *
     * @param type MailProviderType 服务商类型
     * @return Optional<ProviderConfig> 配置
     */
    public Optional<ProviderConfig> findProvider(MailProviderStatus type) {
        return Optional.ofNullable(providers.get(type))
                .filter(ProviderConfig::enabled);
    }

    /**
     * 邮件服务商配置项
     *
     * @param enabled boolean 是否启用
     * @param smtp    SmtpConfig SMTP 配置
     * @param oauth   OAuthConfig OAuth 配置
     */
    public record ProviderConfig(
            boolean enabled,
            SmtpConfig smtp,
            OAuthConfig oauth) {

        public boolean smtpEnabled() {
            return enabled && smtp != null && smtp.isValid();
        }

        public boolean oauthEnabled() {
            return enabled && oauth != null && oauth.isValid();
        }
    }

    /**
     * SMTP 配置
     */
    public record SmtpConfig(
            String host,
            Integer port,
            String username,
            String password,
            String from,
            boolean ssl,
            boolean starttls) {

        public boolean isValid() {
            return StringUtils.hasText(host)
                    && port != null
                    && port > 0
                    && StringUtils.hasText(username)
                    && StringUtils.hasText(password);
        }

        public String fromAddress() {
            return StringUtils.hasText(from) ? from : username;
        }
    }

    /**
     * OAuth 配置
     */
    public record OAuthConfig(
            String tokenUrl,
            String clientId,
            String clientSecret,
            String refreshToken,
            String scope,
            String userId,
            String sendUrl,
            String tenantId) {

        public boolean isValid() {
            return StringUtils.hasText(tokenUrl)
                    && StringUtils.hasText(clientId)
                    && StringUtils.hasText(clientSecret)
                    && StringUtils.hasText(refreshToken)
                    && StringUtils.hasText(userId)
                    && StringUtils.hasText(sendUrl);
        }

        public String resolvedTokenUrl() {
            if (StringUtils.hasText(tokenUrl) && tokenUrl.contains("%s") && StringUtils.hasText(tenantId)) {
                return tokenUrl.formatted(tenantId);
            }
            return tokenUrl;
        }

        public String resolvedSendUrl(String user) {
            if (StringUtils.hasText(sendUrl) && sendUrl.contains("%s")) {
                return sendUrl.formatted(user);
            }
            return sendUrl;
        }
    }
}
