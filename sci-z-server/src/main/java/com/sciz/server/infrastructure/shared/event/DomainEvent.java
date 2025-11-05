package com.sciz.server.infrastructure.shared.event;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * @author JiaWen.Wu
 * @className DomainEvent
 * @date 2025-10-29 10:00
 */
@Data
public abstract class DomainEvent {

    /**
     * 事件唯一标识
     */
    private String eventId;

    /**
     * 事件发生时间
     */
    private LocalDateTime occurredOn;

    /**
     * 事件类型（类名）
     */
    private String eventType;

    public DomainEvent() {
        this.eventId = java.util.UUID.randomUUID().toString();
        this.occurredOn = LocalDateTime.now();
        this.eventType = this.getClass().getSimpleName();
    }

    /**
     * 获取聚合根ID
     *
     * @return 聚合根ID
     */
    public abstract String getAggregateId();

    /**
     * 获取聚合根类型
     *
     * @return 聚合根类型
     */
    public abstract String getAggregateType();
}
