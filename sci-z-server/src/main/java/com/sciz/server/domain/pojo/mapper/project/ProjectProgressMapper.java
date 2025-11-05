package com.sciz.server.domain.pojo.mapper.project;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.project.ProjectProgress;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目进度 Mapper
 *
 * @author JiaWen.Wu
 * @className ProjectProgressMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface ProjectProgressMapper extends BaseMapper<ProjectProgress> {
}
