package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 管理员重置用户密码请求
 *
 * @param userId      Long 用户ID
 * @param newPassword String 新密码（6-32位）
 *
 * @author JiaWen.Wu
 * @className UserAdminResetPasswordReq
 * @date 2025-11-13 22:00
 */
public record UserAdminResetPasswordReq(
        @NotNull(message = "用户ID不能为空") Long userId,

        @NotBlank(message = "新密码不能为空") @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间") String newPassword) {
}
