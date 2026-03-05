package com.sciz.server.infrastructure.external.dify.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Dify 文档处理配置类
 * 用于配置文档上传时的处理参数。
 * 默认值按 RAG 行业实践设置，兼顾召回率与准确率。
 *
 * @author shihang.shang
 * @since 2024-10-24
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dify.document")
public class DifyDocumentConfig {

    /**
     * 索引技术：high_quality 使用嵌入模型做语义索引，召回与准确率优于 economy
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
     * 嵌入模型：通义 text-embedding-v4，行业常用高质向量模型
     */
    private String embeddingModel = "text-embedding-v4";

    /**
     * 嵌入模型提供商
     */
    private String embeddingModelProvider = "tongyi";

    /**
     * 检索模型配置（混合检索 + 权重融合/重排 + Top K 6，利于召回与排序）
     */
    private RetrievalModel retrievalModel = new RetrievalModel();

    /**
     * 处理规则配置
     */
    private ProcessRule processRule = new ProcessRule();

    @Data
    public static class RetrievalModel {
        /**
         * 搜索方法：hybrid_search 向量+全文，召回率优于纯向量
         */
        private String searchMethod = "hybrid_search";

        /**
         * 重排序模式：weight 权重融合（BM25+向量线性加权），reranking_model 使用 Rerank 模型。
         * 默认 weight，延迟低、无额外模型调用；若需更高语义精度可改为 reranking_model 并配置 Rerank 模型。
         */
        private String rerankingMode = "weight";

        /**
         * 是否启用重排序（合并/排序步骤）：true 时按 rerankingMode 做权重融合或 Rerank 精排
         */
        private Boolean rerankingEnable = true;

        /**
         * 返回结果数量：5～6 为常见平衡值，兼顾召回与噪声
         */
        private Integer topK = 6;

        /**
         * 是否启用分数阈值：默认关闭，避免过度过滤导致漏召
         */
        private Boolean scoreThresholdEnabled = false;

        /**
         * 分数阈值（启用时生效），常用 0.5
         */
        private Double scoreThreshold = 0.5;

        // ---------- 权重模式（rerankingMode=weight 时生效）行业常用：关键词 0.3 / 语义 0.7 ----------
        /**
         * 关键词权重（BM25），与 vectorWeight 之和建议为 1.0
         */
        private Double keywordWeight = 0.3;
        /**
         * 向量语义权重，与 keywordWeight 之和建议为 1.0
         */
        private Double vectorWeight = 0.7;

        // ---------- 以下为 Rerank 模型模式（rerankingMode=reranking_model 时使用），当前默认已改为 weight，保留供切换 ----------
        // private String rerankingModeRerank = "reranking_model";
        // private String rerankingProviderName = "langgenius/tongyi/tongyi";
        // private String rerankingModelName = "gte-rerank";
    }
    
    @Data
    public static class ProcessRule {
        /**
         * 处理模式
         */
        private String mode = "automatic";
    }
}
