package com.sciz.server.infrastructure.external.dify.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Dify知识库请求DTO
 *
 * @author JiaWen.Wu
 * @className DifyKnowledgeReq
 * @date 2025-10-29 10:30
 */
@Data
public class DifyKnowledgeReq {

    /**
     * 知识库ID
     */
    @NotBlank(message = "知识库ID不能为空")
    private String knowledgeBaseId;

    /**
     * 查询内容
     */
    @NotBlank(message = "查询内容不能为空")
    private String query;

    /**
     * 检索模式
     */
    private String retrievalMode = "semantic_search";

    /**
     * 返回结果数量
     */
    private Integer topK = 5;

    /**
     * 相似度阈值
     */
    private Double scoreThreshold = 0.7;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 文件ID列表
     */
    private List<String> fileIds;
}
