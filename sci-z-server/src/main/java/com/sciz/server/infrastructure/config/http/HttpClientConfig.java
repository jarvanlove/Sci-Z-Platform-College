package com.sciz.server.infrastructure.config.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * HTTP 客户端配置
 * <p>
 * 配置 RestTemplate 和 WebClient 的超时时间，支持长时间运行的工作流（3-15分钟）。
 * RestTemplate 使用 JdkClientHttpRequestFactory（基于 java.net.http.HttpClient），
 * 以支持 PATCH 等 HttpURLConnection 不支持的方法（如 Dify 知识库同步 PATCH 请求）。
 *
 * @author JiaWen.Wu
 * @className HttpClientConfig
 * @date 2025-11-11 21:50
 */
@Configuration
public class HttpClientConfig {

    /**
     * 创建 RestTemplate Bean
     * <p>
     * 使用 JdkClientHttpRequestFactory 以支持 PATCH 请求体（SimpleClientHttpRequestFactory
     * 底层 HttpURLConnection 不支持 PATCH，会导致 I/O 异常）。
     * 超时：连接 10 秒，读取 15 分钟（支持长时间工作流）。
     *
     * @param builder RestTemplateBuilder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        HttpClient jdkHttpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(jdkHttpClient);
        factory.setReadTimeout(Duration.ofSeconds(900));
        return builder
                .requestFactory(() -> factory)
                .build();
    }

    /**
     * 创建 WebClient Bean
     * <p>
     * 用于处理流式 API（SSE），配置超时时间：
     * - 连接超时：10秒（建立连接的时间）
     * - 响应超时：10分钟（600秒），支持长时间运行的流式响应
     *
     * @return WebClient
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)) // 10MB 缓冲区
                .build();
    }
}
