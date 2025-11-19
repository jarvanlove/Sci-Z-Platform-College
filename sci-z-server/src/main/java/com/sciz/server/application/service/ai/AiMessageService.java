package com.sciz.server.application.service.ai;

import com.sciz.server.domain.pojo.dto.request.ai.AiMessageCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiMessageResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

import java.util.List;

/**
 * AI 消息应用服务接口
 *
 * @author shihangshang
 * @className AiMessageService
 * @date 2025-11-14 10:00
 */
public interface AiMessageService {

    /**
     * 创建消息
     *
     * @param req 创建请求
     * @return 响应
     */
    AiMessageResp create(AiMessageCreateReq req);

    /**
     * 更新消息
     *
     * @param req 更新请求
     * @return 响应
     */
    AiMessageResp update(AiMessageUpdateReq req);

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
     * 根据会话ID删除
     *
     * @param conversationId 会话ID
     */
    void deleteByConversationId(String conversationId);

    /**
     * 根据ID查询详情
     *
     * @param id 主键ID
     * @return 响应
     */
    AiMessageResp findById(String id);

    /**
     * 根据ID查询详情（带权限检查）
     *
     * @param id 主键ID
     * @return 响应
     */
    AiMessageResp findDetail(String id);

    /**
     * 分页查询
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<AiMessageResp> page(AiMessageQueryReq req);

    /**
     * 根据会话ID分页查询
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<AiMessageResp> pageByConversationId(AiMessageQueryReq req);

    /**
     * 根据会话ID查询列表
     *
     * @param conversationId 会话ID
     * @return 列表
     */
    List<AiMessageResp> listByConversationId(String conversationId);

    /**
     * 批量删除
     *
     * @param ids ID列表
     */
    void deleteBatch(List<String> ids);
}

