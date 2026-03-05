package com.sciz.server.domain.pojo.dto.response.dashboard;

/**
 * 按项目类型分布条目
 *
 * @param type  项目类型（例如：国自然-青年基金）
 * @param count 项目数量
 */
public record DashboardByTypeItemResp(
        String type,
        Long count) {
}

