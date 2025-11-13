package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库文件关联创建请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationCreateReq
 * @date 2025-01-28 16:00
 */
@Data
public class KnowledgeFileRelationCreateReq {

    /**
     * 知识库ID
     */
    @NotNull(message = "知识库ID不能为空")
    private String knowledgeId;

    /**
     * 文件夹ID（0为根目录）
     */
    private String folderId = "0";

    /**
     * 附件ID
     */
    @NotNull(message = "附件ID不能为空")
    private String attachmentId;

    /**
     * 文件显示名称
     */
    @Size(max = 255, message = "文件名称长度不能超过255个字符")
    private String fileName;

    /**
     * 排序号
     */
    private Integer sortOrder = 0;

    /**
     * 回调数据（Dify API返回的完整JSON数据）
     */
    private String callback;
}

