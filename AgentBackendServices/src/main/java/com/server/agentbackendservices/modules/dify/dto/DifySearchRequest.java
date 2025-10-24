package com.server.agentbackendservices.modules.dify.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.NotBlank;

/**
 * Dify 搜索请求DTO
 * 根据 Dify API 文档设计
 * 
 * @author jarvanlove
 * @since 2024-10-22
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DifySearchRequest {
    
    /**
     * 搜索查询
     */
    @NotBlank(message = "搜索查询不能为空")
    private String query;
    
    /**
     * 返回结果数量
     */
    @Builder.Default
    private Integer top_k = 5;
    
    /**
     * 搜索方法
     */
    @Builder.Default
    private String search_method = "hybrid_search";
    
    /**
     * 是否启用重排序
     */
    @Builder.Default
    private Boolean reranking_enable = true;
    
    /**
     * 重排序模式
     */
    @Builder.Default
    private String reranking_mode = "reranking_model";
    
    /**
     * 重排序模型
     */
    private RerankingModel reranking_model;
    
    /**
     * 权重设置
     */
    private Weights weights;
    
    /**
     * 是否启用分数阈值
     */
    @Builder.Default
    private Boolean score_threshold_enabled = false;
    
    /**
     * 分数阈值
     */
    @Builder.Default
    private Double score_threshold = 0.0;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RerankingModel {
        private String reranking_provider_name;
        private String reranking_model_name;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Weights {
        @Builder.Default
        private String weight_type = "customized";
        
        private KeywordSetting keyword_setting;
        private VectorSetting vector_setting;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class KeywordSetting {
            @Builder.Default
            private Double keyword_weight = 0.3;
        }
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class VectorSetting {
            @Builder.Default
            private Double vector_weight = 0.7;
            private String embedding_model_name;
            private String embedding_provider_name;
        }
    }
}
