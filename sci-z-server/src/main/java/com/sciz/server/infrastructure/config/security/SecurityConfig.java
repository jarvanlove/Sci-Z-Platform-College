package com.sciz.server.infrastructure.config.security;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Sa-Token 基础安全配置
 *
 * 放行：认证接口、静态/Swagger 资源
 * 其余：默认需要登录
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/api/auth/**")
                .addExclude("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
                .addExclude("/webjars/**", "/static/**", "/favicon.ico")
                .setAuth(obj -> SaRouter.match("/**").check(r -> StpUtil.checkLogin()))
                .setError(e -> {
                    // 统一未登录响应
                    return "NOT_LOGIN";
                });
    }
}
