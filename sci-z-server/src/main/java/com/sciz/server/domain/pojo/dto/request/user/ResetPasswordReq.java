package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求
 *
 * @param email       String 登录邮箱
 * @param captcha     String 图形验证码
 * @param captchaKey  String 图形验证码唯一标识
 * @param emailCode   String 邮箱验证码
 * @param newPassword String 新密码
 * @author JiaWen.Wu
 * @className ResetPasswordReq
 * @date 2025-11-10 21:00
 */
public record ResetPasswordReq(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @NotBlank(message = "图形验证码不能为空")
        String captcha,
        @NotBlank(message = "图形验证码标识不能为空")
        String captchaKey,
        @NotBlank(message = "邮箱验证码不能为空")
        @Size(min = 6, max = 6, message = "邮箱验证码必须为6位数字")
        String emailCode,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 8, max = 32, message = "新密码长度必须在8-32个字符之间")
        String newPassword) {
}