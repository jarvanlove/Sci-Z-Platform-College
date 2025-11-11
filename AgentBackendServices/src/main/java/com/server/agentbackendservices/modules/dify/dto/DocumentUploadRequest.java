package com.server.agentbackendservices.modules.dify.dto;

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
     * 索引技术
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
     * 嵌入模型
     */
    @Builder.Default
    private String embeddingModel = "text-embedding-v3";
    
    /**
     * 嵌入模型提供商
     */
    @Builder.Default
    private String embeddingModelProvider = "tongyi";
    
    /**
     * 检索模型
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
         * 搜索方法
         */
        @Builder.Default
        private String searchMethod = "hybrid_search";
        
        /**
         * 是否启用重排序
         */
        @Builder.Default
        private Boolean rerankingEnable = false;
        
        /**
         * 返回结果数量
         */
        @Builder.Default
        private Integer topK = 3;
        
        /**
         * 是否启用分数阈值
         */
        @Builder.Default
        private Boolean scoreThresholdEnabled = false;
        
        /**
         * 分数阈值
         */
        @Builder.Default
        private Double scoreThreshold = 0.5;
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
