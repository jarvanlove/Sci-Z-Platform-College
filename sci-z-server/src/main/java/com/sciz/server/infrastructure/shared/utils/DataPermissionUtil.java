package com.sciz.server.infrastructure.shared.utils;

import com.sciz.server.application.service.user.PermissionService;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 数据权限工具类
 * <p>
 * 功能：
 * 1. 判断当前用户是否是 admin
 * 2. 获取数据权限过滤条件（admin 返回 null，普通用户返回用户ID）
 * <p>
 * 使用场景：
 * - Repository 层查询时，根据返回值决定是否添加 created_by 过滤条件
 * - 支持在异步线程中使用（通过 AsyncUserContext 传递用户信息）
 * <p>
 * 工作原理：
 * - 使用 ApplicationContextAware 获取 Spring Bean（PermissionService）
 * - 通过 LoginUserUtil 获取当前登录用户信息（支持 Web 请求线程和异步线程）
 * - 查询当前用户的角色列表，判断是否包含 role_code = 'admin' 的角色
 * - 如果是 admin，返回 null（不过滤，可以看到所有数据）
 * - 如果不是 admin，返回当前用户ID（需要过滤，只能看到自己的数据）
 * <p>
 * 异步线程使用说明：
 * - 在异步方法开始时，需要先设置 AsyncUserContext.set(currentUser)
 * - 在异步方法结束时，需要调用 AsyncUserContext.clear() 清理上下文
 * - LoginUserUtil 会优先从 AsyncUserContext（ThreadLocal）获取用户信息
 * - 如果没有设置 AsyncUserContext，则会尝试从 Sa-Token Session 获取（仅在 Web 请求线程中可用）
 *
 * @author JiaWen.Wu
 * @className DataPermissionUtil
 * @date 2025-12-31 14:00
 */
@Component
public class DataPermissionUtil implements ApplicationContextAware {

    private static PermissionService permissionService;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        try {
            DataPermissionUtil.permissionService = applicationContext.getBean(PermissionService.class);
        } catch (Exception e) {
            DataPermissionUtil.permissionService = null;
        }
    }

    /**
     * 判断当前用户是否是 admin
     * <p>
     * 实现逻辑：
     * 1. 获取当前登录用户信息（包含用户ID和行业类型）
     * 2. 查询用户的角色列表
     * 3. 判断是否包含 role_code = 'admin' 的角色
     *
     * @return true 如果是 admin，false 如果不是
     */
    public static boolean isAdmin() {
        try {
            // 1. 获取当前登录用户信息
            var currentUser = LoginUserUtil.getCurrentUser();
            if (currentUser.isEmpty()) {
                return false; // 未登录，不是 admin
            }

            var user = currentUser.get();
            var userId = user.userId();
            var industryType = user.industryType();

            // 2. 如果 PermissionService 未初始化，返回 false
            if (permissionService == null) {
                return false;
            }

            // 3. 查询用户的角色列表
            List<String> roleCodes = permissionService.findRoleCodes(userId, industryType);

            // 4. 判断是否包含 admin 角色
            return roleCodes.contains("admin");
        } catch (Exception e) {
            // 发生异常时，为了安全起见，返回 false（不是 admin）
            return false;
        }
    }

    /**
     * 获取数据权限过滤条件
     * <p>
     * 返回值说明：
     * - null：如果是 admin（不过滤，可以看到所有数据）
     * - Long：如果是普通用户（需要过滤，只能看到自己的数据）
     *
     * @return null 如果是 admin（不过滤），否则返回当前用户ID（需要过滤）
     */
    public static Long getDataPermissionFilter() {
        // 1. admin：不过滤，返回 null
        if (isAdmin()) {
            return null;
        }

        // 2. 尝试获取当前用户ID（支持 Web 请求线程、异步线程、定时任务中已设置 AsyncUserContext 的情况）
        var currentUserIdOpt = LoginUserUtil.getCurrentUserId();

        // 2.1 如果没有用户上下文（如定时任务、系统任务），返回 null，不做数据权限过滤
        if (currentUserIdOpt.isEmpty()) {
            return null;
        }

        // 2.2 普通请求：返回当前用户ID，用于仓储层拼接数据权限条件
        return currentUserIdOpt.get();
    }
}
