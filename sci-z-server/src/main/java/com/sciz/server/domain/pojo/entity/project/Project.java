package com.sciz.server.domain.pojo.entity.project;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目实体
 *
 * - 对应表：project
 *
 * @author JiaWen.Wu
 * @className Project
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("project")
public class Project extends BaseEntity {

    /**
     * 项目ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 项目编号
     */
    @TableField("number")
    private String number;

    /**
     * 项目名称
     */
    @TableField("name")
    private String name;

    /**
     * 项目描述
     */
    @TableField("description")
    private String description;

    /**
     * 关联申报ID
     */
    @TableField("declaration_id")
    private Long declarationId;

    /**
     * 项目预算
     */
    @TableField("budget")
    private BigDecimal budget;

    /**
     * 进度百分比
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 项目状态
     */
    @TableField("status")
    private String status;

    /**
     * Dify知识库ID
     */
    @TableField("dify_knowledge_id")
    private String difyKnowledgeId;
}
