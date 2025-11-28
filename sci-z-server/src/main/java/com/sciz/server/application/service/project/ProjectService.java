package com.sciz.server.application.service.project;

import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 项目应用服务
 * 
 * @author JiaWen.Wu
 * @className ProjectService
 * @date 2025-01-24 16:00
 */
public interface ProjectService {

    /**
     * 创建项目
     *
     * @param req 创建请求
     * @return 项目ID
     */
    Long create(ProjectCreateReq req);

    /**
     * 使用指定的用户ID创建项目（用于异步事件处理，避免Web上下文问题）
     *
     * @param req    创建请求
     * @param userId 用户ID（用于设置 createdBy/updatedBy）
     * @return 项目ID
     */
    Long createWithUserId(ProjectCreateReq req, Long userId);

    /**
     * 分页查询项目列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<ProjectListResp> page(ProjectListQueryReq req);

    /**
     * 获取项目详情
     *
     * @param id 项目ID
     * @return 项目详情
     */
    ProjectDetailResp findDetail(Long id);

    /**
     * 更新项目
     *
     * @param req 更新请求
     */
    void update(ProjectUpdateReq req);

    /**
     * 删除项目
     *
     * @param id 项目ID
     */
    void deleteById(Long id);
}
