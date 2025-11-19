package com.sciz.server.domain.pojo.dto.request.ai;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;

/**
 * AI 会话查询请求
 *
 * @author shihangshang
 * @className AiConversationQueryReq
 * @date 2025-11-14 10:00
 */
public record AiConversationQueryReq(
        Integer pageNo,
        Integer pageSize,
        String sortBy,
        String sortOrder,
        Long userId,
        String keyword
) {
    public AiConversationQueryReq {
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
