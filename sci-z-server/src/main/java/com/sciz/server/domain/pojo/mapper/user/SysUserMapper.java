package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author JiaWen.Wu
 * @className SysUserMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
