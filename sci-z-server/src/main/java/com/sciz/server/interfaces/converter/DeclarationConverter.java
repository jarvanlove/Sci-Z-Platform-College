package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.declaration.DeclarationCreateReq;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationDetailResp;
import com.sciz.server.domain.pojo.dto.response.declaration.DeclarationListResp;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

/**
 * 申报转换器
 *
 * @author JiaWen.Wu
 * @className DeclarationConverter
 * @date 2025-01-20 15:00
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeclarationConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", ignore = true)
    @Mapping(target = "applicantId", ignore = true)
    @Mapping(target = "applicantName", ignore = true)
    @Mapping(target = "researchFields", expression = "java(com.sciz.server.infrastructure.shared.utils.JsonUtil.toJson(req.researchFields()))")
    @Mapping(target = "contentSummary", ignore = true)
    @Mapping(target = "workflowStatus", ignore = true)
    @Mapping(target = "workflowResult", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "submitTime", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    Declaration toEntity(DeclarationCreateReq req);

    /**
     * entity → detailResp
     *
     * @param entity 实体
     * @return 详情响应
     */
    @Mapping(target = "researchFields", expression = "java(com.sciz.server.infrastructure.shared.utils.JsonUtil.fromJsonList(entity.getResearchFields(), String.class))")
    @Mapping(target = "workflowResult", ignore = true)
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "hasAttachment", ignore = true)
    @Mapping(target = "attachmentId", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    DeclarationDetailResp toDetailResp(Declaration entity);

    /**
     * entity → listResp
     *
     * @param entity 实体
     * @return 列表响应
     */
    @Mapping(target = "researchDirection", expression = "java(entity.getResearchDirection() != null && entity.getResearchDirection().length() > 100 ? entity.getResearchDirection().substring(0, 100) : entity.getResearchDirection())")
    @Mapping(target = "researchFields", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "statusDescription", ignore = true)
    @Mapping(target = "workflowStatusDescription", ignore = true)
    @Mapping(target = "hasAttachment", ignore = true)
    DeclarationListResp toListResp(Declaration entity);

    /**
     * entity列表 → listResp列表
     *
     * @param entities 实体列表
     * @return 列表响应列表
     */
    List<DeclarationListResp> toListRespList(List<Declaration> entities);
}
