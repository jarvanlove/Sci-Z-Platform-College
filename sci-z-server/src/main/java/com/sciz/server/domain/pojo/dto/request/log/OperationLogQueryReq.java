package com.sciz.server.domain.pojo.dto.request.log;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import com.sciz.server.infrastructure.shared.enums.LogLevelStatus;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;
import org.springframework.util.StringUtils;

/**
 * 操作日志查询请求
 *
 * @param pageNo    Integer 页码
 * @param pageSize  Integer 每页数量
 * @param sortBy    String 排序字段
 * @param sortOrder String 排序方式
 * @param username  String 用户名关键字（模糊查询）
 * @param startTime LocalDateTime 开始时间
 * @param endTime   LocalDateTime 结束时间
 * @param level     LogLevelStatus 日志级别
 * @author JiaWen.Wu
 * @className OperationLogQueryReq
 * @date 2025-11-14 20:30
 */
public record OperationLogQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        String username,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LogLevelStatus level) {

    private static final int MAX_PAGE_SIZE = 200;

    public OperationLogQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = Math.min(base.pageSize(), MAX_PAGE_SIZE);
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
        username = normalize(username);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    /**
     * 转换为基础分页请求
     *
     * @return BaseQueryReq 基础分页参数
     */
    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }

}
