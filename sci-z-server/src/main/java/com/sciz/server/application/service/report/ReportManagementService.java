package com.sciz.server.application.service.report;

import com.sciz.server.domain.pojo.dto.request.report.ReportManagementCreateReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementListQueryReq;
import com.sciz.server.domain.pojo.dto.request.report.ReportManagementUpdateReq;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementDetailResp;
import com.sciz.server.domain.pojo.dto.response.report.ReportManagementListResp;
import com.sciz.server.infrastructure.shared.result.PageResult;

/**
 * 报告管理应用服务
 *
 * @author JiaWen.Wu
 * @className ReportManagementService
 * @date 2025-01-24 14:30
 */
public interface ReportManagementService {

    /**
     * 创建报告管理
     *
     * @param req 创建请求
     * @return 报告ID
     */
    Long create(ReportManagementCreateReq req);

    /**
     * 分页查询报告列表
     *
     * @param req 查询请求
     * @return 分页结果
     */
    PageResult<ReportManagementListResp> page(ReportManagementListQueryReq req);

    /**
     * 获取报告详情
     *
     * @param id 报告ID
     * @return 报告详情
     */
    ReportManagementDetailResp findDetail(Long id);

    /**
     * 更新报告管理
     *
     * @param req 更新请求
     */
    void update(ReportManagementUpdateReq req);

    /**
     * 删除报告管理
     *
     * @param id 报告ID
     */
    void deleteById(Long id);
}

