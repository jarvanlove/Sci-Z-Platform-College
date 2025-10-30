package com.sciz.server.domain.pojo.entity.knowledge;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 知识库文件夹实体
 *
 * - 对应表：sys_knowledge_folder
 *
 * @author JiaWen.Wu
 * @className SysKnowledgeFolder
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_knowledge_folder")
public class SysKnowledgeFolder extends BaseEntity {

    /**
     * 文件夹ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 知识库ID
     */
    @TableField("knowledge_id")
    private Long knowledgeId;

    /**
     * 父文件夹ID(0为根目录)
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 文件夹名称
     */
    @TableField("folder_name")
    private String folderName;

    /**
     * 文件夹路径
     */
    @TableField("folder_path")
    private String folderPath;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
