package com.server.agentbackendservices.modules.dify.controller;

import com.server.agentbackendservices.modules.dify.dto.DifyWorkflowLogsRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyWorkflowRequest;
import com.server.agentbackendservices.modules.dify.dto.DifyWorkflowStatusRequest;
import com.server.agentbackendservices.modules.dify.service.DifyApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Dify Workflow 工作流控制器
 *
 * @author shihangshang
 * @className DifyWorkflowController
 * @date 2025-11-18 11:00
 */
@Slf4j
@RestController
@RequestMapping("/api/dify/workflows")
@RequiredArgsConstructor
@Tag(name = "Dify Workflow API", description = "Dify 工作流管理接口")
public class DifyWorkflowController {

    private final DifyApiService difyApiService;

    /**
     * 执行 Dify 工作流
     * 
     * 请求示例：
     * {
     *   "inputs": {
     *     "file": [{
     *       "type": "document",
     *       "transfer_method": "local_file",
     *       "upload_file_id": "0c3b29df-def6-4850-aa68-693fc55e5413"
     *     }]
     *   },
     *   "response_mode": "blocking",
     *   "user": "abc-123",
     *   "userId": 1,
     *   "resourceId": "workflow-id",
     *   "keyType": "workflow"
     * }
     *
     * @param request 工作流请求
     * @return 工作流执行结果
     */
    @PostMapping("/run")
    @Operation(summary = "执行 Dify 工作流", description = "执行 Dify 工作流，支持阻塞和流式两种响应模式")
    public ResponseEntity<String> runWorkflow(@Valid @RequestBody DifyWorkflowRequest request) {
        log.info("执行 Dify 工作流，用户: {}, 工作流ID: {}", request.getUserId(), request.getResourceId());
        return difyApiService.runWorkflowWithDynamicKey(request, request.getUserId(), request.getResourceId());
    }

    /**
     * 获取工作流运行状态
     *
     * @param request 工作流状态请求
     * @return 工作流运行状态
     */
    @PostMapping("/run/status")
    @Operation(summary = "获取工作流运行状态", description = "根据工作流运行ID获取执行状态")
    public ResponseEntity<String> getWorkflowRunStatus(@Valid @RequestBody DifyWorkflowStatusRequest request) {
        log.info("获取工作流运行状态，运行ID: {}, 用户: {}", request.getWorkflowRunId(), request.getUserId());
        return difyApiService.getWorkflowRunStatusWithDynamicKey(
                request.getWorkflowRunId(), 
                request.getUserId(), 
                request.getResourceId());
    }

    /**
     * 获取工作流日志
     *
     * @param request 工作流日志请求
     * @return 工作流日志列表
     */
    @PostMapping("/logs")
    @Operation(summary = "获取工作流日志", description = "分页获取工作流执行日志")
    public ResponseEntity<String> getWorkflowLogs(@Valid @RequestBody DifyWorkflowLogsRequest request) {
        log.info("获取工作流日志，页码: {}, 每页数量: {}, 用户: {}", 
                request.getPage(), request.getLimit(), request.getUserId());
        return difyApiService.getWorkflowLogsWithDynamicKey(
                request.getPage(), 
                request.getLimit(), 
                request.getUserId(), 
                request.getResourceId());
    }
}

