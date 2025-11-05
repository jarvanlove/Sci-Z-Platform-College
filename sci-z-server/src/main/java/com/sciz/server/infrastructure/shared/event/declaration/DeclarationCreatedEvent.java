package com.sciz.server.infrastructure.shared.event.declaration;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报创建事件
 *
 * @author JiaWen.Wu
 * @className DeclarationCreatedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class DeclarationCreatedEvent extends DomainEvent {

    private String declarationId;
    private String declarationName;
    private String declarationType;
    private String applicantId;
    private String applicantName;
    private String status;
    private String description;

    public DeclarationCreatedEvent(String declarationId, String declarationName,
            String declarationType, String applicantId,
            String applicantName, String status, String description) {
        super();
        this.declarationId = declarationId;
        this.declarationName = declarationName;
        this.declarationType = declarationType;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.status = status;
        this.description = description;
    }

    @Override
    public String getAggregateId() {
        return declarationId;
    }

    @Override
    public String getAggregateType() {
        return "Declaration";
    }
}
