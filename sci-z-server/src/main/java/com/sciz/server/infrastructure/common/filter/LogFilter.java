package com.sciz.server.infrastructure.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author JiaWen.Wu
 * @className LogFilter
 * @date 2025-10-29 10:00
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class LogFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long start = System.currentTimeMillis();
        HttpServletRequest req = (HttpServletRequest) request;
        String method = req.getMethod();
        String uri = req.getRequestURI();
        String ip = request.getRemoteAddr();
        log.info(String.format("[LogFilter] -> %s %s ip=%s", method, uri, ip));
        try {
            chain.doFilter(request, response);
        } finally {
            long cost = System.currentTimeMillis() - start;
            log.info(String.format("[LogFilter] <- %s %s cost=%dms", method, uri, cost));
        }
    }
}
