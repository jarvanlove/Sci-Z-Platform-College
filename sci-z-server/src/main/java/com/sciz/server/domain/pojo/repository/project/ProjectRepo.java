package com.sciz.server.domain.pojo.repository.project;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.domain.pojo.entity.project.Project;

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
     * 分页查询项目列表
     *
     * @param page    Page<Project> 分页对象
     * @param keyword String 搜索关键字（项目编号/项目名称）
     * @param status  String 项目状态（null表示全部）
     * @param sortBy  String 排序字段
     * @param asc     boolean 是否升序
     * @return IPage<Project> 分页结果
     */
    IPage<Project> page(Page<Project> page, String keyword, String status, String sortBy, boolean asc);

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
}
