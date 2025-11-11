package com.sciz.server.infrastructure.external.dify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Dify 工作流日志查询请求 DTO
 *
 * @author shihang.shang
 * @className DifyWorkflowLogsRequest
 * @date 2025-01-28 15:30
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "Dify 工作流日志查询请求参数")
public class DifyWorkflowLogsRequest extends BaseDifyRequest {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer limit = 20;
}
