package com.sciz.server.application.service.user.impl;

import com.sciz.server.application.service.user.UserService;
import com.sciz.server.domain.pojo.dto.request.user.LoginReq;
import com.sciz.server.domain.pojo.dto.request.user.RegisterReq;
import com.sciz.server.domain.pojo.dto.response.user.LoginResp;
import com.sciz.server.domain.pojo.dto.response.user.UserInfoResp;
import com.sciz.server.domain.pojo.entity.user.User;
import com.sciz.server.infrastructure.shared.event.EventPublisher;
import com.sciz.server.infrastructure.shared.event.user.UserLoginEvent;
import com.sciz.server.infrastructure.shared.event.user.UserLogoutEvent;
import com.sciz.server.infrastructure.shared.event.user.UserRegisteredEvent;
import com.sciz.server.infrastructure.shared.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户服务实现类
 * 集成领域事件发布
 *
 * @author JiaWen.Wu
 * @className UserServiceImpl
 * @date 2025-10-29 10:30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final EventPublisher eventPublisher;
    // 注入其他依赖：UserRepository, PasswordEncoder等

    /**
     * 用户注册
     *
     * @param registerReq 注册请求
     * @return 注册结果
     */
    @Transactional
    public Result<UserInfoResp> register(RegisterReq registerReq) {
        try {
            // 1. 执行业务逻辑
            User user = createUser(registerReq);
            // userRepository.save(user); // 保存用户

            // 2. 发布用户注册事件
            UserRegisteredEvent event = new UserRegisteredEvent(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail());
            eventPublisher.publish(event);

            log.info(String.format("用户注册成功: %s", user.getUsername()));
            return Result.success("用户注册成功");

        } catch (Exception e) {
            log.error(String.format("用户注册失败 err=%s", e.getMessage()), e);
            return Result.fail("用户注册失败: " + e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param loginReq 登录请求
     * @return 登录结果
     */
    @Transactional
    public Result<LoginResp> login(LoginReq loginReq) {
        try {
            // 1. 执行业务逻辑
            User user = authenticateUser(loginReq);
            String token = generateToken(user);

            // 2. 发布用户登录事件
            UserLoginEvent event = new UserLoginEvent(
                    user.getId(),
                    user.getUsername(),
                    getClientIp(), // 获取客户端IP
                    LocalDateTime.now().toString());
            eventPublisher.publish(event);

            // 3. 构建响应
            LoginResp loginResp = new LoginResp();
            loginResp.setToken(token);
            loginResp.setUserId(user.getId());
            loginResp.setUsername(user.getUsername());

            log.info(String.format("用户登录成功: %s", user.getUsername()));
            return Result.success(loginResp);

        } catch (Exception e) {
            log.error(String.format("用户登录失败 err=%s", e.getMessage()), e);
            return Result.fail("用户登录失败: " + e.getMessage());
        }
    }

    /**
     * 用户退出登录
     *
     * @param userId 用户ID
     * @return 退出结果
     */
    @Transactional
    public Result<Void> logout(Long userId) {
        try {
            // 1. 执行业务逻辑
            // 清除用户会话、token等

            // 2. 发布用户退出事件
            UserLogoutEvent event = new UserLogoutEvent(
                    userId,
                    getCurrentUsername(userId), // 获取当前用户名
                    LocalDateTime.now().toString());
            eventPublisher.publish(event);

            log.info(String.format("用户退出登录成功: %s", String.valueOf(userId)));
            return Result.success("退出登录成功");

        } catch (Exception e) {
            log.error(String.format("用户退出登录失败 err=%s", e.getMessage()), e);
            return Result.fail("退出登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @Override
    public Object getUser(Long userId) {
        // 实现获取用户信息的逻辑
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // ==================== 私有方法 ====================

    /**
     * 创建用户
     */
    private User createUser(RegisterReq registerReq) {
        User user = new User();
        user.setUsername(registerReq.getUsername());
        user.setEmail(registerReq.getEmail());
        // 设置其他属性
        return user;
    }

    /**
     * 验证用户
     */
    private User authenticateUser(LoginReq loginReq) {
        // 实现用户验证逻辑
        User user = new User();
        user.setId(1L);
        user.setUsername(loginReq.getUsername());
        return user;
    }

    /**
     * 生成token
     */
    private String generateToken(User user) {
        // 实现token生成逻辑
        return "mock-token-" + user.getId();
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp() {
        // 实现获取客户端IP的逻辑
        return "127.0.0.1";
    }

    /**
     * 获取当前用户名
     */
    private String getCurrentUsername(Long userId) {
        // 实现获取当前用户名的逻辑
        return "用户" + userId;
    }
}
