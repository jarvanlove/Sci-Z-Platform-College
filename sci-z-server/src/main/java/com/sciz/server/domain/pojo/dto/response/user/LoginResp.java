package com.sciz.server.domain.pojo.dto.response.user;

import lombok.Data;
import java.util.List;

/**
 * 登录响应
 *
 * @author JiaWen.Wu
 * @className LoginResp
 * @date 2025-10-31 11:10
 */
@Data
public class LoginResp {

    /**
     * 访问令牌（Sa-Token tokenValue）
     */
    private String token;

    /**
     * 令牌类型，如 Bearer
     */
    private String tokenType;

    /**
     * 令牌过期秒数（示例：7200）
     */
    private Long expiresIn;

    /**
     * 用户基本信息
     */
    private LoginUserInfoResp userInfo;

    /**
     * 角色集合
     */
    private List<String> roles;

    /**
     * 权限码集合
     */
    private List<String> permissions;

    /**
     * 菜单集合
     */
    private List<LoginMenuResp> menus;
}
