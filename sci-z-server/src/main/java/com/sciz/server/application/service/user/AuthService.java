package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.response.user.CaptchaResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import jakarta.servlet.http.HttpServletRequest;

/**
 * 认证应用服务
 * 
 * @author JiaWen.Wu
 * @className AuthService
 * @date 2025-10-28 00:00
 */
public interface AuthService {
    /**
     * 用户登录
     *
     * @param loginReq LoginReq 登录请求（包含用户名、密码、验证码等）
     * @param request  HttpServletRequest 请求对象（用于获取客户端信息）
     * @return LoginResp 登录结果
     */
    LoginResp login(LoginReq loginReq, HttpServletRequest request);

    /**
     * 用户登出
     * 注销当前登录会话，清理 token 和权限缓存
     */
    void logout();

    /**
     * 获取验证码
     * 生成图形验证码并缓存到 Redis
     *
     * @return CaptchaResp 验证码响应（包含 key 和 Base64 图片）
     */
    CaptchaResp getCaptcha();
}
