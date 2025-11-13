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
 * 依赖 Sa-Token Session 机制，提供读取/写入/清理当前登录用户信息的快捷方法。
 * </p>
 * <p>
 * <strong>工作原理：</strong>
 * <ul>
 * <li>使用 {@code StpUtil.getSession()} 获取 Sa-Token Session</li>
 * <li>Session 数据会自动持久化到 Redis（配置了 sa-token-redis 后）</li>
 * <li>服务重启后，Session 数据会自动从 Redis 恢复</li>
 * <li>Session 的过期时间与 Token 的过期时间一致</li>
 * </ul>
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
     * 缓存当前登录用户信息到 Sa-Token Session
     * Session 数据会自动持久化到 Redis，服务重启后会自动恢复
     *
     * @param context LoginUserContext 登录用户上下文
     */
    public static void cacheCurrentUser(LoginUserContext context) {
        if (context == null || !StpUtil.isLogin()) {
            return;
        }
        // 使用 Sa-Token Session 存储用户上下文（自动持久化到 Redis）
        StpUtil.getSession().set(SystemConstant.LOGIN_USER_SESSION_KEY, context);
    }

    /**
     * 清理当前 Sa-Token Session 中的用户信息
     * 同时会从 Redis 中删除对应的 Session 数据
     */
    public static void clearCurrentUser() {
        if (!StpUtil.isLogin()) {
            return;
        }
        // 从 Sa-Token Session 中删除用户上下文（同时从 Redis 删除）
        StpUtil.getSession().delete(SystemConstant.LOGIN_USER_SESSION_KEY);
    }

    /**
     * 获取当前登录用户信息
     * 从 Sa-Token Session 中读取（如果配置了 Redis，会从 Redis 中读取）
     *
     * @return Optional<LoginUserContext> 登录用户上下文
     */
    public static Optional<LoginUserContext> getCurrentUser() {
        if (!StpUtil.isLogin()) {
            return Optional.empty();
        }
        // 从 Sa-Token Session 中获取用户上下文（如果配置了 Redis，会自动从 Redis 读取）
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
