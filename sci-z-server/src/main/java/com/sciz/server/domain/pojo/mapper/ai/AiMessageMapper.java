package com.sciz.server.domain.pojo.mapper.ai;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI 消息 Mapper
 *
 * @author JiaWen.Wu
 * @className AiMessageMapper
 * @date 2025-10-30 11:00
 */
@Mapper
public interface AiMessageMapper extends BaseMapper<AiMessage> {
}
