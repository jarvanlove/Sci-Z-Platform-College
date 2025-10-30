package com.server.agentbackendservices.modules.dify.controller;
import com.server.agentbackendservices.modules.dify.dto.*;
import com.server.agentbackendservices.modules.dify.service.DifyApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
/**
 * Dify API 简化控制器
 * 使用 DifyApiClient 工具类
 * 
 * @author shihang.shang
 * @since 2024-10-22
 */
@Slf4j
@RestController
@RequestMapping("/api/dify")
@RequiredArgsConstructor
@Tag(name = "Dify API", description = "Dify 知识库 API")
public class DifyApiController {
    
    private final DifyApiService difyApiService;
    
    /**
     * 获取数据集列表
     */
    @PostMapping("/datasets/list")
    @Operation(summary = "获取数据集列表")
    public ResponseEntity<String> getDatasets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @Valid @RequestBody BaseDifyRequest request) {
        return difyApiService.getDatasets(page, limit, request.getUserId(), request.getResourceId(), request.getKeyType());
    }
    
    /**
     * 创建数据集
     */
    @PostMapping("/datasets")
    @Operation(summary = "创建数据集")
    public ResponseEntity<String> createDataset(@Valid @RequestBody DifyDatasetRequest request) {
        return difyApiService.createDataset(request, request.getUserId(), request.getResourceId(), request.getKeyType());
    }
    
    /**
     * 获取数据集详情
     */
    @PostMapping("/datasets/{datasetId}")
    @Operation(summary = "获取数据集详情")
    public ResponseEntity<String> getDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody BaseDifyRequest request) {
        return difyApiService.getDataset(datasetId, request.getUserId(), request.getResourceId(), request.getKeyType());
    }
    
    /**
     * 更新数据集
     */
    @PutMapping("/datasets/{datasetId}")
    @Operation(summary = "更新数据集")
    public ResponseEntity<String> updateDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody DifyDatasetRequest request) {
        return difyApiService.updateDataset(datasetId, request, request.getUserId(), request.getResourceId(), request.getKeyType());
    }

    /**
     * 删除数据集
     */
    @PostMapping("/datasets/{datasetId}/delete")
    @Operation(summary = "删除数据集")
    public ResponseEntity<String> deleteDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody BaseDifyRequest request) {
        return difyApiService.deleteDataset(datasetId, request.getUserId(), request.getResourceId(), request.getKeyType());
    }
    /**
     * 检索知识库
     */
    @PostMapping("/datasets/{datasetId}/retrieve")
    @Operation(summary = "检索知识库")
    public ResponseEntity<String> retrieveDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody DifyRetrieveRequest request) {
        return difyApiService.retrieveDataset(datasetId, request, request.getUserId(), request.getResourceId(), request.getKeyType());
    }
    /**
     * 上传文档到数据集 - 先存储文件，再调用Dify API
     */
    @PostMapping("/datasets/{datasetId}/document/upload")
    @Operation(summary = "上传文档到数据集（先存储文件，再调用Dify API）")
    public ResponseEntity<String> uploadDocument(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("resourceId") String resourceId,
            @RequestParam("keyType") String keyType) {
        return difyApiService.uploadDocumentWithFileStorage(datasetId, file, userId, resourceId, keyType);
    }
    /**
     * 执行 Dify 工作流 - 完整版本
     */
    @PostMapping("/workflows/run")
    @Operation(summary = "执行 Dify 工作流")
    public ResponseEntity<String> runWorkflow(@Valid @RequestBody DifyWorkflowRequest request) {
        return difyApiService.runWorkflowWithDynamicKey(request, request.getUserId(), request.getResourceId());
    }

    /**
     * 获取工作流运行状态
     */
    @PostMapping("/workflows/run/status")
    @Operation(summary = "获取工作流运行状态")
    public ResponseEntity<String> getWorkflowRunStatus(@Valid @RequestBody DifyWorkflowStatusRequest request) {
        return difyApiService.getWorkflowRunStatusWithDynamicKey(request.getWorkflowRunId(), request.getUserId(), request.getResourceId());
    }

    /**
     * 获取工作流日志
     */
    @PostMapping("/workflows/logs")
    @Operation(summary = "获取工作流日志")
    public ResponseEntity<String> getWorkflowLogs(@Valid @RequestBody DifyWorkflowLogsRequest request) {
        return difyApiService.getWorkflowLogsWithDynamicKey(request.getPage(), request.getLimit(), request.getUserId(), request.getResourceId());
    }

    /**
     * 上传文件
     */
    @PostMapping("/files/upload")
    @Operation(summary = "上传文件到 Dify")
    public ResponseEntity<String> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("resourceId") String resourceId,
            @RequestParam("keyType") String keyType) {
        return difyApiService.uploadFileWithDynamicKey(userId, file, userId, resourceId);
    }
}