package com.sciz.server.infrastructure.config.resilience;

import com.sciz.server.infrastructure.shared.exception.BusinessException;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.time.Duration;

/**
 * Resilience4j 配置
 *
 * @author JiaWen.Wu
 * @className Resilience4jConfig
 * @date 2025-12-22 14:30
 */
@Slf4j
@Configuration
public class Resilience4jConfig {

    /**
     * Dify API 重试配置
     */
    @Bean
    public Retry difyApiRetry() {
        // 指数退避函数：初始等待 500ms，每次重试延迟时间翻倍（500ms -> 1000ms -> 2000ms）
        IntervalFunction intervalFunction = IntervalFunction.ofExponentialBackoff(
                Duration.ofMillis(500), // 初始等待时间
                2.0 // 指数退避倍数
        );

        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3) // 最多重试 3 次
                .intervalFunction(intervalFunction) // 使用指数退避函数
                .retryExceptions(
                        IOException.class,
                        ResourceAccessException.class,
                        HttpServerErrorException.class)
                .ignoreExceptions(BusinessException.class)
                .build();

        Retry retry = RetryRegistry.of(config).retry("difyApi");
        log.info("Dify API 重试配置初始化完成: maxAttempts=3, initialInterval=500ms, multiplier=2.0");
        return retry;
    }
}
