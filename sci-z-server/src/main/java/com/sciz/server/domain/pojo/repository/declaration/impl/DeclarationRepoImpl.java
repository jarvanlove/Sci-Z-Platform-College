package com.sciz.server.domain.pojo.repository.declaration.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;
import com.sciz.server.domain.pojo.mapper.declaration.DeclarationMapper;
import com.sciz.server.domain.pojo.repository.declaration.DeclarationRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

/**
 * 申报仓储实现
 * 
 * @author JiaWen.Wu
 * @className DeclarationRepoImpl
 * @date 2025-10-30 11:00
 */
@Repository
public class DeclarationRepoImpl implements DeclarationRepo {

    private final DeclarationMapper mapper;

    public DeclarationRepoImpl(DeclarationMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Long save(Declaration entity) {
        int rows = mapper.insert(entity);
        return rows > 0 ? entity.getId() : null;
    }

    @Override
    public Declaration findById(Long id) {
        return new LambdaQueryChainWrapper<>(mapper)
                .eq(Declaration::getId, id)
                .eq(Declaration::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .one();
    }

    /**
     * 分页查询申报列表
     *
     * @param page    Page<Declaration> 分页对象
     * @param keyword String 搜索关键字（申报编号/申报人/研究方向）
     * @param status  Integer 申报状态（null表示全部）
     * @param sortBy  String 排序字段
     * @param asc     boolean 是否升序
     * @return IPage<Declaration> 分页结果
     */
    @Override
    public IPage<Declaration> page(Page<Declaration> page, String keyword, Integer status, String sortBy, boolean asc) {
        var queryWrapper = new LambdaQueryWrapper<Declaration>();
        queryWrapper.eq(Declaration::getIsDeleted, DeleteStatus.NOT_DELETED.getCode());

        // 关键字搜索（申报编号/申报人/研究方向）
        if (StringUtils.hasText(keyword)) {
            queryWrapper.and(wrapper -> wrapper
                    .like(Declaration::getNumber, keyword)
                    .or()
                    .like(Declaration::getApplicantName, keyword)
                    .or()
                    .like(Declaration::getResearchDirection, keyword));
        }

        // 申报状态筛选
        if (status != null) {
            queryWrapper.eq(Declaration::getStatus, String.valueOf(status));
        }

        // 排序
        if (StringUtils.hasText(sortBy)) {
            if (asc) {
                queryWrapper.orderByAsc(getSortField(sortBy));
            } else {
                queryWrapper.orderByDesc(getSortField(sortBy));
            }
        } else {
            // 默认按提交时间倒序
            queryWrapper.orderByDesc(Declaration::getSubmitTime);
        }

        return mapper.selectPage(page, queryWrapper);
    }

    /**
     * 获取排序字段
     *
     * @param sortBy String 排序字段名
     * @return SFunction<Declaration, ?> 排序字段函数
     */
    private SFunction<Declaration, ?> getSortField(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "number" -> Declaration::getNumber;
            case "applicantname", "applicant_name" -> Declaration::getApplicantName;
            case "submittime", "submit_time" -> Declaration::getSubmitTime;
            case "createdtime", "created_time" -> Declaration::getCreatedTime;
            case "updatedtime", "updated_time" -> Declaration::getUpdatedTime;
            default -> Declaration::getSubmitTime;
        };
    }

    @Override
    public boolean updateWorkflowStatus(Long id, String workflowStatus, String workflowResult) {
        var updateWrapper = new LambdaUpdateChainWrapper<>(mapper)
                .eq(Declaration::getId, id)
                .set(Declaration::getWorkflowStatus, workflowStatus);

        if (workflowResult != null) {
            updateWrapper.set(Declaration::getWorkflowResult, workflowResult);
        }

        return updateWrapper.update();
    }

    @Override
    public boolean updateStatus(Long id, String status) {
        return new LambdaUpdateChainWrapper<>(mapper)
                .eq(Declaration::getId, id)
                .set(Declaration::getStatus, status)
                .update();
    }
}
