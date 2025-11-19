package com.sciz.server.application.service.ai;

import com.sciz.server.domain.pojo.dto.request.ai.AiConversationCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiConversationResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

import java.util.List;

/**
 * AI 会话应用服务接口
 *
 * @author shihangshang
 * @className AiConversationService
 * @date 2025-11-14 10:00
 */
public interface AiConversationService {

    /**
     * 创建会话
     *
     * @param req 创建请求
     * @return 响应
     */
    AiConversationResp create(AiConversationCreateReq req);

    /**
     * 更新会话
     *
     * @param req 更新请求
     * @return 响应
     */
    AiConversationResp update(AiConversationUpdateReq req);

    /**
     * 根据ID删除
     *
     * @param id 主键ID
     */
    void deleteById(String id);

    /**
     * 批量删除
     *
     * @param ids ID列表
     */
    void deleteBatchByIds(List<String> ids);

    /**
     * 根据ID查询详情
     *
     * @param id 主键ID
     * @return 响应
     */
    AiConversationResp findById(String id);

    /**
     * 根据ID查询详情（带权限检查）
     *
     * @param id 主键ID
     * @return 响应
     */
    AiConversationResp findDetail(String id);

    /**
     * 分页查询
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<AiConversationResp> page(AiConversationQueryReq req);

    /**
     * 根据用户ID分页查询
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<AiConversationResp> pageByUserId(AiConversationQueryReq req);

    /**
     * 查询当前用户的会话列表
     *
     * @return 列表
     */
    List<AiConversationResp> list();

    /**
     * 根据用户ID查询列表
     *
     * @param userId 用户ID
     * @return 列表
     */
    List<AiConversationResp> listByUserId(Long userId);

    /**
     * 批量删除
     *
     * @param ids ID列表
     */
    void deleteBatch(List<String> ids);

    /**
     * 更新置顶状态
     *
     * @param id 主键ID
     * @param isPinned 是否置顶(0:否,1:是)
     * @return 响应
     */
    AiConversationResp updatePinnedStatus(String id, Integer isPinned);
}
