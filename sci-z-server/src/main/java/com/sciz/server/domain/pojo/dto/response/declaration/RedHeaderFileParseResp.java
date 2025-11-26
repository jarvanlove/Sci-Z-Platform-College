package com.sciz.server.domain.pojo.dto.response.declaration;

/**
 * 红头文件解析响应
 * 包含工作流解析后的3个关键字段
 *
 * @param researchField     String 研究领域
 * @param researchDirection String 研究方向（注意：字段名与工作流输出保持一致）
 * @param researchTopic     String 研究课题
 *
 * @author JiaWen.Wu
 * @className RedHeaderFileParseResp
 * @date 2025-01-24 18:00
 */
public record RedHeaderFileParseResp(
        String researchField,
        String researchDirection,
        String researchTopic) {
}
