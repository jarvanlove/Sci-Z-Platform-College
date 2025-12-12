package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.report.ReportManagementCreateReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementUpdateReq;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementDetailResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementListResp;
import com.sciz.server.domain.pojo.entity.report.ReportManagement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 报告管理转换器
 *
 * @author JiaWen.Wu
 * @className ReportManagementConverter
 * @date 2025-01-24 14:30
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReportManagementConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "creatorName", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "generateTime", ignore = true)
    @Mapping(target = "difyApiKeysId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    ReportManagement toEntity(ReportManagementCreateReq req);

    /**
     * entity → listResp
     *
     * @param entity 实体
     * @return 列表响应
     */
    ReportManagementListResp toListResp(ReportManagement entity);

    /**
     * entity → detailResp
     *
     * @param entity 实体
     * @return 详情响应
     */
    ReportManagementDetailResp toDetailResp(ReportManagement entity);

    /**
     * entityList → listRespList
     *
     * @param entities 实体列表
     * @return 列表响应列表
     */
    List<ReportManagementListResp> toListRespList(List<ReportManagement> entities);

    /**
     * updateReq → entity（更新实体）
     *
     * @param req    更新请求
     * @param entity 实体（会被更新）
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "projectId", ignore = true)
    @Mapping(target = "creatorId", ignore = true)
    @Mapping(target = "creatorName", ignore = true)
    @Mapping(target = "generateTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget ReportManagement entity, ReportManagementUpdateReq req);
}

