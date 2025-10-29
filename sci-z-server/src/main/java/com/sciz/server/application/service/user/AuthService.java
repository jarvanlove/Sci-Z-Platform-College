package com.sciz.server.application.service.user;

/**
 * @author JiaWen.Wu
 * @className AuthService
 * @date 2025-10-28 00:00
 */
public interface AuthService {
    /**
     * 登录占位
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果占位
     */
    Object login(String username, String password);
}
