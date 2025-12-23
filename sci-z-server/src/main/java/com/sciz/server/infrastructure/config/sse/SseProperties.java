package com.sciz.server.infrastructure.config.sse;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * SSE 配置属性
 *
 * @author JiaWen.Wu
 * @className SseProperties
 * @date 2025-12-22 14:30
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sciz.sse")
public class SseProperties {

    /**
     * 知识库对话超时时间（毫秒），默认 30 秒
     */
    private Long knowledgeTimeout = 30000L;

    /**
     * 工作流对话超时时间（毫秒），默认 60 秒
     */
    private Long workflowTimeout = 60000L;

    /**
     * 心跳间隔（秒），默认 15 秒
     */
    private Integer heartbeatInterval = 15;
}
