package com.sciz.server.infrastructure.config.security;

import cn.dev33.satoken.stp.StpInterface;
import com.sciz.server.application.service.user.PermissionService;
import com.sciz.server.infrastructure.config.cache.IndustryConfigCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Sa-Token 权限数据源实现
 * 返回登录用户的角色列表与权限码列表
 * 
 * 当 Sa-Token 执行权限校验时（@SaCheckRole、@SaCheckPermission），
 * 会调用此接口获取用户的角色和权限数据
 *
 * @author JiaWen.Wu
 * @className CustomStpInterface
 * @date 2025-11-07 11:40
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomStpInterface implements StpInterface {

    private final PermissionService permissionService;
    private final IndustryConfigCache industryConfigCache;

    /**
     * 获取用户权限列表
     *
     * @param loginId   登录ID（用户ID）
     * @param loginType 登录类型
     * @return 权限编码列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Optional.ofNullable(loginId)
                .map(String::valueOf)
                .flatMap(this::parseLongSafe)
                .map(userId -> {
                    try {
                        var industryType = industryConfigCache.get().getType();
                        return permissionService.findPermissionCodes(userId, industryType);
                    } catch (Exception e) {
                        log.error(String.format("获取用户权限列表失败: loginId=%s, err=%s", loginId, e.getMessage()), e);
                        return Collections.<String>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    /**
     * 获取用户角色列表
     *
     * @param loginId   登录ID（用户ID）
     * @param loginType 登录类型
     * @return 角色编码列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return Optional.ofNullable(loginId)
                .map(String::valueOf)
                .flatMap(this::parseLongSafe)
                .map(userId -> {
                    try {
                        var industryType = industryConfigCache.get().getType();
                        return permissionService.findRoleCodes(userId, industryType);
                    } catch (Exception e) {
                        log.error(String.format("获取用户角色列表失败: loginId=%s, err=%s", loginId, e.getMessage()), e);
                        return Collections.<String>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    private Optional<Long> parseLongSafe(String value) {
        try {
            return Optional.of(Long.parseLong(value));
        } catch (NumberFormatException exception) {
            log.error(String.format("解析 loginId 失败: value=%s, err=%s", value, exception.getMessage()), exception);
            return Optional.empty();
        }
    }
}
