package com.sciz.server.infrastructure.shared.context;

import com.sciz.server.domain.pojo.dto.response.user.LoginUserContext;
import java.util.Optional;

/**
 * 异步用户上下文工具类
 * <p>
 * 用于在异步线程（@Async、事件处理器等）中传递用户信息
 * 使用 ThreadLocal 存储用户上下文，确保在异步线程中也能获取用户信息
 * <p>
 * <strong>使用场景：</strong>
 * <ul>
 * <li>@Async 异步方法</li>
 * <li>@EventListener 事件处理器</li>
 * <li>定时任务</li>
 * <li>其他非 Web 请求上下文的场景</li>
 * </ul>
 * <p>
 * <strong>工作原理：</strong>
 * <ul>
 * <li>使用 ThreadLocal 存储用户上下文，每个线程独立存储</li>
 * <li>在异步方法开始时设置用户上下文，结束时清理</li>
 * <li>LoginUserUtil 会优先从 ThreadLocal 获取，如果没有则从 Sa-Token Session 获取</li>
 * </ul>
 * <p>
 * <strong>使用示例：</strong>
 * 
 * <pre>
 * {@code
 * &#64;EventListener
 * @Async
 * public void handleEvent(SomeEvent event) {
 *     try {
 *         // 设置异步用户上下文
 *         AsyncUserContext.set(event.getUserId(), event.getUsername(), event.getRealName());
 * 
 *         // 现在可以正常使用 LoginUserUtil，它会自动从 ThreadLocal 获取用户信息
 *         var currentUser = LoginUserUtil.requireCurrentUser();
 * 
 *         // 执行业务逻辑...
 *     } finally {
 *         // 清理异步用户上下文（防止内存泄漏）
 *         AsyncUserContext.clear();
 *     }
 * }
 * }
 * </pre>
 *
 * @author JiaWen.Wu
 * @className AsyncUserContext
 * @date 2025-12-08 10:15
 */
public final class AsyncUserContext {

    /**
     * ThreadLocal 存储用户上下文
     */
    private static final ThreadLocal<LoginUserContext> USER_CONTEXT = new ThreadLocal<>();

    private AsyncUserContext() {
    }

    /**
     * 设置异步用户上下文
     * <p>
     * 在异步方法开始时调用，设置当前线程的用户信息
     *
     * @param userId   Long 用户ID
     * @param username String 用户名
     * @param realName String 真实姓名
     */
    public static void set(Long userId, String username, String realName) {
        if (userId == null || username == null) {
            return;
        }
        var context = LoginUserContext.of(userId, username, realName, null, null, null, null, null);
        USER_CONTEXT.set(context);
    }

    /**
     * 设置异步用户上下文（完整信息）
     * <p>
     * 在异步方法开始时调用，设置当前线程的用户信息
     *
     * @param context LoginUserContext 登录用户上下文
     */
    public static void set(LoginUserContext context) {
        if (context != null) {
            USER_CONTEXT.set(context);
        }
    }

    /**
     * 获取异步用户上下文
     * <p>
     * 从 ThreadLocal 中获取当前线程的用户信息
     *
     * @return Optional<LoginUserContext> 登录用户上下文
     */
    public static Optional<LoginUserContext> get() {
        return Optional.ofNullable(USER_CONTEXT.get());
    }

    /**
     * 清理异步用户上下文
     * <p>
     * 在异步方法结束时调用，防止内存泄漏
     * <p>
     * <strong>重要：</strong>必须在 finally 块中调用，确保即使发生异常也能清理
     */
    public static void clear() {
        USER_CONTEXT.remove();
    }

    /**
     * 检查是否已设置异步用户上下文
     *
     * @return boolean 是否已设置
     */
    public static boolean isSet() {
        return USER_CONTEXT.get() != null;
    }
}
