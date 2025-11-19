package com.sciz.server.domain.pojo.repository.ai;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;

import java.util.List;

/**
 * AI 会话仓储（领域层抽象）
 * 
 * @author shihangshang
 * @className AiConversationRepo
 * @date 2025-10-30 11:00
 */
public interface AiConversationRepo {

    /**
     * 保存 AI 会话
     *
     * @param entity AiConversation 实体
     * @return 生成的主键ID
     */
    Long save(AiConversation entity);

    /**
     * 根据ID查询
     *
     * @param id 主键ID
     * @return 实体
     */
    AiConversation findById(Long id);

    /**
     * 更新
     *
     * @param entity 实体
     * @return 是否更新成功
     */
    boolean updateById(AiConversation entity);

    /**
     * 根据用户ID分页查询
     *
     * @param page 分页对象
     * @param userId 用户ID
     * @return 分页结果
     */
    IPage<AiConversation> pageByUserId(Page<AiConversation> page, Long userId);

    /**
     * 根据用户ID查询列表
     *
     * @param userId 用户ID
     * @return 列表
     */
    List<AiConversation> listByUserId(Long userId);

    /**
     * 根据ID删除（软删除）
     *
     * @param id 主键ID
     * @return 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 批量删除（软删除）
     *
     * @param ids ID列表
     * @return 是否删除成功
     */
    boolean deleteBatchByIds(List<Long> ids);

    /**
     * 更新置顶状态
     *
     * @param id 主键ID
     * @param isPinned 是否置顶(0:否,1:是)
     * @return 是否更新成功
     */
    boolean updatePinnedStatus(Long id, Integer isPinned);
}
