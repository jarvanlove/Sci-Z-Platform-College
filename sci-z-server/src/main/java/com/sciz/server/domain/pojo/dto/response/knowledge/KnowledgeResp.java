package com.sciz.server.domain.pojo.dto.response.knowledge;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 知识库响应
 *
 * @author ShiHang.Shang
 * @className KnowledgeResp
 * @date 2025-01-28 14:30
 */
@Data
public class KnowledgeResp {

    /**
     * 知识库ID
     */
    private Long id;

    /**
     * 知识库名称
     */
    private String name;

    /**
     * 知识库描述（包含Dify返回的完整信息JSON）
     */
    private String description;

    /**
     * 创建人ID
     */
    private Long ownerId;

    /**
     * 创建人姓名
     */
    private String ownerName;

    /**
     * 关联项目ID
     */
    private Long projectId;

    /**
     * 关联项目名称
     */
    private String projectName;

    /**
     * Dify知识库ID
     */
    private String difyKbId;

    /**
     * Dify知识库数据ID（Dify返回的id字段）
     */
    private String difyKnowdataId;

    /**
     * 回调数据（Dify API返回的完整JSON数据）
     */
    private String callback;

    /**
     * 是否共享：0=私有，1=共享
     */
    private Integer isShared;

    /**
     * 状态
     */
    private String status;

    /**
     * 文件数量
     */
    private Integer fileCount;

    /**
     * 文件夹数量
     */
    private Integer folderCount;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
}

