package com.sciz.server.infrastructure.shared.event.declaration;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报更新事件
 *
 * @author JiaWen.Wu
 * @className DeclarationUpdatedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class DeclarationUpdatedEvent extends DomainEvent {

    private String declarationId;
    private String declarationName;
    private String declarationType;
    private String applicantId;
    private String applicantName;
    private String oldStatus;
    private String newStatus;
    private String description;
    private String updateReason;

    public DeclarationUpdatedEvent(String declarationId, String declarationName,
            String declarationType, String applicantId,
            String applicantName, String oldStatus, String newStatus,
            String description, String updateReason) {
        super();
        this.declarationId = declarationId;
        this.declarationName = declarationName;
        this.declarationType = declarationType;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.description = description;
        this.updateReason = updateReason;
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
