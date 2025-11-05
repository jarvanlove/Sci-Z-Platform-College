package com.sciz.server.infrastructure.shared.event.ai;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

/**
 * 工作流执行事件
 *
 * @author JiaWen.Wu
 * @className WorkflowExecutedEvent
 * @date 2025-10-29 11:30
 */
@Getter
@Setter
public class WorkflowExecutedEvent extends DomainEvent {

    private String workflowId;
    private String workflowName;
    private String userId;
    private String userName;
    private String projectId;
    private String projectName;
    private String executionStatus;
    private String startTime;
    private String endTime;
    private String executionResult;
    private String errorMessage;

    public WorkflowExecutedEvent(String workflowId, String workflowName, String userId,
            String userName, String projectId, String projectName,
            String executionStatus, String startTime, String endTime,
            String executionResult, String errorMessage) {
        super();
        this.workflowId = workflowId;
        this.workflowName = workflowName;
        this.userId = userId;
        this.userName = userName;
        this.projectId = projectId;
        this.projectName = projectName;
        this.executionStatus = executionStatus;
        this.startTime = startTime;
        this.endTime = endTime;
        this.executionResult = executionResult;
        this.errorMessage = errorMessage;
    }

    @Override
    public String getAggregateId() {
        return workflowId;
    }

    @Override
    public String getAggregateType() {
        return "Workflow";
    }
}
