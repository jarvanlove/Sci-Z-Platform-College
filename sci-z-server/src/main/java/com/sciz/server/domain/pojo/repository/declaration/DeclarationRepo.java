package com.sciz.server.domain.pojo.repository.declaration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;

/**
 * 申报仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className DeclarationRepo
 * @date 2025-10-30 11:00
 */
public interface DeclarationRepo {

    /**
     * 保存申报
     *
     * @param entity Declaration 实体
     * @return 生成的主键ID
     */
    Long save(Declaration entity);

    /**
     * 根据ID查询申报
     *
     * @param id Long 申报ID
     * @return Declaration 申报实体
     */
    Declaration findById(Long id);

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
    IPage<Declaration> page(Page<Declaration> page, String keyword, Integer status, String sortBy, boolean asc);

    /**
     * 更新工作流状态和工作流结果
     *
     * @param id             Long 申报ID
     * @param workflowStatus String 工作流状态
     * @param workflowResult String 工作流结果（JSON）
     * @return boolean 是否更新成功
     */
    boolean updateWorkflowStatus(Long id, String workflowStatus, String workflowResult);

    /**
     * 更新申报状态
     *
     * @param id     Long 申报ID
     * @param status String 申报状态
     * @return boolean 是否更新成功
     */
    boolean updateStatus(Long id, String status);
}
