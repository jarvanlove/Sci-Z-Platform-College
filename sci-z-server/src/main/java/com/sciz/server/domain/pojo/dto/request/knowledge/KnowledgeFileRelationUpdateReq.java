package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库文件关联更新请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationUpdateReq
 * @date 2025-01-28 16:00
 */
@Data
public class KnowledgeFileRelationUpdateReq {

    /**
     * 文件夹ID（0为根目录）
     */
    private String folderId;

    /**
     * 文件显示名称
     */
    @Size(max = 255, message = "文件名称长度不能超过255个字符")
    private String fileName;

    /**
     * 排序号
     */
    private Integer sortOrder;
}

