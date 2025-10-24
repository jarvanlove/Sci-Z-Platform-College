package com.server.agentbackendservices.modules.dify.controller;

import com.server.agentbackendservices.modules.dify.dto.DifyDatasetRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyRetrieveRequest;
import com.server.agentbackendservices.modules.dify.service.DifyApiService;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
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
 * @author jarvanlove
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
    @GetMapping("/datasets")
    @Operation(summary = "获取数据集列表")
    public ResponseEntity<String> getDatasets(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return difyApiService.getDatasets(page, limit);
    }
    
    /**
     * 创建数据集
     */
    @PostMapping("/datasets")
    @Operation(summary = "创建数据集")
    public ResponseEntity<String> createDataset(@Valid @RequestBody DifyDatasetRequest request) {
        return difyApiService.createDataset(request);
    }
    
    /**
     * 获取数据集详情
     */
    @GetMapping("/datasets/{datasetId}")
    @Operation(summary = "获取数据集详情")
    public ResponseEntity<String> getDataset(@PathVariable String datasetId) {
        return difyApiService.getDataset(datasetId);
    }
    
    /**
     * 更新数据集
     */
    @PutMapping("/datasets/{datasetId}")
    @Operation(summary = "更新数据集")
    public ResponseEntity<String> updateDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody DifyDatasetRequest request) {
        return difyApiService.updateDataset(datasetId, request);
    }
    /**
     * 删除数据集
     */
    @DeleteMapping("/datasets/{datasetId}")
    @Operation(summary = "删除数据集")
    public ResponseEntity<String> deleteDataset(@PathVariable String datasetId) {
        return difyApiService.deleteDataset(datasetId);
    }
    /**
     * 检索知识库
     */
    @PostMapping("/datasets/{datasetId}/retrieve")
    @Operation(summary = "检索知识库")
    public ResponseEntity<String> retrieveDataset(
            @PathVariable String datasetId,
            @Valid @RequestBody DifyRetrieveRequest request) {
        return difyApiService.retrieveDataset(datasetId, request);
    }
    /**
     * 上传文档到数据集 - 先存储文件，再调用Dify API
     */
    @PostMapping("/datasets/{datasetId}/document/upload")
    @Operation(summary = "上传文档到数据集（先存储文件，再调用Dify API）")
    public ResponseEntity<String> uploadDocument(
            @PathVariable String datasetId,
            @RequestParam("file") MultipartFile file) {
        
        return difyApiService.uploadDocumentWithFileStorage(datasetId, file);
    }
}
