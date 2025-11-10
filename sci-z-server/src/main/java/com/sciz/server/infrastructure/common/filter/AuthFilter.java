package com.sciz.server.infrastructure.common.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author JiaWen.Wu
 * @className AuthFilter
 * @date 2025-10-29 10:00
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String uri = req.getRequestURI();
        // 放行静态资源与健康检查等公共路径（可按需扩展）
        if (uri.startsWith("/actuator") || uri.startsWith("/health") || uri.startsWith("/static/")
                || uri.startsWith("/swagger") || uri.startsWith("/v3/")) {
            chain.doFilter(request, response);
            return;
        }
        // 这里只做最小化预检查（日志），真正鉴权交给 AuthInterceptor/Security 层
        String auth = req.getHeader("Authorization");
        boolean hasAuthHeader = Optional.ofNullable(auth).isPresent();
        log.debug(String.format("[AuthFilter] uri=%s authHeaderPresent=%s", uri, Boolean.toString(hasAuthHeader)));
        chain.doFilter(request, response);
    }
}
