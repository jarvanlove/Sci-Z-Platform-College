package com.sciz.server.infrastructure.common.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author JiaWen.Wu
 * @className LogInterceptor
 * @date 2025-01-27 10:00
 */
@Slf4j
@Component
public class LogInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        long start = System.currentTimeMillis();
        request.setAttribute("__log_start_ts", start);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        log.info(String.format("[LogInterceptor] -> %s %s ip=%s", method, uri, ip));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            ModelAndView modelAndView) {
        // no-op
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            Exception ex) {
        long cost = Optional.ofNullable(request.getAttribute("__log_start_ts"))
                .filter(Long.class::isInstance)
                .map(Long.class::cast)
                .map(start -> System.currentTimeMillis() - start)
                .orElse(0L);
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        Optional.ofNullable(ex)
                .ifPresentOrElse(exception -> log.error(
                        String.format("[LogInterceptor] <- %s %s status=%d cost=%dms err=%s",
                                method, uri, status, cost, exception.getMessage()),
                        exception),
                        () -> log.info(String.format("[LogInterceptor] <- %s %s status=%d cost=%dms",
                                method, uri, status, cost)));
    }
}
