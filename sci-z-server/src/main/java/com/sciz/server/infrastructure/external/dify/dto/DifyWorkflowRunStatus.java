package com.sciz.server.infrastructure.external.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Map;

/**
 * Dify 工作流运行状态 DTO
 *
 * @author shihang.shang
 * @className DifyWorkflowRunStatus
 * @date 2025-01-28 11:00
 */
@Data
@Schema(description = "Dify 工作流运行状态")
public class DifyWorkflowRunStatus {

    @Schema(description = "工作流运行ID")
    private String id;

    @Schema(description = "工作流ID")
    private String workflowId;

    @Schema(description = "运行状态", example = "running", allowableValues = {"running", "succeeded", "failed", "stopped"})
    private String status;

    @Schema(description = "输入参数")
    private String inputs;

    @Schema(description = "输出结果")
    private Map<String, Object> outputs;

    @Schema(description = "错误信息")
    private String error;

    @Schema(description = "总步骤数")
    private Integer totalSteps;

    @Schema(description = "总Token数")
    private Integer totalTokens;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "完成时间")
    private Long finishedAt;

    @Schema(description = "执行时间（秒）")
    private Double elapsedTime;
}
