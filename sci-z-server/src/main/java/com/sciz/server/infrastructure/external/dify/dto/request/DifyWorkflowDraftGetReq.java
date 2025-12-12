package com.sciz.server.infrastructure.external.dify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Dify 工作流 Draft 配置获取请求
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowDraftGetReq
 * @date 2025-12-08 15:30
 */
@Data
@Schema(description = "Dify 工作流 Draft 配置获取请求")
public class DifyWorkflowDraftGetReq {

    /**
     * 工作流ID（Dify 应用ID）
     */
    @Schema(description = "工作流ID（Dify 应用ID）", example = "158014eb-6b53-417e-898c-e5feba1de72e")
    @NotBlank(message = "工作流ID不能为空")
    private String appId;
}

