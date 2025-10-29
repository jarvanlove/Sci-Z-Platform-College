package com.sciz.server.infrastructure.external.dify.dto.response;

import lombok.Data;
import java.util.List;

/**
 * Dify知识库响应DTO
 *
 * @author JiaWen.Wu
 * @className DifyKnowledgeResp
 * @date 2025-10-29 10:30
 */
@Data
public class DifyKnowledgeResp {

    /**
     * 查询结果
     */
    private List<QueryResult> queryResults;

    /**
     * 总数量
     */
    private Integer total;

    /**
     * 检索模式
     */
    private String retrievalMode;

    @Data
    public static class QueryResult {
        /**
         * 文档ID
         */
        private String documentId;

        /**
         * 文档名称
         */
        private String documentName;

        /**
         * 内容
         */
        private String content;

        /**
         * 相似度分数
         */
        private Double score;

        /**
         * 位置信息
         */
        private Position position;

        /**
         * 元数据
         */
        private Object metadata;
    }

    @Data
    public static class Position {
        /**
         * 开始位置
         */
        private Integer start;

        /**
         * 结束位置
         */
        private Integer end;

        /**
         * 页码
         */
        private Integer pageNumber;
    }
}
