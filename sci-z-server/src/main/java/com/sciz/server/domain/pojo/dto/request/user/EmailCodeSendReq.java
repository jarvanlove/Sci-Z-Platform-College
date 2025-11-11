package com.sciz.server.domain.pojo.dto.request.user;

import com.sciz.server.infrastructure.shared.enums.MailProviderType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 邮箱验证码发送请求
 *
 * @param email      String 登录邮箱
 * @param captcha    String 图形验证码
 * @param captchaKey String 图形验证码唯一标识
 * @param provider   MailProviderType 邮箱服务商
 * @author JiaWen.Wu
 * @className EmailCodeSendReq
 * @date 2025-11-10 21:00
 */
public record EmailCodeSendReq(
                @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,
                @NotBlank(message = "图形验证码不能为空") String captcha,
                @NotBlank(message = "图形验证码标识不能为空") String captchaKey,
                @NotNull(message = "邮箱服务商不能为空") MailProviderType provider) {
}