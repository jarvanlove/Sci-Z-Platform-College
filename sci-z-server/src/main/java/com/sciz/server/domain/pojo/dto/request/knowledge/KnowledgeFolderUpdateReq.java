package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库文件夹更新请求
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderUpdateReq
 * @date 2025-01-28 16:00
 */
@Getter
@Setter
public class KnowledgeFolderUpdateReq {

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


