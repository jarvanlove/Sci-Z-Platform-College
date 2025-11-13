package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 行业扩展字段响应
 *
 * @param fieldCode  String 字段编码
 * @param fieldLabel String 字段展示名称
 * @param fieldType  String 字段类型(text/select/number/date)
 * @param isRequired Integer 是否必填(0:否,1:是)
 * @param options    List<IndustryProfileFieldOptionResp> 选项列表
 * @author JiaWen.Wu
 * @className IndustryProfileFieldResp
 * @date 2025-11-12 11:20
 */
public record IndustryProfileFieldResp(
        String fieldCode,
        String fieldLabel,
        String fieldType,
        Integer isRequired,
        List<IndustryProfileFieldOptionResp> options) {
}
