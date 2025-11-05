package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.response.user.LoginResp;

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
     * @param username   String 用户名
     * @param password   String 明文密码
     * @param rememberMe Boolean 记住我
     * @return LoginResp 登录结果
     */
    LoginResp login(String username, String password, Boolean rememberMe);
}
