package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 行业配置响应
 *
 * @param industryCode String 行业编码
 * @param industryName String 行业名称
 * @author JiaWen.Wu
 * @className IndustryConfigResp
 * @date 2025-11-10 20:00
 */
public record IndustryConfigResp(
        String industryCode,
        String industryName) {
}

