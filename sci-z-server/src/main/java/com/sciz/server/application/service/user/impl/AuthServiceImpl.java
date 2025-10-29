package com.sciz.server.application.service.user.impl;

import org.springframework.stereotype.Service;

import com.sciz.server.application.service.user.AuthService;

/**
 * @author JiaWen.Wu
 * @className AuthServiceImpl
 * @date 2025-10-28 00:00
 */
@Service
public class AuthServiceImpl implements AuthService {
    /**
     * 登录占位实现
     *
     * @param username String 用户名
     * @param password String 密码
     * @return Object 占位结果
     */
    @Override
    public Object login(String username, String password) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
