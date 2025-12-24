package com.sciz.server.infrastructure.external.literature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

/**
 * OpenAlex 搜索响应
 *
 * @author JiaWen.Wu
 * @className OpenAlexSearchResp
 * @date 2025-01-24 14:30
 */
@Data
public class OpenAlexSearchResp {
    /**
     * 元数据
     */
    private Meta meta;

    /**
     * 结果列表
     */
    private List<OpenAlexWork> results;

    /**
     * 元数据
     */
    @Data
    public static class Meta {
        /**
         * 总记录数
         */
        private Long count;

        /**
         * 数据库响应时间（毫秒）
         */
        @JsonProperty("db_response_time_ms")
        private Integer dbResponseTimeMs;

        /**
         * 页码
         */
        private Integer page;

        /**
         * 每页数量
         */
        @JsonProperty("per_page")
        private Integer perPage;

        /**
         * 分组数量
         */
        @JsonProperty("groups_count")
        private Integer groupsCount;
    }
}

