package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author JiaWen.Wu
 * @className AuthController
 * @date 2025-10-28 00:00
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "认证", description = "认证相关接口")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 登录占位接口
     *
     * @param body Map 请求体
     * @return 占位返回
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody Map<String, Object> body) {
        return Result.success("NOT_IMPLEMENTED");
    }
}
