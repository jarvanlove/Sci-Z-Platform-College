package com.sciz.server.domain.pojo.dto.response.user;

import lombok.Data;

/**
 * @author JiaWen.Wu
 * @className LoginResp
 * @date 2025-01-27 10:00
 */
@Data
public class LoginResp {
    private String token;
    private String refreshToken;
    private Long userId;
    private String username;
    private String realName;
    private Long expireTime;
}
