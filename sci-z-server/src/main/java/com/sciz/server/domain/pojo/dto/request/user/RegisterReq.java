package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户注册请求
 *
 * @author JiaWen.Wu
 * @className RegisterReq
 * @date 2025-10-29 10:00
 */
@Data
public class RegisterReq {

    /**
     * 登录用户名（3-20 位，字母、数字、下划线）
     */
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名格式不正确，只能包含字母、数字或下划线，长度3-20位")
    private String username;

    /**
     * 真实姓名
     */
    @NotBlank(message = "真实姓名不能为空")
    @Size(max = 50, message = "真实姓名长度不能超过50个字符")
    private String realName;

    /**
     * 邮箱地址
     */
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;

    /**
     * 手机号（大陆 11 位）
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确，请输入大陆手机号")
    private String phone;

    /**
     * 部门编码（来自院系列表）
     */
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 64, message = "部门编码长度不能超过64个字符")
    private String department;

    /**
     * 登录密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度必须在6-32个字符之间")
    private String password;

    /**
     * 图形验证码输入值
     */
    @NotBlank(message = "验证码不能为空")
    private String captcha;

    /**
     * 图形验证码唯一标识
     */
    @NotBlank(message = "验证码标识不能为空")
    private String captchaKey;

    /**
     * 预留短信验证码字段
     */
    @Size(max = 6, message = "短信验证码长度不能超过6个字符")
    private String verificationCode;
}
