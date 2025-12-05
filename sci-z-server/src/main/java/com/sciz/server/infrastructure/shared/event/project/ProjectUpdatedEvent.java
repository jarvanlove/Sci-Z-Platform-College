package com.sciz.server.infrastructure.shared.event.project;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 项目更新事件
 * <p>
 * 当项目更新时触发此事件，用于异步处理待关联附件的更新
 *
 * @author JiaWen.Wu
 * @className ProjectUpdatedEvent
 * @date 2025-12-04 16:30
 */
@Getter
@Setter
public class ProjectUpdatedEvent extends DomainEvent {

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 新增的里程碑列表（包含里程碑ID和名称）
     */
    private List<MilestoneInfo> newMilestones;

    /**
     * 里程碑信息
     */
    @Getter
    @Setter
    public static class MilestoneInfo {
        /**
         * 里程碑ID
         */
        private Long milestoneId;

        /**
         * 里程碑名称
         */
        private String milestoneName;

        public MilestoneInfo(Long milestoneId, String milestoneName) {
            this.milestoneId = milestoneId;
            this.milestoneName = milestoneName;
        }
    }

    public ProjectUpdatedEvent(Long projectId, String projectName, Long operatorId, String operatorName,
            List<MilestoneInfo> newMilestones) {
        super();
        this.projectId = projectId;
        this.projectName = projectName;
        this.operatorId = operatorId;
        this.operatorName = operatorName;
        this.newMilestones = newMilestones != null ? newMilestones : List.of();
    }

    @Override
    public String getAggregateId() {
        return String.valueOf(projectId);
    }

    @Override
    public String getAggregateType() {
        return "Project";
    }
}
