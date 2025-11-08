package com.sciz.server.domain.pojo.dto.response.user;

import java.util.List;

/**
 * 登录用户档案响应
 *
 * @param userId          Long 用户ID
 * @param tokenName       String Token 名称
 * @param tokenValue      String Token 值
 * @param tokenTimeout    Long Token 剩余秒数
 * @param userInfo        LoginUserInfoResp 用户详细信息
 * @param roleCodes       List<String> 角色编码集合
 * @param permissionCodes List<String> 权限编码集合
 * @param menus           List<LoginMenuResp> 菜单树
 * @author JiaWen.Wu
 * @className ProfileResp
 * @date 2025-11-09 03:00
 */
public record ProfileResp(
        Long userId,
        String tokenName,
        String tokenValue,
        Long tokenTimeout,
        LoginUserInfoResp userInfo,
        List<String> roleCodes,
        List<String> permissionCodes,
        List<LoginMenuResp> menus) {
}
