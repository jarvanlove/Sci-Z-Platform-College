package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 行业配置实体
 *
 * - 对应表：sys_industry_config
 * - 维护行业类型及其在不同页面中的标签文案
 *
 * @author JiaWen.Wu
 * @className SysIndustryConfig
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_industry_config")
public class SysIndustryConfig extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 行业类型(education/medical/power)
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 行业名称
     */
    @TableField("industry_name")
    private String industryName;

    /**
     * 部门标签
     */
    @TableField("department_label")
    private String departmentLabel;

    /**
     * 角色标签
     */
    @TableField("role_label")
    private String roleLabel;

    /**
     * 员工ID标签
     */
    @TableField("employee_id_label")
    private String employeeIdLabel;

    /**
     * 是否启用(0:禁用,1:启用)
     */
    @TableField("is_active")
    private Integer isActive;
}
