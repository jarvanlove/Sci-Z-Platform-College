package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Dify 数据集创建请求 DTO（POST /datasets）
 * 支持名称、描述、索引模式、Embedding 模型及检索策略，与配置（图2）一致。
 *
 * @author shihang.shang
 * @since 2024-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DifyDatasetRequest extends BaseDifyRequest {

    @NotBlank(message = "数据集名称不能为空")
    private String name;

    private String description;

    @Builder.Default
    private String permission = "only_me";

    /**
     * 索引技术：high_quality / economy
     */
    @JsonProperty("indexing_technique")
    @Builder.Default
    private String indexingTechnique = "high_quality";

    /**
     * 嵌入模型名称（如 bge-m3、text-embedding-v4）
     */
    @JsonProperty("embedding_model")
    private String embeddingModel;

    /**
     * 嵌入模型提供商
     */
    @JsonProperty("embedding_model_provider")
    private String embeddingModelProvider;

    /**
     * 检索模型配置（search_method、top_k、权重融合等），与 DifyDocumentConfig 一致
     */
    @JsonProperty("retrieval_model")
    private DifyDatasetRetrievalModelDto retrievalModel;
}
