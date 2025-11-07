package com.sciz.server.application.service.user;

import com.sciz.server.domain.pojo.dto.response.user.LoginMenuResp;

import java.util.List;

/**
 * 用户权限聚合查询服务
 *
 * 提供用户的角色编码、权限码集合，以及菜单树组装。
 *
 * @author JiaWen.Wu
 * @className PermissionService
 * @date 2025-10-31 11:40
 */
public interface PermissionService {

    /**
     * 查询用户角色编码集合
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 角色编码集合
     */
    List<String> findRoleCodes(Long userId, String industryType);

    /**
     * 查询用户权限码集合
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<String> 权限码集合
     */
    List<String> findPermissionCodes(Long userId, String industryType);

    /**
     * 组装用户菜单树（基于权限中的菜单型权限）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return List<LoginMenuResp> 菜单树
     */
    List<LoginMenuResp> buildMenus(Long userId, String industryType);

    /**
     * 刷新指定用户在指定行业下的权限聚合缓存（角色/权限/菜单）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     */
    void refreshUserAuthCache(Long userId, String industryType);

    /**
     * 获取用户主要角色编码（第一个角色）
     *
     * @param userId       Long 用户ID
     * @param industryType String 行业类型
     * @return String 主要角色编码，如果没有角色则返回 null
     */
    String getPrimaryRoleCode(Long userId, String industryType);
}
