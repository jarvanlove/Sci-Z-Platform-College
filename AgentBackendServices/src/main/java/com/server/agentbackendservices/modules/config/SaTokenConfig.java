package com.server.agentbackendservices.modules.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Sa-Token 配置类
 * 配置拦截器和权限验证
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Configuration
@RequiredArgsConstructor
public class SaTokenConfig implements WebMvcConfigurer {

    private final TokenValidationInterceptor tokenValidationInterceptor;

    /**
     * 注册自定义Token校验拦截器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册自定义Token校验拦截器
        registry.addInterceptor(tokenValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",      // 登录接口
                        "/auth/logout",     // 登出接口
                        "/api/auth/check",      // 检查登录状态
                        "/api/users/save",      // 用户注册接口
                        "/api/swagger-ui/**",   // Swagger UI
                        "/api/api-docs/**",     // API 文档
                        "/api/actuator/**",     // 健康检查
                        "/error",               // 错误页面
                        "/favicon.ico"          // 网站图标
                );
    }
}
