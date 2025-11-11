package com.sciz.server.infrastructure.external.dify.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 状态码枚举
 * 定义系统中使用的各种状态码
 *
 * @author AgentBackendServices
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum StatusCodeEnum {

    SUCCESS(200, "操作成功"),
    NO_LOGIN(401, "用户未登录"),
    AUTHORIZED(403, "没有操作权限"),
    SYSTEM_ERROR(500, "系统异常"),
    FAIL(510, "操作失败"),
    VALID_ERROR(520, "参数格式不正确"),
    USERNAME_EXIST(521, "用户名已存在"),
    USERNAME_NOT_EXIST(522, "用户名不存在"),
    PASSWORD_ERROR(523, "密码错误"),
    USER_DISABLED(524, "用户已被禁用"),
    TOKEN_EXPIRED(525, "令牌已过期"),
    TOKEN_INVALID(526, "令牌无效"),
    LOGIN_REQUIRED(527, "请先登录"),
    PERMISSION_DENIED(528, "权限不足"),
    ROLE_DENIED(529, "角色权限不足");

    private final Integer code;
    private final String desc;
}
