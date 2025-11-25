package com.sciz.server.application.service.report.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sciz.server.application.service.report.ReportManagementService;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementCreateReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementListQueryReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementUpdateReq;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementDetailResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementListResp;
import com.sciz.server.domain.pojo.entity.report.ReportManagement;
import com.sciz.server.domain.pojo.repository.report.ReportManagementRepo;
import com.sciz.server.infrastructure.shared.enums.DeleteStatus;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.PageResult;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import com.sciz.server.interfaces.converter.ReportManagementConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * 报告管理应用服务实现类
 *
 * @author JiaWen.Wu
 * @className ReportManagementServiceImpl
 * @date 2025-01-24 14:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportManagementServiceImpl implements ReportManagementService {

    private final ReportManagementRepo reportManagementRepo;
    private final ReportManagementConverter reportManagementConverter;

    /**
     * 报告编号前缀
     */
    private static final String REPORT_NUMBER_PREFIX = "RPT";

    /**
     * 时间戳格式化器（年月日时分秒）
     */
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(ReportManagementCreateReq req) {
        log.info(String.format("开始创建报告管理: projectId=%s, reportType=%s", req.projectId(), req.reportType()));

        try {
            // 1. 获取当前登录用户
            var currentUser = LoginUserUtil.requireCurrentUser();
            var userId = currentUser.userId();
            var realName = currentUser.realName();

            // 2. 转换为实体
            var entity = reportManagementConverter.toEntity(req);

            // 3. 设置报告基本信息
            initializeReportEntity(entity, userId, realName);

            // 4. 保存报告
            var reportId = reportManagementRepo.save(entity);
            if (reportId == null) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告保存失败");
            }

            log.info(String.format("报告保存成功: reportId=%s, number=%s", reportId, entity.getNumber()));
            return reportId;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告创建失败: err=%s", e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告创建失败: " + e.getMessage());
        }
    }

    @Override
    public PageResult<ReportManagementListResp> page(ReportManagementListQueryReq req) {
        log.info(String.format("分页查询报告列表: pageNo=%s, pageSize=%s, keyword=%s", 
                req.pageNo(), req.pageSize(), req.keyword()));

        var baseQuery = req.toBaseQuery();
        var page = new Page<ReportManagement>(baseQuery.pageNo(), baseQuery.pageSize());
        var asc = "ASC".equalsIgnoreCase(baseQuery.sortOrder());
        var sortBy = Optional.ofNullable(baseQuery.sortBy()).orElse("generateTime");

        IPage<ReportManagement> reportPage = reportManagementRepo.page(
                page, req.keyword(), req.status(), req.reportType(), sortBy, asc);

        var records = reportPage.getRecords().stream()
                .map(reportManagementConverter::toListResp)
                .toList();

        Page<ReportManagementListResp> resultPage = new Page<>(reportPage.getCurrent(), reportPage.getSize());
        resultPage.setRecords(records);
        resultPage.setTotal(reportPage.getTotal());
        return PageResult.of(resultPage);
    }

    @Override
    public ReportManagementDetailResp findDetail(Long id) {
        log.info(String.format("查询报告详情: reportId=%s", id));

        // 1. 查询报告实体
        var report = reportManagementRepo.findById(id);
        if (report == null) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
        }

        // 2. 转换为响应对象
        var resp = reportManagementConverter.toDetailResp(report);

        log.info(String.format("查询报告详情成功: reportId=%s", id));
        return resp;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ReportManagementUpdateReq req) {
        log.info(String.format("开始更新报告管理: reportId=%s", req.id()));

        try {
            // 1. 查询报告实体
            var report = reportManagementRepo.findById(req.id());
            if (report == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
            }

            // 2. 更新实体
            reportManagementConverter.updateEntity(report, req);

            // 3. 设置更新信息
            var currentUser = LoginUserUtil.requireCurrentUser();
            report.setUpdatedBy(currentUser.userId());
            report.setUpdatedTime(LocalDateTime.now());

            // 4. 保存更新
            var success = reportManagementRepo.updateById(report);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告更新失败");
            }

            log.info(String.format("报告更新成功: reportId=%s", req.id()));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告更新失败: reportId=%s, err=%s", req.id(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告更新失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteById(Long id) {
        log.info(String.format("开始删除报告管理: reportId=%s", id));

        try {
            // 1. 查询报告实体
            var report = reportManagementRepo.findById(id);
            if (report == null) {
                throw new BusinessException(ResultCode.DATA_NOT_FOUND, "报告不存在");
            }

            // 2. 软删除
            var success = reportManagementRepo.deleteById(id);
            if (!success) {
                throw new BusinessException(ResultCode.DATABASE_OPERATION_FAILED, "报告删除失败");
            }

            log.info(String.format("报告删除成功: reportId=%s", id));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("报告删除失败: reportId=%s, err=%s", id, e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR, "报告删除失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 初始化报告实体基本信息
     *
     * @param entity   报告实体
     * @param userId   用户ID
     * @param realName 用户真实姓名
     */
    private void initializeReportEntity(ReportManagement entity, Long userId, String realName) {
        var now = LocalDateTime.now();
        entity.setNumber(generateReportNumber());
        entity.setCreatorId(userId);
        entity.setCreatorName(realName);
        entity.setStatus("pending"); // 默认状态：待生成
        entity.setIsDeleted(DeleteStatus.NOT_DELETED.getCode());
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
        entity.setCreatedTime(now);
        entity.setUpdatedTime(now);
    }

    /**
     * 生成报告编号
     * 格式：RPT + 年月日时分秒（时间戳）
     * 示例：RPT20250124143025
     *
     * @return 报告编号
     */
    private String generateReportNumber() {
        var timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
        return REPORT_NUMBER_PREFIX + timestamp;
    }
}

