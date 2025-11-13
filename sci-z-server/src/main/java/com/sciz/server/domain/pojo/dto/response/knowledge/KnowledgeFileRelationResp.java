package com.sciz.server.domain.pojo.dto.response.knowledge;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 知识库文件关联响应
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationResp
 * @date 2025-01-28 16:00
 */
@Data
public class KnowledgeFileRelationResp {

    /**
     * 关联ID，主键
     */
    private String id;

    /**
     * 知识库ID
     */
    private String knowledgeId;

    /**
     * 文件夹ID（0为根目录）
     */
    private String folderId;

    /**
     * 附件ID
     */
    private String attachmentId;

    /**
     * 文件显示名称
     */
    private String fileName;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 回调数据（Dify API返回的完整JSON数据）
     */
    private String callback;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

