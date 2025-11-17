package com.sciz.server.domain.pojo.dto.request.knowledge;

import lombok.Data;

import java.util.List;

/**
 * 知识库文件关联查询请求
 *
 * @author shihangshang
 * @className KnowledgeFileRelationQueryReq
 * @date 2025-01-28 16:00
 */
@Data
public class KnowledgeFileRelationQueryReq {

    /**
     * 知识库ID（单个，保留以兼容旧接口）
     */
    private String knowledgeId;

    /**
     * 知识库ID列表（多个，支持批量查询）
     */
    private List<String> knowledgeIds;

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

