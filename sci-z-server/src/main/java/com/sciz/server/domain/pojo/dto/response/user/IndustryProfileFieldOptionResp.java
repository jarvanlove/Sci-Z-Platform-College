package com.sciz.server.domain.pojo.dto.response.user;

/**
 * 行业扩展字段选项响应
 *
 * @param optionValue String 选项值
 * @param optionLabel String 选项名称
 * @param isDefault   Integer 是否默认(0:否,1:是)
 * @author JiaWen.Wu
 * @className IndustryProfileFieldOptionResp
 * @date 2025-11-12 11:20
 */
public record IndustryProfileFieldOptionResp(
        String optionValue,
        String optionLabel,
        Integer isDefault) {
}
