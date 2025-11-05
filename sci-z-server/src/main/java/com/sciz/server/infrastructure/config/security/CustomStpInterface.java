package com.sciz.server.infrastructure.config.security;

import cn.dev33.satoken.stp.StpInterface;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限数据源实现
 * 返回登录用户的角色列表与权限码列表
 */
@Component
public class CustomStpInterface implements StpInterface {
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        // TODO: 从角色-权限聚合查询权限码集合并缓存，当前占位返回空
        return Collections.emptyList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // TODO: 从用户-角色关联查询角色编码集合并缓存，当前占位返回空
        return Collections.emptyList();
    }
}
