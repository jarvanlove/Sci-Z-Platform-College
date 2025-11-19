package com.sciz.server.application.service.ai.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.ai.AiConversationService;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationCreateReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationQueryReq;
import com.sciz.server.domain.pojo.dto.request.ai.AiConversationUpdateReq;
import com.sciz.server.domain.pojo.dto.response.ai.AiConversationResp;
import com.sciz.server.domain.pojo.entity.ai.AiConversation;
import com.sciz.server.domain.pojo.repository.ai.AiConversationRepo;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.interfaces.converter.AiConversationConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 会话应用服务实现类
 *
 * @author shihangshang
 * @className AiConversationServiceImpl
 * @date 2025-11-14 10:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiConversationServiceImpl implements AiConversationService {

    private final AiConversationRepo conversationRepo;
    private final AiConversationConverter converter;

    /**
     * 创建会话
     *
     * @param req 创建请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversationResp create(AiConversationCreateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("创建AI会话: userId=%s, title=%s", userId, req.getTitle()));

        // 1. 转换为实体
        AiConversation entity = converter.toEntity(req);
        entity.setUserId(userId);
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        if (req.getIsPinned() == null) {
            entity.setIsPinned(0);
        }
        
        // 2. 如果标题为空或空字符串，设置为默认值"新建对话"
        if (!StringUtils.hasText(entity.getTitle())) {
            entity.setTitle("新建对话");
            log.info(String.format("会话标题为空，设置为默认值: 新建对话"));
        }

        // 3. 保存
        Long id = conversationRepo.save(entity);
        if (id == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 4. 查询保存后的实体
        AiConversation savedEntity = conversationRepo.findById(id);
        if (savedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 5. 转换为响应
        AiConversationResp resp = converter.toResp(savedEntity);

        log.info(String.format("创建AI会话成功: id=%s", id));
        return resp;
    }

    /**
     * 更新会话
     *
     * @param req 更新请求
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversationResp update(AiConversationUpdateReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("更新AI会话: userId=%s, id=%s", userId, req.getId()));

        // 1. 转换ID
        Long id;
        try {
            id = Long.parseLong(req.getId());
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 查询实体
        AiConversation entity = conversationRepo.findById(id);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }

        // 3. 权限检查：只能更新自己的会话
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此会话");
        }

        // 4. 更新实体
        converter.updateEntity(entity, req);
        entity.setUpdatedBy(userId);

        // 5. 保存更新
        boolean success = conversationRepo.updateById(entity);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 6. 查询更新后的实体
        AiConversation updatedEntity = conversationRepo.findById(id);
        if (updatedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 7. 转换为响应
        AiConversationResp resp = converter.toResp(updatedEntity);

        log.info(String.format("更新AI会话成功: id=%s", id));
        return resp;
    }

    /**
     * 根据ID查询详情
     *
     * @param id 会话ID
     * @return 响应
     */
    @Override
    public AiConversationResp findById(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI会话: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long conversationId;
        try {
            conversationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 查询实体
        AiConversation entity = conversationRepo.findById(conversationId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }

        // 3. 转换为响应
        return converter.toResp(entity);
    }

    /**
     * 根据ID查询详情（带权限检查）
     *
     * @param id 会话ID
     * @return 响应
     */
    @Override
    public AiConversationResp findDetail(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI会话详情: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long conversationId;
        try {
            conversationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 查询实体
        AiConversation entity = conversationRepo.findById(conversationId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }

        // 3. 权限检查：只能查看自己的会话
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权查看此会话");
        }

        // 4. 转换为响应
        return converter.toResp(entity);
    }

    /**
     * 分页查询会话列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<AiConversationResp> page(AiConversationQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("分页查询AI会话: userId=%s, pageNo=%s, pageSize=%s", 
                userId, req.pageNo(), req.pageSize()));

        // 1. 构建分页对象
        var baseQuery = req.toBaseQuery();
        Page<AiConversation> page = new Page<>(baseQuery.pageNo(), baseQuery.pageSize());

        // 2. 执行分页查询
        var pageResult = conversationRepo.pageByUserId(page, userId);

        // 3. 关键字过滤（如果提供）
        List<AiConversation> filteredList = pageResult.getRecords();
        if (StringUtils.hasText(req.keyword())) {
            filteredList = filteredList.stream()
                    .filter(conversation -> conversation.getTitle().contains(req.keyword()))
                    .collect(Collectors.toList());
        }

        // 4. 转换为响应列表
        var respList = converter.toRespList(filteredList);

        // 5. 构建分页结果
        var result = new PageResult<AiConversationResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );

        log.info(String.format("分页查询AI会话成功: total=%s, current=%s, size=%s", 
                result.getTotal(), result.getCurrent(), result.getSize()));
        return result;
    }

    /**
     * 分页查询（根据用户ID）
     *
     * @param req 查询请求
     * @return 分页结果
     */
    @Override
    public PageResult<AiConversationResp> pageByUserId(AiConversationQueryReq req) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("分页查询AI会话: userId=%s, pageNo=%s, pageSize=%s", 
                userId, req.pageNo(), req.pageSize()));

        // 1. 构建分页对象
        var baseQuery = req.toBaseQuery();
        Page<AiConversation> page = new Page<>(baseQuery.pageNo(), baseQuery.pageSize());

        // 2. 执行分页查询
        var pageResult = conversationRepo.pageByUserId(page, userId);

        // 3. 转换为响应列表
        var respList = converter.toRespList(pageResult.getRecords());

        // 4. 构建分页结果
        var result = new PageResult<AiConversationResp>(
                respList,
                pageResult.getTotal(),
                pageResult.getCurrent(),
                pageResult.getSize()
        );

        log.info(String.format("分页查询AI会话成功: total=%s, current=%s, size=%s", 
                result.getTotal(), result.getCurrent(), result.getSize()));
        return result;
    }

    /**
     * 查询当前用户的会话列表
     *
     * @return 会话列表
     */
    @Override
    public List<AiConversationResp> list() {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("查询AI会话列表: userId=%s", userId));

        // 1. 查询列表
        List<AiConversation> entities = conversationRepo.listByUserId(userId);

        // 2. 转换为响应列表
        return converter.toRespList(entities);
    }

    /**
     * 根据用户ID查询列表
     *
     * @param userId 用户ID
     * @return 列表
     */
    @Override
    public List<AiConversationResp> listByUserId(Long userId) {
        log.info(String.format("查询AI会话列表: userId=%s", userId));

        // 1. 查询列表
        List<AiConversation> entities = conversationRepo.listByUserId(userId);

        // 2. 转换为响应列表
        return converter.toRespList(entities);
    }

    /**
     * 根据ID删除
     *
     * @param id 会话ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(String id) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("删除AI会话: userId=%s, id=%s", userId, id));

        // 1. 转换ID
        Long conversationId;
        try {
            conversationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 查询实体
        AiConversation entity = conversationRepo.findById(conversationId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }

        // 3. 权限检查：只能删除自己的会话
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权删除此会话");
        }

        // 4. 软删除
        boolean success = conversationRepo.deleteById(conversationId);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("删除AI会话成功: id=%s", conversationId));
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
        log.info(String.format("批量删除AI会话: userId=%s, ids=%s", userId, ids));

        // 1. 转换ID列表
        List<Long> conversationIds = ids.stream()
                .map(id -> {
                    try {
                        return Long.parseLong(id);
                    } catch (NumberFormatException e) {
                        throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式: " + id);
                    }
                })
                .collect(Collectors.toList());

        // 2. 权限检查：只能删除自己的会话
        for (Long conversationId : conversationIds) {
            AiConversation entity = conversationRepo.findById(conversationId);
            if (entity != null && !entity.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.FORBIDDEN, "无权删除会话: " + conversationId);
            }
        }

        // 3. 批量软删除
        boolean success = conversationRepo.deleteBatchByIds(conversationIds);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        log.info(String.format("批量删除AI会话成功: count=%s", conversationIds.size()));
    }

    /**
     * 更新置顶状态
     *
     * @param id 会话ID
     * @param isPinned 是否置顶(0:否,1:是)
     * @return 响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiConversationResp updatePinnedStatus(String id, Integer isPinned) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info(String.format("更新AI会话置顶状态: userId=%s, id=%s, isPinned=%s", userId, id, isPinned));

        // 1. 转换ID
        Long conversationId;
        try {
            conversationId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "无效的会话ID格式");
        }

        // 2. 查询实体
        AiConversation entity = conversationRepo.findById(conversationId);
        if (entity == null) {
            throw new BusinessException(ResultCode.AI_CONVERSATION_NOT_FOUND);
        }

        // 3. 权限检查：只能更新自己的会话
        if (!entity.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权更新此会话");
        }

        // 4. 更新置顶状态
        boolean success = conversationRepo.updatePinnedStatus(conversationId, isPinned);
        if (!success) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 5. 查询更新后的实体
        AiConversation updatedEntity = conversationRepo.findById(conversationId);
        if (updatedEntity == null) {
            throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED);
        }

        // 6. 转换为响应
        return converter.toResp(updatedEntity);
    }
}

