package com.sciz.server.domain.pojo.entity.declaration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.time.LocalDateTime;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * 申报实体
 *
 * - 对应表：declaration
 *
 * @author JiaWen.Wu
 * @className Declaration
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("declaration")
public class Declaration extends BaseEntity {

    /**
     * 申报ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 申报编号
     */
    @TableField("number")
    private String number;

    /**
     * 申报人ID
     */
    @TableField("applicant_id")
    private Long applicantId;

    /**
     * 申报人姓名
     */
    @TableField("applicant_name")
    private String applicantName;

    /**
     * 课题发布部门
     */
    @TableField("department")
    private String department;

    /**
     * 项目负责人
     */
    @TableField("project_leader")
    private String projectLeader;

    /**
     * 红头文件发布时间
     */
    @TableField("document_publish_time")
    private LocalDate documentPublishTime;

    /**
     * 项目开始时间
     */
    @TableField("project_start_time")
    private LocalDate projectStartTime;

    /**
     * 项目结束时间
     */
    @TableField("project_end_time")
    private LocalDate projectEndTime;

    /**
     * 研究方向(富文本)
     */
    @TableField("research_direction")
    private String researchDirection;

    /**
     * 研究领域(JSON数组)
     */
    @TableField("research_fields")
    private String researchFields;

    /**
     * 研究课题
     */
    @TableField("research_topic")
    private String researchTopic;

    /**
     * 研究内容摘要
     */
    @TableField("content_summary")
    private String contentSummary;

    /**
     * 工作流ID
     */
    @TableField("workflow_id")
    private String workflowId;

    /**
     * 工作流状态
     */
    @TableField("workflow_status")
    private String workflowStatus;

    /**
     * 工作流结果(JSON)
     */
    @TableField("workflow_result")
    private String workflowResult;

    /**
     * 申报状态
     */
    @TableField("status")
    private String status;

    /**
     * 提交时间
     */
    @TableField("submit_time")
    private LocalDateTime submitTime;
}
