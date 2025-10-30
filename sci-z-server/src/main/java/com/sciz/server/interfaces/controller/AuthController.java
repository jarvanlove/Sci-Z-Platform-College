package com.sciz.server.interfaces.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.HashMap;
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
     * 登录（Sa-Token）
     *
     * @param req 登录请求
     * @return token 信息
     */
    @Operation(summary = "用户登录", description = "使用 Sa-Token 完成登录，返回 Token 信息")
    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq req) {
        LoginResp data = authService.login(req.getUsername(), req.getPassword(), req.getRememberMe());
        return Result.success(data);
    }

    @Operation(summary = "用户注册", description = "注册新用户（占位，后续接入业务校验与持久化）")
    @PostMapping("/register")
    public Result<Object> register(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "重置密码", description = "重置用户密码（占位实现）")
    @PostMapping("/reset-password")
    public Result<Object> resetPassword(@RequestBody Object request) {
        return Result.success(null);
    }

    @Operation(summary = "退出登录", description = "注销当前登录会话")
    @PostMapping("/logout")
    public Result<Void> logout() {
        if (StpUtil.isLogin()) {
            StpUtil.logout();
        }
        return Result.success(null);
    }

    @Operation(summary = "刷新令牌/续期", description = "刷新或续期当前登录 Token（按 Sa-Token 配置生效）")
    @PostMapping("/refresh-token")
    public Result<Object> refreshToken(@RequestBody(required = false) Object request) {
        if (!StpUtil.isLogin()) {
            return Result.fail(401, "未登录");
        }
        // 续期：基于配置与模式（示例：直接返回当前 token 信息）
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        Map<String, Object> data = new HashMap<>();
        data.put("loginId", StpUtil.getLoginId());
        data.put("tokenName", tokenInfo.getTokenName());
        data.put("tokenValue", tokenInfo.getTokenValue());
        data.put("tokenTimeout", StpUtil.getTokenTimeout());
        return Result.success(data);
    }

    @Operation(summary = "个人信息", description = "获取当前登录用户的基本信息（占位）")
    @PostMapping("/profile")
    public Result<ProfileResp> profile() {
        if (!StpUtil.isLogin()) {
            return Result.fail(401, "未登录");
        }
        SaTokenInfo token = StpUtil.getTokenInfo();
        ProfileResp resp = new ProfileResp();
        resp.setUserId(Long.valueOf(String.valueOf(StpUtil.getLoginId())));
        resp.setUsername(String.valueOf(StpUtil.getLoginId()));
        resp.setTokenName(token.getTokenName());
        resp.setTokenValue(token.getTokenValue());
        resp.setTokenTimeout(StpUtil.getTokenTimeout());
        return Result.success(resp);
    }
}
