package com.sciz.server.domain.pojo.dto.request.user;

import lombok.Data;

/**
 * @author JiaWen.Wu
 * @className RegisterReq
 * @date 2025-10-29 10:00
 */
@Data
public class RegisterReq {
    private String username;
    private String email;
    private String password;
    private String realName;
    private String phone;
    private String captcha;
}
