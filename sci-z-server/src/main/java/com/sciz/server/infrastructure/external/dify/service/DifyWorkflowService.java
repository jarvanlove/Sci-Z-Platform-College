package com.sciz.server.infrastructure.external.dify.service;

import com.sciz.server.domain.pojo.dto.request.file.FileSyncDifyReq;
import com.sciz.server.domain.pojo.dto.response.file.FileSyncDifyResp;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;

/**
 * Dify 工作流服务接口
 * 封装所有 Dify 工作流和文件同步相关的交互逻辑
 * <p>
 * 职责：
 * 1. 文件同步到 Dify
 * 2. 执行红头文件解析工作流
 * 3. 执行申报工作流
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowService
 * @date 2025-01-26 11:00
 */
public interface DifyWorkflowService {

    /**
     * 同步文件到 Dify
     *
     * @param req FileSyncDifyReq 同步请求
     * @return FileSyncDifyResp 同步结果（包含 Dify 文件ID）
     */
    FileSyncDifyResp syncFileToDify(FileSyncDifyReq req);

    /**
     * 执行红头文件解析工作流
     *
     * @param difyFileId String Dify 文件ID
     * @param userId     Long 用户ID
     * @param resourceId String 资源ID（工作流ID）
     * @param keyType    String 密钥类型（workflow/file/chatbot）
     * @return RedHeaderFileParseResp 红头文件解析响应（包含研究领域、研究方向、研究课题）
     */
    RedHeaderFileParseResp executeRedHeaderFileWorkflow(String difyFileId, Long userId, String resourceId,
            String keyType);

    /**
     * 执行申报工作流
     *
     * @param inputs     DeclarationWorkflowReq 工作流输入参数（类型安全）
     * @param userId     Long 用户ID
     * @param resourceId String 资源ID（工作流ID）
     * @param keyType    String 密钥类型（workflow/file）
     * @return DeclarationWorkflowResp 申报工作流响应（包含文件下载URL）
     */
    DeclarationWorkflowResp executeDeclarationWorkflow(DeclarationWorkflowReq inputs, Long userId, String resourceId,
            String keyType);
}
