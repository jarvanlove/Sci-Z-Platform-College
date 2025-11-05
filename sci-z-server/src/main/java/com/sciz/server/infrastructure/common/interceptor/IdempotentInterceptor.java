package com.sciz.server.infrastructure.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author JiaWen.Wu
 * @className IdempotentInterceptor
 * @date 2025-01-27 10:00
 */
@Slf4j
@Component
public class IdempotentInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 仅提供简要兜底：读取 Idempotency-Key 头，使用内存占位避免短时间重复请求
        String key = request.getHeader("Idempotency-Key");
        if (key == null || key.isEmpty()) {
            return true;
        }
        long now = System.currentTimeMillis();
        Entry exist = STORE.get(key);
        if (exist != null && now < exist.expireAtMs) {
            log.warn(String.format("[IdempotentInterceptor] duplicated key=%s rejected", key));
            response.setStatus(429);
            return false;
        }
        STORE.put(key, new Entry(now + TimeUnit.SECONDS.toMillis(DEFAULT_TTL_SECONDS)));
        return true;
    }

    private static final long DEFAULT_TTL_SECONDS = 30;
    private static final Map<String, Entry> STORE = new ConcurrentHashMap<>();

    private static final class Entry {
        final long expireAtMs;

        Entry(long expireAtMs) {
            this.expireAtMs = expireAtMs;
        }
    }
}
