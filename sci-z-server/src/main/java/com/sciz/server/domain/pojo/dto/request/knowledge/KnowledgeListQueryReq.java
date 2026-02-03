package com.sciz.server.domain.pojo.dto.request.knowledge;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.Min;
import org.springframework.util.StringUtils;

/**
 * 知识库列表查询请求
 *
 * @param pageNo    Integer 页码，从 1 开始，默认 1
 * @param pageSize  Integer 每页数量，默认 10
 * @param sortBy    String 排序字段
 * @param sortOrder String 排序方式，支持 ASC 或 DESC，默认 DESC
 * @param keyword   String 搜索关键字（知识库名称/描述），非必传
 * @param kbType    String 知识库类型筛选：personal=个人知识库，project=项目知识库，不传=全部
 *
 * @author JiaWen.Wu
 * @className KnowledgeListQueryReq
 * @date 2026-01-26 15:00
 */
public record KnowledgeListQueryReq(
        @Min(value = 1, message = "页码最小为 1") Integer pageNo,
        @Min(value = 1, message = "每页数量最小为 1") Integer pageSize,
        String sortBy,
        String sortOrder,
        String keyword,
        String kbType) {

    public KnowledgeListQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
        keyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        kbType = StringUtils.hasText(kbType) ? kbType.trim().toLowerCase() : null;
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
