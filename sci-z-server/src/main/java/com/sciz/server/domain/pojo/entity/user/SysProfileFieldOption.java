package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户扩展字段选项实体
 *
 * 存储 select 类型字段的候选值，支持行业差异化配置。
 *
 * @author JiaWen.Wu
 * @className SysProfileFieldOption
 * @date 2025-11-12 11:20
 */
@Getter
@Setter
@TableName("sys_profile_field_option")
public class SysProfileFieldOption extends BaseEntity {

    /**
     * 选项ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属字段ID
     */
    @TableField("field_id")
    private Long fieldId;

    /**
     * 选项值
     */
    @TableField("option_value")
    private String optionValue;

    /**
     * 选项展示名称
     */
    @TableField("option_label")
    private String optionLabel;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 是否默认选项(0:否,1:是)
     */
    @TableField("is_default")
    private Integer isDefault;
}
