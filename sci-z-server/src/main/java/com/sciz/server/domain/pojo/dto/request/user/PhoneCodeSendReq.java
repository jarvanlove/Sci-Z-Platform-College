package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 短信验证码发送请求
 *
 * @param phone      String 手机号（必填）
 * @param captcha    String 图形验证码（可选，失败次数达到阈值时必填）
 * @param captchaKey String 图形验证码唯一标识（可选，失败次数达到阈值时必填）
 * @author JiaWen.Wu
 * @className PhoneCodeSendReq
 * @date 2025-11-11 20:30
 */
public record PhoneCodeSendReq(
        @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^\\+?[0-9]{6,20}$", message = "手机号格式不正确") String phone,
        String captcha,
        String captchaKey) {
}
