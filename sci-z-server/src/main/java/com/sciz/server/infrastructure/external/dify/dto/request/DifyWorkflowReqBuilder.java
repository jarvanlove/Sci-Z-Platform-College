package com.sciz.server.infrastructure.external.dify.dto.request;

import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Dify 工作流请求构建器
 * 用于构建工作流请求对象，封装通用逻辑
 * 支持多种文件类型：document、image、audio、video
 * <p>
 * 推荐使用类型安全的工作流输入配置类（如 RedHeaderFileWorkflowReq、DeclarationWorkflowReq）
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowReqBuilder
 * @date 2025-01-24 17:00
 */
public final class DifyWorkflowReqBuilder {

    private DifyWorkflowReqBuilder() {
    }

    // ==================== 类型安全的方法（推荐使用） ====================

    /**
     * 构建红头文件解析工作流请求（类型安全）
     *
     * @param difyFileId String Dify 文件ID
     * @param userId     Long 用户ID
     * @param fileType   String 文件类型（document/image/audio/video），默认 "document"
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildRedHeaderFileWorkflowRequest(String difyFileId, Long userId,
            String fileType) {
        var inputs = RedHeaderFileWorkflowReq.of(difyFileId, fileType);
        return buildWorkflowRequest(inputs.toInputsMap(), userId, "blocking");
    }

    /**
     * 构建红头文件解析工作流请求（类型安全，默认 document 类型）
     *
     * @param difyFileId String Dify 文件ID
     * @param userId     Long 用户ID
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildRedHeaderFileWorkflowRequest(String difyFileId, Long userId) {
        return buildRedHeaderFileWorkflowRequest(difyFileId, userId, "document");
    }

    /**
     * 构建申报工作流请求（类型安全）
     *
     * @param researchFields    List<String> 研究领域列表
     * @param researchDirection String 研究方向
     * @param researchTopic     String 研究课题
     * @param userId            Long 用户ID
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildDeclarationWorkflowRequest(
            List<String> researchFields, String researchDirection, String researchTopic, Long userId) {
        var inputs = DeclarationWorkflowReq.of(researchFields, researchDirection, researchTopic);
        return buildWorkflowRequest(inputs.toInputsMap(), userId, "blocking");
    }

    /**
     * 构建聊天工作流请求（类型安全，单个文件）
     *
     * @param difyFileId String Dify 文件ID
     * @param userId     Long 用户ID
     * @param fileType   String 文件类型（document/image/audio/video），默认 "document"
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildChatWorkflowRequest(String difyFileId, Long userId, String fileType) {
        var inputs = ChatWorkflowReq.of(difyFileId, fileType);
        return buildWorkflowRequest(inputs.toInputsMap(), userId, "blocking");
    }

    /**
     * 构建聊天工作流请求（类型安全，多个文件）
     *
     * @param difyFileIds List<String> Dify 文件ID列表
     * @param userId      Long 用户ID
     * @param fileType    String 文件类型（document/image/audio/video），默认 "document"
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildChatWorkflowRequest(List<String> difyFileIds, Long userId, String fileType) {
        var inputs = ChatWorkflowReq.of(difyFileIds, fileType);
        return buildWorkflowRequest(inputs.toInputsMap(), userId, "blocking");
    }

    /**
     * 构建工作流请求（通用方法，类型安全）
     *
     * @param inputs       Map<String, Object> 工作流输入参数 Map
     * @param userId       Long 用户ID
     * @param responseMode String 响应模式（blocking/streaming）
     * @return DifyWorkflowRequest 工作流请求
     */
    public static DifyWorkflowRequest buildWorkflowRequest(Map<String, Object> inputs, Long userId,
            String responseMode) {
        var workflowRequest = new DifyWorkflowRequest();
        workflowRequest.setInputs(inputs);
        workflowRequest.setResponseMode(responseMode != null ? responseMode : "blocking");
        workflowRequest.setUser(String.valueOf(userId));
        return workflowRequest;
    }

    // ==================== 向后兼容的方法（已废弃，建议使用类型安全的方法） ====================

    /**
     * 构建文件上传工作流请求（支持自定义输入参数名，默认使用 document 类型）
     *
     * @param difyFileId     String Dify 文件ID
     * @param userId         Long 用户ID
     * @param inputParamName String 输入参数名称（如：file_upload、file等）
     * @return DifyWorkflowRequest 工作流请求
     * @deprecated 建议使用 {@link #buildRedHeaderFileWorkflowRequest(String, Long)} 或
     *             {@link #buildChatWorkflowRequest(String, Long, String)}
     */
    @Deprecated
    public static DifyWorkflowRequest buildFileUploadWorkflowRequest(String difyFileId, Long userId,
            String inputParamName) {
        return buildFileUploadWorkflowRequest(difyFileId, userId, inputParamName, "document");
    }

