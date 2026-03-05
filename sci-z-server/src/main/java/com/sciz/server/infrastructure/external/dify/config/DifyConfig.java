package com.sciz.server.infrastructure.external.dify.config;

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
     * 私钥
     */
    private String privateUrl;
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
     * Chatbot 模型配置
     */
    private Chatbot chatbot;

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
    public static class Chatbot {
        /**
         * 预提示词
         */
        private String prePrompt;

        /**
         * 提示词类型
         */
        private String promptType;

        /**
         * 开场白
         */
        private String openingStatement;

        /**
         * 建议问题列表
         */
        private java.util.List<String> suggestedQuestions;

        /**
         * 数据集查询变量
         */
        private String datasetQueryVariable;

        /**
         * 更多类似内容开关
         */
        private Boolean moreLikeThis;

        /**
         * 敏感词规避配置
         */
        private SensitiveWordAvoidance sensitiveWordAvoidance;

        /**
         * 语音转文字开关
         */
        private Boolean speechToText;

        /**
         * 文字转语音开关
         */
        private Boolean textToSpeech;

        /**
         * 文件上传配置
         */
        private FileUpload fileUpload;

        /**
         * 回答后建议问题开关
         */
        private Boolean suggestedQuestionsAfterAnswer;

        /**
         * 检索资源开关
         */
        private Boolean retrieverResource;

        /**
         * Agent模式配置
         */
        private AgentMode agentMode;

        /**
         * 模型配置
         */
        private Model model;

        /**
         * 重排序模型配置
         */
        private RerankingModel rerankingModel;

        /**
         * 数据集配置
         */
        private DatasetConfigs datasetConfigs;

        @Data
        public static class Model {
            /**
             * 模型提供商
             */
            private String provider;

            /**
             * 模型名称
             */
            private String name;

            /**
             * 模型模式（chat/completion）
             */
            private String mode;

            /**
             * 完成参数配置
             */
            private CompletionParams completionParams;

            @Data
            public static class CompletionParams {
                /**
                 * 是否启用搜索
                 */
                private Boolean enableSearch;
            }
        }

        @Data
        public static class RerankingModel {
            /**
             * 重排序提供商名称
             */
            private String rerankingProviderName;

            /**
             * 重排序模型名称
             */
            private String rerankingModelName;
        }

        @Data
        public static class SensitiveWordAvoidance {
            /**
             * 是否启用
             */
            private Boolean enabled;

            /**
             * 类型
             */
            private String type;

            /**
             * 配置列表
             */
            private java.util.List<Object> configs;
        }

        @Data
        public static class FileUpload {
            /**
             * 是否启用
             */
            private Boolean enabled;

            /**
             * 允许的文件类型列表
             */
            private java.util.List<String> allowedFileTypes;

            /**
             * 允许的文件扩展名列表
             */
            private java.util.List<String> allowedFileExtensions;

            /**
             * 允许的文件上传方法列表
             */
            private java.util.List<String> allowedFileUploadMethods;

            /**
             * 数量限制
             */
            private Integer numberLimits;

            /**
             * 图片配置
             */
            private Image image;

            @Data
            public static class Image {
                /**
                 * 是否启用
                 */
                private Boolean enabled;

                /**
                 * 详情级别
                 */
                private String detail;

                /**
                 * 数量限制
                 */
                private Integer numberLimits;

                /**
                 * 传输方法列表
                 */
                private java.util.List<String> transferMethods;
            }
        }

        @Data
        public static class AgentMode {
            /**
             * 是否启用
             */
            private Boolean enabled;

            /**
             * 最大迭代次数
             */
            private Integer maxIteration;

            /**
             * 策略
             */
            private String strategy;

            /**
             * 工具列表
             */
            private java.util.List<Object> tools;
        }

        @Data
        public static class DatasetConfigs {
            /**
             * 检索模型
             */
            private String retrievalModel;

            /**
             * Top K
             */
            private Integer topK;

            /**
             * 重排序模式：weight 权重融合，reranking_model 使用 Rerank 模型。默认 weight。
             */
            private String rerankingMode;

            /**
             * 重排序模型配置（reranking_mode=reranking_model 时使用）
             */
            private RerankingModel rerankingModel;

            /**
             * 是否启用重排序
             */
            private Boolean rerankingEnable;

            /**
             * 关键词权重（权重模式），行业常用 0.3，与 vectorWeight 之和建议 1.0
             */
            private Double keywordWeight;

            /**
             * 向量语义权重（权重模式），行业常用 0.7
             */
            private Double vectorWeight;

            /**
             * 数据集集合
             */
            private DatasetCollection datasets;

            @Data
            public static class DatasetCollection {
                /**
                 * 数据集列表
                 */
                private java.util.List<DatasetWrapper> datasets;

                @Data
                public static class DatasetWrapper {
                    /**
                     * 数据集配置
                     */
                    private Dataset dataset;

                    @Data
                    public static class Dataset {
                        /**
                         * 是否启用
                         */
                        private Boolean enabled;

                        /**
                         * 数据集ID
                         */
                        private String id;
                    }
                }
            }
        }
    }
}