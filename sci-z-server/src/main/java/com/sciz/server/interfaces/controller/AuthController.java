package com.sciz.server.interfaces.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.infrastructure.shared.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.mindrot.jbcrypt.BCrypt;
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
     * @param req     LoginReq 登录请求（包含用户名、密码、验证码等）
     * @param request HttpServletRequest 请求对象
     * @return LoginResp 登录响应
     */
    @Operation(summary = "用户登录", description = "使用 Sa-Token 完成登录，返回 Token 信息。登录失败 >= 5 次时需要验证码")
    @PostMapping("/login")
    public Result<LoginResp> login(@RequestBody @Valid LoginReq req, HttpServletRequest request) {
        LoginResp resp = authService.login(req, request);
        return Result.success(resp);
    }

    /**
     * 获取验证码
     * 生成图形验证码（Base64）
     *
     * @return CaptchaResp 验证码响应
     */
    @Operation(summary = "获取验证码", description = "生成图形验证码，返回 Base64 图片和唯一标识。前端在登录失败 >= 5 次时调用")
    @GetMapping("/captcha")
    public Result<CaptchaResp> getCaptcha() {
        CaptchaResp resp = authService.getCaptcha();
        return Result.success(resp);
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

    /**
     * 退出登录
     * 注销当前登录会话，清理 token 和权限缓存
     *
     * @return Result<Void> 退出结果
     */
    @Operation(summary = "退出登录", description = "注销当前登录会话，清理 token 和权限缓存")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
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

    public static void main(String[] args) {
        // 生成 admin 的哈希值
        String adminPassword = "admin";
        String adminHash = BCrypt.hashpw(adminPassword, BCrypt.gensalt(10));
        System.out.println("=== Admin 密码哈希值 ===");
        System.out.println("密码: " + adminPassword);
        System.out.println("哈希值: " + adminHash);

        // 验证
        boolean adminMatch = BCrypt.checkpw(adminPassword, adminHash);
        System.out.println("验证结果: " + adminMatch);
        System.out.println();

        // 生成 user 的哈希值
        String userPassword = "user";
        String userHash = BCrypt.hashpw(userPassword, BCrypt.gensalt(10));
        System.out.println("=== User 密码哈希值 ===");
        System.out.println("密码: " + userPassword);
        System.out.println("哈希值: " + userHash);

        // 验证
        boolean userMatch = BCrypt.checkpw(userPassword, userHash);
        System.out.println("验证结果: " + userMatch);
    }
}
