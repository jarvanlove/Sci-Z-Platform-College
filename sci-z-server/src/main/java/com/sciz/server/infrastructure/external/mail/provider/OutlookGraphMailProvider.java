package com.sciz.server.infrastructure.external.mail.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sciz.server.infrastructure.config.mail.MailProviderProperties.OAuthConfig;
import com.sciz.server.infrastructure.shared.enums.MailProviderType;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

/**
 * Outlook Graph 邮件服务商
 *
 * @author JiaWen.Wu
 * @className OutlookGraphMailProvider
 * @date 2025-11-11 21:35
 */
@Slf4j
@RequiredArgsConstructor
public class OutlookGraphMailProvider implements MailProvider {

    private final OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;

    @Override
    public MailProviderType type() {
        return MailProviderType.OUTLOOK;
    }

    @Override
    public void send(String to, String subject, String content) {
        try {
            var accessToken = fetchAccessToken();
            var sendUrl = oauthConfig.resolvedSendUrl(oauthConfig.userId());
            var payload = Map.of(
                    "message", Map.of(
                            "subject", subject,
                            "body", Map.of(
                                    "contentType", "Text",
                                    "content", content),
                            "toRecipients", new Object[] {
                                    Map.of("emailAddress", Map.of("address", to))
                            }),
                    "saveToSentItems", Boolean.TRUE);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(sendUrl, entity, Void.class);

            log.info(String.format("Outlook 邮件发送成功: to=%s", to));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(String.format("Outlook 邮件发送失败: to=%s, err=%s", to, exception.getMessage()), exception);
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, exception.getMessage());
        }
    }

    /**
     * 获取 Outlook AccessToken
     *
     * @return String AccessToken
     */
    private String fetchAccessToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", oauthConfig.clientId());
        formData.add("client_secret", oauthConfig.clientSecret());
        formData.add("refresh_token", oauthConfig.refreshToken());
        formData.add("grant_type", "refresh_token");
        if (Objects.nonNull(oauthConfig.scope())) {
            formData.add("scope", oauthConfig.scope());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
        var response = restTemplate.postForObject(oauthConfig.resolvedTokenUrl(), entity,
                OutlookAccessTokenResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, "获取 Outlook AccessToken 失败");
        }
        return response.accessToken();
    }

    /**
     * Outlook AccessToken 响应
     *
     * @param accessToken String AccessToken
     */
    private record OutlookAccessTokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
