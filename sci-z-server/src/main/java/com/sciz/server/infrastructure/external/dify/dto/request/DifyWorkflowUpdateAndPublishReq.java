package com.sciz.server.infrastructure.external.dify.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * Dify 工作流更新并发布请求
 * 综合接口：先获取工作流详情，然后更新，最后发布
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowUpdateAndPublishReq
 * @date 2025-12-08 16:00
 */
@Data
@Schema(description = "Dify 工作流更新并发布请求")
public class DifyWorkflowUpdateAndPublishReq {

    /**
     * 工作流ID（Dify 应用ID）
     */
    @Schema(description = "工作流ID（Dify 应用ID）", example = "158014eb-6b53-417e-898c-e5feba1de72e")
    @NotBlank(message = "工作流ID不能为空")
    private String appId;

    /**
     * 新的知识库ID列表（用于更新 knowledge-retrieval 节点的 dataset_ids）
     */
    @Schema(description = "新的知识库ID列表（用于更新 knowledge-retrieval 节点的 dataset_ids）", example = "[\"6866e4ef-91ba-492c-a91b-5a76dabea0f9\"]")
    @NotEmpty(message = "知识库ID列表不能为空")
    private List<String> datasetIds;

    /**
     * 标记名称（发布时使用）
     */
    @Schema(description = "标记名称（发布时使用）", example = "")
    private String markedName;

    /**
     * 标记注释（发布时使用）
     */
    @Schema(description = "标记注释（发布时使用）", example = "")
    private String markedComment;
}