    /**
     * 构建文件上传工作流请求（支持自定义输入参数名和文件类型）
     * <p>
     * 注意：根据 Dify 官方文档，File Array 类型应该使用数组格式。
     * 但如果工作流中的参数类型是单个 File（如"开始-文件上传"节点），则需要传递单个文件对象。
     * <p>
     * 当前实现使用数组格式（符合官方文档的 File Array 类型要求）。
     * 如果遇到 "must be a file" 错误，请检查 Dify 工作流中该参数的类型定义：
     * - 如果是 File Array 类型，使用当前实现（数组格式）✅
     * - 如果是单个 File 类型，需要修改为传递单个文件对象
     *
     * @param difyFileId     String Dify 文件ID
     * @param userId         Long 用户ID
     * @param inputParamName String 输入参数名称（如：fileUpload、file等）
     * @param fileType       String 文件类型（document/image/audio/video）
     * @return DifyWorkflowRequest 工作流请求
     * @deprecated 建议使用
     *             {@link #buildRedHeaderFileWorkflowRequest(String, Long, String)}
     *             或 {@link #buildChatWorkflowRequest(String, Long, String)}
     */
    @Deprecated
    public static DifyWorkflowRequest buildFileUploadWorkflowRequest(String difyFileId, Long userId,
            String inputParamName, String fileType) {
        // 构建文件输入参数
        var fileInputs = new ArrayList<Map<String, Object>>();
        var fileInput = new HashMap<String, Object>();
        fileInput.put("type", fileType);
        fileInput.put("transfer_method", "local_file");
        fileInput.put("upload_file_id", difyFileId);
        fileInputs.add(fileInput);

        // 构建工作流输入参数
        var workflowInputs = new HashMap<String, Object>();
        workflowInputs.put(inputParamName, fileInputs);

        // 构建工作流请求
        var workflowRequest = new DifyWorkflowRequest();
        workflowRequest.setInputs(workflowInputs);
        workflowRequest.setResponseMode("blocking");
        workflowRequest.setUser(String.valueOf(userId));

        return workflowRequest;
    }

    /**
     * 构建单个文件上传工作流请求（用于"开始-文件上传"节点等场景）
     * <p>
     * 注意：此方法用于单个 File 类型的参数（不是 File Array）。
     * 如果工作流中的参数类型是 File Array，请使用 buildFileUploadWorkflowRequest 方法。
     *
     * @param difyFileId     String Dify 文件ID
     * @param userId         Long 用户ID
     * @param inputParamName String 输入参数名称（如：fileUpload、file等）
     * @param fileType       String 文件类型（document/image/audio/video）
     * @return DifyWorkflowRequest 工作流请求
     * @deprecated 建议使用类型安全的工作流输入配置类
     */
    @Deprecated
    public static DifyWorkflowRequest buildSingleFileUploadWorkflowRequest(String difyFileId, Long userId,
            String inputParamName, String fileType) {
        // 构建单个文件输入参数
        var fileInput = new HashMap<String, Object>();
        fileInput.put("type", fileType);
        fileInput.put("transfer_method", "local_file");
        fileInput.put("upload_file_id", difyFileId);

        // 构建工作流输入参数（直接传递单个文件对象）
        var workflowInputs = new HashMap<String, Object>();
        workflowInputs.put(inputParamName, fileInput);

        // 构建工作流请求
        var workflowRequest = new DifyWorkflowRequest();
        workflowRequest.setInputs(workflowInputs);
        workflowRequest.setResponseMode("blocking");
        workflowRequest.setUser(String.valueOf(userId));

        return workflowRequest;
    }

    /**
     * 构建普通工作流请求（用于申报工作流等场景）
     *
     * @param inputs Map<String, Object> 工作流输入参数
     * @param userId Long 用户ID
     * @return DifyWorkflowRequest 工作流请求
     * @deprecated 建议使用
     *             {@link #buildDeclarationWorkflowRequest(List, String, String, Long)}
     *             或类型安全的工作流输入配置类
     */
    @Deprecated
    public static DifyWorkflowRequest buildWorkflowRequest(Map<String, Object> inputs, Long userId) {
        return buildWorkflowRequest(inputs, userId, "blocking");
    }
}
