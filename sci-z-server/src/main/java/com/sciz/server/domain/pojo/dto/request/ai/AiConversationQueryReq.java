package com.sciz.server.domain.pojo.dto.request.ai;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 会话查询请求
 *
 * @author shihangshang
 * @className AiConversationQueryReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiConversationQueryReq {

    /**
     * 页码
     */
    private Integer pageNo;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方式（ASC/DESC）
     */
    private String sortOrder;

    /**
     * 关键字搜索（标题）
     */
    private String keyword;

    /**
     * 转换为基础分页请求
     *
     * @return BaseQueryReq
     */
    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }
}

