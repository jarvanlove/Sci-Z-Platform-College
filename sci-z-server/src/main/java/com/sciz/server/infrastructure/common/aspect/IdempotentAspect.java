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
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        Optional<Entry> existing = Optional.ofNullable(STORE.get(key))
                .filter(entry -> !entry.isExpired(now));
        if (existing.isPresent()) {
            switch (strategy) {
                case REJECT:
                    throw new BusinessException(429, anno.message());
                case RETURN_CACHED:
                    log.info(String.format("幂等命中，返回缓存结果 key=%s", key));
                    return existing.map(entry -> entry.result).orElse(null);
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

    /**
     * 等待并返回结果
     *
     * @param key           幂等性键
     * @param expireSeconds 过期时间（秒）
     * @return 结果
     * @throws InterruptedException 中断异常
     */
    private Object waitAndReturn(String key, long expireSeconds) throws InterruptedException {
        long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(expireSeconds);
        while (System.currentTimeMillis() < deadline) {
            Optional<Entry> entry = Optional.ofNullable(STORE.get(key));
            Optional<Object> result = entry.filter(Entry::resultAvailable).map(value -> value.result);
            if (result.isPresent()) {
                return result.get();
            }
            Thread.sleep(100);
        }
        // 超时后删除占位并提示重试
        STORE.remove(key);
        throw new BusinessException(408, "请求处理中超时，请稍后重试");
    }

    /**
     * 获取方法
     *
     * @param joinPoint 连接点
     * @return 方法
     */
    private Method getMethod(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }

    /**
     * 构建幂等性键
     *
     * @param anno      注解
     * @param joinPoint 连接点
     * @return 幂等性键
     */
    private String buildKey(Idempotent anno, ProceedingJoinPoint joinPoint) {
        String prefix = anno.prefix();
        String raw = Optional.ofNullable(anno.key())
                .filter(text -> !text.isEmpty())
                .orElseGet(() -> {
                    Method method = getMethod(joinPoint);
                    return method.getDeclaringClass().getName() + "#"
                            + method.getName() + ArraysHash(joinPoint.getArgs());
                });
        return prefix + ":" + sha256(raw);
    }

    /**
     * 数组哈希
     *
     * @param args 参数数组
     * @return 哈希值
     */
    private String ArraysHash(Object[] args) {
        return Optional.ofNullable(args)
                .filter(array -> array.length > 0)
                .map(array -> Arrays.stream(array)
                        .map(Objects::toString)
                        .collect(Collectors.joining(",", "(", ")")))
                .orElse("()");
    }

    /**
     * SHA-256 哈希
     *
     * @param input 输入字符串
     * @return 哈希值
     */
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
            return Optional.ofNullable(result).isPresent();
        }
    }
}
