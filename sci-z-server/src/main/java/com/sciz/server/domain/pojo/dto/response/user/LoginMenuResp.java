package com.sciz.server.domain.pojo.dto.response.user;

import lombok.Data;
import java.util.List;

/**
 * 登录返回中的菜单节点
 *
 * @author JiaWen.Wu
 * @className LoginMenuResp
 * @date 2025-10-31 11:20
 */
@Data
public class LoginMenuResp {

    /**
     * 路由路径
     */
    private String path;

    /**
     * 菜单标题
     */
    private String title;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 菜单/按钮绑定的权限码
     */
    private String permission;

    /**
     * 子菜单
     */
    private List<LoginMenuResp> children;
}
