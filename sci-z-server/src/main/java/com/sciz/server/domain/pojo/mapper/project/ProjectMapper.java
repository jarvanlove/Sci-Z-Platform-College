package com.sciz.server.domain.pojo.mapper.project;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.project.Project;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目 Mapper
 *
 * @author JiaWen.Wu
 * @className ProjectMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}
