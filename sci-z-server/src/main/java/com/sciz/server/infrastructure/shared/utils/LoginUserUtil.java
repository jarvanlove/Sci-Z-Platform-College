package com.sciz.server.infrastructure.shared.utils;

import cn.dev33.satoken.stp.StpUtil;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import com.sciz.server.infrastructure.shared.constant.SystemConstant;
import com.sciz.server.infrastructure.shared.exception.BusinessException;
import com.sciz.server.infrastructure.shared.result.ResultCode;
import java.util.Optional;

/**
 * 登录用户工具类
 *
 * <p>
 * 依赖 Sa-Token 会话缓存，提供读取/写入/清理当前登录用户信息的快捷方法。
 * </p>
 *
 * @author JiaWen.Wu
 * @className LoginUserUtil
 * @date 2025-11-11 15:25
 */
public final class LoginUserUtil {

    private LoginUserUtil() {
    }

    /**
     * 缓存当前登录用户信息到会话
     *
     * @param context LoginUserContext 登录用户上下文
     */
    public static void cacheCurrentUser(LoginUserContext context) {
        if (context == null || !StpUtil.isLogin()) {
            return;
        }
        StpUtil.getSession().set(SystemConstant.LOGIN_USER_SESSION_KEY, context);
    }

    /**
     * 清理当前会话中的用户信息
     */
    public static void clearCurrentUser() {
        if (!StpUtil.isLogin()) {
            return;
        }
        StpUtil.getSession().delete(SystemConstant.LOGIN_USER_SESSION_KEY);
    }

    /**
     * 获取当前登录用户信息
     *
     * @return Optional<LoginUserContext> 登录用户上下文
     */
    public static Optional<LoginUserContext> getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return Optional.empty();
        }
        Object value = StpUtil.getSession().get(SystemConstant.LOGIN_USER_SESSION_KEY);
        if (value instanceof LoginUserContext context) {
            return Optional.of(context);
        }
        return Optional.empty();
    }

    /**
     * 获取当前登录用户信息（必需存在）
     *
     * @return LoginUserContext 登录用户上下文
     */
    public static LoginUserContext requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录或会话已失效"));
    }

    /**
     * 获取当前登录用户ID
     *
     * @return Optional<Long> 用户ID
     */
    public static Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(LoginUserContext::userId);
    }

    /**
     * 获取当前登录用户ID（必需存在）
     *
     * @return Long 用户ID
     */
    public static Long requireCurrentUserId() {
        return getCurrentUserId()
                .orElseThrow(() -> new BusinessException(ResultCode.UNAUTHORIZED, "用户未登录或会话已失效"));
    }
}
