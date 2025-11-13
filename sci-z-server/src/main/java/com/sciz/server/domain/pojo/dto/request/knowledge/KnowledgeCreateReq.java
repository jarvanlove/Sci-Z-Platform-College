package com.sciz.server.domain.pojo.dto.request.knowledge;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 知识库创建请求
 *
 * @author ShiHang.Shang
 * @className KnowledgeCreateReq
 * @date 2025-01-28 14:30
 */
@Data
public class KnowledgeCreateReq {

    /**
     * 知识库名称
     */
    @NotBlank(message = "知识库名称不能为空")
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
    private Integer isShared = 0;

    /**
     * 权限设置（Dify API参数）
     */
    private String permission = "only_me";

    /**
     * 索引技术（Dify API参数）
     */
    private String indexingTechnique = "high_quality";

    /**
     * 用户ID
     */
    private Long userId;
    /**
     * 资源ID
     */
    private String resourceId;
    /**
     * 密钥类型
     */
    private String keyType;


}

