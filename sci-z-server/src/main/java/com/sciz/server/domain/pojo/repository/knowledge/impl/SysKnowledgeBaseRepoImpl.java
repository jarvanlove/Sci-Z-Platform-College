package com.sciz.server.domain.pojo.repository.knowledge.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.knowledge.SysKnowledgeBase;
import com.sciz.server.domain.pojo.mapper.knowledge.SysKnowledgeBaseMapper;
import com.sciz.server.domain.pojo.repository.knowledge.SysKnowledgeBaseRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.utils.DataPermissionUtil;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sciz.server.infrastructure.shared.enums.KnowledgeStatus;

/**
 * 知识库仓储实现
 * 
 * @author ShiHang.Shang
 * @className SysKnowledgeBaseRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class SysKnowledgeBaseRepoImpl implements SysKnowledgeBaseRepo {

    private final SysKnowledgeBaseMapper mapper;

    public SysKnowledgeBaseRepoImpl(SysKnowledgeBaseMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(SysKnowledgeBase entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public SysKnowledgeBase findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getId, id)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public SysKnowledgeBase findByName(String name) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getName, name)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    @Override
    public SysKnowledgeBase findByDifyKnowdataId(int difyKnowdataId) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getId, difyKnowdataId)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 分页查询知识库列表
     *
     * @param page   分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @return 分页结果
     */
    @Override
    public IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId) {
        LambdaQueryWrapper<SysKnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 判断是否是管理员：管理员可以看到所有知识库，普通用户只能看到自己创建的知识库
        if (!DataPermissionUtil.isAdmin() && userId != null) {
            queryWrapper.eq(SysKnowledgeBase::getOwnerId, userId);
        }

        // 按创建时间倒序排列
        queryWrapper.orderByDesc(SysKnowledgeBase::getCreatedTime);

        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 分页查询知识库列表（支持关键字搜索）
     *
     * @param page   分页对象
     * @param userId 用户ID（可选，如果为null则查询所有）
     * @param keyword 搜索关键字（知识库名称/描述），可为null
     * @param sortBy 排序字段，可为null
     * @param asc 是否升序，false为降序
     * @return 分页结果
     */
    @Override
    public IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId, String keyword, String sortBy, boolean asc) {
        LambdaQueryWrapper<SysKnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 判断是否是管理员：管理员可以看到所有知识库，普通用户只能看到自己创建的知识库
        if (!DataPermissionUtil.isAdmin() && userId != null) {
            queryWrapper.eq(SysKnowledgeBase::getOwnerId, userId);
        }

        // 关键字搜索（知识库名称/描述）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysKnowledgeBase::getName, keyword)
                    .or()
                    .like(SysKnowledgeBase::getDescription, keyword));
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                queryWrapper.orderByAsc(getSortField(sortBy));
            } else {
                queryWrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            // 默认按更新时间倒序
            queryWrapper.orderByDesc(SysKnowledgeBase::getUpdatedTime);
        }

        return mapper.selectPage(page, queryWrapper);
    }

    @Override
    public IPage<SysKnowledgeBase> pageByCondition(Page<SysKnowledgeBase> page, Long userId,
                                                     List<Long> memberProjectIds, String kbType, String keyword,
                                                     String sortBy, boolean asc) {
        LambdaQueryWrapper<SysKnowledgeBase> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 可见性：管理员不限；普通用户 = 本人创建的 OR 项目成员可见的项目知识库 OR 他人公开的个人知识库（personal + is_shared=1）
        if (!DataPermissionUtil.isAdmin() && userId != null) {
            if (CollectionUtils.isEmpty(memberProjectIds)) {
                queryWrapper.and(w -> w.eq(SysKnowledgeBase::getOwnerId, userId)
                        .or(i -> i.eq(SysKnowledgeBase::getKbType, KnowledgeStatus.PERSONAL.getCode())
                                .eq(SysKnowledgeBase::getIsShared, 1)));
            } else {
                queryWrapper.and(w -> w.eq(SysKnowledgeBase::getOwnerId, userId)
                        .or(i -> i.isNotNull(SysKnowledgeBase::getProjectId)
                                .in(SysKnowledgeBase::getProjectId, memberProjectIds))
                        .or(j -> j.eq(SysKnowledgeBase::getKbType, KnowledgeStatus.PERSONAL.getCode())
                                .eq(SysKnowledgeBase::getIsShared, 1)));
            }
        }

        // 类型筛选
        if (KnowledgeStatus.isValid(kbType)) {
            queryWrapper.eq(SysKnowledgeBase::getKbType, kbType);
        }

        // 关键字搜索
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(SysKnowledgeBase::getName, keyword)
                    .or()
                    .like(SysKnowledgeBase::getDescription, keyword));
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                queryWrapper.orderByAsc(getSortField(sortBy));
            } else {
                queryWrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            queryWrapper.orderByDesc(SysKnowledgeBase::getUpdatedTime);
        }

        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 获取排序字段
     *
     * @param sortBy String 排序字段名
     * @return SFunction<SysKnowledgeBase, ?> 排序字段函数
     */
    private SFunction<SysKnowledgeBase, ?> getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "name" -> SysKnowledgeBase::getName;
            case "createdtime", "create_time" -> SysKnowledgeBase::getCreatedTime;
            case "updatedtime", "update_time" -> SysKnowledgeBase::getUpdatedTime;
            case "filecount", "file_count" -> SysKnowledgeBase::getFileCount;
            default -> SysKnowledgeBase::getCreatedTime;
        };
    }

    /**
     * 根据ID删除知识库（软删除）
     *
     * @param id 知识库ID
     * @return 是否删除成功
     */
    @Override
    public boolean deleteById(Long id) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getId, id)
                .set(SysKnowledgeBase::getIsDeleted, DeleteStatus.DELETED.getCode())
                .update();
    }

    /**
     * 更新知识库
     *
     * @param entity 知识库实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(SysKnowledgeBase entity) {
        if (entity == null || entity.getId() == null) {
            return false;
        }
        int rows = mapper.updateById(entity);
        return rows > 0;
    }

    /**
     * 更新知识库文件数量
     *
     * @param knowledgeId 知识库ID
     * @param fileCount   文件数量
     * @return 是否更新成功
     */
    @Override
    public boolean updateFileCount(Long knowledgeId, Integer fileCount) {
        if (knowledgeId == null) {
            return false;
        }
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(SysKnowledgeBase::getId, knowledgeId)
                .set(SysKnowledgeBase::getFileCount, fileCount)
                .update();
    }

    @Override
    public Map<Long, SysKnowledgeBase> findByProjectIds(List<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return Map.of();
        }
        var records = mapper.selectList(new LambdaQueryWrapper<SysKnowledgeBase>()
                .in(SysKnowledgeBase::getProjectId, projectIds)
                .eq(SysKnowledgeBase::getIsDeleted, DeleteStatus.NOT_DELETED.getCode()));
        return records.stream()
                .collect(Collectors.toMap(SysKnowledgeBase::getProjectId, knowledge -> knowledge,
                        (existing, replacement) -> existing));
    }
}
