package com.sciz.server.domain.pojo.dto.request.declaration;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;

/**
 * 申报列表查询请求
 *
 * @param pageNo    Integer 页码，从 1 开始，默认 1
 * @param pageSize  Integer 每页数量，默认 10
 * @param sortBy    String 排序字段
 * @param sortOrder String 排序方式，支持 ASC 或 DESC，默认 DESC
 * @param keyword   String 搜索关键字（申报编号/申报人/研究方向）
 * @param status    Integer 申报状态（null表示全部）
 *
 * @author JiaWen.Wu
 * @className DeclarationListQueryReq
 * @date 2025-11-20 17:30
 */
public record DeclarationListQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        String keyword,
        Integer status) {

    public DeclarationListQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
        keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
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
