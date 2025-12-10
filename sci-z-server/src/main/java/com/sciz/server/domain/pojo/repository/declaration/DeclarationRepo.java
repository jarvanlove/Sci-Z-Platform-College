package com.sciz.server.domain.pojo.repository.declaration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;

import java.util.List;
import java.time.LocalDate;
import java.util.Map;

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

    /**
     * 根据申报ID列表批量查询申报信息
     *
     * @param declarationIds List<Long> 申报ID列表
     * @return Map<Long, Declaration> 申报ID -> 申报实体
     */
    Map<Long, Declaration> findByIds(List<Long> declarationIds);

    /**
     * 根据时间范围查询申报ID列表（用于项目查询的时间筛选）
     * <p>
     * 查询条件：
     * - startTime: 项目开始时间 >= startTime（如果提供）
     * - endTime: 项目结束时间 <= endTime（如果提供）
     *
     * @param startTime LocalDate 项目开始时间（查询开始时间 >= startTime 的申报，null表示不限制）
     * @param endTime   LocalDate 项目结束时间（查询结束时间 <= endTime 的申报，null表示不限制）
     * @return List<Long> 符合条件的申报ID列表
     */
    List<Long> findIdsByTimeRange(LocalDate startTime, LocalDate endTime);

    /**
     * 根据ID更新申报
     *
     * @param entity Declaration 实体
     * @return boolean 是否更新成功
     */
    boolean updateById(Declaration entity);
}
