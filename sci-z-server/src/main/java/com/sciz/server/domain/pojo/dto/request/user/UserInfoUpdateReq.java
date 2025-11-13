package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.util.StringUtils;

/**
 * 个人信息更新请求
 * 仅包含基础信息，不包含头像（头像通过单独的 /user/avatar 接口上传）
 *
 * @param realName   String 真实姓名
 * @param email      String 邮箱
 * @param phone      String 手机号
 * @param department String 部门编码
 * @param title      String 职称编码
 *
 * @author JiaWen.Wu
 * @className UserInfoUpdateReq
 * @date 2025-11-12 18:30
 */
public record UserInfoUpdateReq(
        @NotBlank(message = "真实姓名不能为空") @Size(max = 50, message = "真实姓名长度不能超过50个字符") String realName,

        @NotBlank(message = "邮箱不能为空") @Email(message = "邮箱格式不正确") String email,

        @NotBlank(message = "手机号不能为空") @Pattern(regexp = "^\\d{6,20}$", message = "手机号格式不正确") String phone,

        @NotBlank(message = "所属部门不能为空") String department,

        @NotBlank(message = "职称不能为空") String title) {

    public UserInfoUpdateReq {
        realName = normalize(realName);
        email = normalize(email);
        phone = normalize(phone);
        department = normalize(department);
        title = normalize(title);
    }

    private static String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return value.trim();
    }
}
