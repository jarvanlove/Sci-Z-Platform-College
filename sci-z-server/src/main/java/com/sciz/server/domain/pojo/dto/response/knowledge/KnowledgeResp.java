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
     * 知识库类型：personal=个人知识库，project=项目知识库
     */
    private String kbType;

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
     * 封面附件ID
     */
    private Long coverFileId;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;

    /**
     * 当前用户是否可编辑该知识库（含删除文件/文件夹、修改资料等；用于前端显示删除按钮等）
     * true=管理员/创建人/项目知识库的项目成员或项目负责人
     */
    private Boolean canEdit;

    /**
     * 最近一次更新时 Dify 同步是否成功（仅更新接口在发生名称/描述同步时返回）
     * null=未尝试同步，true=同步成功，false=同步失败
     */
    private Boolean difySyncSuccess;

    /**
     * Dify 同步失败时的提示信息（仅当 difySyncSuccess=false 时可能有值）
     */
    private String difySyncMessage;
}

