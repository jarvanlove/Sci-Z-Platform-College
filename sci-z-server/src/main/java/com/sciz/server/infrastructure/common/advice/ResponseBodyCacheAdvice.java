package com.sciz.server.infrastructure.common.advice;

import com.sciz.server.infrastructure.shared.utils.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 响应体拦截器
 * 用于拦截Controller返回的响应结果，缓存到request属性中，供操作日志使用
 *
 * @author JiaWen.Wu
 * @className ResponseBodyCacheAdvice
 * @date 2025-11-17 16:00
 */
@Slf4j
@RestControllerAdvice
public class ResponseBodyCacheAdvice implements ResponseBodyAdvice<Object> {

    /**
     * 响应体缓存属性名
     */
    private static final String CACHED_RESPONSE_BODY_ATTRIBUTE = "cachedResponseBody";

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 拦截所有Controller方法的返回值
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        try {
            // 获取HttpServletRequest
            if (request instanceof ServletServerHttpRequest servletRequest) {
                HttpServletRequest httpRequest = servletRequest.getServletRequest();

                // 将响应体转换为JSON字符串并缓存到request属性中
                String responseJson = JsonUtil.toJson(body);
                if (responseJson != null) {
                    httpRequest.setAttribute(CACHED_RESPONSE_BODY_ATTRIBUTE, responseJson);
                    log.debug(String.format("[ResponseBodyCacheAdvice] 缓存响应结果: uri=%s, response=%s",
                            httpRequest.getRequestURI(), truncate(responseJson, 200)));
                }
            }
        } catch (Exception e) {
            // 缓存响应失败不影响主流程，只记录错误日志
            log.warn(String.format("[ResponseBodyCacheAdvice] 缓存响应结果失败: err=%s", e.getMessage()));
        }

        return body;
    }

    /**
     * 截取字符串
     *
     * @param s   String 字符串
     * @param max int 最大长度
     * @return String 截取后的字符串
     */
    private String truncate(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() <= max ? s : s.substring(0, max) + "...";
    }
}
