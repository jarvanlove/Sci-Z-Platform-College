package com.sciz.server.interfaces.controller;

import com.sciz.server.application.service.user.AuthService;
import com.sciz.server.domain.pojo.dto.request.user.EmailCodeSendReq;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.request.user.RegisterReq;
import com.sciz.server.domain.pojo.dto.request.user.ResetPasswordReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.DepartmentLabelResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckLoginResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckRoleResp;
import com.sciz.server.domain.pojo.dto.response.user.CheckPermResp;
import com.sciz.server.domain.pojo.dto.response.user.RefreshTokenResp;
import com.sciz.server.domain.pojo.dto.response.user.RegisterResp;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import com.sciz.server.infrastructure.shared.result.Result;
import com.sciz.server.infrastructure.shared.result.ResultCode;

import cn.dev33.satoken.annotation.SaIgnore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private final IndustryConfigCache industryConfigCache;

    public AuthController(AuthService authService, IndustryConfigCache industryConfigCache) {
        this.authService = authService;
        this.industryConfigCache = industryConfigCache;
    }

    /**
     * 登录（Sa-Token）
     *
     * @param req     LoginReq 登录请求（包含用户名、密码、验证码等）
     * @param request HttpServletRequest 请求对象
     * @return LoginResp 登录响应
     */
    @Operation(summary = "用户登录", description = "使用 Sa-Token 完成登录，返回 Token 信息。登录失败 >= 3 次时需要验证码")
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

    @Operation(summary = "发送重置密码邮箱验证码", description = "校验图形验证码并向邮箱发送重置密码验证码")
    @PostMapping("/email-code")
    public Result<Void> sendResetPasswordEmailCode(@RequestBody @Valid EmailCodeSendReq request) {
        authService.sendResetPasswordEmailCode(request);
        return Result.success(null, ResultCode.EMAIL_CODE_SEND_SUCCESS.getMessage());
    }

    @Operation(summary = "用户注册", description = "注册新用户，校验验证码并写入系统用户表")
    @PostMapping("/register")
    public Result<RegisterResp> register(@RequestBody @Valid RegisterReq request) {
        RegisterResp resp = authService.register(request);
        return Result.success(resp, ResultCode.USER_REGISTER_SUCCESS.getMessage());
    }

    @Operation(summary = "重置密码", description = "校验图形验证码、邮箱验证码并重置密码")
    @PostMapping("/reset-password")
    public Result<Void> resetPassword(@RequestBody @Valid ResetPasswordReq request) {
        authService.resetPassword(request);
        return Result.success(null, ResultCode.RESET_PASSWORD_SUCCESS.getMessage());
    }

    /**
     * 获取行业部门标签
     *
     * @return Result<List<DepartmentLabelResp>> 部门标签列表
     */
    @Operation(summary = "行业部门标签", description = "获取当前行业下的部门标签列表")
    @GetMapping("/department/label")
    public Result<List<DepartmentLabelResp>> getDepartmentLabels() {
        var industryView = industryConfigCache.get();
        var respList = Optional.ofNullable(industryView.getDepartments())
                .stream()
                .flatMap(Collection::stream)
                .map(dept -> new DepartmentLabelResp(dept.getDepartmentName(), dept.getDepartmentCode()))
                .collect(Collectors.toList());
        return Result.success(respList);
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
    public Result<RefreshTokenResp> refreshToken() {
        var resp = authService.refreshToken();
        return Result.success(resp);
    }

    @Operation(summary = "个人信息", description = "获取当前登录用户的档案信息（基础资料、角色、权限、菜单）")
    @PostMapping("/profile")
    public Result<ProfileResp> profile() {
        var resp = authService.profile();
        return Result.success(resp);
    }

    @Operation(summary = "登录状态校验", description = "校验当前会话是否已登录，并返回 token 基础信息")
    @GetMapping("/check/login")
    public Result<CheckLoginResp> checkLogin() {
        var resp = authService.checkLogin();
        return Result.success(resp);
    }

    @Operation(summary = "角色校验", description = "校验当前登录用户是否拥有指定角色")
    @GetMapping("/check/role")
    public Result<CheckRoleResp> checkRole(@RequestParam("roleCode") String roleCode,
            @RequestParam(value = "industryType", required = false) String industryType) {
        var resp = authService.checkRole(roleCode, industryType);
        return Result.success(resp);
    }

    @Operation(summary = "权限校验", description = "校验当前登录用户是否拥有指定权限码")
    @GetMapping("/check/perm")
    public Result<CheckPermResp> checkPermission(@RequestParam("permissionCode") String permissionCode,
            @RequestParam(value = "industryType", required = false) String industryType) {
        var resp = authService.checkPermission(permissionCode, industryType);
        return Result.success(resp);
    }
}
