package com.sciz.server.domain.pojo.dto.response.project;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 项目详情响应
 *
 * @author JiaWen.Wu
 * @className ProjectDetailResp
 * @date 2025-11-24 16:00
 */
@Getter
@Setter
public class ProjectDetailResp {

    /**
     * 项目ID
     */
    private Long id;

    /**
     * 项目编号
     */
    private String number;

    /**
     * 项目名称
     */
    private String name;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 关联申报ID
     */
    private Long declarationId;

    /**
     * 项目预算
     */
    private BigDecimal budget;

    /**
     * 进度百分比
     */
    private Integer progress;

    /**
     * 项目状态
     */
    private String status;

    /**
     * Dify知识库ID
     */
    private String difyKnowledgeId;

    /**
     * 创建人ID
     */
    private Long createdBy;

    /**
     * 更新人ID
     */
    private Long updatedBy;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}
