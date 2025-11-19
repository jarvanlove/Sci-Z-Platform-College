package com.sciz.server.application.service.ai.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.ai.AiMessageService;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiMessageUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiMessageResp;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import com.sciz.server.domain.pojo.entity.ai.AiMessage;
import com.sciz.server.domain.pojo.repository.ai.AiConversationRepo;
import com.sciz.server.domain.pojo.repository.ai.AiMessageRepo;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.AiMessageConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 消息应用服务实现类
 *
 * @author shihangshang
 * @className AiMessageServiceImpl
 * @date 2025-11-14 10:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiMessageServiceImpl implements AiMessageService {

    private final AiMessageRepo messageRepo;
    private final AiConversationRepo conversationRepo;
    private final AiMessageConverter converter;

    /**
     * 创建消息
     *
     * @param req 创建请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiMessageResp create(AiMessageCreateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("创建AI消息: userId=%s, conversationId=%s, role=%s", 
                userId, req.getConversationId(), req.getRole()));

        // 1. 验证会话是否存在且属于当前用户
        Long conversationId;
        try {
            conversationId = Long.parseLong(req.getConversationId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        AiConversation conversation = conversationRepo.findById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权在此会话中创建消息");
        }

        // 2. 如果是用户消息，检查是否是该会话的第一条用户消息（在保存之前检查）
        boolean isFirstUserMessage = false;
        if ("user".equals(req.getRole())) {
            List<AiMessage> existingMessages = messageRepo.listByConversationId(conversationId);
            long existingUserMessageCount = existingMessages.stream()
                    .filter(msg -> "user".equals(msg.getRole()))
                    .count();
            isFirstUserMessage = (existingUserMessageCount == 0);
        }

        // 3. 转换为实体
        AiMessage entity = converter.toEntity(req);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);

        // 4. 保存
        Long id = messageRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 5. 如果是第一条用户消息且会话标题为"新建对话"，则更新会话标题为消息内容
        if (isFirstUserMessage && StringUtils.hasText(req.getContent()) 
                && "新建对话".equals(conversation.getTitle())) {
            String newTitle = req.getContent();
            // 如果内容超过20个字符，截取前20个字符并加上省略号
            if (newTitle.length() > 20) {
                newTitle = newTitle.substring(0, 20) + "...";
            }
            conversation.setTitle(newTitle);
            conversation.setUpdatedBy(userId);
            boolean updateSuccess = conversationRepo.updateById(conversation);
            if (updateSuccess) {
                log.info(String.format("首次用户消息，更新会话标题: conversationId=%s, newTitle=%s", 
                        conversationId, newTitle));
            } else {
                log.warn(String.format("更新会话标题失败: conversationId=%s", conversationId));
            }
        }

        // 6. 查询保存后的实体
        AiMessage savedEntity = messageRepo.findById(id);
        if (savedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 7. 转换为响应
        AiMessageResp resp = converter.toResp(savedEntity);

        log.info(String.format("创建AI消息成功: id=%s", id));
        return resp;
    }

    /**
     * 更新消息
     *
     * @param req 更新请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiMessageResp update(AiMessageUpdateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("更新AI消息: userId=%s, id=%s", userId, req.getId()));

        // 1. 转换ID
        Long id;
        try {
            id = Long.parseLong(req.getId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的消息ID格式");
        }

        // 2. 查询实体
        AiMessage entity = messageRepo.findById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_MESSAGE_NOT_FOUND);
        }

        // 3. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(entity.getConversationId());
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此消息");
        }

        // 4. 更新实体
        converter.updateEntity(entity, req);
        entity.setUpdatedBy(userId);

        // 5. 保存更新
        boolean success = messageRepo.updateById(entity);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 6. 查询更新后的实体
        AiMessage updatedEntity = messageRepo.findById(id);
        if (updatedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 7. 转换为响应
        AiMessageResp resp = converter.toResp(updatedEntity);

        log.info(String.format("更新AI消息成功: id=%s", id));
        return resp;
    }

    /**
     * 根据ID查询详情
     *
     * @param id 消息ID
     * @return 响应
     */
    @Override
    public AiMessageResp findById(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI消息: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long messageId;
        try {
            messageId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的消息ID格式");
        }

        // 2. 查询实体
        AiMessage entity = messageRepo.findById(messageId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_MESSAGE_NOT_FOUND);
        }

        // 3. 转换为响应
        return converter.toResp(entity);
    }

    /**
     * 根据ID查询详情（带权限检查）
     *
     * @param id 消息ID
     * @return 响应
     */
    @Override
    public AiMessageResp findDetail(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI消息详情: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long messageId;
        try {
            messageId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的消息ID格式");
        }

        // 2. 查询实体
        AiMessage entity = messageRepo.findById(messageId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_MESSAGE_NOT_FOUND);
        }

        // 3. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(entity.getConversationId());
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此消息");
        }

        // 4. 转换为响应
        return converter.toResp(entity);
    }

    /**
     * 分页查询消息列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<AiMessageResp> page(AiMessageQueryReq req) {
        return pageByConversationId(req);
    }

    @Override
    public PageResult<AiMessageResp> pageByConversationId(AiMessageQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("分页查询AI消息: userId=%s, conversationId=%s, pageNo=%s, pageSize=%s", 
                userId, req.conversationId(), req.pageNo(), req.pageSize()));

        // 1. 转换会话ID
        Long conversationId;
        try {
            conversationId = Long.parseLong(req.conversationId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(conversationId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此会话的消息");
        }

        // 3. 构建分页对象
        var baseQuery = req.toBaseQuery();
        Page<AiMessage> page = new Page<>(baseQuery.pageNo(), baseQuery.pageSize());

        // 4. 执行分页查询
        var pageResult = messageRepo.pageByConversationId(page, conversationId);

        // 5. 转换为响应列表
        var respList = converter.toRespList(pageResult.getRecords());

        // 6. 构建分页结果
        var result = new PageResult<AiMessageResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );

        log.info(String.format("分页查询AI消息成功: total=%s, current=%s, size=%s", 
                result.getTotal(), result.getCurrent(), result.getSize()));
        return result;
    }

    /**
     * 根据会话ID查询消息列表
     *
     * @param conversationId 会话ID
     * @return 消息列表
     */
    @Override
    public List<AiMessageResp> listByConversationId(String conversationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI消息列表: userId=%s, conversationId=%s", userId, conversationId));

        // 1. 转换会话ID
        Long convId;
        try {
            convId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(convId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此会话的消息");
        }

        // 3. 查询列表
        List<AiMessage> entities = messageRepo.listByConversationId(convId);

        // 4. 转换为响应列表
        return converter.toRespList(entities);
    }

    /**
     * 根据ID删除
     *
     * @param id 消息ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("删除AI消息: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long messageId;
        try {
            messageId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的消息ID格式");
        }

        // 2. 查询实体
        AiMessage entity = messageRepo.findById(messageId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_MESSAGE_NOT_FOUND);
        }

        // 3. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(entity.getConversationId());
        if (conversation == null || !conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此消息");
        }

        // 4. 软删除
        boolean success = messageRepo.deleteById(messageId);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("删除AI消息成功: id=%s", messageId));
    }

    /**
     * 批量删除（根据ID列表）
     *
     * @param ids ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchByIds(List<String> ids) {
        deleteBatch(ids);
    }

    /**
     * 批量删除
     *
     * @param ids ID列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatch(List<String> ids) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("批量删除AI消息: userId=%s, ids=%s", userId, ids));

        // 1. 转换ID列表
        List<Long> messageIds = ids.stream()
                .map(id -> {
                    try {
                        return Long.parseLong(id);
                    } catch (NumberFormatException e) {
                        throw new BusinessException(ResultCode.BAD_REQUEST, "无效的消息ID格式: " + id);
                    }
                })
                .collect(Collectors.toList());

        // 2. 权限检查：验证所有消息的会话是否属于当前用户
        for (Long messageId : messageIds) {
            AiMessage entity = messageRepo.findById(messageId);
            if (entity != null) {
                AiConversation conversation = conversationRepo.findById(entity.getConversationId());
                if (conversation == null || !conversation.getUserId().equals(userId)) {
                    throw new BusinessException(ResultCode.FORBIDDEN, "无权删除消息: " + messageId);
                }
            }
        }

        // 3. 批量软删除
        boolean success = messageRepo.deleteBatchByIds(messageIds);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("批量删除AI消息成功: count=%s", messageIds.size()));
    }

    /**
     * 根据会话ID删除所有消息
     *
     * @param conversationId 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByConversationId(String conversationId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("删除会话所有消息: userId=%s, conversationId=%s", userId, conversationId));

        // 1. 转换会话ID
        Long convId;
        try {
            convId = Long.parseLong(conversationId);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 权限检查：验证会话是否属于当前用户
        AiConversation conversation = conversationRepo.findById(convId);
        if (conversation == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }
        if (!conversation.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此会话的消息");
        }

        // 3. 删除所有消息
        boolean success = messageRepo.deleteByConversationId(convId);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("删除会话所有消息成功: conversationId=%s", convId));
    }
}

