package com.sciz.server.infrastructure.external.dify.dto.response;

import java.util.Map;

/**
 * 红头文件解析工作流输出参数配置
 * 类型安全的工作流输出参数定义
 * <p>
 * 字段名与 Dify 工作流输出保持一致，集中管理，避免硬编码
 *
 * @param researchField     String 研究领域
 * @param researchDirection String 研究方向
 * @param researchTopic     String 研究课题
 * @author JiaWen.Wu
 * @className RedHeaderFileWorkflowResp
 * @date 2025-01-26 16:00
 */
public record RedHeaderFileWorkflowResp(
        String researchField,
        String researchDirection,
        String researchTopic) {

    /**
     * 工作流输出字段名常量
     */
    private static final String FIELD_RESEARCH_FIELD = "researchField";
    private static final String FIELD_RESEARCH_DIRECTION = "researchDirection";
    private static final String FIELD_RESEARCH_TOPIC = "researchTopic";

    /**
     * 从 outputs Map 中解析工作流输出参数（类型安全）
     *
     * @param outputs Map<String, Object> 工作流 outputs Map
     * @return RedHeaderFileWorkflowResp 工作流输出参数
     */
    public static RedHeaderFileWorkflowResp from(Map<String, Object> outputs) {
        if (outputs == null) {
            return new RedHeaderFileWorkflowResp("", "", "");
        }

        String researchField = extractString(outputs, FIELD_RESEARCH_FIELD);
        String researchDirection = extractString(outputs, FIELD_RESEARCH_DIRECTION);
        String researchTopic = extractString(outputs, FIELD_RESEARCH_TOPIC);

        return new RedHeaderFileWorkflowResp(researchField, researchDirection, researchTopic);
    }

    /**
     * 从 Map 中安全提取字符串值
     *
     * @param outputs   Map<String, Object> outputs Map
     * @param fieldName String 字段名
     * @return String 字段值，如果不存在则返回空字符串
     */
    private static String extractString(Map<String, Object> outputs, String fieldName) {
        if (!outputs.containsKey(fieldName)) {
            return "";
        }
        Object value = outputs.get(fieldName);
        return value != null ? value.toString() : "";
    }
}
