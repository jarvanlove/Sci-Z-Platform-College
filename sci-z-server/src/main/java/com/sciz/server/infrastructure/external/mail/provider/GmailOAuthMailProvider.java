package com.sciz.server.infrastructure.external.mail.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sciz.server.infrastructure.config.mail.MailProviderProperties.OAuthConfig;
import com.sciz.server.infrastructure.shared.enums.MailProviderStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
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
 * Gmail OAuth 邮件服务商
 *
 * @author JiaWen.Wu
 * @className GmailOAuthMailProvider
 * @date 2025-11-11 21:35
 */
@Slf4j
@RequiredArgsConstructor
public class GmailOAuthMailProvider implements MailProvider {

    private final OAuthConfig oauthConfig;
    private final RestTemplate restTemplate;

    @Override
    public MailProviderStatus type() {
        return MailProviderStatus.GMAIL;
    }

    @Override
    public void send(String to, String subject, String content) {
        try {
            var accessToken = fetchAccessToken();
            var rawMessage = buildRawMessage(to, subject, content);

            var sendUrl = oauthConfig.resolvedSendUrl(oauthConfig.userId());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("raw", rawMessage), headers);
            restTemplate.postForEntity(sendUrl, entity, Void.class);

            log.info(String.format("Gmail 邮件发送成功: to=%s", to));
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error(String.format("Gmail 邮件发送失败: to=%s, err=%s", to, exception.getMessage()), exception);
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, exception.getMessage());
        }
    }

    /**
     * 获取 Gmail AccessToken
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
                GmailAccessTokenResponse.class);
        if (response == null || !StringUtils.hasText(response.accessToken())) {
            throw new BusinessException(ResultCode.EMAIL_CODE_SEND_FAILED, "获取 Gmail AccessToken 失败");
        }
        return response.accessToken();
    }

    /**
     * 构建原始邮件消息
     *
     * @param to      String 收件人
     * @param subject String 主题
     * @param content String 内容
     * @return String 原始邮件消息
     * @throws Exception 异常
     */
    private String buildRawMessage(String to, String subject, String content) throws Exception {
        var session = Session.getInstance(new Properties());
        var message = new MimeMessage(session);
        message.setFrom(new InternetAddress(oauthConfig.userId()));
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject, StandardCharsets.UTF_8.name());
        message.setText(content, StandardCharsets.UTF_8.name());
        var buffer = new ByteArrayOutputStream();
        message.writeTo(buffer);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.toByteArray());
    }

    /**
     * Gmail AccessToken 响应
     *
     * @param accessToken String AccessToken
     */
    private record GmailAccessTokenResponse(@JsonProperty("access_token") String accessToken) {
    }
}
