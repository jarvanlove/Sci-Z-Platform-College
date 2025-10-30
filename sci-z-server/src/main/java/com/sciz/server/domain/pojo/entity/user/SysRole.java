package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统角色实体
 *
 * - 对应表：sys_role
 *
 * @author JiaWen.Wu
 * @className SysRole
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_role")
public class SysRole extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色名称
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色编码
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述
     */
    @TableField("description")
    private String description;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 角色类型(system/custom)
     */
    @TableField("role_type")
    private String roleType;

    /**
     * 排序
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 角色状态：1=启用，0=禁用
     */
    @TableField("status")
    private Integer status;
}
