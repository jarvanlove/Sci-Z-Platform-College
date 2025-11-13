package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户扩展字段模板实体
 *
 * 定义各行业的扩展字段（例如职称、执业证号等），用于驱动前端动态渲染。
 *
 * @author JiaWen.Wu
 * @className SysProfileField
 * @date 2025-11-12 11:20
 */
@Getter
@Setter
@TableName("sys_profile_field")
public class SysProfileField extends BaseEntity {

    /**
     * 模板ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 字段编码
     */
    @TableField("field_code")
    private String fieldCode;

    /**
     * 字段展示名称
     */
    @TableField("field_label")
    private String fieldLabel;

    /**
     * 字段类型(text/select/number/date)
     */
    @TableField("field_type")
    private String fieldType;

    /**
     * 字段描述
     */
    @TableField("description")
    private String description;

    /**
     * 是否必填(0:否,1:是)
     */
    @TableField("is_required")
    private Integer isRequired;

    /**
     * 是否启用(0:禁用,1:启用)
     */
    @TableField("is_enabled")
    private Integer isEnabled;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
