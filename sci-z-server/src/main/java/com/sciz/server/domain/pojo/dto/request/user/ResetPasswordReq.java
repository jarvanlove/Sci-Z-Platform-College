package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 重置密码请求
 * 支持手机号和邮箱两种重置方式
 *
 * @param phone       String 手机号（手机号重置时必填）
 * @param email       String 登录邮箱（邮箱重置时必填）
 * @param smsCode     String 短信验证码（手机号重置时必填）
 * @param emailCode   String 邮箱验证码（邮箱重置时必填）
 * @param newPassword String 新密码
 * @author JiaWen.Wu
 * @className ResetPasswordReq
 * @date 2025-01-16 10:00
 */
public record ResetPasswordReq(
        String phone,
        @Email(message = "邮箱格式不正确")
        String email,
        @Size(min = 6, max = 6, message = "短信验证码必须为6位数字")
        String smsCode,
        @Size(min = 6, max = 6, message = "邮箱验证码必须为6位数字")
        String emailCode,
        @NotBlank(message = "新密码不能为空")
        @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间")
        String newPassword) {
}