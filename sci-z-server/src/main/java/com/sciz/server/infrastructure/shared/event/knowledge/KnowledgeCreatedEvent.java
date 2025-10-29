package com.sciz.server.infrastructure.shared.event.knowledge;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库创建事件
 *
 * @author JiaWen.Wu
 * @className KnowledgeCreatedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class KnowledgeCreatedEvent extends DomainEvent {

    private String knowledgeId;
    private String knowledgeName;
    private String knowledgeType;
    private String projectId;
    private String projectName;
    private String creatorId;
    private String creatorName;
    private String status;
    private String description;
    private String filePath;
    private String fileSize;

    public KnowledgeCreatedEvent(String knowledgeId, String knowledgeName, String knowledgeType,
            String projectId, String projectName, String creatorId,
            String creatorName, String status, String description,
            String filePath, String fileSize) {
        super();
        this.knowledgeId = knowledgeId;
        this.knowledgeName = knowledgeName;
        this.knowledgeType = knowledgeType;
        this.projectId = projectId;
        this.projectName = projectName;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.status = status;
        this.description = description;
        this.filePath = filePath;
        this.fileSize = fileSize;
    }

    @Override
    public String getAggregateId() {
        return knowledgeId;
    }

    @Override
    public String getAggregateType() {
        return "Knowledge";
    }
}
