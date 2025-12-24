package com.sciz.server.infrastructure.external.translation.impl;

import com.aliyun.alimt20181012.Client;
import com.aliyun.alimt20181012.models.TranslateGeneralRequest;
import com.aliyun.alimt20181012.models.TranslateGeneralResponse;
import com.aliyun.teaopenapi.models.Config;
import com.sciz.server.infrastructure.external.translation.TranslationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 阿里云翻译服务实现
 *
 * @author JiaWen.Wu
 * @className AliyunTranslationServiceImpl
 * @date 2025-01-24 14:30
 */
@Slf4j
@Service
public class AliyunTranslationServiceImpl implements TranslationService {

    @Value("${aliyun.translation.access-key-id:}")
    private String accessKeyId;

    @Value("${aliyun.translation.access-key-secret:}")
    private String accessKeySecret;

    @Value("${aliyun.translation.endpoint:mt.cn-hangzhou.aliyuncs.com}")
    private String endpoint;

    @Override
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (!StringUtils.hasText(text)) {
            return text;
        }

        // 如果源语言和目标语言相同，直接返回原文
        if (sourceLanguage != null && sourceLanguage.equalsIgnoreCase(targetLanguage)) {
            return text;
        }

        try {
            Client client = createClient();
            TranslateGeneralRequest request = new TranslateGeneralRequest()
                    .setFormatType("text")
                    .setSourceLanguage(normalizeLanguageCode(sourceLanguage))
                    .setTargetLanguage(normalizeLanguageCode(targetLanguage))
                    .setSourceText(text)
                    .setScene("general");

            TranslateGeneralResponse response = client.translateGeneral(request);
            if (response != null && response.body != null && response.body.data != null) {
                String translated = response.body.data.translated;
                log.info(String.format("翻译成功: source=%s, target=%s, text=%s, translated=%s",
                        sourceLanguage, targetLanguage, text.substring(0, Math.min(50, text.length())), 
                        translated != null ? translated.substring(0, Math.min(50, translated.length())) : "null"));
                return translated != null ? translated : text;
            }
            log.warn(String.format("翻译响应为空: source=%s, target=%s", sourceLanguage, targetLanguage));
            return text;
        } catch (Exception e) {
            log.error(String.format("翻译失败: source=%s, target=%s, text=%s, error=%s",
                    sourceLanguage, targetLanguage, text, e.getMessage()), e);
            // 翻译失败时返回原文
            return text;
        }
    }

    /**
     * 创建阿里云翻译客户端
     *
     * @return Client 阿里云翻译客户端
     * @throws Exception 创建客户端异常
     */
    private Client createClient() throws Exception {
        Config config = new Config()
                .setAccessKeyId(accessKeyId)
                .setAccessKeySecret(accessKeySecret);
        config.endpoint = endpoint;
        return new Client(config);
    }

    /**
     * 标准化语言代码
     *
     * @param languageCode 语言代码
     * @return 标准化后的语言代码
     */
    private String normalizeLanguageCode(String languageCode) {
        if (!StringUtils.hasText(languageCode)) {
            return "en";
        }
        String normalized = languageCode.toLowerCase();
        // 处理常见的语言代码变体
        if (normalized.startsWith("zh")) {
            return "zh";
        }
        return normalized;
    }
}

