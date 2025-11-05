package com.sciz.server.domain.pojo.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目进度实体
 *
 * - 对应表：project_progress
 *
 * @author JiaWen.Wu
 * @className ProjectProgress
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("project_progress")
public class ProjectProgress extends BaseEntity {

    /**
     * 进度ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 进度标题
     */
    @TableField("title")
    private String title;

    /**
     * 进度内容
     */
    @TableField("content")
    private String content;

    /**
     * 进度百分比
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 是否里程碑
     */
    @TableField("is_milestone")
    private Integer isMilestone;

    /**
     * 里程碑开始时间
     */
    @TableField("milestone_start_time")
    private java.time.LocalDate milestoneStartTime;

    /**
     * 里程碑结束时间
     */
    @TableField("milestone_end_time")
    private java.time.LocalDate milestoneEndTime;

    /**
     * 记录人ID
     */
    @TableField("recorder_id")
    private Long recorderId;

    /**
     * 记录人姓名
     */
    @TableField("recorder_name")
    private String recorderName;

    /**
     * 记录时间
     */
    @TableField("record_time")
    private LocalDateTime recordTime;
}
