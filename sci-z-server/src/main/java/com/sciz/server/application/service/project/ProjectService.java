package com.sciz.server.application.service.project;

import com.sciz.server.domain.pojo.dto.request.file.FileBatchUploadReq;
import com.sciz.server.domain.pojo.dto.request.project.MilestoneDocumentDeleteReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.MilestoneDocumentUploadResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectProgressResp;

import java.util.List;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectSelectResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectStatisticsResp;
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
     * 取消项目
     *
     * @param id 项目ID
     */
    void cancelById(Long id);

    /**
     * 获取项目统计信息
     *
     * @return 项目统计信息（总项目数、进行中、已延期、已完成）
     */
    ProjectStatisticsResp getStatistics();

    /**
     * 批量上传里程碑文档
     *
     * @param req 批量文件上传请求
     * @return 文档上传响应列表
     */
    List<MilestoneDocumentUploadResp> uploadMilestoneDocument(FileBatchUploadReq req);

    /**
     * 删除里程碑文档
     *
     * @param req 删除请求（包含附件ID、项目ID、Dify文档ID）
     */
    void deleteMilestoneDocument(MilestoneDocumentDeleteReq req);

    /**
     * 获取项目进度
     *
     * @param id 项目ID
     * @return 项目进度响应
     */
    ProjectProgressResp findProgress(Long id);

    /**
     * 完成里程碑
     *
     * @param milestoneId 里程碑ID
     */
    void completeMilestone(Long milestoneId);

    /**
     * 取消完成里程碑
     *
     * @param milestoneId 里程碑ID
     */
    void cancelCompleteMilestone(Long milestoneId);

    /**
     * 自动更新单个项目的进度和状态
     * <p>
     * 用于定时任务批量更新项目
     *
     * @param projectId 项目ID
     * @return boolean 是否更新成功
     */
    boolean autoUpdateProjectProgressAndStatus(Long projectId);

    /**
     * 查询项目下拉框列表
     * <p>
     * 返回所有项目的下拉框数据，包括项目ID、编号、名称、状态描述、文档数量、文档总字数、项目进度
     *
     * @return List<ProjectSelectResp> 项目下拉框列表
     */
    List<ProjectSelectResp> findSelectList();
}
