package com.sciz.server.domain.pojo.dto.response.knowledge;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库文件夹响应
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderResp
 * @date 2025-01-28 16:00
 */
@Getter
@Setter
public class KnowledgeFolderResp {

    /**
     * 文件夹ID
     */
    private Long id;

    /**
     * 知识库ID
     */
    private Long knowledgeId;

    /**
     * 父文件夹ID（0为根目录）
     */
    private Long parentId;

    /**
     * 文件夹名称
     */
    private String folderName;

    /**
     * 文件夹路径
     */
    private String folderPath;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 子文件夹列表（用于树形结构）
     */
    private List<KnowledgeFolderResp> children;
}


