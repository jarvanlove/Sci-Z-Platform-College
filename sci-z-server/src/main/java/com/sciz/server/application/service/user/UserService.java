package com.sciz.server.application.service.user;

/**
 * 用户应用服务
 * 
 * @author JiaWen.Wu
 * @className UserService
 * @date 2025-10-28 00:00
 */
public interface UserService {

    /**
     * 获取用户信息
     *
     * @param userId Long 用户ID
     * @return Object 用户信息
     */
    Object getUser(Long userId);
}
