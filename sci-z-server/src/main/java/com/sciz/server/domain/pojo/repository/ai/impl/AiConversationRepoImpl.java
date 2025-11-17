package com.sciz.server.domain.pojo.repository.ai.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import com.sciz.server.domain.pojo.mapper.ai.AiConversationMapper;
import com.sciz.server.domain.pojo.repository.ai.AiConversationRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * AI 会话仓储实现
 * 
 * @author shihangshang
 * @className AiConversationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class AiConversationRepoImpl implements AiConversationRepo {

    private final AiConversationMapper mapper;

    public AiConversationRepoImpl(AiConversationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(AiConversation entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public boolean updateById(AiConversation entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public AiConversation findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(AiConversation::getId, id)
                .eq(AiConversation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public IPage<AiConversation> pageByUserId(Page<AiConversation> page, Long userId) {
        LambdaQueryWrapper<AiConversation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByDesc(AiConversation::getIsPinned)
                .orderByDesc(AiConversation::getCreatedTime);
        return mapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<AiConversation> listByUserId(Long userId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(AiConversation::getUserId, userId)
                .eq(AiConversation::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByDesc(AiConversation::getIsPinned)
                .orderByDesc(AiConversation::getCreatedTime)
                .list();
    }

    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(AiConversation::getId, id)
                .set(AiConversation::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public boolean deleteBatchByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return new LambdaUpdateChainWrapper<>(mapper)
                .in(AiConversation::getId, ids)
                .set(AiConversation::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    @Override
    public boolean updatePinnedStatus(Long id, Integer isPinned) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(AiConversation::getId, id)
                .set(AiConversation::getIsPinned, isPinned)
                .update();
    }
}
