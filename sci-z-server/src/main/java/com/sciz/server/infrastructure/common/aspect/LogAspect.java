package com.sciz.server.infrastructure.common.aspect;

import com.sciz.server.infrastructure.shared.utils.TraceIdUtil;
import com.sciz.server.infrastructure.common.annotation.Log;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author JiaWen.Wu
 * @className LogAspect
 * @date 2025-10-28 00:00
 */
@Aspect
@Component
public class LogAspect {
    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 记录控制器层调用的开始/成功/失败日志
     *
     * @param joinPoint 切点
     * @return 执行结果
     */
    @Around("@within(org.springframework.web.bind.annotation.RestController)")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String method = joinPoint.getSignature().toShortString();
        String traceId = TraceIdUtil.getTraceId();
        long start = System.currentTimeMillis();
        log.info(String.format("[TraceId:%s] 请求开始: %s", traceId, method));
        try {
            Object result = joinPoint.proceed();
            long cost = System.currentTimeMillis() - start;
            log.info(String.format("[TraceId:%s] 请求成功: %s | 耗时: %dms", traceId, method, cost));
            return result;
        } catch (Throwable ex) {
            long cost = System.currentTimeMillis() - start;
            log.error(
                    String.format("[TraceId:%s] 请求失败: %s | 耗时: %dms | 异常: %s", traceId, method, cost, ex.getMessage()),
                    ex);
            throw ex;
        }
    }

    /**
     * 基于 @Log 的细粒度记录
     */
    @Around("@annotation(com.sciz.server.infrastructure.common.annotation.Log)")
    public Object logAnnotation(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = getMethod(joinPoint);
        Log logAnno = method.getAnnotation(Log.class);
        String traceId = TraceIdUtil.getTraceId();
        String methodName = joinPoint.getSignature().toShortString();

        long startNs = System.nanoTime();
        if (logAnno.logArgs()) {
            Object[] args = joinPoint.getArgs();
            Object[] masked = maskArgs(args, new HashSet<>(Arrays.asList(logAnno.sensitiveParams())));
            logByLevel(logAnno.level(), String.format("[TraceId:%s] %s 调用开始 | 描述: %s | 参数: %s", traceId, methodName,
                    logAnno.value(), Arrays.toString(masked)), null);
        } else {
            logByLevel(logAnno.level(),
                    String.format("[TraceId:%s] %s 调用开始 | 描述: %s", traceId, methodName, logAnno.value()), null);
        }

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            if (logAnno.logResult()) {
                logByLevel(logAnno.level(), String.format("[TraceId:%s] %s 调用成功 | 耗时: %dms | 返回: %s", traceId,
                        methodName, elapsedMs, safeToString(result)), null);
            } else {
                logByLevel(logAnno.level(),
                        String.format("[TraceId:%s] %s 调用成功 | 耗时: %dms", traceId, methodName, elapsedMs), null);
            }
            return result;
        } catch (Throwable ex) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000L;
            if (logAnno.logException()) {
                log.error(String.format("[TraceId:%s] %s 调用异常 | 耗时: %dms | 描述: %s | 异常: %s", traceId, methodName,
                        elapsedMs,
                        logAnno.value(), ex.getMessage()), ex);
            }
            throw ex;
        }
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }

    private void logByLevel(Log.LogLevel level, String message, Throwable t) {
        switch (level) {
            case DEBUG:
                if (t == null)
                    log.debug(message);
                else
                    log.debug(message, t);
                break;
            case WARN:
                if (t == null)
                    log.warn(message);
                else
                    log.warn(message, t);
                break;
            case ERROR:
                if (t == null)
                    log.error(message);
                else
                    log.error(message, t);
                break;
            case INFO:
            default:
                if (t == null)
                    log.info(message);
                else
                    log.info(message, t);
        }
    }

    private Object[] maskArgs(Object[] args, Set<String> sensitiveNames) {
        // 名称级敏感无法直接拿到参数名，这里仅做常见字段名的粗略脱敏
        if (args == null)
            return new Object[0];
        Object[] copy = Arrays.copyOf(args, args.length);
        for (int i = 0; i < copy.length; i++) {
            Object val = copy[i];
            if (val instanceof CharSequence) {
                String s = String.valueOf(val);
                if (looksSensitive(s)) {
                    copy[i] = maskString(s);
                }
            }
        }
        return copy;
    }

    private boolean looksSensitive(String s) {
        String lower = s.toLowerCase();
        return lower.contains("password") || lower.contains("token") || lower.contains("secret");
    }

    private String maskString(String s) {
        if (s == null)
            return null;
        int keep = Math.min(3, s.length());
        char[] stars = new char[Math.max(0, s.length() - keep)];
        Arrays.fill(stars, '*');
        return s.substring(0, keep) + new String(stars);
    }

    private String safeToString(Object obj) {
        try {
            return String.valueOf(obj);
        } catch (Throwable e) {
            return "<unprintable>";
        }
    }
}
