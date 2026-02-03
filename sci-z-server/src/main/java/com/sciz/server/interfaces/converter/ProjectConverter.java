package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.domain.pojo.entity.project.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 项目转换器
 *
 * @author JiaWen.Wu
 * @className ProjectConverter
 * @date 2025-01-24 16:00
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Project toEntity(ProjectCreateReq req);

    /**
     * entity → listResp
     * canDelete 由列表接口按权限在 Service 层设置，此处默认 true
     *
     * @param entity 实体
     * @return 列表响应
     */
    @Mapping(target = "canDelete", constant = "true")
    ProjectListResp toListResp(Project entity);

    /**
     * entity → detailResp
     * 
     * 注意：此方法已废弃，项目详情响应现在在 Service 中直接构建
     * （因为需要从多个表查询数据：项目、申报、成员、里程碑、附件）
     *
     * @param entity 实体
     * @return 详情响应
     * @deprecated 项目详情响应现在在 Service 中直接构建，不再使用此方法
     */
    @Deprecated
    ProjectDetailResp toDetailResp(Project entity);

    /**
     * entityList → listRespList
     *
     * @param entities 实体列表
     * @return 列表响应列表
     */
    List<ProjectListResp> toListRespList(List<Project> entities);

}
