package com.sciz.server.domain.pojo.dto.request.ai;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;

/**
 * AI 消息查询请求
 *
 * @author shihangshang
 * @className AiMessageQueryReq
 * @date 2025-11-14 10:00
 */
public record AiMessageQueryReq(
        Integer pageNo,
        Integer pageSize,
        String sortBy,
        String sortOrder,
        String conversationId,
        String keyword
) {
    public AiMessageQueryReq {
        var base = BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
        pageNo = base.pageNo();
        pageSize = base.pageSize();
        sortBy = base.sortBy();
        sortOrder = base.sortOrder();
    }

    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }
}
