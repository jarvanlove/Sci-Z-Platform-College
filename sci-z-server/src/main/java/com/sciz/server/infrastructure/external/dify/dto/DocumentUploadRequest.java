package com.sciz.server.infrastructure.external.dify.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文档上传请求DTO
 * 对应Python中的data参数
 * 
 * @author shihang.shang
 * @since 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {
    
    /**
     * 上传的文件
     */
    private MultipartFile file;
    
    /**
     * 索引技术：high_quality 语义索引，利于召回与准确率
     */
    @Builder.Default
    private String indexingTechnique = "high_quality";

    /**
     * 文档形式
     */
    @Builder.Default
    private String docForm = "text_model";

    /**
     * 文档语言
     */
    @Builder.Default
    private String docLanguage = "Chinese";

    /**
     * 嵌入模型：通义 text-embedding-v4
     */
    @Builder.Default
    private String embeddingModel = "text-embedding-v4";

    /**
     * 嵌入模型提供商
     */
    @Builder.Default
    private String embeddingModelProvider = "tongyi";

    /**
     * 检索模型（未传时按行业默认：混合检索 + 权重融合 + Top K 6）
     */
    private RetrievalModel retrievalModel;

    /**
     * 处理规则
     */
    private ProcessRule processRule;

    /**
     * 检索模型内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RetrievalModel {
        /**
         * 搜索方法：hybrid_search 向量+全文，提升召回
         */
        @Builder.Default
        private String searchMethod = "hybrid_search";

        /**
         * 重排序模式：weight 权重融合，reranking_model 使用 Rerank 模型。默认 weight。
         */
        @Builder.Default
        private String rerankingMode = "weight";

        /**
         * 是否启用重排序（合并/排序步骤）
         */
        @Builder.Default
        private Boolean rerankingEnable = true;

        /**
         * 返回结果数量：6 为常用平衡值
         */
        @Builder.Default
        private Integer topK = 6;

        /**
         * 是否启用分数阈值：默认关闭，避免漏召
         */
        @Builder.Default
        private Boolean scoreThresholdEnabled = false;

        /**
         * 分数阈值（启用时），常用 0.5
         */
        @Builder.Default
        private Double scoreThreshold = 0.5;

        /** 关键词权重（权重模式），行业常用 0.3 */
        @Builder.Default
        private Double keywordWeight = 0.3;
        /** 向量语义权重（权重模式），行业常用 0.7 */
        @Builder.Default
        private Double vectorWeight = 0.7;
    }
    
    /**
     * 处理规则内部类
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProcessRule {
        /**
         * 处理模式
         */
        @Builder.Default
        private String mode = "automatic";
        
        /**
         * 处理规则
         */
        private Rules rules;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Rules {
            /**
             * 预处理规则
             */
            private PreProcessingRules preProcessingRules;
            
            /**
             * 分段规则
             */
            private Segmentation segmentation;
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class PreProcessingRules {
                @Builder.Default
                private Boolean removeExtraSpaces = true;
                
                @Builder.Default
                private Boolean removeUrlsEmails = false;
            }
            
            @Data
            @Builder
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Segmentation {
                @Builder.Default
                private String separator = "\\n";
                
                @Builder.Default
                private Integer maxTokens = 1000;
            }
        }
    }
}
