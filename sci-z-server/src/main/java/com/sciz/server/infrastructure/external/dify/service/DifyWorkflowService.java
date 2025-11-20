package com.sciz.server.infrastructure.external.dify.service;

import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;

/**
 * Dify 工作流服务接口
 * 专门用于申报工作流的调用
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowService
 * @date 2025-01-20 15:00
 */
public interface DifyWorkflowService {

    /**
     * 执行申报工作流
     *
     * @param request 申报工作流请求
     * @return 申报工作流响应
     */
    DeclarationWorkflowResp executeDeclarationWorkflow(DeclarationWorkflowReq request);
}
