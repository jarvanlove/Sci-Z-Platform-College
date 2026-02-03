package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库文件夹创建请求
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderCreateReq
 * @date 2025-01-28 16:00
 */
@Getter
@Setter
public class KnowledgeFolderCreateReq {

    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private Long knowledgeId;

    /**
     * 父文件夹ID（0为根目录，即知识库本身）
     */
    @NotNull(message = "父文件夹ID不能为空")
    private Long parentId;

    /**
     * 文件夹名称
     */
    @NotBlank(message = "文件夹名称不能为空")
    private String folderName;

    /**
     * 排序号
     */
    private Integer sortOrder;
}


