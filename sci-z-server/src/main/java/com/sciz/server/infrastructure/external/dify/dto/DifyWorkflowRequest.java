package com.sciz.server.infrastructure.external.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * Dify Workflow 请求 DTO
 *
 * @author shihang.shang
 * @className DifyWorkflowRequest
 * @date 2025-01-28 10:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dify Workflow 请求参数")
public class DifyWorkflowRequest extends BaseDifyRequest {

    @Schema(description = "工作流输入参数", required = true)
    @NotNull(message = "输入参数不能为null")
    @NotEmpty(message = "输入参数不能为空")
    private Map<String, Object> inputs;

    @Schema(description = "响应模式", example = "blocking", allowableValues = {"blocking", "streaming"})
    @com.fasterxml.jackson.annotation.JsonProperty("response_mode")
    private String responseMode = "blocking";

    @Schema(description = "用户标识", example = "workflow_user_001")
    private String user = "admin";

    @Schema(description = "工作流ID（可选，如果不指定则使用API Token绑定的默认工作流）")
    private String workflowId;
}
