package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFileRelationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFileRelationResp;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFileRelation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 知识库文件关联转换器
 *
 * @author ShiHang.Shang
 * @className KnowledgeFileRelationConverter
 * @date 2025-01-28 16:00
 */
@Mapper(componentModel = "spring")
public interface KnowledgeFileRelationConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeId", ignore = true)
    @Mapping(target = "folderId", ignore = true)
    @Mapping(target = "attachmentId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    SysKnowledgeFileRelation toEntity(KnowledgeFileRelationCreateReq req);

    /**
     * entity → resp
     *
     * @param entity 实体
     * @return 响应
     */
    @Mapping(target = "id", expression = "java(entity.getId() != null ? String.valueOf(entity.getId()) : null)")
    @Mapping(target = "knowledgeId", expression = "java(entity.getKnowledgeId() != null ? String.valueOf(entity.getKnowledgeId()) : null)")
    @Mapping(target = "folderId", expression = "java(entity.getFolderId() != null ? String.valueOf(entity.getFolderId()) : null)")
    @Mapping(target = "attachmentId", expression = "java(entity.getAttachmentId() != null ? String.valueOf(entity.getAttachmentId()) : null)")
    KnowledgeFileRelationResp toResp(SysKnowledgeFileRelation entity);

    /**
     * entity列表 → resp列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    List<KnowledgeFileRelationResp> toRespList(List<SysKnowledgeFileRelation> entities);

    /**
     * updateReq → entity（更新）
     *
     * @param req 更新请求
     * @param entity 目标实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeId", ignore = true)
    @Mapping(target = "folderId", ignore = true)
    @Mapping(target = "attachmentId", ignore = true)
    @Mapping(target = "callback", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget SysKnowledgeFileRelation entity, KnowledgeFileRelationUpdateReq req);
}

