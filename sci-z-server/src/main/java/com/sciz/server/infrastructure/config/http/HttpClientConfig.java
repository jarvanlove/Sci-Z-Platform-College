package com.sciz.server.infrastructure.config.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * HTTP 客户端配置
 * <p>
 * 配置 RestTemplate 的超时时间，支持长时间运行的工作流（3-15分钟）
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
     * 配置超时时间：
     * - 连接超时：10秒（建立连接的时间）
     * - 读取超时：15分钟（900秒），支持长时间运行的工作流（申报工作流3-6分钟，后续可能还有更长时间的工作流）
     *
     * @param builder RestTemplateBuilder
     * @return RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(10)) // 连接超时：10秒
                .setReadTimeout(Duration.ofSeconds(900)) // 读取超时：15分钟（900秒），支持长时间运行的工作流
                .requestFactory(SimpleClientHttpRequestFactory.class)
                .build();
    }
}
