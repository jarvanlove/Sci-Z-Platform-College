package com.sciz.server.interfaces.controller;

import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeHealth;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * 提供业务层的健康检查接口，封装 Actuator 的健康检查功能
 * 适用于负载均衡器、监控系统和运维人员使用
 * 
 * 注意：Actuator 的标准端点 `/actuator/health` 仍然可用，本控制器提供更友好的业务接口
 * 
 * @author JiaWen.Wu
 * @className HealthController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "健康检查", description = "运行状态检查")
@RequiredArgsConstructor
public class HealthController {

    private final HealthEndpoint healthEndpoint;

    /**
     * 就绪检查（轻量级）
     * 
     * 用于负载均衡器的快速健康检查，仅返回简单的就绪状态
     * 不进行深度检查，响应速度快
     *
     * @return 服务就绪结果
     */
    @GetMapping("/ready")
    @Operation(summary = "就绪检查", description = "轻量级就绪检查，用于负载均衡器快速判断服务是否就绪")
    public Result<String> ready() {
        return Result.success("OK");
    }

    /**
     * 存活检查（轻量级）
     * 
     * 用于判断服务是否存活，不检查依赖组件
     * 适用于容器编排系统的存活探针
     *
     * @return 服务存活结果
     */
    @GetMapping("/live")
    @Operation(summary = "存活检查", description = "轻量级存活检查，用于判断服务是否存活，不检查依赖组件")
    public Result<String> live() {
        return Result.success("ALIVE");
    }

    /**
     * 完整健康检查
     * 
     * 检查所有中间件组件的健康状态（数据库、Redis、Kafka、MinIO）
     * 返回详细的健康状态信息
     *
     * @return 完整健康检查结果
     */
    @GetMapping("/check")
    @Operation(summary = "完整健康检查", description = "检查所有中间件组件的健康状态，返回详细状态信息")
    public Result<Map<String, Object>> check() {
        HealthComponent healthComponent = healthEndpoint.health();

        Map<String, Object> result = new HashMap<>();
        result.put("status", healthComponent.getStatus().getCode());

        // 构建组件状态信息
        if (healthComponent instanceof CompositeHealth compositeHealth) {
            Map<String, Object> components = new HashMap<>();

            compositeHealth.getComponents().forEach((name, component) -> {
                Map<String, Object> componentStatus = new HashMap<>();
                componentStatus.put("status", component.getStatus().getCode());
                components.put(name, componentStatus);
            });

            result.put("components", components);
        } else if (healthComponent instanceof Health health) {
            // 如果不是复合健康状态，过滤敏感信息后返回详情
            Map<String, Object> details = health.getDetails();
            if (details != null && !details.isEmpty()) {
                Map<String, Object> safeDetails = new HashMap<>();
                details.forEach((key, value) -> {
                    if (!isSensitiveKey(key)) {
                        safeDetails.put(key, value);
                    }
                });
                if (!safeDetails.isEmpty()) {
                    result.put("details", safeDetails);
                }
            }
        }

        return Result.success(result);
    }

    /**
     * 单个组件健康检查
     * 
     * 检查指定中间件组件的健康状态
     * 支持的组件：database、redis、kafka、minio
     *
     * @param component 组件名称（database、redis、kafka、minio）
     * @return 组件健康状态
     */
    @GetMapping("/component/{component}")
    @Operation(summary = "组件健康检查", description = "检查指定中间件组件的健康状态，支持：database、redis、kafka、minio")
    public Result<Map<String, Object>> checkComponent(
            @Parameter(description = "组件名称", example = "database") @PathVariable String component) {
        HealthComponent healthComponent = healthEndpoint.health();

        Map<String, Object> result = new HashMap<>();
        result.put("component", component);

        // 如果是复合健康状态，尝试获取指定组件
        if (healthComponent instanceof CompositeHealth compositeHealth) {
            HealthComponent componentHealth = compositeHealth.getComponents().get(component);

            if (componentHealth != null) {
                result.put("status", componentHealth.getStatus().getCode());
            } else {
                result.put("status", "UNKNOWN");
                result.put("message", "组件不存在: " + component);
            }
        } else {
            // 如果不是复合健康状态，返回总体状态
            result.put("status", healthComponent.getStatus().getCode());
            result.put("message", "系统未配置组件级别的健康检查");
        }

        return Result.success(result);
    }

    /**
     * 判断是否为敏感键（如 URL、密码等）
     *
     * @param key 键名
     * @return 是否为敏感键
     */
    private boolean isSensitiveKey(String key) {
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("url") ||
                lowerKey.contains("password") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("key") ||
                lowerKey.contains("host");
    }
}
