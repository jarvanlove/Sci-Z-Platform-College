package com.server.agentbackendservices.modules.common.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.server.agentbackendservices.modules.common.enums.StatusCodeEnum;
import com.server.agentbackendservices.modules.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

/**
 * Token工具类
 * 提供Token相关的工具方法
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Slf4j
public class TokenUtils {

    /**
     * 检查用户是否已登录
     * @return 是否已登录
     */
    public static boolean isLogin() {
        try {
            return StpUtil.isLogin();
        } catch (Exception e) {
            log.warn("检查登录状态异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 获取当前登录用户ID
     * @return 用户ID
     * @throws BusinessException 未登录时抛出异常
     */
    public static Long getCurrentUserId() {
        if (!isLogin()) {
            throw new BusinessException(StatusCodeEnum.NO_LOGIN, "用户未登录，无法获取用户ID");
        }
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            log.error("获取用户ID异常: {}", e.getMessage());
            throw new BusinessException(StatusCodeEnum.SYSTEM_ERROR, "获取用户ID失败");
        }
    }

    /**
     * 获取当前登录用户ID（安全版本，不抛异常）
     * @return 用户ID，未登录时返回null
     */
    public static Long getCurrentUserIdSafe() {
        try {
            if (isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.warn("安全获取用户ID异常: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 获取Token剩余有效时间（秒）
     * @return 剩余时间，-1表示永不过期
     */
    public static long getTokenTimeout() {
        try {
            return StpUtil.getTokenTimeout();
        } catch (Exception e) {
            log.warn("获取Token超时时间异常: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 检查Token是否即将过期（剩余时间小于1小时）
     * @return 是否即将过期
     */
    public static boolean isTokenExpiringSoon() {
        long timeout = getTokenTimeout();
        return timeout > 0 && timeout < 3600; // 小于1小时
    }

    /**
     * 刷新Token
     * @return 新的Token值
     * @throws BusinessException 未登录时抛出异常
     */
    public static String refreshToken() {
        if (!isLogin()) {
            throw new BusinessException(StatusCodeEnum.NO_LOGIN, "用户未登录，无法刷新Token");
        }
        try {
            StpUtil.renewTimeout(StpUtil.getTokenTimeout());
            return StpUtil.getTokenValue();
        } catch (Exception e) {
            log.error("刷新Token异常: {}", e.getMessage());
            throw new BusinessException(StatusCodeEnum.SYSTEM_ERROR, "刷新Token失败");
        }
    }

    /**
     * 登出当前用户
     */
    public static void logout() {
        try {
            if (isLogin()) {
                StpUtil.logout();
                log.info("用户登出成功");
            }
        } catch (Exception e) {
            log.error("用户登出异常: {}", e.getMessage());
        }
    }

    /**
     * 强制登出指定用户
     * @param userId 用户ID
     */
    public static void logoutByUserId(Long userId) {
        try {
            StpUtil.logout(userId);
            log.info("强制登出用户成功: {}", userId);
        } catch (Exception e) {
            log.error("强制登出用户异常: {}", e.getMessage());
        }
    }
}
