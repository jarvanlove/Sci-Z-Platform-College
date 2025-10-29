package com.sciz.server.infrastructure.config.web;

import com.sciz.server.infrastructure.common.interceptor.AuthInterceptor;
import com.sciz.server.infrastructure.common.interceptor.IdempotentInterceptor;
import com.sciz.server.infrastructure.common.interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 拦截器注册
 *
 * @author JiaWen.Wu
 * @className WebMvcConfig
 * @date 2025-10-29 15:00
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LogInterceptor logInterceptor;
    private final AuthInterceptor authInterceptor;
    private final IdempotentInterceptor idempotentInterceptor;

    public WebMvcConfig(LogInterceptor logInterceptor,
            AuthInterceptor authInterceptor,
            IdempotentInterceptor idempotentInterceptor) {
        this.logInterceptor = logInterceptor;
        this.authInterceptor = authInterceptor;
        this.idempotentInterceptor = idempotentInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        /**
         * 注册 MVC 拦截器
         *
         * - 顺序：鉴权 -> 幂等 -> 日志
         * - 公共路径统一排除
         *
         * @param registry InterceptorRegistry 拦截器注册器
         * @return void
         */
        String[] exclude = new String[] {
                "/actuator/**", "/health/**", "/error",
                "/swagger-ui/**", "/v3/api-docs/**", "/static/**"
        };

        // 顺序：鉴权 -> 幂等 -> 日志（或根据需要调整）
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**").excludePathPatterns(exclude).order(1);
        registry.addInterceptor(idempotentInterceptor)
                .addPathPatterns("/**").excludePathPatterns(exclude).order(2);
        registry.addInterceptor(logInterceptor)
                .addPathPatterns("/**").excludePathPatterns(exclude).order(3);
    }
}
