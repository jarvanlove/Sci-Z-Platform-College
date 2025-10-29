package com.sciz.server.infrastructure.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 重试注解
 * 
 * 支持多种重试策略、异常类型过滤、退避算法等功能
 *
 * @author JiaWen.Wu
 * @className Retry
 * @date 2025-10-29 10:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Retry {

    /**
     * 最大重试次数
     */
    int maxAttempts() default 3;

    /**
     * 重试间隔时间（毫秒）
     */
    long delay() default 1000;

    /**
     * 重试策略
     */
    RetryStrategy strategy() default RetryStrategy.FIXED;

    /**
     * 退避倍数（用于指数退避）
     */
    double multiplier() default 2.0;

    /**
     * 最大延迟时间（毫秒）
     */
    long maxDelay() default 30000;

    /**
     * 需要重试的异常类型（为空则重试所有异常）
     */
    Class<? extends Throwable>[] retryFor() default {};

    /**
     * 不需要重试的异常类型
     */
    Class<? extends Throwable>[] noRetryFor() default {};

    /**
     * 是否记录重试日志
     */
    boolean logRetry() default true;

    /**
     * 重试条件表达式（SpEL表达式）
     */
    String condition() default "";

    /**
     * 重试策略枚举
     */
    enum RetryStrategy {
        /**
         * 固定间隔
         */
        FIXED,
        /**
         * 指数退避
         */
        EXPONENTIAL,
        /**
         * 线性递增
         */
        LINEAR,
        /**
         * 随机退避
         */
        RANDOM
    }
}
