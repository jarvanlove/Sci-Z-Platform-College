package com.sciz.server.domain.pojo.mapper.log;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.log.SysOperationLog;

import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author JiaWen.Wu
 * @className SysOperationLogMapper
 * @date 2025-10-29 16:00
 */
@Mapper
public interface SysOperationLogMapper extends BaseMapper<SysOperationLog> {
}
