package com.sciz.server.domain.pojo.entity.declaration;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 工作流模板实体
 *
 * - 对应表：sys_workflow_template
 *
 * @author JiaWen.Wu
 * @className SysWorkflowTemplate
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_workflow_template")
public class SysWorkflowTemplate extends BaseEntity {

    /**
     * 模板ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 工作流名称
     */
    @TableField("name")
    private String name;

    /**
     * 工作流描述
     */
    @TableField("description")
    private String description;

    /**
     * Dify工作流ID
     */
    @TableField("dify_workflow_id")
    private String difyWorkflowId;

    /**
     * 工作流分类
     */
    @TableField("category")
    private String category;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Integer isActive;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
