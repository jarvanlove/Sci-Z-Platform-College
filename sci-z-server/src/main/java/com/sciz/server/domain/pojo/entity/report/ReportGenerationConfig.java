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
 * 报告生成配置实体
 *
 * - 对应表：report_generation_config
 *
 * @author JiaWen.Wu
 * @className ReportGenerationConfig
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("report_generation_config")
public class ReportGenerationConfig extends BaseEntity {

    /**
     * 配置ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 报告ID
     */
    @TableField("report_id")
    private Long reportId;

    /**
     * 报告风格（formal/academic/concise）
     */
    @TableField("report_style")
    private String reportStyle;

    /**
     * 详细程度（brief/standard/detailed）
     */
    @TableField("detail_level")
    private String detailLevel;

    /**
     * 特殊要求
     */
    @TableField("special_requirements")
    private String specialRequirements;

    /**
     * 生成状态（pending/generating/success/failed）
     */
    @TableField("generate_status")
    private String generateStatus;

    /**
     * 生成进度（0-100）
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 预计剩余时间（秒）
     */
    @TableField("estimated_time")
    private Integer estimatedTime;

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * Dify任务ID
     */
    @TableField("dify_task_id")
    private String difyTaskId;

    /**
     * 开始生成时间
     */
    @TableField("generate_start_time")
    private LocalDateTime generateStartTime;

    /**
     * 生成完成时间
     */
    @TableField("generate_end_time")
    private LocalDateTime generateEndTime;
}
