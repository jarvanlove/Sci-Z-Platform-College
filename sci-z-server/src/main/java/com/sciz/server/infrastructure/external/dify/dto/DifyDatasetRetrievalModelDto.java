package com.sciz.server.infrastructure.external.dify.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Dify 知识库创建/更新请求中的 retrieval_model 结构
 * 对应 API 文档：POST /datasets、PATCH /datasets/{id} 的 retrieval_model 字段
 *
 * @author platform
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DifyDatasetRetrievalModelDto {

    @JsonProperty("search_method")
    private String searchMethod;

    @JsonProperty("reranking_enable")
    private Boolean rerankingEnable;

    @JsonProperty("reranking_mode")
    private String rerankingMode;

    @JsonProperty("top_k")
    private Integer topK;

    @JsonProperty("score_threshold_enabled")
    private Boolean scoreThresholdEnabled;

    @JsonProperty("score_threshold")
    private Double scoreThreshold;

    @JsonProperty("weights")
    private WeightsDto weights;

    @JsonProperty("reranking_model")
    private RerankingModelDto rerankingModel;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeightsDto {
        @JsonProperty("weight_type")
        private String weightType;

        @JsonProperty("keyword_setting")
        private KeywordSettingDto keywordSetting;

        @JsonProperty("vector_setting")
        private VectorSettingDto vectorSetting;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class KeywordSettingDto {
        @JsonProperty("keyword_weight")
        private Double keywordWeight;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class VectorSettingDto {
        @JsonProperty("vector_weight")
        private Double vectorWeight;

        @JsonProperty("embedding_model_name")
        private String embeddingModelName;

        @JsonProperty("embedding_provider_name")
        private String embeddingProviderName;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RerankingModelDto {
        @JsonProperty("reranking_provider_name")
        private String rerankingProviderName;

        @JsonProperty("reranking_model_name")
        private String rerankingModelName;
    }
}
