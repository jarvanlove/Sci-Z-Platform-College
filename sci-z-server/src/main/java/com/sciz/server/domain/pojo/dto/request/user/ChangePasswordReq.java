package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求
 *
 * @param oldPassword     String 原密码
 * @param newPassword     String 新密码
 * @param confirmPassword String 确认密码
 * @author JiaWen.Wu
 * @className ChangePasswordReq
 * @date 2025-11-13 10:30
 */
public record ChangePasswordReq(
        @NotBlank(message = "原密码不能为空") String oldPassword,
        @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 20, message = "新密码长度必须在6-20个字符之间") String newPassword,
        @NotBlank(message = "确认密码不能为空") String confirmPassword) {
}
