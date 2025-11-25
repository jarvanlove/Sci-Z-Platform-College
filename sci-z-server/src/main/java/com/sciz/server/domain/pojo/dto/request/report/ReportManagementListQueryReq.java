package com.sciz.server.domain.pojo.dto.request.report;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;

/**
 * 报告管理列表查询请求
 *
 * @param pageNo         Integer 页码，从 1 开始，默认 1
 * @param pageSize       Integer 每页数量，默认 10
 * @param sortBy         String 排序字段
 * @param sortOrder      String 排序方式，支持 ASC 或 DESC，默认 DESC
 * @param keyword        String 搜索关键字（报告编号/项目名称/创建人）
 * @param status         String 报告状态（null表示全部）
 * @param reportType     String 报告类型（null表示全部）
 *
 * @author JiaWen.Wu
 * @className ReportManagementListQueryReq
 * @date 2025-01-24 14:30
 */
public record ReportManagementListQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        String keyword,
        String status,
        String reportType) {

    public ReportManagementListQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
        keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        status = StringUtils.hasText(status) ? status.trim() : null;
        reportType = StringUtils.hasText(reportType) ? reportType.trim() : null;
    }

    /**
     * 转换为 BaseQueryReq
     *
     * @return BaseQueryReq 基础查询请求
     */
    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }
}

