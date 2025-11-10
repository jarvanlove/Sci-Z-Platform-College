package com.sciz.server.infrastructure.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Optional;
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
        return Optional.ofNullable(request.getHeader("Idempotency-Key"))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(key -> {
                    long now = System.currentTimeMillis();
                    boolean exists = Optional.ofNullable(STORE.get(key))
                            .filter(entry -> now < entry.expireAtMs)
                            .isPresent();
                    if (exists) {
                        log.warn(String.format("[IdempotentInterceptor] duplicated key=%s rejected", key));
                        response.setStatus(429);
                        return false;
                    }
                    STORE.put(key, new Entry(now + TimeUnit.SECONDS.toMillis(DEFAULT_TTL_SECONDS)));
                    return true;
                })
                .orElse(true);
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
