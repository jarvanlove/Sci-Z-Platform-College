package com.sciz.server.infrastructure.external.dify.dto.request;

import java.util.List;
import java.util.Map;

/**
 * 聊天工作流输入参数配置
 * 类型安全的工作流输入参数定义
 *
 * @param file DifyFileArrayInput 文件上传参数（数组格式）
 * @author JiaWen.Wu
 * @className ChatWorkflowReq
 * @date 2025-01-26 15:00
 */
public record ChatWorkflowReq(
        DifyFileArrayInput file) {

    /**
     * 创建聊天工作流输入参数（单个文件）
     *
     * @param difyFileId String Dify 文件ID
     * @param fileType   String 文件类型（document/image/audio/video）
     * @return ChatWorkflowReq 工作流输入参数
     */
    public static ChatWorkflowReq of(String difyFileId, String fileType) {
        var fileInput = DifyFileInput.of(difyFileId, fileType);
        return new ChatWorkflowReq(new DifyFileArrayInput(List.of(fileInput)));
    }

    /**
     * 创建聊天工作流输入参数（多个文件）
     *
     * @param difyFileIds List<String> Dify 文件ID列表
     * @param fileType    String 文件类型（document/image/audio/video）
     * @return ChatWorkflowReq 工作流输入参数
     */
    public static ChatWorkflowReq of(List<String> difyFileIds, String fileType) {
        var fileInputs = difyFileIds.stream()
                .map(fileId -> DifyFileInput.of(fileId, fileType))
                .toList();
        return new ChatWorkflowReq(new DifyFileArrayInput(fileInputs));
    }

    /**
     * 转换为 Map（用于 DifyWorkflowRequest）
     *
     * @return Map<String, Object> 工作流输入参数 Map
     */
    public Map<String, Object> toInputsMap() {
        var inputs = new java.util.HashMap<String, Object>();
        // 将 DifyFileArrayInput 转换为 JSON 格式
        var fileArray = file.files().stream()
                .map(f -> Map.of(
                        "transfer_method", f.transferMethod(),
                        "upload_file_id", f.uploadFileId(),
                        "type", f.type()))
                .toList();
        inputs.put("file", fileArray);
        return inputs;
    }
}
