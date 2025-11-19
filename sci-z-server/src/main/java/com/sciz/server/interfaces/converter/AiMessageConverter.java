package com.sciz.server.interfaces.converter;

import com.sciz.server.domain.pojo.dto.request.ai.AiMessageCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiMessageResp;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * AI 消息转换器
 *
 * @author shihangshang
 * @className AiMessageConverter
 * @date 2025-11-14 10:00
 */
@Mapper(componentModel = "spring")
public interface AiMessageConverter {

    /**
     * createReq → entity
     *
     * @param req 创建请求
     * @return 实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversationId", expression = "java(req.getConversationId() != null && !req.getConversationId().isEmpty() ? Long.parseLong(req.getConversationId()) : null)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "sendTime", expression = "java(req.getSendTime() != null ? req.getSendTime() : java.time.LocalDateTime.now())")
    AiMessage toEntity(AiMessageCreateReq req);

    /**
     * updateReq → entity（更新）
     *
     * @param req 更新请求
     * @param entity 目标实体
     */
    @Mapping(target = "id", expression = "java(req.getId() != null && !req.getId().isEmpty() ? Long.parseLong(req.getId()) : null)")
    @Mapping(target = "conversationId", expression = "java(req.getConversationId() != null && !req.getConversationId().isEmpty() ? Long.parseLong(req.getConversationId()) : null)")
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdTime", ignore = true)
    @Mapping(target = "updatedTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    void updateEntity(@MappingTarget AiMessage entity, AiMessageUpdateReq req);

    /**
     * entity → resp
     *
     * @param entity 实体
     * @return 响应
     */
    @Mapping(target = "id", expression = "java(entity.getId() != null ? String.valueOf(entity.getId()) : null)")
    @Mapping(target = "conversationId", expression = "java(entity.getConversationId() != null ? String.valueOf(entity.getConversationId()) : null)")
    AiMessageResp toResp(AiMessage entity);

    /**
     * entity列表 → resp列表
     *
     * @param entities 实体列表
     * @return 响应列表
     */
    List<AiMessageResp> toRespList(List<AiMessage> entities);
}

