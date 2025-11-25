package com.sciz.server.application.service.project.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.project.ProjectService;
import com.sciz.server.domain.pojo.dto.request.project.ProjectCreateReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectListQueryReq;
import com.sciz.server.domain.pojo.dto.request.project.ProjectUpdateReq;
import com.sciz.server.domain.pojo.dto.response.project.ProjectDetailResp;
import com.sciz.server.domain.pojo.dto.response.project.ProjectListResp;
import com.sciz.server.domain.pojo.entity.project.Project;
import com.sciz.server.domain.pojo.repository.project.ProjectRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.enums.ProjectStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.interfaces.converter.ProjectConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 项目应用服务实现类
 *
 * @author JiaWen.Wu
 * @className ProjectServiceImpl
 * @date 2025-01-24 16:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepo projectRepo;
    private final ProjectConverter projectConverter;

    /**
     * 项目编号前缀
     */
    private static final String PROJECT_NUMBER_PREFIX = "PRJ";

    /**
     * 时间戳格式化器（年月日时分秒）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ProjectCreateReq req) {
        log.info(String.format("开始创建项目: name=%s", req.name()));

        try {
            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();

            // 2. 转换为实体
            var entity = projectConverter.toEntity(req);

            // 3. 设置项目基本信息
            initializeProjectEntity(entity, userId);

            // 4. 保存项目
            var projectId = projectRepo.save(entity);
            if (projectId == null) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "项目保存失败");
            }

            log.info(String.format("项目保存成功: projectId=%s, number=%s", projectId, entity.getNumber()));
            return projectId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目创建失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "项目创建失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<ProjectListResp> page(ProjectListQueryReq req) {
        log.info(String.format("分页查询项目列表: pageNo=%s, pageSize=%s, keyword=%s",
                req.pageNo(), req.pageSize(), req.keyword()));

        var baseQuery = req.toBaseQuery();
        var page = new Page<Project>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("createdTime");

        IPage<Project> projectPage = projectRepo.page(
                page, req.keyword(), req.status(), sortBy, asc);

        var records = projectPage.getRecords().stream()
                .map(projectConverter::toListResp)
                .toList();

        Page<ProjectListResp> resultPage = new Page<>(projectPage.getCurrent(), projectPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(projectPage.getTotal());
        return PageResult.of(resultPage);
    }

    @Override
    public ProjectDetailResp findDetail(Long id) {
        log.info(String.format("查询项目详情: projectId=%s", id));

        // 1. 查询项目实体
        var project = projectRepo.findById(id);
        if (project == null) {
            throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
        }

        // 2. 转换为响应对象
        var resp = projectConverter.toDetailResp(project);

        log.info(String.format("查询项目详情成功: projectId=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ProjectUpdateReq req) {
        log.info(String.format("开始更新项目: projectId=%s", req.id()));

        try {
            // 1. 查询项目实体
            var project = projectRepo.findById(req.id());
            if (project == null) {
                throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
            }

            // 2. 更新实体
            projectConverter.updateEntity(project, req);

            // 3. 设置更新信息
            var currentUser = LoginUserUtil.requireCurrentUser();
            project.setUpdatedBy(currentUser.userId());
            project.setUpdatedTime(LocalDateTime.now());

            // 4. 保存更新
            var success = projectRepo.updateById(project);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "项目更新失败");
            }

            log.info(String.format("项目更新成功: projectId=%s", req.id()));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目更新失败: projectId=%s, err=%s", req.id(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "项目更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info(String.format("开始删除项目: projectId=%s", id));

        try {
            // 1. 查询项目实体
            var project = projectRepo.findById(id);
            if (project == null) {
                throw new BusinessException(ResultCode.PROJECT_NOT_FOUND);
            }

            // 2. 软删除
            var success = projectRepo.deleteById(id);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "项目删除失败");
            }

            log.info(String.format("项目删除成功: projectId=%s", id));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("项目删除失败: projectId=%s, err=%s", id, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "项目删除失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化项目实体基本信息
     *
     * @param entity 项目实体
     * @param userId 用户ID
     */
    private void initializeProjectEntity(Project entity, Long userId) {
        var now = LocalDateTime.now();
        entity.setNumber(generateProjectNumber());
        entity.setStatus(Optional.ofNullable(entity.getStatus())
                .orElse(String.valueOf(ProjectStatus.DRAFT.getCode())));
        entity.setProgress(Optional.ofNullable(entity.getProgress()).orElse(0));
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 生成项目编号
     * 格式：PRJ + 年月日时分秒（时间戳）
     * 示例：PRJ20250124143025
     *
     * @return 项目编号
     */
    private String generateProjectNumber() {
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return PROJECT_NUMBER_PREFIX + timestamp;
    }
}
