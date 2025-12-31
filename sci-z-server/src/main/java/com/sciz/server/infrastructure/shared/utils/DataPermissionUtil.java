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
 * <p>
 * 工作原理：
 * - 使用 ApplicationContextAware 获取 Spring Bean（PermissionService）
 * - 查询当前用户的角色列表，判断是否包含 role_code = 'admin' 的角色
 * - 如果是 admin，返回 null（不过滤，可以看到所有数据）
 * - 如果不是 admin，返回当前用户ID（需要过滤，只能看到自己的数据）
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
        if (isAdmin()) {
            return null; // admin 可以看到所有数据
        }
        return LoginUserUtil.requireCurrentUserId(); // 普通用户只能看到自己的数据
    }
}
