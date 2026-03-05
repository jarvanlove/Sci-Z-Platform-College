package com.sciz.server.domain.pojo.repository.project;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.project.Project;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目仓储（领域层抽象）
 * 
 * @author JiaWen.Wu
 * @className ProjectRepo
 * @date 2025-10-30 11:00
 */
public interface ProjectRepo {

    /**
     * 保存项目
     *
     * @param entity Project 实体
     * @return 生成的主键ID
     */
    Long save(Project entity);

    /**
     * 根据ID查询项目
     *
     * @param id Long 项目ID
     * @return Project 项目实体
     */
    Project findById(Long id);

    /**
     * 根据 ID 列表查询项目（带数据权限：仅返回当前用户可见的项目）
     * 用于导出选中项等场景。
     *
     * @param ids 项目 ID 列表
     * @return 符合条件的项目列表（可能少于 ids 数量，因权限过滤）
     */
    List<Project> findByIds(List<Long> ids);

    /**
     * 分页查询项目列表
     *
     * @param page                  Page<Project> 分页对象
     * @param keyword               String 搜索关键字（项目编号/项目名称/项目负责人）
     * @param status                String 项目状态（null表示全部）
     * @param sortBy                String 排序字段
     * @param asc                   boolean 是否升序
     * @param declarationIds        List<Long> 申报ID列表（用于时间范围筛选，null表示不限制）
     * @param declarationIdsByLeader List<Long> 申报ID列表（负责人姓名匹配 keyword 时使用，null 表示不按负责人筛选）
     * @return IPage<Project> 分页结果
     */
    IPage<Project> page(Page<Project> page, String keyword, String status, String sortBy, boolean asc,
            List<Long> declarationIds, List<Long> declarationIdsByLeader);

    /**
     * 根据项目ID列表查询关联的申报ID列表（用于申报列表权限：项目成员/负责人可见其项目关联的申报）
     *
     * @param projectIds 项目ID列表
     * @return 申报ID列表，无重复
     */
    List<Long> findDeclarationIdsByProjectIds(List<Long> projectIds);

    /**
     * 根据项目负责人用户ID查询其负责的项目ID列表（用于申报列表权限：负责人可见其负责项目关联的申报）
     *
     * @param managerId 项目负责人用户ID
     * @return 项目ID列表
     */
    List<Long> findProjectIdsByManagerId(Long managerId);

    /**
     * 根据项目创建人用户ID查询其创建的项目ID列表（用于申报详情权限：创建人可见其创建项目关联的申报）
     *
     * @param createdBy 项目创建人用户ID
     * @return 项目ID列表
     */
    List<Long> findProjectIdsByCreatedBy(Long createdBy);

    /**
     * 根据申报ID查询关联的项目（一对一，用于申报附件权限：项目负责人/成员可访问该申报的附件）
     *
     * @param declarationId 申报ID
     * @return 项目实体，无则 null
     */
    Project findByDeclarationId(Long declarationId);

    /**
     * 根据项目ID列表查询关联的申报ID列表（用于申报列表权限：项目成员/负责人可见其项目关联的申报）
     *
     * @param projectIds 项目ID列表
     * @return 申报ID列表，无重复
     */
    List<Long> findDeclarationIdsByProjectIds(List<Long> projectIds);

    /**
     * 根据项目负责人用户ID查询其负责的项目ID列表（用于申报列表权限：负责人可见其负责项目关联的申报）
     *
     * @param managerId 项目负责人用户ID
     * @return 项目ID列表
     */
    List<Long> findProjectIdsByManagerId(Long managerId);

    /**
     * 根据项目创建人用户ID查询其创建的项目ID列表（用于申报详情权限：创建人可见其创建项目关联的申报）
     *
     * @param createdBy 项目创建人用户ID
     * @return 项目ID列表
     */
    List<Long> findProjectIdsByCreatedBy(Long createdBy);

    /**
     * 根据申报ID查询关联的项目（一对一，用于申报附件权限：项目负责人/成员可访问该申报的附件）
     *
     * @param declarationId 申报ID
     * @return 项目实体，无则 null
     */
    Project findByDeclarationId(Long declarationId);

    /**
     * 更新项目
     *
     * @param entity Project 实体
     * @return boolean 是否更新成功
     */
    boolean updateById(Project entity);

    /**
     * 根据ID删除项目（软删除）
     *
     * @param id Long 项目ID
     * @return boolean 是否删除成功
     */
    boolean deleteById(Long id);

    /**
     * 统计项目数量（根据状态）
     *
     * @param status String 项目状态（null表示统计所有未删除的项目）
     * @return Long 项目数量
     */
    Long countByStatus(String status);

    /**
     * 查询所有活跃项目（用于定时任务自动更新）
     * 排除已取消和已删除的项目
     *
     * @return List<Project> 活跃项目列表
     */
    List<Project> findAllActiveProjects();

    /**
     * 查询所有活跃项目（仅用于系统定时任务，不走数据权限）
     * 排除已取消和已删除的项目
     *
     * @return List<Project> 活跃项目列表
     */
    List<Project> findAllActiveProjectsForTask();

    /**
     * 查询所有项目（用于下拉框，排除已删除的项目）
     *
     * @return List<Project> 项目列表
     */
    List<Project> findAll();

    /**
     * 产教研智能体匹配：按申报ID列表或项目名称/描述关键词查询项目，带数据权限，限制条数
     *
     * @param declarationIds 申报ID列表（来自关键词匹配申报表，可为空）
     * @param keyword        项目名称/描述关键词（可为空）
     * @param limit          最大返回条数
     * @return 项目列表，按更新时间倒序
     */
    List<Project> findForIndustryEducationMatch(List<Long> declarationIds, String keyword, int limit);

    /**
     * 统计指定时间范围内新建的项目数量（按创建时间）
     *
     * @param startTime LocalDateTime 开始时间（包含），null 表示不限制下界
     * @param endTime   LocalDateTime 结束时间（包含），null 表示不限制上界
     * @return Long 项目数量
     */
    Long countByCreatedTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
}
