package com.sciz.server.infrastructure.external.dify.dto.response;

import java.util.List;
import java.util.Map;

/**
 * 申报工作流输出参数配置
 * 类型安全的工作流输出参数定义
 * <p>
 * 字段名与 Dify 工作流输出保持一致，集中管理，避免硬编码
 * <p>
 * 根据工作流配置，输出变量包括：fileName, fileUrl, fileType, files
 *
 * @param fileName String 文件名
 * @param fileUrl  String 文件下载URL
 * @param fileType String 文件类型
 * @param files    List<Object> 文件列表
 * @author JiaWen.Wu
 * @className DeclarationWorkflowResp
 * @date 2025-01-26 16:00
 */
public record DeclarationWorkflowResp(
        String fileName,
        String fileUrl,
        String fileType,
        List<Object> files) {

    /**
     * 工作流输出字段名常量
     */
    private static final String FIELD_FILE_NAME = "fileName";
    private static final String FIELD_FILE_URL = "fileUrl";
    private static final String FIELD_FILE_TYPE = "fileType";
    private static final String FIELD_FILES = "files";

    /**
     * 使用部分参数创建（最常用）
     * <p>
     * 当前业务只需要 fileUrl 字段，其他字段使用默认值
     *
     * @param fileUrl String 文件下载URL
     * @return DeclarationWorkflowResp 工作流输出参数
     */
    public static DeclarationWorkflowResp of(String fileUrl) {
        return new DeclarationWorkflowResp("", fileUrl, "", null);
    }

    /**
     * 从 outputs Map 中解析工作流输出参数（类型安全）
     * <p>
     * 从其他对象转换创建，支持所有字段
     *
     * @param outputs Map<String, Object> 工作流 outputs Map
     * @return DeclarationWorkflowResp 工作流输出参数
     */
    public static DeclarationWorkflowResp from(Map<String, Object> outputs) {
        if (outputs == null) {
            return new DeclarationWorkflowResp("", "", "", null);
        }

        String fileName = extractString(outputs, FIELD_FILE_NAME);
        String fileUrl = extractString(outputs, FIELD_FILE_URL);
        // 对 fileUrl 特殊处理：去除所有空格（包括中间的空格），避免 URL 解析错误
        if (fileUrl != null && !fileUrl.isEmpty()) {
            fileUrl = fileUrl.replaceAll("\\s+", "");
        }
        String fileType = extractString(outputs, FIELD_FILE_TYPE);
        List<Object> files = extractList(outputs, FIELD_FILES);

        return new DeclarationWorkflowResp(fileName, fileUrl, fileType, files);
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
        return value != null ? value.toString().trim() : "";
    }

    /**
     * 从 Map 中安全提取列表值
     *
     * @param outputs   Map<String, Object> outputs Map
     * @param fieldName String 字段名
     * @return List<Object> 字段值，如果不存在则返回 null
     */
    @SuppressWarnings("unchecked")
    private static List<Object> extractList(Map<String, Object> outputs, String fieldName) {
        if (!outputs.containsKey(fieldName)) {
            return null;
        }
        Object value = outputs.get(fieldName);
        if (value instanceof List) {
            return (List<Object>) value;
        }
        return null;
    }
}
