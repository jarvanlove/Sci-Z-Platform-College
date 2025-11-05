package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 部门实体
 *
 * - 对应表：sys_department
 * - 支持多行业、多层级部门结构
 *
 * @author JiaWen.Wu
 * @className SysDepartment
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_department")
public class SysDepartment extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父部门ID(0为顶级)
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 部门名称
     */
    @TableField("department_name")
    private String departmentName;

    /**
     * 部门编码
     */
    @TableField("department_code")
    private String departmentCode;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 部门层级
     */
    @TableField("level")
    private Integer level;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态(0:禁用,1:启用)
     */
    @TableField("status")
    private Integer status;
}
