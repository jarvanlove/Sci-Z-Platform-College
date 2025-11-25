package com.sciz.server.domain.pojo.dto.response.report;

import lombok.Getter;
import lombok.Setter;

/**
 * 报告类型响应
 *
 * @author JiaWen.Wu
 * @className ReportTypeResp
 * @date 2025-01-24 18:00
 */
@Getter
@Setter
public class ReportTypeResp {
    /**
     * 密钥ID
     */
    private Long id;

    /**
     * 资源ID（工作流ID）
     */
    private String resourceId;

    /**
     * 密钥名称（报告类型名称）
     */
    private String keyName;

    /**
     * 密钥描述
     */
    private String description;
}

