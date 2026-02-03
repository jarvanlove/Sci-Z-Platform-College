package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderCreateReq;
import com.sciz.server.domain.pojo.dto.request.knowledge.KnowledgeFolderUpdateReq;
import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeFolderResp;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeFolder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 知识库文件夹转换器
 *
 * @author JiaWen.Wu
 * @className KnowledgeFolderConverter
 * @date 2025-01-28 16:00
 */
@Mapper(componentModel = "spring")
public interface KnowledgeFolderConverter {

    /**
     * req → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "folderPath", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    SysKnowledgeFolder toEntity(KnowledgeFolderCreateReq req);

    /**
     * entity → resp
     *
     * @param entity 实体
     * @return 响应
     */
    @Mapping(target = "children", ignore = true)
    KnowledgeFolderResp toResp(SysKnowledgeFolder entity);

    /**
     * entity列表 → resp列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    List<KnowledgeFolderResp> toRespList(List<SysKnowledgeFolder> entities);

    /**
     * 更新实体
     *
     * @param req 更新请求
     * @param entity 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "knowledgeId", ignore = true)
    @Mapping(target = "parentId", ignore = true)
    @Mapping(target = "folderPath", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    void updateEntity(@MappingTarget SysKnowledgeFolder entity, KnowledgeFolderUpdateReq req);
}

