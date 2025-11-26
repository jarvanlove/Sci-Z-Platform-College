package com.sciz.server.infrastructure.external.dify.dto.request;

import java.util.List;
import java.util.Map;

/**
 * 红头文件解析工作流输入参数配置
 * 类型安全的工作流输入参数定义
 *
 * @param fileUpload DifyFileArrayInput 文件上传参数（数组格式）
 * @author JiaWen.Wu
 * @className RedHeaderFileWorkflowReq
 * @date 2025-01-26 15:00
 */
public record RedHeaderFileWorkflowReq(
        DifyFileArrayInput fileUpload) {

    /**
     * 创建红头文件解析工作流输入参数
     *
     * @param difyFileId String Dify 文件ID
     * @param fileType   String 文件类型（document/image/audio/video）
     * @return RedHeaderFileWorkflowReq 工作流输入参数
     */
    public static RedHeaderFileWorkflowReq of(String difyFileId, String fileType) {
        var fileInput = DifyFileInput.of(difyFileId, fileType);
        return new RedHeaderFileWorkflowReq(new DifyFileArrayInput(List.of(fileInput)));
    }

    /**
     * 转换为 Map（用于 DifyWorkflowRequest）
     *
     * @return Map<String, Object> 工作流输入参数 Map
     */
    public Map<String, Object> toInputsMap() {
        var inputs = new java.util.HashMap<String, Object>();
        // 将 DifyFileArrayInput 转换为 JSON 格式
        var fileArray = fileUpload.files().stream()
                .map(file -> Map.of(
                        "transfer_method", file.transferMethod(),
                        "upload_file_id", file.uploadFileId(),
                        "type", file.type()))
                .toList();
        inputs.put("fileUpload", fileArray);
        return inputs;
    }
}
