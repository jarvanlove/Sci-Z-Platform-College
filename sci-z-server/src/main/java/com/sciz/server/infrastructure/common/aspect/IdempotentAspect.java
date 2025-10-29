package com.sciz.server.infrastructure.common.aspect;

import com.sciz.server.infrastructure.common.annotation.Idempotent;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author JiaWen.Wu
 * @className IdempotentAspect
 * @date 2025-10-29 10:00
 */
@Slf4j
@Aspect
@Component
public class IdempotentAspect {

    @Around("@annotation(com.sciz.server.infrastructure.common.annotation.Idempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Idempotent anno = method.getAnnotation(Idempotent.class);

        String key = buildKey(anno, joinPoint);
        long expireSeconds = Math.max(1, anno.expire());

        Idempotent.IdempotentStrategy strategy = anno.strategy();

        long now = System.currentTimeMillis();
        Entry exist = STORE.get(key);
        if (exist != null && !exist.isExpired(now)) {
            switch (strategy) {
                case REJECT:
                    throw new BusinessException(429, anno.message());
                case RETURN_CACHED:
                    log.info(String.format("幂等命中，返回缓存结果 key=%s", key));
                    return exist.result;
                case WAIT_AND_RETURN:
                    return waitAndReturn(key, expireSeconds);
                default:
                    throw new BusinessException(429, anno.message());
            }
        }

        // 占位，防止并发进入
        STORE.put(key, Entry.placeholder(expireSeconds));
        try {
            Object result = joinPoint.proceed();
            STORE.put(key, Entry.success(result, expireSeconds));
            if (anno.deleteAfterExecution()) {
                STORE.remove(key);
            }
            return result;
        } catch (Throwable ex) {
            if (anno.deleteOnException()) {
                STORE.remove(key);
            }
            throw ex;
        }
    }

    private Object waitAndReturn(String key, long expireSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds);
        while (System.currentTimeMillis() < deadline) {
            Entry entry = STORE.get(key);
            if (entry != null && entry.resultAvailable()) {
                return entry.result;
            }
            Thread.sleep(100);
        }
        // 超时后删除占位并提示重试
        STORE.remove(key);
        throw new BusinessException(408, "请求处理中超时，请稍后重试");
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }

    private String buildKey(Idempotent anno, ProceedingJoinPoint joinPoint) {
        String prefix = anno.prefix();
        String raw;
        if (anno.key() != null && !anno.key().isEmpty()) {
            // 简化实现：不解析 SpEL，直接使用给定 key 文本；
            // 若需要 SpEL，可引入 Spring Expression 解析（此处避免额外依赖/复杂性）。
            raw = anno.key();
        } else {
            Method method = getMethod(joinPoint);
            raw = method.getDeclaringClass().getName() + "#" + method.getName() + ArraysHash(joinPoint.getArgs());
        }
        return prefix + ":" + sha256(raw);
    }

    private String ArraysHash(Object[] args) {
        if (args == null || args.length == 0)
            return "()";
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < args.length; i++) {
            sb.append(Objects.toString(args[i]));
            if (i < args.length - 1)
                sb.append(',');
        }
        sb.append(')');
        return sb.toString();
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            return Integer.toHexString(input.hashCode());
        }
    }

    private static final Map<String, Entry> STORE = new ConcurrentHashMap<>();

    private static class Entry {
        final long expireAtMs;
        final Object result;

        private Entry(long expireAtMs, Object result) {
            this.expireAtMs = expireAtMs;
            this.result = result;
        }

        static Entry placeholder(long expireSeconds) {
            return new Entry(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds), null);
        }

        static Entry success(Object result, long expireSeconds) {
            return new Entry(System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds), result);
        }

        boolean isExpired(long now) {
            return now > expireAtMs;
        }

        boolean resultAvailable() {
            return result != null;
        }
    }
}
