package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.ai.AiConversationCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiConversationResp;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * AI 会话转换器
 *
 * @author shihangshang
 * @className AiConversationConverter
 * @date 2025-11-14 10:00
 */
@Mapper(componentModel = "spring")
public interface AiConversationConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    AiConversation toEntity(AiConversationCreateReq req);

    /**
     * updateReq → entity（更新）
     *
     * @param req 更新请求
     * @param entity 目标实体
     */
    @Mapping(target = "id", expression = "java(Long.parseLong(req.getId()))")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget AiConversation entity, AiConversationUpdateReq req);

    /**
     * entity → resp
     *
     * @param entity 实体
     * @return 响应
     */
    @Mapping(target = "id", expression = "java(String.valueOf(entity.getId()))")
    @Mapping(target = "userId", expression = "java(entity.getUserId() != null ? String.valueOf(entity.getUserId()) : null)")
    AiConversationResp toResp(AiConversation entity);

    /**
     * entity列表 → resp列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    List<AiConversationResp> toRespList(List<AiConversation> entities);
}

