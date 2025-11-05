package com.sciz.server.domain.pojo.entity.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sciz.server.domain.pojo.entity.BaseEntity;
import lombok.Getter;
import lombok.Setter;

/**
 * 角色权限关联实体
 *
 * - 对应表：sys_role_permission
 *
 * @author JiaWen.Wu
 * @className SysRolePermission
 * @date 2025-10-30 00:00
 */
@Getter
@Setter
@TableName("sys_role_permission")
public class SysRolePermission extends BaseEntity {

    /**
     * 关联ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限ID
     */
    @TableField("permission_id")
    private Long permissionId;
}
