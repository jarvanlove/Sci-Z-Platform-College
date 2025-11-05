package com.sciz.server.domain.pojo.mapper.project;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.project.ProjectMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 项目成员 Mapper
 *
 * @author JiaWen.Wu
 * @className ProjectMemberMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface ProjectMemberMapper extends BaseMapper<ProjectMember> {
}
