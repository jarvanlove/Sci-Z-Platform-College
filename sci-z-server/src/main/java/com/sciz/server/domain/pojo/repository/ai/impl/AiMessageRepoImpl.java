package com.sciz.server.domain.pojo.repository.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import com.sciz.server.domain.pojo.mapper.ai.AiMessageMapper;
import com.sciz.server.domain.pojo.repository.ai.AiMessageRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AI 消息仓储实现
 * 
 * @author shihangshang
 * @className AiMessageRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class AiMessageRepoImpl implements AiMessageRepo {

    private final AiMessageMapper mapper;

    public AiMessageRepoImpl(AiMessageMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(AiMessage entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public boolean updateById(AiMessage entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public AiMessage findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(AiMessage::getId, id)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public IPage<AiMessage> pageByConversationId(Page<AiMessage> page, Long conversationId) {
        LambdaQueryWrapper<AiMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessage::getConversationId, conversationId)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(AiMessage::getSendTime);
        return mapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<AiMessage> listByConversationId(Long conversationId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(AiMessage::getConversationId, conversationId)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(AiMessage::getSendTime)
                .list();
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(AiMessage::getId, id)
                .set(AiMessage::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public boolean deleteBatchByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return new LambdaUpdateChainWrapper<>(mapper)
                .in(AiMessage::getId, ids)
                .set(AiMessage::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public boolean deleteByConversationId(Long conversationId) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(AiMessage::getConversationId, conversationId)
                .set(AiMessage::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }
}
