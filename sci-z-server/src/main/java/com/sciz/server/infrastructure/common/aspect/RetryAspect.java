package com.sciz.server.infrastructure.common.aspect;

import com.sciz.server.infrastructure.common.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author JiaWen.Wu
 * @className RetryAspect
 * @date 2025-10-29 10:00
 */
@Slf4j
@Aspect
@Component("customRetryAspect")
public class RetryAspect {

    @Around("@annotation(com.sciz.server.infrastructure.common.annotation.Retry)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Retry anno = method.getAnnotation(Retry.class);

        int maxAttempts = Math.max(1, anno.maxAttempts());
        long baseDelay = Math.max(0, anno.delay());
        Retry.RetryStrategy strategy = anno.strategy();
        double multiplier = Math.max(1.0, anno.multiplier());
        long maxDelay = Math.max(baseDelay, anno.maxDelay());
        Class<? extends Throwable>[] retryFor = anno.retryFor();
        Class<? extends Throwable>[] noRetryFor = anno.noRetryFor();
        String condition = anno.condition();

        int attempt = 0;
        Throwable lastError = null;
        Object lastResult = null;
        while (attempt < maxAttempts) {
            attempt++;
            try {
                lastResult = joinPoint.proceed();
                // 条件重试：当 condition 表达式为真时继续重试（此处简化：仅对 null/空字符串判定）
                if (Optional.ofNullable(condition).filter(text -> !text.isEmpty()).isPresent()) {
                    boolean needRetry = Optional.ofNullable(lastResult)
                            .map(result -> result instanceof String && ((String) result).isEmpty())
                            .orElse(true);
                    if (needRetry) {
                        waitDelay(attempt, baseDelay, strategy, multiplier, maxDelay);
                        continue;
                    }
                }
                return lastResult;
            } catch (Throwable ex) {
                lastError = ex;
                if (!shouldRetry(ex, retryFor, noRetryFor) || attempt >= maxAttempts) {
                    throw ex;
                }
                if (anno.logRetry()) {
                    log.warn(String.format("方法 %s 第%s/%s次失败，将重试。原因: %s",
                            method.getName(), String.valueOf(attempt), String.valueOf(maxAttempts), ex.getMessage()));
                }
                waitDelay(attempt, baseDelay, strategy, multiplier, maxDelay);
            }
        }
        Optional.ofNullable(lastError).ifPresent(RetryAspect::sneakyThrow);
        return lastResult;
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
     * 是否需要重试
     *
     * @param ex         异常
     * @param retryFor   需要重试的异常类型
     * @param noRetryFor 不需要重试的异常类型
     * @return boolean 是否需要重试
     */
    private boolean shouldRetry(Throwable ex, Class<? extends Throwable>[] retryFor,
            Class<? extends Throwable>[] noRetryFor) {
        boolean inNoRetry = Optional.ofNullable(noRetryFor)
                .map(array -> Arrays.stream(array).anyMatch(clazz -> clazz.isInstance(ex)))
                .orElse(false);
        if (inNoRetry) {
            return false;
        }
        Optional<Class<? extends Throwable>[]> retryArray = Optional.ofNullable(retryFor)
                .filter(array -> array.length > 0);
        if (retryArray.isPresent()) {
            boolean matchRetry = Arrays.stream(retryArray.get()).anyMatch(clazz -> clazz.isInstance(ex));
            return matchRetry;
        }
        // 默认重试所有异常
        return true;
    }

    /**
     * 等待延迟
     *
     * @param attempt    尝试次数
     * @param baseDelay  基础延迟时间
     * @param strategy   重试策略
     * @param multiplier 退避倍数
     * @param maxDelay   最大延迟时间
     */
    private void waitDelay(int attempt, long baseDelay, Retry.RetryStrategy strategy, double multiplier,
            long maxDelay) {
        long delay;
        switch (strategy) {
            case EXPONENTIAL:
                delay = (long) Math.min(maxDelay, baseDelay * Math.pow(multiplier, Math.max(0, attempt - 1)));
                break;
            case LINEAR:
                delay = Math.min(maxDelay, baseDelay * Math.max(1, attempt));
                break;
            case RANDOM:
                delay = ThreadLocalRandom.current().nextLong(Math.max(1, baseDelay));
                break;
            case FIXED:
            default:
                delay = baseDelay;
        }
        if (delay > 0) {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void sneakyThrow(Throwable throwable) throws E {
        throw (E) throwable;
    }
}
