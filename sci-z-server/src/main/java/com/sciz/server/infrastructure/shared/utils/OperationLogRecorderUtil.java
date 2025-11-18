package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 操作日志记录工具类
 * 提供通用的操作日志记录功能，自动获取用户信息、请求信息等
 *
 * @author JiaWen.Wu
 * @className OperationLogRecorderUtil
 * @date 2025-11-17 11:40
 */
@Component
public class OperationLogRecorderUtil {

    private final EventPublisher eventPublisher;

    public OperationLogRecorderUtil(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    /**
     * 记录操作日志
     *
     * @param operation      String 操作名称（如：禁用用户、启用用户、创建用户等）
     * @param detail         String 详细描述（可选）
     * @param status         Integer 操作状态（1成功 0失败）
     * @param errorMessage   String 错误信息（失败时提供）
     * @param executionTime  Integer 执行耗时（毫秒）
     * @param responseResult String 响应结果（可选，如果提供则使用，否则尝试从请求属性获取）
     */
    public void record(String operation, String detail, Integer status, String errorMessage, Integer executionTime,
            String responseResult) {
        record(operation, detail, status, errorMessage, executionTime, responseResult, null);
    }

    /**
     * 记录操作日志（不包含响应结果）
     *
     * @param operation     String 操作名称
     * @param detail        String 详细描述
     * @param status        Integer 操作状态
     * @param errorMessage  String 错误信息
     * @param executionTime Integer 执行耗时
     */
    public void record(String operation, String detail, Integer status, String errorMessage, Integer executionTime) {
        record(operation, detail, status, errorMessage, executionTime, null);
    }

    /**
     * 记录成功操作日志
     *
     * @param operation     String 操作名称
     * @param detail        String 详细描述（可选）
     * @param executionTime Integer 执行耗时（毫秒）
     */
    public void recordSuccess(String operation, String detail, Integer executionTime) {
        record(operation, detail, 1, null, executionTime, null);
    }

    /**
     * 记录成功操作日志（带响应结果）
     *
     * @param operation      String 操作名称
     * @param detail         String 详细描述（可选）
     * @param executionTime  Integer 执行耗时（毫秒）
     * @param responseResult String 响应结果
     */
    public void recordSuccess(String operation, String detail, Integer executionTime, String responseResult) {
        record(operation, detail, 1, null, executionTime, responseResult);
    }

    /**
     * 记录失败操作日志
     *
     * @param operation     String 操作名称
     * @param detail        String 详细描述（可选）
     * @param errorMessage  String 错误信息
     * @param executionTime Integer 执行耗时（毫秒）
     */
    public void recordFailure(String operation, String detail, String errorMessage, Integer executionTime) {
        record(operation, detail, 0, errorMessage, executionTime, null);
    }

    /**
     * 记录警告操作日志
     *
     * @param operation     String 操作名称
     * @param detail        String 详细描述（可选）
     * @param executionTime Integer 执行耗时（毫秒）
     */
    public void recordWarning(String operation, String detail, Integer executionTime) {
        record(operation, detail, 1, null, executionTime, null, LogLevelStatus.WARN);
    }

    /**
     * 记录警告操作日志（带响应结果）
     *
     * @param operation      String 操作名称
     * @param detail         String 详细描述（可选）
     * @param executionTime  Integer 执行耗时（毫秒）
     * @param responseResult String 响应结果
     */
    public void recordWarning(String operation, String detail, Integer executionTime, String responseResult) {
        record(operation, detail, 1, null, executionTime, responseResult, LogLevelStatus.WARN);
    }

    /**
     * 记录操作日志（支持指定日志级别）
     *
     * @param operation      String 操作名称
     * @param detail         String 详细描述
     * @param status         Integer 操作状态
     * @param errorMessage   String 错误信息
     * @param executionTime  Integer 执行耗时
     * @param responseResult String 响应结果
     * @param level          LogLevelStatus 日志级别（可选，默认根据status自动判断）
     */
    private void record(String operation, String detail, Integer status, String errorMessage, Integer executionTime,
            String responseResult, LogLevelStatus level) {
        try {
            var event = buildEvent(operation, detail, status, errorMessage, executionTime, responseResult, level);
            eventPublisher.publishAsync(event);
        } catch (Exception e) {
            System.err.println(String.format("[OperationLogRecorder] 记录操作日志失败: operation=%s, err=%s",
                    operation, e.getMessage()));
        }
    }

    /**
     * 构建操作日志事件
     *
     * @param operation      String 操作名称
     * @param detail         String 详细描述
     * @param status         Integer 操作状态
     * @param errorMessage   String 错误信息
     * @param executionTime  Integer 执行耗时
     * @param responseResult String 响应结果
     * @param level          LogLevelStatus 日志级别（可选，默认根据status自动判断）
     * @return OperationLoggedEvent 操作日志事件
     */
    private OperationLoggedEvent buildEvent(String operation, String detail, Integer status,
            String errorMessage, Integer executionTime, String responseResult, LogLevelStatus level) {
        var event = new OperationLoggedEvent();

        // 获取当前登录用户信息
        var currentUser = LoginUserUtil.getCurrentUser();
        currentUser.ifPresent(user -> {
            event.setUserId(user.userId());
            event.setUsername(user.username());
        });

        // 获取请求信息
        var request = getCurrentRequest();
        if (request != null) {
            event.setMethod(request.getMethod());
            event.setRequestUrl(request.getRequestURI());
            event.setIpAddress(ClientInfoUtil.getClientIp(request));
            event.setLocation(ClientInfoUtil.getLocation(ClientInfoUtil.getClientIp(request)));
            event.setBrowser(ClientInfoUtil.getBrowser(request));
            event.setOs(ClientInfoUtil.getOs(request));
        }

        // 设置操作信息
        event.setOperation(operation);
        event.setDetail(detail);
        event.setStatus(status);
        event.setErrorMessage(errorMessage);
        event.setExecutionTime(executionTime);

        // 设置日志级别：如果指定了level则使用，否则根据操作状态自动判断
        if (level != null) {
            event.setLevel(level.getCode());
        } else {
            // 根据操作状态设置日志级别：成功=INFO，失败=ERROR
            if (status != null && status == 1) {
                event.setLevel(LogLevelStatus.INFO.getCode());
            } else {
                event.setLevel(LogLevelStatus.ERROR.getCode());
            }
        }

        // 获取请求参数并转换为JSON格式
        if (request != null) {
            var requestParams = extractRequestParamsAsJson(request);
            event.setRequestParams(requestParams);
        }

        // 获取响应结果并转换为JSON格式：优先使用传入的responseResult，否则尝试从请求属性获取
        String responseResultJson = null;
        if (responseResult != null) {
            responseResultJson = convertToJson(responseResult);
        } else if (request != null) {
            var cachedResponse = request.getAttribute("cachedResponseBody");
            if (cachedResponse != null) {
                responseResultJson = convertToJson(cachedResponse);
            }
        }
        event.setResponseResult(responseResultJson);

        // 设置 TraceId（如果有的话，可以从请求头或上下文获取）
        if (request != null) {
            var traceId = Optional.ofNullable(request.getHeader("X-Trace-Id"))
                    .orElse(Optional.ofNullable(request.getHeader("Trace-Id"))
                            .orElse(null));
            event.setTraceId(traceId);
        }

        return event;
    }

    /**
     * 获取当前请求对象
     *
     * @return HttpServletRequest 或 null
     */
    private HttpServletRequest getCurrentRequest() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .orElse(null);
    }

