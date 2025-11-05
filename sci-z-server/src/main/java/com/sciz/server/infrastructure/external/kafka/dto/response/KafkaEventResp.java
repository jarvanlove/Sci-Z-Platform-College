package com.sciz.server.infrastructure.external.kafka.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Kafka事件响应DTO
 *
 * @author JiaWen.Wu
 * @className KafkaEventResp
 * @date 2025-10-29 10:30
 */
@Data
public class KafkaEventResp {

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 聚合根ID
     */
    private String aggregateId;

    /**
     * 事件版本
     */
    private Integer version;

    /**
     * 主题名称
     */
    private String topic;

    /**
     * 分区
     */
    private Integer partition;

    /**
     * 偏移量
     */
    private Long offset;

    /**
     * 处理时间
     */
    private LocalDateTime processTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 处理状态
     */
    private String status;
}
