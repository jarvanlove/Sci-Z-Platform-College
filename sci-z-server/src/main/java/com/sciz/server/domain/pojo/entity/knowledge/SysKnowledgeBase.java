package com.sciz.server.domain.pojo.entity.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库实体
 *
 * - 对应表：sys_knowledge_base
 *
 * @author ShiHang.Shang
 * @className SysKnowledgeBase
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_knowledge_base")
public class SysKnowledgeBase extends BaseEntity {

    /**
     * 知识库ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 知识库名称
     */
    @TableField("name")
    private String name;

    /**
     * 知识库描述
     */
    @TableField("description")
    private String description;

    /**
     * 创建人ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 创建人姓名
     */
    @TableField("owner_name")
    private String ownerName;

    /**
     * 关联项目ID
     */
    @TableField("project_id")
    private Long projectId;

    /**
     * 关联项目名称
     */
    @TableField("project_name")
    private String projectName;

    /**
     * Dify知识库ID
     */
    @TableField("dify_kb_id")
    private String difyKbId;

    /**
     * Dify知识库数据ID（Dify返回的id字段）
     */
    @TableField("dify_knowdata_id")
    private String difyKnowdataId;

    /**
     * 回调数据（Dify API返回的完整JSON数据）
     */
    @TableField("callback")
    private String callback;

    /**
     * 是否共享
     */
    @TableField("is_shared")
    private Integer isShared;

    /**
     * 状态
     */
    @TableField("status")
    private String status;

    /**
     * 文件数量
     */
    @TableField("file_count")
    private Integer fileCount;

    /**
     * 文件夹数量
     */
    @TableField("folder_count")
    private Integer folderCount;
}
