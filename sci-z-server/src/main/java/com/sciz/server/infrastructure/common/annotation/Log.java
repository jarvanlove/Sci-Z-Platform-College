package com.sciz.server.infrastructure.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 日志记录注解
 * 
 * 支持多种日志级别、参数记录、执行时间统计等功能
 *
 * @author JiaWen.Wu
 * @className Log
 * @date 2025-10-29 10:00
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    /**
     * 日志描述信息
     */
    String value() default "";

    /**
     * 日志级别
     */
    LogLevel level() default LogLevel.INFO;

    /**
     * 是否记录方法参数
     */
    boolean logArgs() default true;

    /**
     * 是否记录返回值
     */
    boolean logResult() default true;

    /**
     * 是否记录执行时间
     */
    boolean logExecutionTime() default true;

    /**
     * 是否记录异常信息
     */
    boolean logException() default true;

    /**
     * 是否记录用户信息（从SecurityContext获取）
     */
    boolean logUser() default false;

    /**
     * 是否记录IP地址
     */
    boolean logIp() default false;

    /**
     * 敏感参数名称（这些参数的值会被脱敏）
     */
    String[] sensitiveParams() default {};

    /**
     * 日志级别枚举
     */
    enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }
}
