package com.sciz.server.domain.pojo.dto.response.dashboard;

/**
 * 仪表板状态分布条目
 *
 * @param status 状态编码
 * @param count  数量
 */
public record DashboardStatusItemResp(
        String status,
        Long count) {
}

