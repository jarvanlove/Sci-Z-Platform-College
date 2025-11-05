package com.server.agentbackendservices.modules.dify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify API 配置类
 * 用于配置 Dify API 的基础信息
 * 
 * @author shihang.shang
 * @since 2024-10-22
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dify")
public class DifyConfig {
    /**
     * Dify API 基础URL·
     */
    private String baseUrl;
    /**
     * API Key (知识库)
     */
    private String apiKey;
    
    /**
     * 工作流 API Key
     */
    private String apiWorkflowKey;
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout;
    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout;
    /**
     * 重试次数
     */
    private Integer retryCount;
    
    /**
     * 是否启用重试
     */
    private Boolean enableRetry;
    
    /**
     * 文档上传配置
     */
    private Document document;
    
    /**
     * 文件上传配置
     */
    private Upload upload;
    
    @Data
    public static class Document {
        private String indexingTechnique;
        private String docForm;
        private String docLanguage;
        private String embeddingModel;
        private String embeddingModelProvider;
        private String searchMethod;
        private Boolean rerankingEnable;
        private Integer topK;
        private Double scoreThreshold;
        private String processMode;
        private Integer maxTokens;
    }
    @Data
    public static class Upload {
        private String dir;
        private Long maxFileSize;
        private String allowedExtensions;
    }
}