package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.response.knowledge.KnowledgeResp;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 知识库转换器
 *
 * @author ShiHang.Shang
 * @className KnowledgeConverter
 * @date 2025-01-28 14:30
 */
@Mapper(componentModel = "spring")
public interface KnowledgeConverter {

    /**
     * entity → resp
     *
     * @param entity 知识库实体
     * @return 知识库响应
     */
    @Mapping(target = "projectName", ignore = true)
    KnowledgeResp toResp(SysKnowledgeBase entity);

    /**
     * entity列表 → resp列表
     *
     * @param entities 知识库实体列表
     * @return 知识库响应列表
     */
    List<KnowledgeResp> toRespList(List<SysKnowledgeBase> entities);
}

