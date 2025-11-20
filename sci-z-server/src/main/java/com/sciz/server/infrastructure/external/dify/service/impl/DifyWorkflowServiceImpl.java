package com.sciz.server.infrastructure.external.dify.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Dify 工作流服务实现类
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowServiceImpl
 * @date 2025-01-20 15:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyWorkflowServiceImpl implements DifyWorkflowService {

    private final DifyApiService difyApiService;
    private final ObjectMapper objectMapper;

    /**
     * 执行申报工作流
     *
     * @param request 申报工作流请求
     * @return 申报工作流响应
     */
    @Override
    public DeclarationWorkflowResp executeDeclarationWorkflow(DeclarationWorkflowReq request) {
        log.info(String.format("开始执行申报工作流: userId=%s, workflowId=%s",
                request.userId(), request.resourceId()));

        try {
            // 1. 转换为 DifyWorkflowRequest
            var difyRequest = convertToDifyWorkflowRequest(request);

            // 2. 调用 Dify API
            ResponseEntity<String> response = difyApiService.runWorkflowWithDynamicKey(
                    difyRequest,
                    request.userId(),
                    request.resourceId());

            // 3. 检查响应状态
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.error(String.format("申报工作流执行失败: statusCode=%s, body=%s",
                        response.getStatusCode(), response.getBody()));
                throw new BusinessException(ResultCode.SERVER_ERROR,
                        "申报工作流执行失败: " + response.getBody());
            }

            // 4. 解析响应
            var workflowResponse = objectMapper.readValue(
                    response.getBody(),
                    DeclarationWorkflowResp.class);

            log.info(String.format("申报工作流执行成功: workflowRunId=%s, status=%s",
                    workflowResponse.workflowRunId(),
                    workflowResponse.data() != null ? workflowResponse.data().status() : "unknown"));

            return workflowResponse;

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error(String.format("申报工作流执行异常: userId=%s, workflowId=%s, err=%s",
                    request.userId(), request.resourceId(), e.getMessage()), e);
            throw new BusinessException(ResultCode.SERVER_ERROR,
                    "申报工作流执行异常: " + e.getMessage());
        }
    }

    /**
     * 转换为 DifyWorkflowRequest
     *
     * @param request 申报工作流请求
     * @return DifyWorkflowRequest
     */
    private DifyWorkflowRequest convertToDifyWorkflowRequest(DeclarationWorkflowReq request) {
        var difyRequest = new DifyWorkflowRequest();
        difyRequest.setUserId(request.userId());
        difyRequest.setResourceId(request.resourceId());
        difyRequest.setKeyType(request.keyType());
        difyRequest.setInputs(request.inputs());
        difyRequest.setResponseMode(request.responseMode());
        difyRequest.setUser(request.user());
        return difyRequest;
    }
}
