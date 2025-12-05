package com.sciz.server.infrastructure.external.dify.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sciz.server.domain.pojo.dto.request.file.FileSyncDifyReq;
import com.sciz.server.domain.pojo.dto.response.declaration.RedHeaderFileParseResp;
import com.sciz.server.infrastructure.external.dify.dto.response.FileSyncDifyResp;
import com.sciz.server.infrastructure.external.dify.dto.DifyFileUploadResponse;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowRequest;
import com.sciz.server.infrastructure.external.dify.dto.DifyWorkflowResponse;
import com.sciz.server.infrastructure.external.dify.dto.request.DeclarationWorkflowReq;
import com.sciz.server.infrastructure.external.dify.dto.request.DifyWorkflowReqBuilder;
import com.sciz.server.infrastructure.external.dify.dto.response.DeclarationWorkflowResp;
import com.sciz.server.infrastructure.external.dify.dto.response.DifyDocumentBatchUploadResp;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.IntStream;

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

        /**
         * 批量创建知识库文档（即知识库文件批量上传）
         * 支持一次上传多个文件（最多10个，Dify API限制最多20个）
         *
         * @param datasetId  String 数据集ID（知识库ID）
         * @param files      MultipartFile[] 文件列表
         * @param userId     Long 用户ID（必须传入，因为可能在异步上下文中调用）
         * @param resourceId String 资源ID（用于查找 API Key）
         * @param keyType    String 密钥类型（dataset/file）
         * @return DifyDocumentBatchUploadResp 批量文档上传响应（类型安全，包含文档信息和批次ID）
         */
        @Override
        public DifyDocumentBatchUploadResp createDocumentsBatch(String datasetId, MultipartFile[] files, Long userId,
                        String resourceId, String keyType) {
                var fileCount = files != null ? files.length : 0;
                log.info(String.format("开始批量创建知识库文档: datasetId=%s, fileCount=%s, userId=%s, resourceId=%s, keyType=%s",
                                datasetId, fileCount, userId, resourceId, keyType));

                // 1. 验证文件数量（系统限制最多10个，Dify API限制最多20个）
                if (files == null || files.length == 0) {
                        throw BusinessException.of(ResultCode.BAD_REQUEST, "文件列表不能为空");
                }
                if (files.length > 10) {
                        throw BusinessException.of(ResultCode.BAD_REQUEST, "文件数量超过限制，最多支持10个文件");
                }

                // 2. 将 MultipartFile[] 转换为 List<MultipartFile>
                var fileList = Arrays.asList(files);

                // 3. 调用 Dify API 异步批量上传文档
                log.info(String.format("调用 Dify API 异步批量上传文档: datasetId=%s, fileCount=%s", datasetId, fileCount));
                var future = difyApiService.uploadDocumentsAsync(datasetId, fileList, userId, resourceId, keyType);

                // 4. 等待异步上传完成
                List<ResponseEntity<String>> uploadResults;
                try {
                        uploadResults = future.get();
                        log.info(String.format("异步批量上传完成: datasetId=%s, fileCount=%s, resultCount=%s",
                                        datasetId, fileCount, uploadResults != null ? uploadResults.size() : 0));
                } catch (Exception e) {
                        log.error(String.format("异步批量上传失败: datasetId=%s, fileCount=%s, err=%s",
                                        datasetId, fileCount, e.getMessage()), e);
                        throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED, "异步批量上传失败: %s", e.getMessage());
                }

                // 5. 解析所有上传响应，合并成 DifyDocumentBatchUploadResp 对象
                if (uploadResults == null || uploadResults.isEmpty()) {
                        throw BusinessException.of(ResultCode.FILE_UPLOAD_FAILED, "上传结果为空");
                }

                // 6. 解析每个响应，提取文档信息
                var documentsList = IntStream.range(0, uploadResults.size())
                                .mapToObj(i -> {
                                        var response = uploadResults.get(i);
                                        if (response == null || !response.getStatusCode().is2xxSuccessful()) {
                                                log.warn(String.format(
                                                                "文件上传失败: datasetId=%s, fileIndex=%s, statusCode=%s",
                                                                datasetId, i,
                                                                response != null ? response.getStatusCode() : "null"));
                                                return null;
                                        }

                                        try {
                                                // 验证 HTTP 响应状态
                                                int fileIndex = i + 1;
                                                String responseBody = DifyWorkflowRespBuilder.validateHttpResponse(
                                                                response,
                                                                String.format("Dify 文档上传（文件 %d/%d）", fileIndex,
                                                                                fileCount));

                                                // 解析单个文档上传响应
                                                var singleUploadResp = DifyDocumentBatchUploadResp.from(responseBody,
                                                                objectMapper);

                                                // 提取文档信息（优先使用 documents 数组，否则使用 document 对象）
                                                DifyDocumentBatchUploadResp.DifyDocumentInfo docInfo = null;
                                                if (singleUploadResp.documents() != null
                                                                && !singleUploadResp.documents().isEmpty()) {
                                                        docInfo = singleUploadResp.documents().get(0);
                                                } else if (singleUploadResp.document() != null) {
                                                        docInfo = singleUploadResp.document();
                                                }

                                                if (docInfo != null && docInfo.id() != null
                                                                && !docInfo.id().trim().isEmpty()) {
                                                        // 设置 position（文件在批次中的位置）
                                                        var docInfoWithPosition = new DifyDocumentBatchUploadResp.DifyDocumentInfo(
                                                                        docInfo.id(),
                                                                        docInfo.name(),
                                                                        fileIndex, // position 从 1 开始
                                                                        docInfo.indexingStatus(),
                                                                        docInfo.createdAt(),
                                                                        docInfo.tokens(),
                                                                        docInfo.wordCount(),
                                                                        docInfo.error());

                                                        log.debug(String.format(
                                                                        "解析文档信息成功: datasetId=%s, fileIndex=%s, docId=%s, fileName=%s",
                                                                        datasetId, i, docInfo.id(), docInfo.name()));

                                                        return docInfoWithPosition;
                                                } else {
                                                        log.warn(String.format(
                                                                        "文档信息无效: datasetId=%s, fileIndex=%s, responseBody=%s",
                                                                        datasetId, i, responseBody));
                                                        return null;
                                                }
                                        } catch (Exception e) {
                                                log.error(String.format(
                                                                "解析文档上传响应失败: datasetId=%s, fileIndex=%s, err=%s",
                                                                datasetId, i, e.getMessage()),
                                                                e);
                                                // 返回 null，后续会被过滤掉
                                                return null;
                                        }
                                })
                                .filter(docInfo -> docInfo != null)
                                .toList();

                // 获取第一个文档信息
                DifyDocumentBatchUploadResp.DifyDocumentInfo firstDocument = documentsList.isEmpty() ? null
                                : documentsList.get(0);

                // 7. 构建批量上传响应对象
                var batchUploadResp = new DifyDocumentBatchUploadResp(
                                firstDocument, // 第一个文档信息
                                null, // 异步上传方式不返回 batch 字段
                                documentsList.isEmpty() ? null : documentsList); // 所有文档信息列表

                log.info(String.format("批量创建知识库文档成功: datasetId=%s, fileCount=%s, documentCount=%s",
                                datasetId, fileCount, documentsList.size()));

                return batchUploadResp;
        }

        /**
         * 删除知识库文档
         *
         * @param datasetId  String 数据集ID（知识库ID）
         * @param documentId String 文档ID
         * @param userId     Long 用户ID（必须传入，因为可能在异步上下文中调用）
         * @param resourceId String 资源ID（用于查找 API Key）
         * @param keyType    String 密钥类型（dataset/file/workflow）
         * @return String Dify API 响应体（JSON字符串）
         */
        @Override
        public String deleteDocument(String datasetId, String documentId, Long userId, String resourceId,
                        String keyType) {
                log.info(String.format("开始删除知识库文档: datasetId=%s, documentId=%s, userId=%s, resourceId=%s, keyType=%s",
                                datasetId, documentId, userId, resourceId, keyType));

                // 1. 调用 Dify API 删除文档
                log.info(String.format("调用 Dify API 删除文档: datasetId=%s, documentId=%s", datasetId, documentId));
                ResponseEntity<String> difyResponse = difyApiService.deleteDocument(
                                datasetId, documentId, userId, resourceId, keyType);

                // 2. 验证 HTTP 响应状态
                String responseBody = DifyWorkflowRespBuilder.validateHttpResponse(
                                difyResponse, "Dify 文档删除");

                log.info(String.format("删除知识库文档成功: datasetId=%s, documentId=%s, responseLength=%d",
                                datasetId, documentId, responseBody != null ? responseBody.length() : 0));

                return responseBody;
        }
}
