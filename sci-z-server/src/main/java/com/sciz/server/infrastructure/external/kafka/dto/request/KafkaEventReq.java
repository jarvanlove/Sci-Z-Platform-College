package com.sciz.server.infrastructure.external.kafka.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;

/**
 * Kafka事件请求DTO
 *
 * @author JiaWen.Wu
 * @className KafkaEventReq
 * @date 2025-10-29 10:30
 */
@Data
public class KafkaEventReq {

    /**
     * 事件类型
     */
    @NotBlank(message = "事件类型不能为空")
    private String eventType;

    /**
     * 事件数据
     */
    @NotBlank(message = "事件数据不能为空")
    private String eventData;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 聚合根类型
     */
    private String aggregateType;

    /**
     * 事件版本
     */
    private Integer version = 1;

    /**
     * 事件时间戳
     */
    private Long eventTimestamp;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 租户ID
     */
    private String tenantId;

    /**
     * 事件属性
     */
    private Map<String, Object> properties;

    /**
     * 关联事件ID
     */
    private String correlationId;
}
