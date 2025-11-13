package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

/**
 * 管理员更新用户请求
 *
 * @param id           Long 用户ID
 * @param realName     String 真实姓名
 * @param email        String 邮箱
 * @param phone        String 手机号（大陆11位）
 * @param departmentId Long 部门ID
 * @param industryType String 行业类型
 *
 * @author JiaWen.Wu
 * @className UserUpdateReq
 * @date 2025-11-13 22:00
 */
public record UserUpdateReq(
        @NotNull(message = "用户ID不能为空") Long id,

        @NotBlank(message = "真实姓名不能为空") @Size(max = 50, message = "真实姓名长度不能超过50个字符") String realName,

        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") @Size(max = 100, message = "邮箱长度不能超过100个字符") String email,

        @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确，请输入大陆手机号") String phone,

        @NotNull(message = "部门ID不能为空") Long departmentId,

        @NotBlank(message = "行业类型不能为空") String industryType) {

    public UserUpdateReq {
        realName = normalize(realName);
        email = normalize(email);
        phone = normalize(phone);
        industryType = normalize(industryType);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.trim();
    }
}
