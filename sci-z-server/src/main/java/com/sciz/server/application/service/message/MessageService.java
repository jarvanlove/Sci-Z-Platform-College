package com.sciz.server.application.service.message;

import com.sciz.server.domain.pojo.dto.request.message.MessageListQueryReq;
import com.sciz.server.domain.pojo.dto.response.message.MessageDetailResp;
import com.sciz.server.domain.pojo.dto.response.message.MessageListResp;
import com.sciz.server.domain.pojo.entity.message.SysMessage;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 站内消息应用服务
 *
 * @author Sci-Z
 */
public interface MessageService {

    /**
     * 分页查询当前用户的消息列表
     *
     * @param req 查询条件（分页、是否仅未读）
     * @return 分页结果
     */
    PageResult<MessageListResp> list(MessageListQueryReq req);

    /**
     * 获取消息详情（仅能查看自己为接收人的消息）
     *
     * @param id 消息ID
     * @return 详情，不存在或无权限时返回 null
     */
    MessageDetailResp getById(Long id);

    /**
     * 当前用户未读消息数
     *
     * @return 未读数
     */
    long unreadCount();

    /**
     * 标为已读（仅接收人可操作）
     *
     * @param id 消息ID
     */
    void markRead(Long id);

    /**
     * 当前用户全部标为已读
     */
    void markReadAll();

    /**
     * 创建消息并落库；若接收人在线则通过 WebSocket 推送
     *
     * @param message 消息实体（senderId、receiverId、title、content、type、status、extraJson 等需已设置）
     * @return 消息ID
     */
    Long createAndPush(SysMessage message);
}
