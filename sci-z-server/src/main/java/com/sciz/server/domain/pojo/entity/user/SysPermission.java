package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 系统权限实体
 *
 * - 对应表：sys_permission
 * - 采用小驼峰字段命名与数据库字段一一映射
 *
 * @author JiaWen.Wu
 * @className SysPermission
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_permission")
public class SysPermission extends BaseEntity {

    /**
     * 权限ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 父权限ID（0 表示顶级权限）
     */
    @TableField("parent_id")
    private Long parentId;

    /**
     * 权限名称
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限编码（唯一）
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限类型：1=菜单，2=按钮，3=API
     */
    @TableField("permission_type")
    private Integer permissionType;

    /**
     * 行业类型
     */
    @TableField("industry_type")
    private String industryType;

    /**
     * 路由路径
     */
    @TableField("path")
    private String path;

    /**
     * 组件路径
     */
    @TableField("component")
    private String component;

    /**
     * 图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 排序号
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 权限状态：1=启用，0=禁用
     */
    @TableField("status")
    private Integer status;
}
