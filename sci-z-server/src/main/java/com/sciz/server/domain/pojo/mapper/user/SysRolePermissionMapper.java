package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysRolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联 Mapper
 *
 * @author JiaWen.Wu
 * @className SysRolePermissionMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {
}
