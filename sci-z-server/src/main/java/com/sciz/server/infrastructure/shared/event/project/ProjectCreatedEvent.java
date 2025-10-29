package com.sciz.server.infrastructure.shared.event.project;

import com.sciz.server.infrastructure.shared.event.DomainEvent;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 项目创建事件
 *
 * @author JiaWen.Wu
 * @className ProjectCreatedEvent
 * @date 2025-10-29 10:30
 */
@Getter
@Setter
public class ProjectCreatedEvent extends DomainEvent {

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String projectDescription;

    /**
     * 创建者ID
     */
    private String creatorId;

    /**
     * 创建者姓名
     */
    private String creatorName;

    /**
     * 项目类型
     */
    private String projectType;

    /**
     * 项目状态
     */
    private String projectStatus;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 构造函数
     */
    public ProjectCreatedEvent() {
        super();
        this.createdAt = LocalDateTime.now();
    }

    /**
     * 构造函数
     *
     * @param projectId   项目ID
     * @param projectName 项目名称
     * @param creatorId   创建者ID
     */
    public ProjectCreatedEvent(String projectId, String projectName, String creatorId) {
        this();
        this.projectId = projectId;
        this.projectName = projectName;
        this.creatorId = creatorId;
    }

    /**
     * 构造函数
     *
     * @param projectId          项目ID
     * @param projectName        项目名称
     * @param projectDescription 项目描述
     * @param creatorId          创建者ID
     * @param creatorName        创建者姓名
     * @param projectType        项目类型
     * @param projectStatus      项目状态
     */
    public ProjectCreatedEvent(String projectId, String projectName, String projectDescription,
            String creatorId, String creatorName, String projectType, String projectStatus) {
        this(projectId, projectName, creatorId);
        this.projectDescription = projectDescription;
        this.creatorName = creatorName;
        this.projectType = projectType;
        this.projectStatus = projectStatus;
    }

    /**
     * 获取聚合根ID
     *
     * @return 聚合根ID
     */
    public String getAggregateId() {
        return projectId;
    }

    /**
     * 获取聚合根类型
     *
     * @return 聚合根类型
     */
    public String getAggregateType() {
        return "Project";
    }
}
