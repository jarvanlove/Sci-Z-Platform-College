package com.sciz.server.interfaces.converter;

import cn.dev33.satoken.stp.SaTokenInfo;
import com.sciz.server.domain.pojo.dto.response.user.LoginMenuResp;
import com.sciz.server.domain.pojo.dto.response.user.LoginUserInfoResp;
import com.sciz.server.domain.pojo.dto.response.user.ProfileResp;
import com.sciz.server.domain.pojo.dto.response.user.RefreshTokenResp;
import com.sciz.server.domain.pojo.dto.response.user.RegisterResp;
import com.sciz.server.domain.pojo.entity.user.SysUser;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 认证模块 DTO ↔ 实体转换器
 *
 * 负责接口层请求/响应与领域实体之间的映射，避免在 Controller / Service 中编写重复的赋值逻辑。
 *
 * @author JiaWen.Wu
 * @className AuthConverter
 * @date 2025-10-30 00:00
 */
@Mapper(componentModel = "spring")
public interface AuthConverter {

    /**
     * SysUser → LoginUserInfoResp
     *
     * @param user SysUser 实体
     * @return LoginUserInfoResp 基础用户信息响应
     */
    @Mapping(target = "department", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "industry", ignore = true)
    @Mapping(target = "avatar", ignore = true)
    LoginUserInfoResp toLoginUserInfoResp(SysUser user);

    /**
     * SysUser → RegisterResp
     *
     * @param user SysUser 实体
     * @return RegisterResp 注册响应
     */
    @Mapping(target = "userId", source = "id")
    RegisterResp toRegisterResp(SysUser user);

    /**
     * 组装 ProfileResp
     *
     * @param user            SysUser 实体
     * @param tokenInfo       SaTokenInfo token信息
     * @param userInfo        LoginUserInfoResp 用户基础信息
     * @param roleCodes       List<String> 角色编码集合
     * @param permissionCodes List<String> 权限编码集合
     * @param menus           List<LoginMenuResp> 菜单集合
     * @param tokenTimeout    Long token剩余秒数
     * @return ProfileResp 档案响应
     */
    default ProfileResp toProfileResp(SysUser user,
            SaTokenInfo tokenInfo,
            LoginUserInfoResp userInfo,
            List<String> roleCodes,
            List<String> permissionCodes,
            List<LoginMenuResp> menus,
            Long tokenTimeout) {
        var safeRoles = roleCodes == null ? List.<String>of() : List.copyOf(roleCodes);
        var safePermissions = permissionCodes == null ? List.<String>of() : List.copyOf(permissionCodes);
        var safeMenus = menus == null ? List.<LoginMenuResp>of() : List.copyOf(menus);
        return new ProfileResp(
                user.getId(),
                Objects.requireNonNull(tokenInfo).getTokenName(),
                tokenInfo.getTokenValue(),
                tokenTimeout,
                userInfo,
                safeRoles,
                safePermissions,
                safeMenus);
    }

    /**
     * 组装刷新 Token 响应
     *
     * @param loginId   String 登录ID
     * @param tokenInfo SaTokenInfo token信息
     * @param expiresIn Long 剩余秒
     * @param expiresAt LocalDateTime 过期时间
     * @return RefreshTokenResp 刷新结果
     */
    default RefreshTokenResp toRefreshTokenResp(String loginId,
            SaTokenInfo tokenInfo,
            Long expiresIn,
            LocalDateTime expiresAt) {
        return new RefreshTokenResp(
                loginId,
                Objects.requireNonNull(tokenInfo).getTokenName(),
                tokenInfo.getTokenValue(),
                expiresIn,
                expiresAt);
    }
}
