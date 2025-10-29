package com.sciz.server.infrastructure.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author JiaWen.Wu
 * @className AuthInterceptor
 * @date 2025-01-27 10:00
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String auth = request.getHeader("Authorization");
        String uri = request.getRequestURI();
        // 放行公共端点（可在 WebMvcConfig 中通过 excludePatterns 精细控制）
        if (uri.startsWith("/actuator") || uri.startsWith("/health") || uri.startsWith("/swagger")
                || uri.startsWith("/v3/")) {
            return true;
        }
        // 这里只记录并放行，真正认证交由安全模块
        log.debug(String.format("[AuthInterceptor] uri=%s authHeaderPresent=%s", uri, auth != null));
        return true;
    }
}
