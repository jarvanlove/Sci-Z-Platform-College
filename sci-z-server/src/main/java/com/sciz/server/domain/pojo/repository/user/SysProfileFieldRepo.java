package com.sciz.server.domain.pojo.repository.user;

import com.sciz.server.domain.pojo.entity.user.SysProfileField;
import com.sciz.server.domain.pojo.entity.user.SysProfileFieldOption;

import java.util.Collection;
import java.util.List;

/**
 * 用户扩展字段模板仓储接口
 *
 * 提供行业维度的扩展字段与选项查询能力，为应用层提供动态表单元数据。
 *
 * @author JiaWen.Wu
 * @className SysProfileFieldRepo
 * @date 2025-11-12 11:20
 */
public interface SysProfileFieldRepo {

    /**
     * 查询指定行业下启用的扩展字段模板
     *
     * @param industryType String 行业类型
     * @return List<SysProfileField> 扩展字段模板列表
     */
    List<SysProfileField> listEnabledByIndustry(String industryType);

    /**
     * 根据字段ID集合查询选项列表
     *
     * @param fieldIds Collection<Long> 字段ID集合
     * @return List<SysProfileFieldOption> 选项列表
     */
    List<SysProfileFieldOption> listOptionsByFieldIds(Collection<Long> fieldIds);
}
