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
    public AiMessage findById(Long id) {
        var queryWrapper = new LambdaQueryChainWrapper<>(mapper)
                .eq(AiMessage::getId, id)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 数据权限过滤：admin 用户可以看到所有数据，普通用户只能看到自己的数据
        // 注意：AI 消息表没有直接的用户字段，需要通过会话表来检查权限
        // 由于在 Repository 层进行关联查询会影响性能，这里只做基础过滤
        // Service 层会通过会话表进行完整的权限检查
        // 如果需要在 Repository 层进行完整过滤，需要通过子查询或关联查询实现
        // 为了性能考虑，这里暂时不添加关联查询，由 Service 层负责权限检查

        return queryWrapper.one();
    }

    @Override
    public boolean updateById(AiMessage entity) {
        return mapper.updateById(entity) > 0;
    }

    @Override
    public IPage<AiMessage> pageByConversationId(Page<AiMessage> page, Long conversationId) {
        LambdaQueryWrapper<AiMessage> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AiMessage::getConversationId, conversationId)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(AiMessage::getSendTime)
                .orderByAsc(AiMessage::getCreatedTime);
        return mapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<AiMessage> listByConversationId(Long conversationId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(AiMessage::getConversationId, conversationId)
                .eq(AiMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(AiMessage::getSendTime)
                .orderByAsc(AiMessage::getCreatedTime)
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
