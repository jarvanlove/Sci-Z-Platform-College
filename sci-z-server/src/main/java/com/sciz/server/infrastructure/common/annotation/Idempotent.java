package com.sciz.server.infrastructure.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 幂等性注解
 * 
 * 支持多种幂等性策略、自定义键生成、异常处理等功能
 *
 * @author JiaWen.Wu
 * @className Idempotent
 * @date 2025-10-29 10:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {

    /**
     * 幂等性键（支持SpEL表达式）
     * 例如：@Idempotent(key = "#userId + '_' + #orderId")
     */
    String key() default "";

    /**
     * 键过期时间（秒）
     */
    long expire() default 300;

    /**
     * 幂等性策略
     */
    IdempotentStrategy strategy() default IdempotentStrategy.REJECT;

    /**
     * 键前缀
     */
    String prefix() default "idempotent";

    /**
     * 键生成器类名
     */
    String keyGenerator() default "";

    /**
     * 重复请求时的提示信息
     */
    String message() default "请勿重复提交";

    /**
     * 是否记录幂等性日志
     */
    boolean logIdempotent() default true;

    /**
     * 是否在方法执行完成后删除键
     */
    boolean deleteAfterExecution() default false;

    /**
     * 异常时是否删除键
     */
    boolean deleteOnException() default false;

    /**
     * 幂等性策略枚举
     */
    enum IdempotentStrategy {
        /**
         * 拒绝重复请求
         */
        REJECT,
        /**
         * 返回缓存结果
         */
        RETURN_CACHED,
        /**
         * 等待正在执行的请求完成
         */
        WAIT_AND_RETURN
    }
}
