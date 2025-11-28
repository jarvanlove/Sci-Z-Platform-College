package com.sciz.server.infrastructure.external.dify.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.domain.pojo.dto.request.file.FileSyncDifyReq;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.domain.pojo.dto.response.file.FileSyncDifyResp;
import com.sciz.server.infrastructure.external.dify.dto.DifyFileUploadResponse;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowResponse;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.request.DifyWorkflowReqBuilder;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;
import com.sciz.server.infrastructure.external.dify.dto.response.DifyWorkflowRespBuilder;
import com.sciz.server.infrastructure.external.dify.dto.response.RedHeaderFileWorkflowResp;
import com.sciz.server.infrastructure.external.dify.service.DifyApiService;
import com.sciz.server.infrastructure.external.dify.service.DifyWorkflowService;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import com.sciz.server.infrastructure.shared.utils.LoginUserUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

/**
 * Dify 工作流服务实现类
 * 封装所有 Dify 工作流和文件同步相关的交互逻辑
 *
 * @author JiaWen.Wu
 * @className DifyWorkflowServiceImpl
 * @date 2025-01-26 20:00
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DifyWorkflowServiceImpl implements DifyWorkflowService {

        private final DifyApiService difyApiService;
        private final ObjectMapper objectMapper;

        /**
         * 同步文件到 Dify
         *
         * @param req FileSyncDifyReq 同步请求
         * @return FileSyncDifyResp 同步结果（包含 Dify 文件ID）
         */
        @Override
        public FileSyncDifyResp syncFileToDify(FileSyncDifyReq req) {
                var fileName = Optional.ofNullable(req.file())
                                .map(file -> Optional.ofNullable(file.getOriginalFilename()).orElse("unknown"))
                                .orElse("unknown");
                log.info(String.format("开始同步文件到 Dify: fileName=%s, resourceId=%s, keyType=%s",
                                fileName, req.resourceId(), req.keyType()));

                // 1. 获取当前登录用户
                var currentUser = LoginUserUtil.requireCurrentUser();
                Long userId = currentUser.userId();

                // 2. 同步上传文件到 Dify
                log.info("开始上传文件到 Dify");
                ResponseEntity<String> difyResponse = difyApiService.uploadFileWithDynamicKey(
                                userId, req.file(), userId, req.resourceId(), req.keyType());

                // 3. 验证 HTTP 响应状态（使用通用方法，避免重复代码）
                String responseBody = DifyWorkflowRespBuilder.validateHttpResponse(
                                difyResponse, "Dify 文件上传");

                // 4. 解析 Dify 上传响应
                DifyFileUploadResponse difyUploadResult;
                try {
                        difyUploadResult = objectMapper.readValue(
                                        responseBody, DifyFileUploadResponse.class);
                } catch (JsonProcessingException e) {
                        log.error(String.format("解析 Dify 上传响应失败: body=%s, err=%s",
                                        responseBody, e.getMessage()), e);
                        throw BusinessException.of(ResultCode.SERVER_ERROR, "解析 Dify 上传响应失败: %s",
                                        e.getMessage());
                }
                String difyFileId = difyUploadResult.getId();

                if (difyFileId == null || difyFileId.trim().isEmpty()) {
                        log.error(String.format("Dify 响应中缺少文件ID: body=%s", responseBody));
                        throw BusinessException.of(ResultCode.SERVER_ERROR,
                                        "Dify 文件上传失败: 响应中缺少文件ID");
                }

                log.info(String.format("文件已上传到 Dify: difyFileId=%s, fileName=%s", difyFileId, fileName));
                return new FileSyncDifyResp(difyFileId);
        }

        /**
         * 执行红头文件解析工作流
         *
         * @param difyFileId String Dify 文件ID
         * @param userId     Long 用户ID
         * @param resourceId String 资源ID（工作流ID）
         * @param keyType    String 密钥类型（workflow/file）
         * @return RedHeaderFileParseResp 红头文件解析响应（包含研究领域、研究方向、研究课题）
         */
        @Override
        public RedHeaderFileParseResp executeRedHeaderFileWorkflow(String difyFileId, Long userId, String resourceId,
                        String keyType) {
                log.info(String.format("开始调用红头文件解析工作流: difyFileId=%s, workflowId=%s", difyFileId, resourceId));

                // 1. 使用类型安全的构建器构建工作流请求参数
                DifyWorkflowRequest workflowRequest = DifyWorkflowReqBuilder.buildRedHeaderFileWorkflowRequest(
                                difyFileId, userId, "document");

                // 2. 执行工作流
                ResponseEntity<String> workflowResponse = difyApiService.runWorkflowWithDynamicKey(
                                workflowRequest, userId, resourceId, keyType);

                // 3. 验证 HTTP 响应状态
                String workflowResult = DifyWorkflowRespBuilder.validateHttpResponse(
                                workflowResponse, "红头文件解析工作流执行");

                log.info(String.format("红头文件解析工作流执行成功: difyFileId=%s, workflowId=%s", difyFileId, resourceId));
                log.info(String.format("工作流执行结果: %s", workflowResult));

                // 4. 解析工作流响应，提取关键字段（类型安全）
                DifyWorkflowResponse difyResponse = DifyWorkflowRespBuilder.parseFromJson(workflowResult, objectMapper);

                // 5. 检查工作流执行状态
                DifyWorkflowRespBuilder.validateWorkflowStatus(difyResponse, workflowResult);

                // 6. 提取 outputs Map
                Map<String, Object> outputs = DifyWorkflowRespBuilder.extractOutputsAsMap(objectMapper, workflowResult);

                // 7. 使用类型安全的方式解析工作流输出参数
                RedHeaderFileWorkflowResp workflowOutputs = RedHeaderFileWorkflowResp.from(outputs);

                log.info(String.format("解析工作流结果: researchField=%s, researchDirection=%s, researchTopic=%s",
                                workflowOutputs.researchField(), workflowOutputs.researchDirection(),
                                workflowOutputs.researchTopic()));

                return new RedHeaderFileParseResp(
                                workflowOutputs.researchField(),
                                workflowOutputs.researchDirection(),
                                workflowOutputs.researchTopic());
        }

        /**
         * 执行申报工作流
         *
         * @param inputs     DeclarationWorkflowReq 工作流输入参数（类型安全）
         * @param userId     Long 用户ID
         * @param resourceId String 资源ID（工作流ID）
         * @param keyType    String 密钥类型（workflow/file）
         * @return DeclarationWorkflowResp 申报工作流响应（包含文件下载URL）
         */
        @Override
        public DeclarationWorkflowResp executeDeclarationWorkflow(DeclarationWorkflowReq inputs, Long userId,
                        String resourceId, String keyType) {
                log.info(String.format("开始执行申报工作流: userId=%s, resourceId=%s, keyType=%s", userId, resourceId, keyType));

                // 1. 使用类型安全的构建器构建工作流请求
                DifyWorkflowRequest workflowRequest = DifyWorkflowReqBuilder.buildDeclarationWorkflowRequest(
                                inputs.researchFields().values(),
                                inputs.researchDirection().value(),
                                inputs.researchTopic().value(),
                                userId);

                // 2. 调用 Dify 工作流 API（阻塞等待完成，3-6分钟）
                log.info(String.format("调用 Dify 工作流 API: resourceId=%s", resourceId));
                ResponseEntity<String> workflowResponseEntity = difyApiService.runWorkflowWithDynamicKey(
                                workflowRequest, userId, resourceId, keyType);

                // 3. 验证 HTTP 响应状态
                String workflowResult = DifyWorkflowRespBuilder.validateHttpResponse(
                                workflowResponseEntity, "申报工作流执行");

                // 4. 使用构建器解析工作流响应
                DifyWorkflowResponse workflowResponse = DifyWorkflowRespBuilder.parseFromJson(workflowResult,
                                objectMapper);

                // 5. 检查工作流执行状态
                DifyWorkflowRespBuilder.validateWorkflowStatus(workflowResponse, workflowResult);

                // 6. 使用类型安全的方式解析工作流输出参数，获取文件下载 URL
                Map<String, Object> outputs = DifyWorkflowRespBuilder.extractOutputsAsMap(objectMapper, workflowResult);
                DeclarationWorkflowResp workflowOutputs = DeclarationWorkflowResp.from(outputs);

                String fileUrl = workflowOutputs.fileUrl();
                if (fileUrl == null || fileUrl.isEmpty()) {
                        throw BusinessException.of(ResultCode.SERVER_ERROR, "工作流未返回文件下载URL");
                }

                log.info(String.format("申报工作流执行完成: resourceId=%s, fileUrl=%s", resourceId, fileUrl));
                return workflowOutputs;
        }
}
