package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysPermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限 Mapper
 *
 * @author JiaWen.Wu
 * @className SysPermissionMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysPermissionMapper extends BaseMapper<SysPermission> {
}
