package com.sciz.server.domain.pojo.dto.response.report;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 报告管理列表响应
 *
 * @author JiaWen.Wu
 * @className ReportManagementListResp
 * @date 2025-01-24 14:30
 */
@Getter
@Setter
public class ReportManagementListResp {

    /**
     * 报告ID
     */
    private Long id;

    /**
     * 报告编号
     */
    private String number;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目编号
     */
    private String projectCode;

    /**
     * 报告类型(tech/self)
     */
    private String reportType;

    /**
     * Dify API Keys 表 ID
     */
    private String difyApiKeysId;

    /**
     * 创建人ID
     */
    private Long creatorId;

    /**
     * 创建人姓名
     */
    private String creatorName;

    /**
     * 报告摘要
     */
    private String summary;

    /**
     * 状态
     */
    private String status;

    /**
     * 生成完成时间
     */
    private LocalDateTime generateTime;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * MinIO 附件 ID
     */
    private Long attachmentId;
}

