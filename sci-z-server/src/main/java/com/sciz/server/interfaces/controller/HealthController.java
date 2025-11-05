package com.sciz.server.interfaces.controller;

import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author JiaWen.Wu
 * @className HealthController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "运行状态检查")
public class HealthController {

    /**
     * 就绪检查
     *
     * @param 
     * @return 服务就绪结果
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "返回服务就绪状态")
    public Result<String> ready() {
        return Result.success("OK");
    }
}
