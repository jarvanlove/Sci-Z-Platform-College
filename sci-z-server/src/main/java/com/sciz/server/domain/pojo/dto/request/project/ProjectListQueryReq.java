package com.sciz.server.domain.pojo.dto.request.project;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import com.sciz.server.infrastructure.shared.serializer.FlexibleDateDeserializer;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.List;

/**
 * 项目列表查询请求
 *
 * @param pageNo     Integer 页码，从 1 开始，默认 1
 * @param pageSize   Integer 每页数量，默认 10
 * @param sortBy     String 排序字段
 * @param sortOrder  String 排序方式，支持 ASC 或 DESC，默认 DESC
 * @param keyword    String 搜索关键字（项目编号/项目名称/项目负责人）
 * @param status     String 项目状态（null表示全部）
 * @param startTime  LocalDate 项目开始时间（查询开始时间 >= startTime 的项目，null表示不限制）
 *                   <p>
 *                   前端可传入字符串格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，后端自动转换
 *                   </p>
 * @param endTime    LocalDate 项目结束时间（查询结束时间 <= endTime 的项目，null表示不限制）
 *                   <p>
 *                   前端可传入字符串格式：yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss，后端自动转换
 *                   </p>
 * @param projectIds List&lt;Long&gt; 导出时指定要导出的项目 ID 列表（非空时仅导出这些项目，与列表权限一致）
 *
 * @author JiaWen.Wu
 * @className ProjectListQueryReq
 * @date 2025-01-24 16:00
 */
public record ProjectListQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        String keyword,
        String status,
        @JsonDeserialize(using = FlexibleDateDeserializer.class) LocalDate startTime,
        @JsonDeserialize(using = FlexibleDateDeserializer.class) LocalDate endTime,
        List<Long> projectIds) {

    public ProjectListQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
        keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        status = StringUtils.hasText(status) ? status.trim() : null;
        projectIds = projectIds != null && !projectIds.isEmpty() ? projectIds : null;
    }

    /**
     * 用于内部构造（无 projectIds）的兼容构造
     */
    public ProjectListQueryReq(Integer pageNo, Integer pageSize, String sortBy, String sortOrder,
            String keyword, String status, LocalDate startTime, LocalDate endTime) {
        this(pageNo, pageSize, sortBy, sortOrder, keyword, status, startTime, endTime, null);
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
