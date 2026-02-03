package com.sciz.server.domain.pojo.dto.response.knowledge;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 知识库文件夹及文件响应（包含文件夹和文档的树形结构）
 * 
 * 根目录时：每个文件夹包含其文件列表（按文件夹分组）
 * 文件夹内时：每个项目可以是文件夹或文件（混合列表）
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderWithFilesResp
 * @date 2025-01-28 17:00
 */
@Getter
@Setter
public class KnowledgeFolderWithFilesResp {

    /**
     * 项目类型：folder（文件夹）或 file（文件）
     * 根目录时：所有项目都是 folder
     * 文件夹内时：可以是 folder 或 file
     */
    private String type;

    /**
     * 文件夹ID（仅文件夹有此字段）
     */
    private Long folderId;

    /**
     * 文件夹名称（仅文件夹有此字段）
     */
    private String folderName;

    /**
     * 文件夹下的文档数量（仅文件夹有此字段）
     */
    private Integer fileCount;

    /**
     * 文件夹下的文档列表（仅文件夹有此字段，根目录时使用）
     */
    private List<KnowledgeFileRelationResp> files;

    /**
     * 文件关联ID（仅文件有此字段）
     */
    private String fileId;

    /**
     * 文件名称（仅文件有此字段）
     */
    private String fileName;

    /**
     * 附件ID（仅文件有此字段）
     */
    private String attachmentId;

    /**
     * 文件大小（字节，仅文件有此字段）
     */
    private Long fileSize;

    /**
     * 文件扩展名（仅文件有此字段）
     */
    private String ext;

    /**
     * 回调数据（仅文件有此字段）
     */
    private String callback;

    /**
     * 知识库封面URL（预签名URL）
     */
    private String coverUrl;

    /**
     * 知识库封面附件ID
     */
    private Long coverFileId;

    /**
     * 创建时间（仅文件有此字段，用于列表展示上传时间）
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间（仅文件有此字段）
     */
    private LocalDateTime updatedTime;
}

