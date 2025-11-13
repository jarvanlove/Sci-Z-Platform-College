package com.sciz.server.infrastructure.external.sms;

import com.sciz.server.infrastructure.config.sms.SmsProviderProperties.TwilioConfig;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Twilio 短信服务商实现
 *
 * @author JiaWen.Wu
 * @className TwilioSmsProvider
 * @date 2025-11-11 21:45
 */
@Slf4j
@RequiredArgsConstructor
public class TwilioSmsProvider implements SmsProvider {

    private final TwilioConfig config;
    private final RestTemplate restTemplate;

    @Override
    public void send(String phone, String code) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("To", phone);
            formData.add("From", config.fromNumber());
            var message = String.format("【Sci-Z Platform】验证码：%s，该验证码10分钟内有效，请勿泄露。", code);
            formData.add("Body", message);

            var uri = String.format("%s/Accounts/%s/Messages.json", config.resolvedApiUrl(), config.accountSid());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBasicAuth(config.accountSid(), config.authToken(), StandardCharsets.UTF_8);
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);
            restTemplate.postForEntity(uri, entity, Void.class);

            log.info(String.format("Twilio 短信发送成功: to=%s", phone));
        } catch (Exception exception) {
            log.error(String.format("Twilio 短信发送失败: to=%s, err=%s", phone, exception.getMessage()), exception);
            throw new BusinessException(ResultCode.SMS_CODE_SEND_FAILED, exception.getMessage());
        }
    }
}
