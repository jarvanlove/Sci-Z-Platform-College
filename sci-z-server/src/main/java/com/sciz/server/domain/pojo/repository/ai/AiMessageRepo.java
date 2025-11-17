package com.sciz.server.domain.pojo.repository.ai;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;

import java.util.List;

/**
 * AI 消息仓储（领域层抽象）
 * 
 * @author shihangshang
 * @className AiMessageRepo
 * @date 2025-10-30 11:00
 */
public interface AiMessageRepo {

    /**
     * 保存 AI 消息
     *
     * @param entity AiMessage 实体
     * @return 生成的主键ID
     */
    Long save(AiMessage entity);

    /**
     * 根据ID更新
     *
     * @param entity AiMessage 实体
     * @return 是否更新成功
     */
    boolean updateById(AiMessage entity);

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体
     */
    AiMessage findById(Long id);

    /**
     * 根据会话ID分页查询
     *
     * @param page 分页对象
     * @param conversationId 会话ID
     * @return 分页结果
     */
    IPage<AiMessage> pageByConversationId(Page<AiMessage> page, Long conversationId);

    /**
     * 根据会话ID查询列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    List<AiMessage> listByConversationId(Long conversationId);

    /**
     * 根据ID软删除
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 批量软删除
     *
     * @param ids ID列表
     * @return 是否删除成功
     */
    boolean deleteBatchByIds(List<Long> ids);

    /**
     * 根据会话ID批量删除
     *
     * @param conversationId 会话ID
     * @return 是否删除成功
     */
    boolean deleteByConversationId(Long conversationId);
}
