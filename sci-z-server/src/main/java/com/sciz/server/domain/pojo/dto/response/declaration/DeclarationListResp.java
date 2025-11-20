package com.sciz.server.domain.pojo.dto.response.declaration;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 申报列表响应
 *
 * @author JiaWen.Wu
 * @className DeclarationListResp
 * @date 2025-01-20 15:00
 */
@Getter
@Setter
public class DeclarationListResp {

    /**
     * 申报ID
     */
    private Long id;

    /**
     * 申报编号
     */
    private String number;

    /**
     * 申报人姓名
     */
    private String applicantName;

    /**
     * 研究课题
     */
    private String researchTopic;

    /**
     * 研究方向（摘要，前100字符）
     */
    private String researchDirection;

    /**
     * 研究领域（JSON数组）
     */
    private List<String> researchFields;

    /**
     * 申报状态
     */
    private Integer status;

    /**
     * 申报状态描述
     */
    private String statusDescription;

    /**
     * 工作流状态
     */
    private String workflowStatus;

    /**
     * 工作流状态描述
     */
    private String workflowStatusDescription;

    /**
     * 提交时间
     */
    private LocalDateTime submitTime;

    /**
     * 是否有附件（申报书）- 用于控制下载按钮显示
     */
    private Boolean hasAttachment;
}
