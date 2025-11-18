package com.sciz.server.infrastructure.shared.enums;

import lombok.Getter;

/**
 * 权限类型枚举
 * 对应数据库表中的 permission_type 字段
 *
 * @author JiaWen.Wu
 * @className PermissionStatus
 * @date 2025-11-07 11:20
 */
@Getter
public enum PermissionStatus {

    /**
     * 菜单
     */
    MENU(1, "菜单"),

    /**
     * 按钮
     */
    BUTTON(2, "按钮"),

    /**
     * API
     */
    API(3, "API");

    private final Integer code;
    private final String description;

    PermissionStatus(Integer code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 代码
     * @return 权限类型枚举
     */
    public static PermissionStatus fromCode(Integer code) {
        for (PermissionStatus type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的权限类型代码: " + code);
    }

    /**
     * 判断是否为菜单类型
     *
     * @return 是否为菜单类型
     */
    public boolean isMenu() {
        return this == MENU;
    }

    /**
     * 判断是否为按钮类型
     *
     * @return 是否为按钮类型
     */
    public boolean isButton() {
        return this == BUTTON;
    }

    /**
     * 判断是否为API类型
     *
     * @return 是否为API类型
     */
    public boolean isApi() {
        return this == API;
    }
}
