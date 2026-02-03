package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库更新请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeUpdateReq
 * @date 2025-01-23 14:30
 */
@Data
public class KnowledgeUpdateReq {

    /**
     * 知识库名称
     */
    @Size(max = 200, message = "知识库名称长度不能超过200个字符")
    private String name;

    /**
     * 知识库描述
     */
    @Size(max = 1000, message = "知识库描述长度不能超过1000个字符")
    private String description;

    /**
     * 关联项目ID（可选）
     */
    private Long projectId;

    /**
     * 是否共享：0=私有，1=共享
     */
    private Integer isShared;
}
