package com.sciz.server.domain.pojo.dto.response.literature;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 文献搜索分页响应
 *
 * @author JiaWen.Wu
 * @className LiteratureSearchPageResp
 * @date 2025-01-24 14:30
 */
@Data
@Builder
public class LiteratureSearchPageResp {
    /**
     * 文献列表
     */
    private List<LiteratureSearchResp> records;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer current;

    /**
     * 每页数量
     */
    private Integer size;

    /**
     * 总页数
     */
    private Integer pages;
}

