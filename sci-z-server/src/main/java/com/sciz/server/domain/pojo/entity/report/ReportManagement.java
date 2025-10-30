package com.sciz.server.domain.pojo.entity.report;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 报告管理实体
 *
 * - 对应表：report_management
 *
 * @author JiaWen.Wu
 * @className ReportManagement
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("report_management")
public class ReportManagement extends BaseEntity {

    /**
     * 报告ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报告编号
     */
    @TableField("number")
    private String number;

    /**
     * 项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 项目名称
     */
    @TableField("project_name")
    private String projectName;

    /**
     * 项目编号
     */
    @TableField("project_code")
    private String projectCode;

    /**
     * 项目知识库ID
     */
    @TableField("project_knowledge_id")
    private String projectKnowledgeId;

    /**
     * 报告类型(tech/self)
     */
    @TableField("report_type")
    private String reportType;

    /**
     * 创建人ID
     */
    @TableField("creator_id")
    private Long creatorId;

    /**
     * 创建人姓名
     */
    @TableField("creator_name")
    private String creatorName;

    /**
     * 报告摘要
     */
    @TableField("summary")
    private String summary;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 生成完成时间
     */
    @TableField("generate_time")
    private LocalDateTime generateTime;
}
