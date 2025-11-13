package com.sciz.server.domain.pojo.repository.user.impl;

import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.sciz.server.domain.pojo.entity.user.SysProfileField;
import com.sciz.server.domain.pojo.entity.user.SysProfileFieldOption;
import com.sciz.server.domain.pojo.mapper.user.SysProfileFieldMapper;
import com.sciz.server.domain.pojo.mapper.user.SysProfileFieldOptionMapper;
import com.sciz.server.domain.pojo.repository.user.SysProfileFieldRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.EnableStatus;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * 用户扩展字段模板仓储实现
 *
 * @author JiaWen.Wu
 * @className SysProfileFieldRepoImpl
 * @date 2025-11-12 11:20
 */
@Repository
public class SysProfileFieldRepoImpl implements SysProfileFieldRepo {

    private final SysProfileFieldMapper fieldMapper;
    private final SysProfileFieldOptionMapper optionMapper;

    public SysProfileFieldRepoImpl(SysProfileFieldMapper fieldMapper,
            SysProfileFieldOptionMapper optionMapper) {
        this.fieldMapper = fieldMapper;
        this.optionMapper = optionMapper;
    }

    /**
     * 查询指定行业下启用的扩展字段模板
     *
     * @param industryType String 行业类型
     * @return List<SysProfileField> 扩展字段模板列表
     */
    @Override
    public List<SysProfileField> listEnabledByIndustry(String industryType) {
        return new LambdaQueryChainWrapper<>(fieldMapper)
                .eq(SysProfileField::getIndustryType, industryType)
                .eq(SysProfileField::getIsEnabled, EnableStatus.ENABLED.getCode())
                .eq(SysProfileField::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysProfileField::getSortOrder)
                .orderByAsc(SysProfileField::getId)
                .list();
    }

    /**
     * 根据字段ID集合查询选项列表
     *
     * @param fieldIds Collection<Long> 字段ID集合
     * @return List<SysProfileFieldOption> 选项列表
     */
    @Override
    public List<SysProfileFieldOption> listOptionsByFieldIds(Collection<Long> fieldIds) {
        if (CollectionUtils.isEmpty(fieldIds)) {
            return List.of();
        }
        return new LambdaQueryChainWrapper<>(optionMapper)
                .in(SysProfileFieldOption::getFieldId, fieldIds)
                .eq(SysProfileFieldOption::getIsDeleted, DeleteStatus.NOT_DELETED.getCode())
                .orderByAsc(SysProfileFieldOption::getSortOrder)
                .orderByAsc(SysProfileFieldOption::getId)
                .list();
    }
}
