package com.sciz.server.domain.pojo.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.user.SysDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 部门 Mapper
 *
 * @author JiaWen.Wu
 * @className SysDepartmentMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {
}
