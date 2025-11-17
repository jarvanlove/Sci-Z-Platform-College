package com.sciz.server.domain.pojo.dto.request.ai;

import com.sciz.server.domain.pojo.dto.request.BaseQueryReq;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * AI 消息查询请求
 *
 * @author shihangshang
 * @className AiMessageQueryReq
 * @date 2025-11-14 10:00
 */
@Getter
@Setter
public class AiMessageQueryReq {

    /**
     * 会话ID
     */
    @NotNull(message = "会话ID不能为空")
    private String conversationId;

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
     * 转换为基础分页请求
     *
     * @return BaseQueryReq
     */
    public BaseQueryReq toBaseQuery() {
        return BaseQueryReq.of(pageNo, pageSize, sortBy, sortOrder);
    }
}

