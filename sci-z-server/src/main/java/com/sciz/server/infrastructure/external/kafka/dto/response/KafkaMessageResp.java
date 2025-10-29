package com.sciz.server.infrastructure.external.kafka.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * Kafka消息响应DTO
 *
 * @author JiaWen.Wu
 * @className KafkaMessageResp
 * @date 2025-10-29 10:30
 */
@Data
public class KafkaMessageResp {

    /**
     * 消息ID
     */
    private String messageId;

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
     * 时间戳
     */
    private Long timestamp;

    /**
     * 发送时间
     */
    private LocalDateTime sendTime;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 重试次数
     */
    private Integer retryCount = 0;
}
