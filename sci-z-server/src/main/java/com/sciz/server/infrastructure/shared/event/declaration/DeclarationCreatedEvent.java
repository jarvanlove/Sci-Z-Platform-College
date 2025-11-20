package com.sciz.server.infrastructure.shared.event.declaration;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报创建事件
 * <p>
 * 当用户创建新的申报记录时触发此事件，用于通知其他模块进行相应的处理。
 *
 * @author JiaWen.Wu
 * @className DeclarationCreatedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class DeclarationCreatedEvent extends DomainEvent {

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
     * 申报状态
     */
    private String status;
    /**
     * 申报描述
     */
    private String description;

    public DeclarationCreatedEvent(String declarationId, String declarationName,
            String applicantId, String applicantName, String status, String description) {
        super();
        this.declarationId = declarationId;
        this.declarationName = declarationName;
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
