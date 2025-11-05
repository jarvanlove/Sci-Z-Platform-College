package com.sciz.server.infrastructure.external.dify.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模糊查询注解
 * 标记字段支持模糊查询
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FuzzyQueries {
    // 可以添加其他配置参数
}
