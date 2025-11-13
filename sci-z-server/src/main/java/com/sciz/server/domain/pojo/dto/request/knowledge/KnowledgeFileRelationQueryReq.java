package com.sciz.server.domain.pojo.dto.request.knowledge;

import lombok.Data;

/**
 * 知识库文件关联查询请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationQueryReq
 * @date 2025-01-28 16:00
 */
@Data
public class KnowledgeFileRelationQueryReq {

    /**
     * 知识库ID
     */
    private String knowledgeId;

    /**
     * 文件夹ID（可选，用于筛选特定文件夹下的文件）
     */
    private String folderId;

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 页大小
     */
    private Integer size = 10;
}

