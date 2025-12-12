package com.sciz.server.infrastructure.external.dify.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Dify 工作流发布请求
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowPublishReq
 * @date 2025-12-08 16:00
 */
@Data
@Schema(description = "Dify 工作流发布请求")
public class DifyWorkflowPublishReq {

    /**
     * 工作流配置的 hash 值（更新后的 hash）
     */
    @Schema(description = "工作流配置的 hash 值（更新后的 hash）", example = "a9deebdcb0f4c5eea6d4213355a4758ee1260aa533560adbb404ab67f222e5ad")
    @JsonProperty("hash")
    private String hash;

    /**
     * 标记名称
     */
    @Schema(description = "标记名称", example = "")
    @JsonProperty("marked_name")
    private String markedName;

    /**
     * 标记注释
     */
    @Schema(description = "标记注释", example = "")
    @JsonProperty("marked_comment")
    private String markedComment;
}