    /**
     * 提取请求参数并转换为JSON格式
     * 优先从查询参数获取，如果是POST/PUT等有请求体的方法，尝试从请求体获取
     *
     * @param request HttpServletRequest 请求对象
     * @return String 请求参数JSON字符串
     */
    private String extractRequestParamsAsJson(HttpServletRequest request) {
        try {
            // 1. 对于POST/PUT等有请求体的方法，尝试从请求体获取（JSON格式）
            var method = request.getMethod();
            if ("POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)
                    || "PATCH".equalsIgnoreCase(method)) {
                // 检查Content-Type
                var contentType = request.getContentType();
                if (contentType != null && contentType.contains("application/json")) {
                    // 尝试从请求属性中获取（如果拦截器已经缓存了）
                    var cachedBody = request.getAttribute("cachedRequestBody");
                    if (cachedBody instanceof String) {
                        // 如果已经是JSON格式，直接返回
                        if (JsonUtil.isValidJson((String) cachedBody)) {
                            return (String) cachedBody;
                        }
                    }
                    // 如果没有缓存，尝试读取（注意：流只能读取一次，可能已经读取过了）
                    try (var reader = request.getReader()) {
                        var body = reader.lines().collect(Collectors.joining());
                        if (StringUtils.hasText(body) && JsonUtil.isValidJson(body)) {
                            return body;
                        }
                    } catch (IOException e) {
                        // 流已读取，忽略
                    }
                } else if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                    // 表单参数转换为JSON
                    var paramMap = request.getParameterMap();
                    if (!paramMap.isEmpty()) {
                        Map<String, Object> jsonMap = new HashMap<>();
                        paramMap.forEach((key, values) -> {
                            if (values.length == 1) {
                                jsonMap.put(key, parseValue(values[0]));
                            } else {
                                jsonMap.put(key, Arrays.asList(values));
                            }
                        });
                        return JsonUtil.toJson(jsonMap);
                    }
                }
            }

            // 2. 获取查询参数并转换为JSON格式（适用于GET请求）
            var queryString = request.getQueryString();
            if (StringUtils.hasText(queryString)) {
                return convertQueryStringToJson(queryString);
            }

            // 3. 获取所有请求参数（包括路径参数和查询参数）并转换为JSON
            var paramNames = request.getParameterNames();
            Map<String, Object> paramMap = new HashMap<>();
            while (paramNames.hasMoreElements()) {
                var paramName = paramNames.nextElement();
                var paramValues = request.getParameterValues(paramName);
                if (paramValues.length == 1) {
                    paramMap.put(paramName, parseValue(paramValues[0]));
                } else {
                    paramMap.put(paramName, Arrays.asList(paramValues));
                }
            }
            if (!paramMap.isEmpty()) {
                return JsonUtil.toJson(paramMap);
            }

            return null;
        } catch (Exception e) {
            // 获取请求参数失败，不影响主流程
            return null;
        }
    }

    /**
     * 将查询字符串转换为JSON格式
     *
     * @param queryString String 查询字符串（如：disabled=false&pageNo=1）
     * @return String JSON字符串（如：{"disabled":false,"pageNo":1}）
     */
    private String convertQueryStringToJson(String queryString) {
        if (!StringUtils.hasText(queryString)) {
            return null;
        }

        try {
            Map<String, Object> paramMap = new HashMap<>();
            String[] pairs = queryString.split("&");
            for (String pair : pairs) {
                if (!StringUtils.hasText(pair)) {
                    continue;
                }
                String[] keyValue = pair.split("=", 2);
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    paramMap.put(key, parseValue(value));
                } else if (keyValue.length == 1) {
                    paramMap.put(keyValue[0], "");
                }
            }
            return JsonUtil.toJson(paramMap);
        } catch (Exception e) {
            // 转换失败，返回原始查询字符串
            return queryString;
        }
    }

    /**
     * 解析参数值（尝试转换为合适的类型）
     *
     * @param value String 参数值
     * @return Object 解析后的值（Boolean、Integer、Long、Double或String）
     */
    private Object parseValue(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }

        // 尝试解析为布尔值
        if ("true".equalsIgnoreCase(value)) {
            return true;
        }
        if ("false".equalsIgnoreCase(value)) {
            return false;
        }

        // 尝试解析为整数
        try {
            if (value.matches("^-?\\d+$")) {
                long longValue = Long.parseLong(value);
                if (longValue >= Integer.MIN_VALUE && longValue <= Integer.MAX_VALUE) {
                    return (int) longValue;
                }
                return longValue;
            }
        } catch (NumberFormatException e) {
            // 忽略，继续尝试其他类型
        }

        // 尝试解析为浮点数
        try {
            if (value.matches("^-?\\d+\\.\\d+$")) {
                return Double.parseDouble(value);
            }
        } catch (NumberFormatException e) {
            // 忽略，返回字符串
        }

        // 默认返回字符串
        return value;
    }

    /**
     * 将对象转换为JSON字符串
     *
     * @param obj Object 对象
     * @return String JSON字符串
     */
    private String convertToJson(Object obj) {
        if (obj == null) {
            return null;
        }

        // 如果已经是JSON字符串，直接返回
        if (obj instanceof String stringValue) {
            if (JsonUtil.isValidJson(stringValue)) {
                return stringValue;
            }
            // 如果不是有效JSON，尝试包装成JSON对象
            return JsonUtil.toJson(Map.of("value", stringValue));
        }

        // 转换为JSON
        return JsonUtil.toJson(obj);
    }
}
