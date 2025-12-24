package com.sciz.server.domain.pojo.dto.request.literature;

import lombok.Getter;
import lombok.Setter;

/**
 * 文献搜索请求
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchReq
 * @date 2025-01-24 14:30
 */
@Getter
@Setter
public class LiteratureSearchReq {
    /**
     * 搜索关键词
     */
    private String search;

    /**
     * 发表年份过滤（如：2023-2025）
     */
    private String publicationYearFilter;

    /**
     * 数据源（如：openalex）
     */
    private String dataSource;

    /**
     * 每页数量
     */
    private Integer perPage;

    /**
     * 页码
     */
    private Integer page;
}

