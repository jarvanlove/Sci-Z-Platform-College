package com.sciz.server.infrastructure.config.ai;

import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * LangChain4j 配置
 * 
 * 默认对接 OpenAI 兼容协议（可通过 baseUrl 指向国产兼容网关，如 Qwen 网关、One-API 等）。
 * 
 * @author JiaWen.Wu
 * @className LangChain4jConfig
 * @date 2025-10-29 17:00
 */
@Configuration
public class LangChain4jConfig {

    @Value("${llm.openai.api-key:}")
    private String apiKey;

    @Value("${llm.openai.base-url:https://api.openai.com/v1}")
    private String baseUrl;

    @Value("${llm.openai.model:gpt-4o-mini}")
    private String model;

    @Value("${llm.openai.timeout-seconds:60}")
    private Integer timeoutSeconds;

    /**
     * 通用 Chat 模型（对接 OpenAI 兼容网关）
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        return OpenAiChatModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .modelName(model)
                .timeout(java.time.Duration.ofSeconds(timeoutSeconds))
                .build();
    }
}
