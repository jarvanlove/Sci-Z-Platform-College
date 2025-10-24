package com.server.agentbackendservices.modules.auth.controller;

import com.server.agentbackendservices.modules.auth.dto.LoginRequest;
import com.server.agentbackendservices.modules.auth.dto.LoginResponse;
import com.server.agentbackendservices.modules.auth.entity.User;
import com.server.agentbackendservices.modules.auth.service.AuthService;
import com.server.agentbackendservices.modules.common.vo.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 认证控制器
 * 提供登录、登出、用户信息等接口
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户认证相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    public ResultVO<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResultVO.ok(response, "登录成功");
    }
    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出接口")
    public ResultVO<Void> logout() {
        authService.logout();
        return ResultVO.ok(null, "登出成功");
    }

    /**
     * 刷新令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "刷新访问令牌")
    public ResultVO<String> refreshToken() {
        String newToken = authService.refreshToken();
        return ResultVO.ok(newToken, "令牌刷新成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/userinfo")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户信息")
    public ResultVO<User> getUserInfo() {
        User user = authService.getCurrentUser();
        return ResultVO.ok(user, "获取用户信息成功");
    }

    /**
     * 检查登录状态
     */
    @GetMapping("/check")
    @Operation(summary = "检查登录状态", description = "检查用户是否已登录")
    public ResultVO<Boolean> checkLogin() {
        boolean isLogin = authService.isLogin();
        return ResultVO.ok(isLogin, isLogin ? "用户已登录" : "用户未登录");
    }
}
