package com.sciz.server.infrastructure.shared.utils;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * TraceId 工具
 * 
 * @author JiaWen.Wu
 * @className TraceIdUtil
 * @date 2025-10-29 10:00
 */
public final class TraceIdUtil {
    private static final ThreadLocal<String> TRACE_ID = new ThreadLocal<>();

    private TraceIdUtil() {
    }

    /**
     * 生成 TraceId
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 设置 TraceId 到上下文
     */
    public static void setTraceId(String traceId) {
        TRACE_ID.set(traceId);
        MDC.put("traceId", traceId);
    }

    /**
     * 获取 TraceId
     */
    public static String getTraceId() {
        return TRACE_ID.get();
    }

    /**
     * 清理 TraceId
     */
    public static void clear() {
        TRACE_ID.remove();
        MDC.remove("traceId");
    }
}
