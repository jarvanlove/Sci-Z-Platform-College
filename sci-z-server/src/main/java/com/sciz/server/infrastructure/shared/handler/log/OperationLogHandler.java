package com.sciz.server.infrastructure.shared.handler.log;

import com.sciz.server.application.service.log.OperationLogService;
import com.sciz.server.infrastructure.shared.event.log.OperationLoggedEvent;
import com.sciz.server.infrastructure.shared.utils.JsonUtil;
// import com.sciz.server.infrastructure.shared.utils.KafkaUtil;
// import org.springframework.kafka.core.KafkaTemplate;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 系统操作日志事件处理器
 *
 * @author JiaWen.Wu
 * @className OperationLogHandler
 * @date 2025-11-14 21:00
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogHandler {

    private final OperationLogService operationLogService;
    // ==================== Kafka异步写入（已注释，需要时启用） ====================
    // private final KafkaTemplate<String, String> kafkaTemplate;
    // private static final String OPERATION_LOG_TOPIC = "operation-log-topic";

    @EventListener
    public void handle(OperationLoggedEvent event) {
        try {
            // 在事件处理时再次尝试获取响应结果（此时ResponseBodyCacheAdvice已经执行，响应结果已缓存）
            // 注意：使用同步事件处理，确保请求上下文仍然存在
            if (event.getResponseResult() == null) {
                var request = getCurrentRequest();
                if (request != null) {
                    var cachedResponse = request.getAttribute("cachedResponseBody");
                    if (cachedResponse != null) {
                        var responseJson = convertToJson(cachedResponse);
                        event.setResponseResult(responseJson);
                        log.debug(String.format("[OperationLogHandler] 从缓存获取响应结果: traceId=%s, response=%s",
                                event.getTraceId(), truncate(responseJson, 100)));
                    }
                }
            }

            // ==================== 方案一：直接数据库写入（当前使用） ====================
            var id = operationLogService.saveFromEvent(event);
            log.info(String.format(
                    "[OperationLog] saved id=%s traceId=%s user=%s op=%s method=%s level=%s status=%s cost=%sms",
                    id, event.getTraceId(), event.getUsername(), event.getOperation(),
                    event.getMethod(), event.getLevel(), event.getStatus(), event.getExecutionTime()));

            // ==================== 方案二：Kafka异步写入（已注释，需要时启用） ====================
            // 说明：
            // 1. 使用Kafka异步写入可以进一步提升性能，不阻塞请求线程
            // 2. 需要配置Kafka消费者来消费消息并写入数据库
            // 3. 启用步骤：
            // a. 取消注释下面的代码
            // b. 在构造函数中注入 KafkaTemplate<String, String> kafkaTemplate
            // c. 创建Kafka消费者服务来消费消息并写入数据库
            // d. 配置Kafka主题和消费者组
            //
            // var eventJson = JsonUtil.toJson(event);
            // if (eventJson != null) {
            // KafkaUtil.send(kafkaTemplate, OPERATION_LOG_TOPIC, eventJson);
            // log.debug(String.format("[OperationLog] sent to kafka: traceId=%s, op=%s",
            // event.getTraceId(), event.getOperation()));
            // }

        } catch (Exception e) {
            log.error(String.format("[OperationLog] save failed traceId=%s op=%s err=%s",
                    event.getTraceId(), event.getOperation(), e.getMessage()), e);
        }
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
