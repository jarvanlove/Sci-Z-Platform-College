package com.sciz.server.domain.pojo.entity.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库文件关联实体
 *
 * - 对应表：sys_knowledge_file_relation
 *
 * @author JiaWen.Wu
 * @className SysKnowledgeFileRelation
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_knowledge_file_relation")
public class SysKnowledgeFileRelation extends BaseEntity {

    /**
     * 关联ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    @TableField("knowledge_id")
    private Long knowledgeId;

    /**
     * 文件夹ID（0为根目录）
     */
    @TableField("folder_id")
    private Long folderId;

    /**
     * 附件ID
     */
    @TableField("attachment_id")
    private Long attachmentId;

    /**
     * 文件显示名称
     */
    @TableField("file_name")
    private String fileName;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
