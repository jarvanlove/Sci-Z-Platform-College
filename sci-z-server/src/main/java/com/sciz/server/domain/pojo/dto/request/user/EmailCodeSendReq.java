package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 重置密码邮箱验证码发送请求
 *
 * @param email      String 登录邮箱
 * @param captcha    String 图形验证码
 * @param captchaKey String 图形验证码唯一标识
 * @author JiaWen.Wu
 * @className EmailCodeSendReq
 * @date 2025-11-10 21:00
 */
public record EmailCodeSendReq(
        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        String email,
        @NotBlank(message = "图形验证码不能为空")
        String captcha,
        @NotBlank(message = "图形验证码标识不能为空")
        String captchaKey) {
}