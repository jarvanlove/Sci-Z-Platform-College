package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置 Mapper
 *
 * @author JiaWen.Wu
 * @className SysConfigMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysConfigMapper extends BaseMapper<SysConfig> {
}
