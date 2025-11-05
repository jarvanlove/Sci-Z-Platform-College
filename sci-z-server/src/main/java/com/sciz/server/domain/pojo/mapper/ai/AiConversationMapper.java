package com.sciz.server.domain.pojo.mapper.ai;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 会话 Mapper
 *
 * @author JiaWen.Wu
 * @className AiConversationMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface AiConversationMapper extends BaseMapper<AiConversation> {
}
