package com.sciz.server.infrastructure.config.security;

import cn.dev33.satoken.filter.SaServletFilter;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;

/**
 * Sa-Token 基础安全配置
 *
 * 放行：登录、注册、验证码发送、重置密码、静态资源、Swagger 文档
 * 其余：默认需要登录（包括 logout、refresh-token、profile 等）
 * 当前策略：直接放行 `/api/auth/**` 下所有接口
 *
 * @author JiaWen.Wu
 * @className SecurityConfig
 * @date 2025-11-07 11:30
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SaServletFilter saServletFilter() {
        return new SaServletFilter()
                .addInclude("/**")
                .addExclude("/api/auth/**") // 认证相关接口全部放行
                // 静态资源和文档
                .addExclude("/swagger-ui/**", "/swagger-resources/**", "/v3/api-docs/**")
                .addExclude("/webjars/**", "/static/**", "/favicon.ico")
                // 其他所有接口都需要登录
                .setAuth(obj -> SaRouter.match("/**").check(r -> StpUtil.checkLogin()))
                .setError(e -> {
                    // 统一未登录响应
                    var result = Result.fail(ResultCode.UNAUTHORIZED);
                    return Optional.ofNullable(JsonUtil.toJson(result))
                            .orElse(result.getMessage());
                });
    }
}
