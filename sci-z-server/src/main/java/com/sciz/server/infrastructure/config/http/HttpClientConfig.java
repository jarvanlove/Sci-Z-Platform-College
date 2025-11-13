package com.sciz.server.infrastructure.config.http;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP 客户端配置
 *
 * @author JiaWen.Wu
 * @className HttpClientConfig
 * @date 2025-11-11 21:50
 */
@Configuration
public class HttpClientConfig {

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }
}
