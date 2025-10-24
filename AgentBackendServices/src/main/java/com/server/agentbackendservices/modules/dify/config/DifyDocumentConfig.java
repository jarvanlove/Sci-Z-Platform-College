package com.server.agentbackendservices.modules.dify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify 文档处理配置类
 * 用于配置文档上传时的处理参数
 *
 * @author jarvanlove
 * @since 2024-10-24
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dify.document")
public class DifyDocumentConfig {
    
    /**
     * 索引技术
     */
    private String indexingTechnique = "high_quality";
    
    /**
     * 文档形式
     */
    private String docForm = "text_model";
    
    /**
     * 文档语言
     */
    private String docLanguage = "Chinese";
    
    /**
     * 嵌入模型
     */
    private String embeddingModel = "text-embedding-v3";
    
    /**
     * 嵌入模型提供商
     */
    private String embeddingModelProvider = "tongyi";
    
    /**
     * 检索模型配置
     */
    private RetrievalModel retrievalModel = new RetrievalModel();
    
    /**
     * 处理规则配置
     */
    private ProcessRule processRule = new ProcessRule();
    
    @Data
    public static class RetrievalModel {
        /**
         * 搜索方法
         */
        private String searchMethod = "hybrid_search";
        
        /**
         * 是否启用重排序
         */
        private Boolean rerankingEnable = false;
        
        /**
         * 返回结果数量
         */
        private Integer topK = 3;
        
        /**
         * 是否启用分数阈值
         */
        private Boolean scoreThresholdEnabled = false;
        
        /**
         * 分数阈值
         */
        private Double scoreThreshold = 0.5;
    }
    
    @Data
    public static class ProcessRule {
        /**
         * 处理模式
         */
        private String mode = "automatic";
    }
}
