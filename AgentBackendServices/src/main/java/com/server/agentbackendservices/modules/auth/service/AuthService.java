package com.server.agentbackendservices.modules.auth.service;

import com.server.agentbackendservices.modules.auth.dto.LoginRequest;
import com.server.agentbackendservices.modules.auth.dto.LoginResponse;
import com.server.agentbackendservices.modules.auth.entity.User;

/**
 * 认证服务接口
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
public interface AuthService {

    /**
     * 用户登录
     * @param loginRequest 登录请求
     * @return 登录响应
     */
    LoginResponse login(LoginRequest loginRequest);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 刷新令牌
     * @return 新的令牌
     */
    String refreshToken();

    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    User getCurrentUser();

    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    boolean isLogin();

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     */
    Long getCurrentUserId();
}
