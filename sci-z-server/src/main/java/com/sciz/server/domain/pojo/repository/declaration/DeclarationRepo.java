package com.sciz.server.domain.pojo.repository.declaration;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.declaration.Declaration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
     * 分页查询申报列表（支持项目成员可见：普通用户可见自己创建的或所属项目关联的申报）
     *
     * @param page                        Page<Declaration> 分页对象
     * @param keyword                     String 搜索关键字
     * @param status                      Integer 申报状态
     * @param sortBy                      String 排序字段
     * @param asc                         boolean 是否升序
     * @param includeDeclarationIdsForMember 项目成员可见的申报ID列表（来自其所属项目的 declaration_id），可为 null
     * @return IPage<Declaration> 分页结果
     */
    IPage<Declaration> page(Page<Declaration> page, String keyword, Integer status, String sortBy, boolean asc,
            List<Long> includeDeclarationIdsForMember);

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

    /**
     * 根据关键词检索申报ID（用于产教研智能体匹配：research_topic、research_direction、content_summary）
     * 不做数据权限过滤，仅用于匹配候选。
     *
     * @param keyword  关键词
     * @param maxCount 最大返回数量
     * @return 申报ID列表
     */
    List<Long> findIdsByKeywordForMatch(String keyword, int maxCount);

    /**
     * 根据项目负责人姓名模糊查询申报ID（用于项目列表 keyword 支持负责人筛选）
     * 不做申报创建人维度数据权限过滤，项目列表侧已按项目权限（创建人/成员/负责人）过滤。
     *
     * @param keyword 负责人姓名关键词（project_leader like %keyword%）
     * @return 符合条件的申报ID列表
     */
    List<Long> findIdsByProjectLeaderLike(String keyword);

    /**
     * 统计指定时间范围内的申报数量（按提交时间）
     *
     * @param startTime LocalDateTime 开始时间（包含），null 表示不限制下界
     * @param endTime   LocalDateTime 结束时间（包含），null 表示不限制上界
     * @return Long 申报数量
     */
    Long countBySubmitTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 按申报状态统计数量（带数据权限）
     *
     * @return Map<状态编码, 数量>
     */
    Map<String, Long> countByStatus();
}
