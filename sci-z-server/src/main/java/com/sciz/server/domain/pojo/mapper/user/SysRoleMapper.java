package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色 Mapper
 *
 * @author JiaWen.Wu
 * @className SysRoleMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysRoleMapper extends BaseMapper<SysRole> {
}
