package com.sciz.server.domain.pojo.dto.request.user;

import jakarta.validation.constraints.NotBlank;
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
     * 用户名或邮箱
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 5, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

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
