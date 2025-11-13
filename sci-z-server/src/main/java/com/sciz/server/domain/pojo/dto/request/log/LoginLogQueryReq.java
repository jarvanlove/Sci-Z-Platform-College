package com.sciz.server.domain.pojo.dto.request.log;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.Min;
import java.time.LocalDate;

/**
 * 登录日志查询请求
 *
 * @param pageNo    Integer 页码，从 1 开始，默认 1
 * @param pageSize  Integer 每页数量，默认 10
 * @param sortBy    String 排序字段
 * @param sortOrder String 排序方式，支持 ASC 或 DESC，默认 DESC
 * @param status    Integer 登录状态（0失败 1成功，null表示全部）
 * @param startDate LocalDate 开始日期
 * @param endDate   LocalDate 结束日期
 * @author JiaWen.Wu
 * @className LoginLogQueryReq
 * @date 2025-11-13 10:30
 */
public record LoginLogQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        Integer status,
        LocalDate startDate,
        LocalDate endDate) {

    public LoginLogQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
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
