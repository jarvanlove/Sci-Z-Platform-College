package com.sciz.server.application.service.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.application.service.message.MessageService;
import com.sciz.server.domain.pojo.dto.request.message.MessageListQueryReq;
import com.sciz.server.domain.pojo.dto.response.message.MessageDetailResp;
import com.sciz.server.domain.pojo.dto.response.message.MessageListResp;
import com.sciz.server.domain.pojo.entity.message.SysMessage;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import com.sciz.server.domain.pojo.mapper.message.SysMessageMapper;
import com.sciz.server.domain.pojo.repository.user.SysUserRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.infrastructure.websocket.MessageWebSocketSessionHolder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 站内消息应用服务实现
 *
 * @author Sci-Z
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_READ = "read";
    public static final String TYPE_INDUSTRY_EDUCATION_DISTRIBUTE = "industry_education_distribute";

    private final SysMessageMapper messageMapper;
    private final SysUserRepo sysUserRepo;
    private final MessageWebSocketSessionHolder sessionHolder;
    private final ObjectMapper objectMapper;

    @Override
    public PageResult<MessageListResp> list(MessageListQueryReq req) {
        Long userId = LoginUserUtil.requireCurrentUserId();
        LambdaQueryWrapper<SysMessage> q = new LambdaQueryWrapper<>();
        q.eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByDesc(SysMessage::getCreatedTime);
        if (Boolean.TRUE.equals(req.getUnreadOnly())) {
            q.eq(SysMessage::getStatus, STATUS_PENDING);
        }
        Page<SysMessage> page = messageMapper.selectPage(
                new Page<>(req.getPage(), req.getSize()),
                q
        );
        List<MessageListResp> records = page.getRecords().stream()
                .map(this::toListResp)
                .collect(Collectors.toList());
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    public MessageDetailResp getById(Long id) {
        Long userId = LoginUserUtil.requireCurrentUserId();
        SysMessage msg = messageMapper.selectById(id);
        if (msg == null || msg.getIsDeleted() != null && msg.getIsDeleted().equals(DeleteStatus.DELETED.getCode())) {
            return null;
        }
        if (!userId.equals(msg.getReceiverId())) {
            return null;
        }
        return toDetailResp(msg);
    }

    @Override
    public long unreadCount() {
        Long userId = LoginUserUtil.requireCurrentUserId();
        return messageMapper.selectCount(new LambdaQueryWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getStatus, STATUS_PENDING)
                .eq(SysMessage::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id) {
        Long userId = LoginUserUtil.requireCurrentUserId();
        SysMessage msg = messageMapper.selectById(id);
        if (msg == null || !userId.equals(msg.getReceiverId())) {
            throw BusinessException.of(ResultCode.BAD_REQUEST, "消息不存在或无权限");
        }
        if (STATUS_PENDING.equals(msg.getStatus())) {
            messageMapper.update(null, new LambdaUpdateWrapper<SysMessage>()
                    .eq(SysMessage::getId, id)
                    .set(SysMessage::getStatus, STATUS_READ)
                    .set(SysMessage::getReadTime, LocalDateTime.now()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markReadAll() {
        Long userId = LoginUserUtil.requireCurrentUserId();
        messageMapper.update(null, new LambdaUpdateWrapper<SysMessage>()
                .eq(SysMessage::getReceiverId, userId)
                .eq(SysMessage::getStatus, STATUS_PENDING)
                .set(SysMessage::getStatus, STATUS_READ)
                .set(SysMessage::getReadTime, LocalDateTime.now()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createAndPush(SysMessage message) {
        Long userId = LoginUserUtil.requireCurrentUserId();
        message.setCreatedBy(userId);
        message.setUpdatedBy(userId);
        message.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        message.setCreatedTime(LocalDateTime.now());
        message.setUpdatedTime(LocalDateTime.now());
        if (message.getStatus() == null || message.getStatus().isBlank()) {
            message.setStatus(STATUS_PENDING);
        }
        messageMapper.insert(message);
        Long messageId = message.getId();
        pushNewMessage(message.getReceiverId(), messageId, message.getTitle(), message.getType());
        return messageId;
    }

    private void pushNewMessage(Long receiverId, Long messageId, String title, String type) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "NEW_MESSAGE");
        payload.put("messageId", messageId);
        payload.put("title", title);
        payload.put("messageType", type);
        try {
            String json = objectMapper.writeValueAsString(payload);
            sessionHolder.sendToUser(receiverId, json);
        } catch (Exception e) {
            log.warn("WebSocket 推送新消息失败: receiverId={}, messageId={}", receiverId, messageId, e);
        }
    }

    private MessageListResp toListResp(SysMessage m) {
        String senderName = resolveUserName(m.getSenderId());
        return MessageListResp.builder()
                .id(m.getId())
                .type(m.getType())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .title(m.getTitle())
                .content(m.getContent())
                .status(m.getStatus())
                .createdTime(m.getCreatedTime())
                .readTime(m.getReadTime())
                .extraJson(m.getExtraJson())
                .build();
    }

    private MessageDetailResp toDetailResp(SysMessage m) {
        String senderName = resolveUserName(m.getSenderId());
        return MessageDetailResp.builder()
                .id(m.getId())
                .type(m.getType())
                .senderId(m.getSenderId())
                .senderName(senderName)
                .receiverId(m.getReceiverId())
                .title(m.getTitle())
                .content(m.getContent())
                .status(m.getStatus())
                .extraJson(m.getExtraJson())
                .createdTime(m.getCreatedTime())
                .readTime(m.getReadTime())
                .build();
    }

    private String resolveUserName(Long userId) {
        if (userId == null) return null;
        SysUser u = sysUserRepo.findById(userId);
        return u != null ? u.getRealName() : null;
    }
}
