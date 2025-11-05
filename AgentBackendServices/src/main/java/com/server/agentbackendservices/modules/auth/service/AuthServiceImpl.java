package com.server.agentbackendservices.modules.auth.service;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.util.SaResult;
import com.server.agentbackendservices.modules.auth.dto.LoginRequest;
import com.server.agentbackendservices.modules.auth.dto.LoginResponse;
import com.server.agentbackendservices.modules.auth.entity.User;
import com.server.agentbackendservices.modules.auth.mapper.UserMapper;
import com.server.agentbackendservices.modules.common.exception.BusinessException;
import com.server.agentbackendservices.modules.common.enums.StatusCodeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("用户登录请求: {}", loginRequest.getUsername());

        // 1. 根据用户名查找用户
        User user = userMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                .eq(User::getUsername, loginRequest.getUsername())
                .eq(User::getDeleted, 0)
        );

        if (user == null) {
            throw new BusinessException(StatusCodeEnum.USERNAME_NOT_EXIST);
        }

        // 2. 验证密码（直接比较明文密码）
        if (!loginRequest.getPassword().equals(user.getPassword())) {
            throw new BusinessException(StatusCodeEnum.PASSWORD_ERROR);
        }

        // 3. 检查用户状态
        if (user.getStatus() == 0) {
            throw new BusinessException(StatusCodeEnum.USER_DISABLED);
        }

        // 4. 执行登录
        StpUtil.login(user.getId());

        // 5. 设置记住我
        // if (loginRequest.getRememberMe()) {
        //     StpUtil.getSession().setTimeout(30 * 24 * 60 * 60); // 30天
        // }

        // 6. 获取token信息
        String token = StpUtil.getTokenValue();
        long timeout = StpUtil.getTokenTimeout();

        // 7. 构建用户信息
        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .status(user.getStatus())
                .build();

        // 8. 构建响应
        LoginResponse response = LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(timeout)
                .userInfo(userInfo)
                .build();

        log.info("用户登录成功: {}", user.getUsername());
        return response;
    }

    @Override
    public void logout() {
        log.info("用户登出: {}", StpUtil.getLoginId());
        StpUtil.logout();
    }

    @Override
    public String refreshToken() {
        if (!StpUtil.isLogin()) {
            throw new BusinessException(StatusCodeEnum.NO_LOGIN);
        }
        
        // 刷新token
        StpUtil.renewTimeout(StpUtil.getTokenTimeout());
        return StpUtil.getTokenValue();
    }

    @Override
    public User getCurrentUser() {
        Long userId = com.server.agentbackendservices.modules.common.utils.TokenUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);
        
        if (user == null || user.getDeleted() == 1) {
            throw new BusinessException("用户不存在或已被删除");
        }

        return user;
    }

    @Override
    public boolean isLogin() {
        return com.server.agentbackendservices.modules.common.utils.TokenUtils.isLogin();
    }

    @Override
    public Long getCurrentUserId() {
        return com.server.agentbackendservices.modules.common.utils.TokenUtils.getCurrentUserId();
    }
}
