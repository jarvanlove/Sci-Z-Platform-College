package com.sciz.server.infrastructure.shared.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * 领域事件发布器
 * 负责发布领域事件到Spring事件总线
 *
 * @author JiaWen.Wu
 * @className EventPublisher
 * @date 2025-10-29 10:00
 */
@Slf4j
@Component
public class EventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public EventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 发布领域事件
     *
     * @param event 领域事件
     */
    public void publish(DomainEvent event) {
        try {
            log.debug("发布领域事件: {} - {}", event.getEventType(), event.getEventId());
            applicationEventPublisher.publishEvent(event);
            log.info("领域事件发布成功: {} - {}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            log.error("领域事件发布失败: {} - {}", event.getEventType(), event.getEventId(), e);
            throw new RuntimeException("事件发布失败", e);
        }
    }

    /**
     * 异步发布领域事件
     *
     * @param event 领域事件
     */
    public void publishAsync(DomainEvent event) {
        try {
            log.debug("异步发布领域事件: {} - {}", event.getEventType(), event.getEventId());
            // 使用Spring的异步事件发布
            applicationEventPublisher.publishEvent(event);
            log.info("领域事件异步发布成功: {} - {}", event.getEventType(), event.getEventId());
        } catch (Exception e) {
            // 异步发布失败不抛出异常，只记录日志
            log.error("领域事件异步发布失败: {} - {}", event.getEventType(), event.getEventId(), e);
        }
    }
}
