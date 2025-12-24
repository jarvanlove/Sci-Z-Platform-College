package com.sciz.server.infrastructure.external.literature.dto;

import lombok.Builder;
import lombok.Data;

/**
 * OpenAlex 搜索请求
 *
 * @author JiaWen.Wu
 * @className OpenAlexSearchReq
 * @date 2025-01-24 14:30
 */
@Data
@Builder
public class OpenAlexSearchReq {
    /**
     * 搜索关键词
     */
    private String search;

    /**
     * 过滤条件
     */
    private String filter;

    /**
     * 每页数量
     */
    private Integer perPage;

    /**
     * 页码
     */
    private Integer page;
}

