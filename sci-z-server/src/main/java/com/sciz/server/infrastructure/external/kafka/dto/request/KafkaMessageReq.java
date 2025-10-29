package com.sciz.server.infrastructure.external.kafka.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Kafka消息请求DTO
 *
 * @author JiaWen.Wu
 * @className KafkaMessageReq
 * @date 2025-10-29 10:30
 */
@Data
public class KafkaMessageReq {

    /**
     * 主题名称
     */
    @NotBlank(message = "主题名称不能为空")
    private String topic;

    /**
     * 消息键
     */
    private String key;

    /**
     * 消息内容
     */
    @NotBlank(message = "消息内容不能为空")
    private String message;

    /**
     * 分区
     */
    private Integer partition;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 消息头
     */
    private Map<String, String> headers;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 消息类型
     */
    private String messageType;

    /**
     * 优先级
     */
    private Integer priority = 0;
}
