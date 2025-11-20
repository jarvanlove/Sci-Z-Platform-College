package com.sciz.server.domain.pojo.dto.response.declaration;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 申报详情响应
 *
 * @author JiaWen.Wu
 * @className DeclarationDetailResp
 * @date 2025-01-20 15:00
 */
@Getter
@Setter
public class DeclarationDetailResp {

    /**
     * 申报ID
     */
    private Long id;

    /**
     * 申报编号
     */
    private String number;

    /**
     * 申报人ID
     */
    private Long applicantId;

    /**
     * 申报人姓名
     */
    private String applicantName;

    /**
     * 课题发布部门
     */
    private String department;

    /**
     * 项目负责人
     */
    private String projectLeader;

    /**
     * 红头文件发布时间
     */
    private LocalDate documentPublishTime;

    /**
     * 项目开始时间
     */
    private LocalDate projectStartTime;

    /**
     * 项目结束时间
     */
    private LocalDate projectEndTime;

    /**
     * 研究方向（富文本）
     */
    private String researchDirection;

    /**
     * 研究领域（JSON数组）
     */
    private List<String> researchFields;

    /**
     * 研究课题
     */
    private String researchTopic;

    /**
     * 研究内容摘要
     */
    private String contentSummary;

    /**
     * 工作流ID
     */
    private String workflowId;

    /**
     * 工作流状态
     */
    private String workflowStatus;

    /**
     * 工作流结果（JSON，包含步骤信息）
     */
    private WorkflowResult workflowResult;

    /**
     * 申报状态
     */
    private Integer status;

    /**
     * 申报状态描述
     */
    private String statusDescription;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 是否有附件（申报书）
     */
    private Boolean hasAttachment;

    /**
     * 附件ID（如果有）
     */
    private Long attachmentId;

    /**
     * 附件URL（如果有）
     */
    private String attachmentUrl;

    /**
     * 工作流结果
     */
    @Getter
    @Setter
    public static class WorkflowResult {
        /**
         * 工作流步骤列表
         */
        private List<WorkflowStep> steps;

        /**
         * 文件URL（工作流返回的下载URL）
         */
        private String fileUrl;

        /**
         * 文件格式（pdf/docx）
         */
        private String fileFormat;
    }

    /**
     * 工作流步骤
     */
    @Getter
    @Setter
    public static class WorkflowStep {
        /**
         * 步骤名称
         */
        private String name;

        /**
         * 步骤状态（success/failed）
         */
        private String status;

        /**
         * 时间戳
         */
        private String timestamp;
    }
}
