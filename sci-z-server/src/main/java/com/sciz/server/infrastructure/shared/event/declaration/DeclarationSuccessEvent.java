package com.sciz.server.infrastructure.shared.event.declaration;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报成功事件
 * <p>
 * 当申报状态更新为"申报成功"时触发此事件，用于异步创建项目和知识库
 *
 * @author JiaWen.Wu
 * @className DeclarationSuccessEvent
 * @date 2025-11-27 17:30
 */
@Getter
@Setter
public class DeclarationSuccessEvent extends DomainEvent {

    /**
     * 申报ID
     */
    private Long declarationId;

    /**
     * 申报编号
     */
    private String declarationNumber;

    /**
     * 研究课题（用于项目名称和知识库名称）
     */
    private String researchTopic;

    /**
     * 申报人ID（用于创建项目和知识库）
     */
    private Long applicantId;

    /**
     * 申报人姓名
     */
    private String applicantName;

    /**
     * 操作人ID（更新申报状态的操作人，用于创建项目和知识库时的 createdBy/updatedBy）
     */
    private Long operatorId;

    public DeclarationSuccessEvent(Long declarationId, String declarationNumber,
            String researchTopic, Long applicantId, String applicantName, Long operatorId) {
        super();
        this.declarationId = declarationId;
        this.declarationNumber = declarationNumber;
        this.researchTopic = researchTopic;
        this.applicantId = applicantId;
        this.applicantName = applicantName;
        this.operatorId = operatorId;
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(declarationId);
    }

    @Override
    public String getAggregateType() {
        return "Declaration";
    }
}
