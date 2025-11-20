package com.sciz.server.infrastructure.shared.event.declaration;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报更新事件
 * <p>
 * 当申报记录的状态或信息发生变化时触发此事件，用于通知其他模块进行相应的处理。
 *
 * @author JiaWen.Wu
 * @className DeclarationUpdatedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class DeclarationUpdatedEvent extends DomainEvent {

    /**
     * 申报ID
     */
    private String declarationId;
    /**
     * 申报名称（研究课题）
     */
    private String declarationName;
    /**
     * 申报人ID
     */
    private String applicantId;
    /**
     * 申报人姓名
     */
    private String applicantName;
    /**
     * 旧状态
     */
    private String oldStatus;
    /**
     * 新状态
     */
    private String newStatus;
    /**
     * 更新描述
     */
    private String description;
    /**
     * 更新原因
     */
    private String updateReason;

    public DeclarationUpdatedEvent(String declarationId, String declarationName,
            String applicantId, String applicantName, String oldStatus, String newStatus,
            String description, String updateReason) {
        super();
        this.declarationId = declarationId;
        this.declarationName = declarationName;
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
