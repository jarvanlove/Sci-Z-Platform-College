package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 *
 * @author JiaWen.Wu
 * @className LoginReq
 * @date 2025-10-29 10:30
 */
@Data
public class LoginReq {

    /**
     * 登录类型（password: 账号密码登录, sms: 短信验证码登录）
     * 默认为 sms（短信登录）
     */
    private String loginType = "sms";

    /**
     * 账号（密码登录时必填）
     * 支持三种格式：
     * - 用户名（3-20位，字母、数字、下划线）
     * - 手机号（11位数字，支持 +86 前缀）
     * - 邮箱（标准邮箱格式）
     */
    @Size(min = 3, max = 100, message = "账号长度必须在3-100个字符之间")
    private String username;

    /**
     * 密码（密码登录时必填）
     */
    @Size(min = 5, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    /**
     * 手机号（短信登录时必填）
     */
    @Pattern(regexp = "^\\+?[0-9]{6,20}$", message = "手机号格式不正确")
    private String phone;

    /**
     * 短信验证码（短信登录时必填）
     */
    @Size(max = 6, message = "短信验证码长度不能超过6个字符")
    private String smsCode;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;

    /**
     * 验证码（用户输入的验证码文本）
     * 当登录失败次数 >= 3 次时，前端会显示验证码，此时此字段必填
     */
    private String captcha;

    /**
     * 验证码唯一标识（从获取验证码接口返回）
     * 当登录失败次数 >= 3 次时，前端需要携带此 key
     */
    private String captchaKey;
}
