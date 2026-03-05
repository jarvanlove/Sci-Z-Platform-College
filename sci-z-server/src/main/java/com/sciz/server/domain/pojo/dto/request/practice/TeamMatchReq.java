package com.sciz.server.domain.pojo.dto.request.practice;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 产教研智能体 - 团队匹配请求
 *
 * @param keyword 申报课题关键词（用于检索 declaration / project 的 research_topic、name、description 等）
 * @param limit   返回团队数量上限，默认 6
 */
public record TeamMatchReq(
        @Size(max = 500, message = "关键词长度不能超过500") String keyword,
        @Min(value = 1, message = "limit 最小为 1") @Max(value = 20, message = "limit 最大为 20") Integer limit) {

    /**
     * 默认 limit 为 6
     */
    public int effectiveLimit() {
        return limit != null ? limit : 6;
    }
}
