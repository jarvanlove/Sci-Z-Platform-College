package com.sciz.server.domain.pojo.entity.file;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 附件关联实体
 *
 * - 对应表：sys_attachment_relation
 *
 * @author JiaWen.Wu
 * @className SysAttachmentRelation
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_attachment_relation")
public class SysAttachmentRelation extends BaseEntity {

    /**
     * 关联ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 附件ID
     */
    @TableField("attachment_id")
    private Long attachmentId;

    /**
     * 关联类型（project/declaration/report/user/knowledge）
     */
    @TableField("relation_type")
    private String relationType;

    /**
     * 关联对象ID
     */
    @TableField("relation_id")
    private Long relationId;

    /**
     * 关联对象名称
     */
    @TableField("relation_name")
    private String relationName;

    /**
     * 附件类型（document/image/export/other）
     */
    @TableField("attachment_type")
    private String attachmentType;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
